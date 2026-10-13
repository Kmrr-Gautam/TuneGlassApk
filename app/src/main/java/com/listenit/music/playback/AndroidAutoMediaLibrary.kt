package com.listenit.music.playback

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import com.listenit.music.data.local.db.DownloadedTrackDao
import com.listenit.music.data.local.db.DownloadedTrackEntity
import com.listenit.music.data.lyrics.LyricsRepository
import com.listenit.music.data.lyrics.LyricsResult
import com.listenit.music.data.playlist.PlaylistRepository
import com.listenit.music.data.playlist.SavedPlaylist
import com.listenit.music.data.playlist.isYouTubeOnly
import com.listenit.music.data.search.SearchRepository
import com.listenit.music.data.search.SearchTab
import com.listenit.music.data.ytmusic.YtMusicLibraryManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import java.io.File
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Driver-safe browse tree and media controller bridge for Android Auto.
 * Provides comprehensive control across car displays of all shapes and sizes:
 * - Search & Explore (curated genres, trending hits, voice search)
 * - Queue management (view up next with artwork, jump to tracks, shuffle, repeat, clear)
 * - Live synchronized lyrics (view active line, tap line to seek timestamp)
 * - Offline listening library (offline playlist with format badges and shuffle)
 * - Responsive Content Style hints that adapt to wide, ultrawide, and portrait car screens.
 */
@Singleton
class AndroidAutoMediaLibrary @Inject constructor(
    @ApplicationContext private val context: Context,
    private val playlistRepository: PlaylistRepository,
    private val ytMusicLibraryManager: YtMusicLibraryManager,
    private val downloadedTrackDao: DownloadedTrackDao,
    private val searchRepository: SearchRepository,
    private val searchHistoryRepository: com.listenit.music.data.search.SearchHistoryRepository,
    private val lyricsRepository: LyricsRepository,
) {
    private val nextSearchToken = AtomicLong()
    private val searchQueues = object : LinkedHashMap<String, List<PlayableTrack>>(12, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, List<PlayableTrack>>?): Boolean =
            size > MAX_CACHED_SEARCHES
    }

    val playlistChanges: Flow<Unit>
        get() = merge(
            playlistRepository.changes,
            ytMusicLibraryManager.playlists.map { },
        )

    val downloadChanges get() = downloadedTrackDao.getAll()

    suspend fun loadChildren(parentId: String, state: MusicPlayerState): List<MediaBrowserCompat.MediaItem> =
        when {
            parentId == ROOT_ID -> rootItems(state)
            parentId == QUEUE_ID -> loadQueueItems(state)
            parentId == SEARCH_ID -> loadSearchExploreCards()
            parentId.startsWith(SEARCH_CATEGORY_PREFIX) -> loadCategoryTracks(parentId.removePrefix(SEARCH_CATEGORY_PREFIX))
            parentId == LYRICS_ID -> loadLyricsItems(state)
            parentId == PLAYLISTS_ID -> getMergedPlaylists().map { playlist ->
                val trackCount = playlist.remoteTrackCount ?: playlist.tracks.size
                browsableItem(
                    mediaId = "$PLAYLIST_PREFIX${playlist.id}",
                    title = playlist.title,
                    subtitle = if (playlist.isYouTubeOnly) "YouTube Music • ${trackCountLabel(trackCount)}" else trackCountLabel(trackCount),
                    playable = trackCount > 0 || playlist.tracks.isNotEmpty(),
                    isGrid = true,
                )
            }
            parentId.startsWith(PLAYLIST_PREFIX) -> {
                val playlistId = parentId.removePrefix(PLAYLIST_PREFIX).toLongOrNull()
                val playlist = playlistId?.let { resolvePlaylistWithTracks(it) }
                if (playlist == null || playlist.tracks.isEmpty()) {
                    emptyList()
                } else {
                    playlist.tracks.mapIndexed { index, track ->
                        playableItem(
                            mediaId = "$PLAYLIST_TRACK_PREFIX${playlist.id}:$index",
                            track = track.toPlayableTrack(),
                        )
                    }
                }
            }
            parentId == DOWNLOADS_ID -> loadDownloadsItems()
            else -> emptyList()
        }

    suspend fun search(query: String): List<MediaBrowserCompat.MediaItem> {
        val cleanQuery = query.trim()
        if (cleanQuery.isEmpty()) return emptyList()

        // Record search in history so it syncs with mobile and car recent searches
        searchHistoryRepository.add(cleanQuery)

        // 1. Dynamic online search via InnerTube / YouTube Music (just like mobile)
        val tracks = searchTracks(cleanQuery)
        val trackItems = if (tracks.isNotEmpty()) {
            val token = nextSearchToken.incrementAndGet().toString(36)
            synchronized(searchQueues) { searchQueues[token] = tracks }
            tracks.mapIndexed { index, track ->
                playableItem("$SEARCH_PREFIX$token:$index", track)
            }
        } else {
            emptyList()
        }

        // 2. Matching playlists
        val matchingPlaylists = runCatching {
            getMergedPlaylists().filter { it.title.contains(cleanQuery, ignoreCase = true) }
        }.getOrDefault(emptyList()).map { playlist ->
            val count = playlist.remoteTrackCount ?: playlist.tracks.size
            browsableItem(
                mediaId = "$PLAYLIST_PREFIX${playlist.id}",
                title = playlist.title,
                subtitle = if (playlist.isYouTubeOnly) "YouTube Music • ${trackCountLabel(count)}" else trackCountLabel(count),
                playable = true,
                isGrid = true,
            )
        }

        return (trackItems + matchingPlaylists).take(MAX_SEARCH_RESULTS)
    }

    suspend fun playMediaId(mediaId: String, player: MusicPlayer): Boolean = when {
        mediaId == QUEUE_ID -> player.state.value.current?.let { player.resume(); true } ?: false
        mediaId == "$QUEUE_ACTION_PREFIX:shuffle" -> {
            player.setShuffleEnabled(!player.state.value.shuffleEnabled)
            true
        }
        mediaId == "$QUEUE_ACTION_PREFIX:repeat" -> {
            val next = when (player.state.value.repeatMode) {
                androidx.media3.common.Player.REPEAT_MODE_OFF -> androidx.media3.common.Player.REPEAT_MODE_ALL
                androidx.media3.common.Player.REPEAT_MODE_ALL -> androidx.media3.common.Player.REPEAT_MODE_ONE
                else -> androidx.media3.common.Player.REPEAT_MODE_OFF
            }
            player.setRepeatMode(next)
            true
        }
        mediaId == "$QUEUE_ACTION_PREFIX:clear" -> {
            player.clearUpcomingQueue()
            true
        }
        mediaId == "$DOWNLOAD_ACTION_PREFIX:shuffle" -> {
            val tracks = downloadedTrackDao.getAllList().map { it.toPlayableTrack() }.shuffled()
            if (tracks.isNotEmpty()) {
                player.playQueue(tracks, startIndex = 0, sourceLabel = "Offline Shuffled")
                true
            } else false
        }
        mediaId == DOWNLOADS_ID -> playDownloads(player)
        mediaId.startsWith(SEEK_LYRIC_PREFIX) -> {
            val timeMs = mediaId.removePrefix(SEEK_LYRIC_PREFIX).toLongOrNull() ?: 0L
            player.seekTo(timeMs)
            player.resume()
            true
        }
        mediaId.startsWith(QUEUE_TRACK_PREFIX) -> {
            val index = mediaId.removePrefix(QUEUE_TRACK_PREFIX).toIntOrNull()
            if (index != null && index in player.state.value.queue.indices) {
                player.seekToQueueItem(index)
                true
            } else {
                false
            }
        }
        mediaId.startsWith(PLAYLIST_TRACK_PREFIX) -> {
            val parts = mediaId.removePrefix(PLAYLIST_TRACK_PREFIX).split(':', limit = 2)
            playPlaylist(parts.getOrNull(0)?.toLongOrNull(), parts.getOrNull(1)?.toIntOrNull(), player)
        }
        mediaId.startsWith(PLAYLIST_PREFIX) ->
            playPlaylist(mediaId.removePrefix(PLAYLIST_PREFIX).toLongOrNull(), 0, player)
        mediaId.startsWith(DOWNLOAD_PREFIX) -> {
            val track = mediaId.removePrefix(DOWNLOAD_PREFIX).toLongOrNull()
                ?.let { downloadedTrackDao.findById(it) }
                ?.toPlayableTrack()
            track?.let { player.play(it, "Downloads"); true } ?: false
        }
        mediaId.startsWith(SEARCH_PREFIX) -> playCachedSearch(mediaId, player)
        else -> false
    }

    suspend fun playSearch(query: String, player: MusicPlayer): Boolean {
        val cleanQuery = query.trim()
        if (cleanQuery.isBlank()) return false

        // 1. Explicit offline request (e.g. "play offline songs", "shuffle downloads")
        val isExplicitOffline = cleanQuery.equals("offline", ignoreCase = true) ||
            cleanQuery.startsWith("offline ", ignoreCase = true) ||
            cleanQuery.endsWith(" offline", ignoreCase = true) ||
            cleanQuery.contains("downloaded songs", ignoreCase = true) ||
            cleanQuery.contains("offline songs", ignoreCase = true)

        if (isExplicitOffline) {
            val localTracks = downloadedTrackDao.getAllList()
            if (localTracks.isNotEmpty()) {
                val filterTerm = cleanQuery.replace("offline", "", ignoreCase = true)
                    .replace("songs", "", ignoreCase = true)
                    .replace("downloaded", "", ignoreCase = true)
                    .trim()
                val matching = if (filterTerm.isNotEmpty()) {
                    localTracks.filter {
                        it.title.contains(filterTerm, ignoreCase = true) ||
                            it.artist.contains(filterTerm, ignoreCase = true)
                    }
                } else localTracks
                val toPlay = if (matching.isNotEmpty()) matching else localTracks
                player.playQueue(toPlay.map { it.toPlayableTrack() }, sourceLabel = "Offline: $cleanQuery")
                return true
            }
        }

        // 2. Dynamic online search via YouTube Music (InnerTube) — exactly like mobile!
        val onlineTracks = searchTracks(cleanQuery)
        if (onlineTracks.isNotEmpty()) {
            searchHistoryRepository.add(cleanQuery)
            player.playQueue(onlineTracks, startIndex = 0, sourceLabel = "Android Auto: $cleanQuery")
            return true
        }

        // 3. Fallback to matching playlist if online search returned nothing
        val matchingPlaylist = runCatching {
            val playlists = getMergedPlaylists()
            playlists.firstOrNull { it.title.equals(cleanQuery, ignoreCase = true) }
                ?: playlists.firstOrNull { it.title.contains(cleanQuery, ignoreCase = true) }
        }.getOrNull()

        if (matchingPlaylist != null) {
            return playPlaylist(matchingPlaylist.id, 0, player)
        }

        // 4. Graceful fallback for offline car driving when cellular network is unavailable
        val localTracks = downloadedTrackDao.getAllList()
        val offlineMatching = localTracks.filter {
            it.title.contains(cleanQuery, ignoreCase = true) ||
                it.artist.contains(cleanQuery, ignoreCase = true)
        }
        if (offlineMatching.isNotEmpty()) {
            player.playQueue(offlineMatching.map { it.toPlayableTrack() }, sourceLabel = "Offline: $cleanQuery")
            return true
        }

        return false
    }

    private suspend fun getMergedPlaylists(): List<SavedPlaylist> {
        val local = runCatching { playlistRepository.getAll() }.getOrDefault(emptyList())
        val remote = runCatching { ytMusicLibraryManager.playlists.value }.getOrDefault(emptyList())
        val localRemoteIds = local.mapNotNull { it.remotePlaylistId?.removePrefix("VL") }.toSet()
        val filteredRemote = remote.filterNot { it.remotePlaylistId?.removePrefix("VL") in localRemoteIds }
        return (local + filteredRemote).sortedByDescending { it.createdAtMillis }
    }

    private suspend fun resolvePlaylistWithTracks(playlistId: Long): SavedPlaylist? {
        val local = runCatching { playlistRepository.getById(playlistId) }.getOrNull()
        if (local != null && local.tracks.isNotEmpty()) {
            return local
        }
        return runCatching { ytMusicLibraryManager.loadDetail(playlistId) }.getOrNull() ?: local
    }

    private fun rootItems(state: MusicPlayerState): List<MediaBrowserCompat.MediaItem> {
        val queueSubtitle = if (state.queue.isEmpty()) {
            "No active queue"
        } else if (state.current != null) {
            "Now: ${state.current.title} (${state.queue.size} songs)"
        } else {
            trackCountLabel(state.queue.size)
        }

        return listOf(
            browsableItem(
                mediaId = QUEUE_ID,
                title = "Queue & Up Next",
                subtitle = queueSubtitle,
                playable = state.current != null,
                iconUri = carIconUri("ic_car_queue"),
            ),
            browsableItem(
                mediaId = SEARCH_ID,
                title = "Search & Explore",
                subtitle = "Top charts, curated mixes & genres",
                playable = false,
                isGrid = true,
                iconUri = carIconUri("ic_car_search"),
            ),
            browsableItem(
                mediaId = LYRICS_ID,
                title = "Live Lyrics",
                subtitle = state.current?.let { "Synced lyrics • ${it.title}" } ?: "Play a song to view lyrics",
                playable = state.current != null,
                iconUri = carIconUri("ic_car_lyrics"),
            ),
            browsableItem(
                mediaId = DOWNLOADS_ID,
                title = "Offline Listening",
                subtitle = "Saved songs & cached offline library",
                playable = true,
                iconUri = carIconUri("ic_car_download"),
            ),
            browsableItem(
                mediaId = PLAYLISTS_ID,
                title = "Playlists",
                subtitle = "Your saved mixes and playlists",
                isGrid = true,
                iconUri = carIconUri("ic_car_attribution"),
            ),
        )
    }

    private fun loadQueueItems(state: MusicPlayerState): List<MediaBrowserCompat.MediaItem> {
        if (state.queue.isEmpty()) {
            return listOf(
                playableItem(
                    mediaId = "$QUEUE_ACTION_PREFIX:empty",
                    track = PlayableTrack(
                        title = "Queue is empty",
                        artist = "Play songs from Search, Playlists, or Offline",
                    ),
                    overrideIconUri = carIconUri("ic_car_queue"),
                ),
            )
        }

        val actions = mutableListOf<MediaBrowserCompat.MediaItem>()
        val shuffleLabel = if (state.shuffleEnabled) "🔀 Shuffle is ON (Tap to turn OFF)" else "🔀 Shuffle is OFF (Tap to turn ON)"
        actions.add(
            playableItem(
                mediaId = "$QUEUE_ACTION_PREFIX:shuffle",
                track = PlayableTrack(title = shuffleLabel, artist = "Playback Controls"),
                overrideIconUri = carIconUri("ic_car_shuffle"),
            ),
        )

        val repLabel = when (state.repeatMode) {
            androidx.media3.common.Player.REPEAT_MODE_ONE -> "Repeat: Track (Tap to turn OFF)"
            androidx.media3.common.Player.REPEAT_MODE_ALL -> "Repeat: All (Tap to repeat One)"
            else -> "Repeat: OFF (Tap to repeat All)"
        }
        actions.add(
            playableItem(
                mediaId = "$QUEUE_ACTION_PREFIX:repeat",
                track = PlayableTrack(title = "🔁 $repLabel", artist = "Playback Controls"),
                overrideIconUri = carIconUri("ic_car_repeat"),
            ),
        )

        if (state.queue.size > 1) {
            actions.add(
                playableItem(
                    mediaId = "$QUEUE_ACTION_PREFIX:clear",
                    track = PlayableTrack(title = "🗑 Clear Upcoming Queue", artist = "Keep playing current track"),
                    overrideIconUri = carIconUri("ic_car_queue"),
                ),
            )
        }

        val trackItems = state.queue.mapIndexed { index, track ->
            val isNowPlaying = index == state.currentIndex
            val titlePrefix = if (isNowPlaying) "▶ " else "${index + 1}. "
            val subtitlePrefix = if (isNowPlaying) "Now Playing • " else ""
            playableItem(
                mediaId = "$QUEUE_TRACK_PREFIX$index",
                track = track,
                overrideTitle = "$titlePrefix${track.title}",
                overrideSubtitle = "$subtitlePrefix${track.artist}",
            )
        }
        return actions + trackItems
    }

    private fun loadSearchExploreCards(): List<MediaBrowserCompat.MediaItem> {
        val items = mutableListOf<MediaBrowserCompat.MediaItem>()

        // Recent searches from mobile & car history (tap to run dynamic online search)
        val recent = runCatching { searchHistoryRepository.history.value.take(4) }.getOrDefault(emptyList())
        for (q in recent) {
            items.add(
                browsableItem(
                    mediaId = "$SEARCH_CATEGORY_PREFIX$q",
                    title = "🔍 $q",
                    subtitle = "Recent Search • Tap to search online",
                    isGrid = false,
                    iconUri = carIconUri("ic_car_search"),
                ),
            )
        }

        // Curated dynamic explore cards (each queries YouTube Music live online)
        items.addAll(
            listOf(
                browsableItem(
                    mediaId = "${SEARCH_CATEGORY_PREFIX}trending songs",
                    title = "Top Trending Hits",
                    subtitle = "Global chart toppers & trending music",
                    isGrid = true,
                    iconUri = carIconUri("ic_car_search"),
                ),
                browsableItem(
                    mediaId = "${SEARCH_CATEGORY_PREFIX}new releases",
                    title = "New Releases",
                    subtitle = "Fresh new popular tracks",
                    isGrid = true,
                    iconUri = carIconUri("ic_car_search"),
                ),
                browsableItem(
                    mediaId = "${SEARCH_CATEGORY_PREFIX}bollywood hits",
                    title = "Bollywood & Hindi Hits",
                    subtitle = "Top Indian & Bollywood chartbusters",
                    isGrid = true,
                    iconUri = carIconUri("ic_car_search"),
                ),
                browsableItem(
                    mediaId = "${SEARCH_CATEGORY_PREFIX}pop hits",
                    title = "Pop & Dance",
                    subtitle = "Upbeat dance & energetic pop",
                    isGrid = true,
                    iconUri = carIconUri("ic_car_search"),
                ),
                browsableItem(
                    mediaId = "${SEARCH_CATEGORY_PREFIX}hip hop",
                    title = "Hip-Hop & R&B",
                    subtitle = "Beats, rap & smooth flow",
                    isGrid = true,
                    iconUri = carIconUri("ic_car_search"),
                ),
                browsableItem(
                    mediaId = "${SEARCH_CATEGORY_PREFIX}rock classics",
                    title = "Rock & Alternative",
                    subtitle = "Guitar anthems & indie rock",
                    isGrid = true,
                    iconUri = carIconUri("ic_car_search"),
                ),
                browsableItem(
                    mediaId = "${SEARCH_CATEGORY_PREFIX}chill driving",
                    title = "Chill Driving Mix",
                    subtitle = "Mellow beats for highway cruising",
                    isGrid = true,
                    iconUri = carIconUri("ic_car_search"),
                ),
                browsableItem(
                    mediaId = "${SEARCH_CATEGORY_PREFIX}workout energy",
                    title = "High Energy & Workout",
                    subtitle = "Pump-up beats and motivation",
                    isGrid = true,
                    iconUri = carIconUri("ic_car_search"),
                ),
                browsableItem(
                    mediaId = "${SEARCH_CATEGORY_PREFIX}lofi relax",
                    title = "Lofi & Calm Beats",
                    subtitle = "Peaceful study and chill rhythms",
                    isGrid = true,
                    iconUri = carIconUri("ic_car_search"),
                ),
                browsableItem(
                    mediaId = "${SEARCH_CATEGORY_PREFIX}retro 80s 90s hits",
                    title = "Retro 80s & 90s Hits",
                    subtitle = "Timeless classics and nostalgia",
                    isGrid = true,
                    iconUri = carIconUri("ic_car_search"),
                ),
            ),
        )
        return items
    }

    private suspend fun loadCategoryTracks(query: String): List<MediaBrowserCompat.MediaItem> {
        searchHistoryRepository.add(query)
        val tracks = searchTracks(query)
        if (tracks.isEmpty()) {
            return listOf(
                playableItem(
                    mediaId = "$QUEUE_ACTION_PREFIX:empty",
                    track = PlayableTrack(title = "No songs found for \"$query\"", artist = "Try another category"),
                    overrideIconUri = carIconUri("ic_car_search"),
                ),
            )
        }
        val token = nextSearchToken.incrementAndGet().toString(36)
        synchronized(searchQueues) { searchQueues[token] = tracks }
        return tracks.mapIndexed { index, track ->
            playableItem("$SEARCH_PREFIX$token:$index", track)
        }
    }

    private suspend fun loadDownloadsItems(): List<MediaBrowserCompat.MediaItem> {
        val list = downloadedTrackDao.getAllList()
        if (list.isEmpty()) {
            return listOf(
                playableItem(
                    mediaId = "$DOWNLOAD_ACTION_PREFIX:empty",
                    track = PlayableTrack(
                        title = "No offline songs saved",
                        artist = "Downloaded songs will appear here",
                    ),
                    overrideIconUri = carIconUri("ic_car_download"),
                ),
            )
        }
        val shuffleItem = playableItem(
            mediaId = "$DOWNLOAD_ACTION_PREFIX:shuffle",
            track = PlayableTrack(
                title = "🔀 Shuffle All Offline Songs",
                artist = "${list.size} songs available without internet",
            ),
            overrideIconUri = carIconUri("ic_car_shuffle"),
        )
        val trackItems = list.map { entity ->
            val track = entity.toPlayableTrack()
            val badge = entity.formatBadge
            val lyricsBadge = if (entity.hasLyrics) " • Lyrics" else ""
            playableItem(
                mediaId = "$DOWNLOAD_PREFIX${entity.id}",
                track = track,
                overrideSubtitle = "${entity.artist} • $badge$lyricsBadge",
            )
        }
        return listOf(shuffleItem) + trackItems
    }

    private suspend fun loadLyricsItems(state: MusicPlayerState): List<MediaBrowserCompat.MediaItem> {
        val current = state.current ?: return listOf(
            playableItem(
                mediaId = "$SEEK_LYRIC_PREFIX 0",
                track = PlayableTrack(
                    title = "No song playing",
                    artist = "Play a track from Up Next, Search, or Playlists",
                ),
            ),
        )
        val lyricsResult = runCatching {
            lyricsRepository.getLyrics(
                title = current.title,
                artist = current.artist,
                album = current.album,
                durationSeconds = if (state.durationMs > 0) (state.durationMs / 1000).toInt() else null,
            )
        }.getOrNull()

        if (lyricsResult is LyricsResult.Success) {
            if (lyricsResult.lines.isNotEmpty()) {
                val header = playableItem(
                    mediaId = "$SEEK_LYRIC_PREFIX${state.positionMs}",
                    track = current.copy(
                        title = "♫ ${current.title}",
                        artist = "${current.artist} • Synced Lyrics (Tap line to seek)",
                    ),
                )
                val lineItems = lyricsResult.lines.map { line ->
                    val min = line.timeMs / 60000
                    val sec = (line.timeMs % 60000) / 1000
                    val timeStr = "%02d:%02d".format(min, sec)
                    val isNearCurrent = kotlin.math.abs(state.positionMs - line.timeMs) < 3000
                    val prefix = if (isNearCurrent) "▶ " else ""
                    playableItem(
                        mediaId = "$SEEK_LYRIC_PREFIX${line.timeMs}",
                        track = PlayableTrack(
                            title = "$prefix${line.text}",
                            artist = "$timeStr • Tap to jump here",
                            artworkUrl = current.artworkUrl,
                        ),
                    )
                }
                return listOf(header) + lineItems
            } else if (!lyricsResult.plainLyrics.isNullOrBlank()) {
                val lines = lyricsResult.plainLyrics.lines().filter { it.isNotBlank() }
                val header = playableItem(
                    mediaId = "$SEEK_LYRIC_PREFIX${state.positionMs}",
                    track = current.copy(
                        title = "♫ ${current.title}",
                        artist = "${current.artist} • Plain Lyrics",
                    ),
                )
                val lineItems = lines.mapIndexed { idx, line ->
                    playableItem(
                        mediaId = "$SEEK_LYRIC_PREFIX${state.positionMs}",
                        track = PlayableTrack(
                            title = line.trim(),
                            artist = current.title,
                            artworkUrl = current.artworkUrl,
                        ),
                    )
                }
                return listOf(header) + lineItems
            }
        }

        return listOf(
            playableItem(
                mediaId = "$SEEK_LYRIC_PREFIX${state.positionMs}",
                track = PlayableTrack(
                    title = "♫ ${current.title}",
                    artist = "Instrumental or lyrics unavailable for this track",
                    artworkUrl = current.artworkUrl,
                ),
            ),
        )
    }

    private suspend fun playPlaylist(id: Long?, index: Int?, player: MusicPlayer): Boolean {
        val playlist = id?.let { resolvePlaylistWithTracks(it) } ?: return false
        val tracks = playlist.tracks.map { it.toPlayableTrack() }
        if (tracks.isEmpty()) return false
        player.playQueue(
            tracks = tracks,
            startIndex = index?.coerceIn(tracks.indices) ?: 0,
            sourceLabel = playlist.title,
        )
        return true
    }

    private suspend fun playDownloads(player: MusicPlayer): Boolean {
        val tracks = downloadedTrackDao.getAllList().map { it.toPlayableTrack() }
        if (tracks.isEmpty()) return false
        player.playQueue(tracks, sourceLabel = "Downloads")
        return true
    }

    private fun playCachedSearch(mediaId: String, player: MusicPlayer): Boolean {
        val parts = mediaId.removePrefix(SEARCH_PREFIX).split(':', limit = 2)
        val token = parts.getOrNull(0) ?: return false
        val index = parts.getOrNull(1)?.toIntOrNull() ?: return false
        val tracks = synchronized(searchQueues) { searchQueues[token] } ?: return false
        if (index !in tracks.indices) return false
        player.playQueue(tracks, startIndex = index, sourceLabel = "Android Auto")
        return true
    }

    private suspend fun searchTracks(query: String): List<PlayableTrack> {
        val cleanQuery = query.trim()
        if (cleanQuery.isEmpty()) return emptyList()
        return searchRepository.search(SearchTab.TRACKS, cleanQuery)
            .asSequence()
            .filter { it.name.isNotBlank() }
            .map { result ->
                PlayableTrack(
                    title = result.name,
                    artist = result.artist.orEmpty().ifBlank { "Unknown artist" },
                    album = result.subtitle,
                    artworkUrl = result.artworkUrl,
                    videoId = result.videoId,
                )
            }
            .distinctBy { "${it.title.lowercase()}|${it.artist.lowercase()}" }
            .take(MAX_SEARCH_RESULTS)
            .toList()
    }

    private fun browsableItem(
        mediaId: String,
        title: String,
        subtitle: String,
        playable: Boolean = false,
        isGrid: Boolean = false,
        iconUri: Uri = appIconUri(),
    ): MediaBrowserCompat.MediaItem {
        val flags = MediaBrowserCompat.MediaItem.FLAG_BROWSABLE or
            if (playable) MediaBrowserCompat.MediaItem.FLAG_PLAYABLE else 0
        val extras = Bundle().apply {
            putInt(CONTENT_STYLE_BROWSABLE_KEY, if (isGrid) CONTENT_STYLE_GRID else CONTENT_STYLE_LIST)
            putInt(CONTENT_STYLE_PLAYABLE_KEY, CONTENT_STYLE_LIST)
            putBoolean(CONTENT_STYLE_SUPPORTED_KEY, true)
        }
        return MediaBrowserCompat.MediaItem(
            MediaDescriptionCompat.Builder()
                .setMediaId(mediaId)
                .setTitle(title)
                .setSubtitle(subtitle)
                .setIconUri(iconUri)
                .setExtras(extras)
                .build(),
            flags,
        )
    }

    private fun playableItem(
        mediaId: String,
        track: PlayableTrack,
        overrideTitle: String? = null,
        overrideSubtitle: String? = null,
        overrideIconUri: Uri? = null,
    ): MediaBrowserCompat.MediaItem {
        val extras = Bundle().apply {
            putInt(CONTENT_STYLE_PLAYABLE_KEY, CONTENT_STYLE_LIST)
            putBoolean(CONTENT_STYLE_SUPPORTED_KEY, true)
        }
        val artUri = overrideIconUri ?: track.artworkUrl?.takeIf(String::isNotBlank)?.let { Uri.parse(it) } ?: appIconUri()
        return MediaBrowserCompat.MediaItem(
            MediaDescriptionCompat.Builder()
                .setMediaId(mediaId)
                .setTitle(overrideTitle ?: track.title)
                .setSubtitle(overrideSubtitle ?: track.artist)
                .setDescription(track.album)
                .setIconUri(artUri)
                .setExtras(extras)
                .build(),
            MediaBrowserCompat.MediaItem.FLAG_PLAYABLE,
        )
    }

    private fun carIconUri(name: String): Uri {
        val resId = context.resources.getIdentifier(name, "drawable", context.packageName)
        return if (resId != 0) {
            Uri.parse("android.resource://${context.packageName}/$resId")
        } else {
            appIconUri()
        }
    }

    private fun appIconUri(): Uri = Uri.parse(
        "android.resource://${context.packageName}/${com.listenit.music.R.drawable.ic_car_attribution}",
    )

    private fun DownloadedTrackEntity.toPlayableTrack() = PlayableTrack(
        title = title,
        artist = artist,
        album = album.takeIf(String::isNotBlank),
        artworkUrl = artworkUrl,
        playbackUrl = mediaStoreUri?.takeIf(String::isNotBlank)
            ?: Uri.fromFile(File(filePath)).toString(),
    )

    private fun trackCountLabel(count: Int) = if (count == 1) "1 track" else "$count tracks"

    companion object {
        const val ROOT_ID = "listenit_root"
        const val QUEUE_ID = "category:queue"
        const val SEARCH_ID = "category:search"
        const val LYRICS_ID = "category:lyrics"
        const val PLAYLISTS_ID = "category:playlists"
        const val DOWNLOADS_ID = "category:downloads"

        private const val SEEK_LYRIC_PREFIX = "lyric_seek:"
        private const val QUEUE_TRACK_PREFIX = "queue:"
        private const val QUEUE_ACTION_PREFIX = "queue_action:"
        private const val DOWNLOAD_ACTION_PREFIX = "download_action:"
        private const val SEARCH_CATEGORY_PREFIX = "search:category:"
        private const val PLAYLIST_PREFIX = "playlist:"
        private const val PLAYLIST_TRACK_PREFIX = "playlist-track:"
        private const val DOWNLOAD_PREFIX = "download:"
        private const val SEARCH_PREFIX = "search:"
        private const val MAX_CACHED_SEARCHES = 12
        private const val MAX_SEARCH_RESULTS = 35

        const val CONTENT_STYLE_BROWSABLE_KEY = "androidx.media.contentstyle.MediaDescriptionCompat.KEY_CONTENT_STYLE_BROWSABLE"
        const val CONTENT_STYLE_PLAYABLE_KEY = "androidx.media.contentstyle.MediaDescriptionCompat.KEY_CONTENT_STYLE_PLAYABLE"
        const val CONTENT_STYLE_SUPPORTED_KEY = "android.media.browse.CONTENT_STYLE_SUPPORTED"
        const val CONTENT_STYLE_LIST = 1
        const val CONTENT_STYLE_GRID = 2
    }
}
