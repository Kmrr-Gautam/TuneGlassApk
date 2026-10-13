package com.listenit.music.ui.feed

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.listenit.music.data.feed.FeedMix
import com.listenit.music.data.feed.FeedArtist
import com.listenit.music.data.feed.FeedData
import com.listenit.music.data.feed.FeedQuickTile
import com.listenit.music.data.feed.FeedRepository
import com.listenit.music.data.generate.GeneratedTrack
import com.listenit.music.data.generate.youtubeVideoIdOrNull
import com.listenit.music.data.local.SessionPreferences
import com.listenit.music.data.model.ArtistRef
import com.listenit.music.data.model.ImageDto
import com.listenit.music.data.model.RecentTrack
import com.listenit.music.data.music.InnerTubeMusicApi
import com.listenit.music.data.music.YouTubeMusicTrack
import com.listenit.music.data.music.YouTubePlaylistSummary
import com.listenit.music.playback.MusicPlayer
import com.listenit.music.playback.PlayableTrack
import com.listenit.music.data.ytmusic.YtMusicAuthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
data class FeedUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val launchingRadio: String? = null,
    val error: String? = null,
    val feedData: FeedData = FeedData(),
    val recommendedSeedTitle: String? = null,
    val recommendedSeedArtist: String? = null,
)

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val repository: FeedRepository,
    private val sessionPreferences: SessionPreferences,
    private val musicPlayer: MusicPlayer,
    private val innerTube: InnerTubeMusicApi,
    private val ytAuth: YtMusicAuthManager,
    private val followedArtistsRepository: com.listenit.music.data.repository.FollowedArtistsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()
    private var feedJob: Job? = null
    private var lastLoadedMillis: Long = 0L
    private var recommendationJob: Job? = null
    private var lastListenedSeedKey: String? = null

    private fun loadRecommendationsForListenedTrack(track: PlayableTrack) {
        val seedKey = "${track.artist.trim().lowercase()}|${track.title.trim().lowercase()}"
        if (seedKey == lastListenedSeedKey) return
        lastListenedSeedKey = seedKey

        recommendationJob?.cancel()
        recommendationJob = viewModelScope.launch {
            try {
                var videoId = track.videoId?.takeIf { it.isNotBlank() }
                if (videoId.isNullOrBlank()) {
                    val searchResult = innerTube.searchSongs("${track.artist} ${track.title}", limit = 1, prefetchStreams = false)
                    videoId = searchResult.firstOrNull()?.videoId
                }

                val relatedSongs = if (!videoId.isNullOrBlank()) {
                    innerTube.fetchRelatedSongs(videoId, limit = 20, prefetchStreams = false)
                } else {
                    innerTube.searchSongs("${track.artist} ${track.title} radio", limit = 15, prefetchStreams = false)
                }

                if (relatedSongs.isNotEmpty()) {
                    _uiState.update { current ->
                        current.copy(
                            feedData = current.feedData.copy(
                                quickPicks = relatedSongs,
                                hasYtRecommendations = true,
                            ),
                            recommendedSeedTitle = track.title,
                            recommendedSeedArtist = track.artist,
                        )
                    }
                }
            } catch (_: CancellationException) {
                // Cancelled when a new track starts
            } catch (_: Exception) {
                // Ignore transient failures
            }
        }
    }

    init {
        viewModelScope.launch {
            ytAuth.awaitLoadedConnection()
            combine(ytAuth.connection, sessionPreferences.session) { connection, session ->
                connection to session.username
            }.distinctUntilChanged().collect { (connection, _) ->
                feedJob?.cancel()
                _uiState.value = FeedUiState(
                    feedData = FeedData(
                        isYtConnected = connection.isConnected,
                        ytAccountName = connection.accountName.takeIf { connection.isConnected },
                    ),
                )
                loadFeed()
            }
        }
        viewModelScope.launch {
            var initialObserved = false
            followedArtistsRepository.followedArtists.collect {
                if (initialObserved) {
                    refresh()
                }
                initialObserved = true
            }
        }
        viewModelScope.launch {
            musicPlayer.state.collect { playback ->
                val track = playback.current ?: return@collect
                val existing = _uiState.value.feedData.jumpBackIn
                if (existing.firstOrNull()?.name.equals(track.title, ignoreCase = true) &&
                    existing.firstOrNull()?.artist?.displayName.equals(track.artist, ignoreCase = true)
                ) {
                    return@collect
                }
                val newRecent = RecentTrack(
                    name = track.title,
                    artist = ArtistRef(name = track.artist),
                    album = ArtistRef(name = track.album.orEmpty()),
                    image = track.artworkUrl?.let { listOf(ImageDto(it, "extralarge")) }.orEmpty(),
                    url = "https://www.youtube.com/watch?v=${track.videoId.orEmpty()}",
                )
                val updated = (listOf(newRecent) + existing.filterNot {
                    it.name.equals(track.title, ignoreCase = true) &&
                        it.artist.displayName.equals(track.artist, ignoreCase = true)
                }).take(20)
                _uiState.update { current ->
                    current.copy(feedData = current.feedData.copy(jumpBackIn = updated))
                }
                loadRecommendationsForListenedTrack(track)
            }
        }
    }

    fun loadFeed() = fetchFeed(refreshing = false)

    fun refresh() = fetchFeed(refreshing = true)

    fun onVisible() {
        if (feedJob?.isActive == true) return
        // ON_START fires on every return from a pushed screen (artist, album,
        // playlist...). Reloading the whole feed each time kept the tabs in
        // a near-constant loading state and hammered the network — only
        // refresh when there's nothing yet or the data is stale.
        val hasContent = _uiState.value.feedData.quickPicks.isNotEmpty() ||
            _uiState.value.feedData.topArtists.isNotEmpty() ||
            _uiState.value.feedData.newReleases.isNotEmpty()
        val stale = System.currentTimeMillis() - lastLoadedMillis > STALE_AFTER_MILLIS
        if (!hasContent || stale) refresh()
    }

    fun playInfiniteRadio() {
        val feed = _uiState.value.feedData
        (feed.quickPicks + feed.ytLikedSongs + feed.freshFinds).randomOrNull()?.let {
            playTrack(it, "Infinite Radio")
        }
    }

    fun playMix(mix: FeedMix) = playTrack(mix.seed, mix.title)

    private fun fetchFeed(refreshing: Boolean) {
        feedJob?.cancel()
        feedJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = !refreshing || it.isLoading, isRefreshing = refreshing, error = null) }
            try {
                val connection = ytAuth.awaitLoadedConnection()
                val username = sessionPreferences.session.value.username.takeIf(String::isNotBlank)
                val data = repository.loadFeed(username) { update ->
                    ensureActive()
                    if (ytAuth.connection.value == connection &&
                        sessionPreferences.session.value.username.takeIf(String::isNotBlank) == username
                    ) {
                        val sessionRecent = _uiState.value.feedData.jumpBackIn
                        val mergedRecent = (sessionRecent + update.jumpBackIn).distinctBy {
                            it.name.trim().lowercase() to it.artist.displayName.trim().lowercase()
                        }.take(20)
                        _uiState.update { it.copy(feedData = update.copy(jumpBackIn = mergedRecent), isLoading = false) }
                    }
                }
                ensureActive()
                if (ytAuth.connection.value != connection ||
                    sessionPreferences.session.value.username.takeIf(String::isNotBlank) != username
                ) return@launch
                val sessionRecent = _uiState.value.feedData.jumpBackIn
                val mergedRecent = (sessionRecent + data.jumpBackIn).distinctBy {
                    it.name.trim().lowercase() to it.artist.displayName.trim().lowercase()
                }.take(20)
                val currentSeed = _uiState.value.recommendedSeedTitle
                val finalQuickPicks = if (currentSeed != null) {
                    _uiState.value.feedData.quickPicks
                } else {
                    data.quickPicks
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        feedData = data.copy(
                            jumpBackIn = mergedRecent,
                            quickPicks = finalQuickPicks,
                        ),
                        error = null,
                    )
                }
                lastLoadedMillis = System.currentTimeMillis()
                if (currentSeed == null && mergedRecent.isNotEmpty()) {
                    val firstRecent = mergedRecent.first()
                    loadRecommendationsForListenedTrack(
                        PlayableTrack(
                            title = firstRecent.name,
                            artist = firstRecent.artist.displayName,
                            album = firstRecent.album.displayName,
                            artworkUrl = firstRecent.artworkUrl,
                            videoId = GeneratedTrack(firstRecent.name, firstRecent.artist.displayName, firstRecent.artworkUrl, firstRecent.url).youtubeVideoIdOrNull(),
                        )
                    )
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                ensureActive()
                _uiState.update {
                    it.copy(isLoading = false, isRefreshing = false, error = "Couldn't update your feed. Please try again.")
                }
            }
        }
    }

    fun dismissError() = _uiState.update { it.copy(error = null) }

    fun playTrack(track: YouTubeMusicTrack, sourceLabel: String = "Feed") {
        musicPlayer.play(track.toPlayableTrack(), sourceLabel = sourceLabel, startRadio = true)
    }

    fun shuffleTracksQueue(tracks: List<YouTubeMusicTrack>, sourceLabel: String = "Feed") {
        if (tracks.isEmpty()) return
        val shuffled = tracks.shuffled()
        musicPlayer.playQueue(
            shuffled.map { it.toPlayableTrack() },
            startIndex = 0,
            sourceLabel = "$sourceLabel Shuffle",
        )
    }

    fun playTracksQueue(tracks: List<YouTubeMusicTrack>, startIndex: Int = 0, sourceLabel: String = "Feed") {
        if (tracks.isEmpty()) return
        val playable = tracks.map { it.toPlayableTrack() }
        musicPlayer.playQueue(playable, startIndex = startIndex.coerceIn(0, playable.lastIndex.coerceAtLeast(0)), sourceLabel = sourceLabel)
    }

    fun playRecentQueue(tracks: List<RecentTrack>, startIndex: Int = 0) {
        if (tracks.isEmpty()) return
        val playable = tracks.map {
            PlayableTrack(
                title = it.name,
                artist = it.artist.displayName,
                album = it.album.displayName,
                artworkUrl = it.artworkUrl,
                videoId = GeneratedTrack(it.name, it.artist.displayName, it.artworkUrl, it.url).youtubeVideoIdOrNull(),
            )
        }
        musicPlayer.playQueue(playable, startIndex = startIndex.coerceIn(0, playable.lastIndex.coerceAtLeast(0)), sourceLabel = "Jump Back In")
    }

    fun playGeneratedQueue(tracks: List<GeneratedTrack>, startIndex: Int = 0, sourceLabel: String = "Heavy Rotation") {
        if (tracks.isEmpty()) return
        val playable = tracks.map {
            PlayableTrack(
                title = it.name,
                artist = it.artist,
                album = it.album,
                artworkUrl = it.artworkUrl,
                videoId = it.youtubeVideoIdOrNull(),
            )
        }
        musicPlayer.playQueue(playable, startIndex = startIndex.coerceIn(0, playable.lastIndex.coerceAtLeast(0)), sourceLabel = sourceLabel)
    }

    fun playArtistRadio(artist: FeedArtist) {
        viewModelScope.launch {
            val seeds = try {
                innerTube.searchSongs("${artist.name} songs", limit = 5, prefetchStreams = false)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                emptyList()
            }
            val seed = seeds.firstOrNull {
                it.artist.contains(artist.name, ignoreCase = true) || artist.name.contains(it.artist, ignoreCase = true)
            } ?: seeds.firstOrNull()
            val related = seed?.let {
                try {
                    innerTube.fetchRelatedSongs(it.videoId, limit = 25, prefetchStreams = false)
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (_: Exception) {
                    emptyList()
                }
            }.orEmpty()
            val songs = (listOfNotNull(seed) + related).distinctBy(YouTubeMusicTrack::videoId)
            if (songs.isNotEmpty()) {
                playTracksQueue(songs, startIndex = 0, sourceLabel = "${artist.name} Radio")
            }
        }
    }

    fun playDiscoveryQuery(title: String, query: String) {
        if (_uiState.value.launchingRadio != null) return
        viewModelScope.launch {
            _uiState.update { it.copy(launchingRadio = title) }
            try {
                val seeds = try {
                    innerTube.searchSongs(query, limit = 8, prefetchStreams = false)
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (_: Exception) {
                    emptyList()
                }
                val seed = seeds.take(5).randomOrNull()
                val related = seed?.let {
                    try {
                        innerTube.fetchRelatedSongs(it.videoId, limit = 24, prefetchStreams = false)
                    } catch (cancellation: CancellationException) {
                        throw cancellation
                    } catch (_: Exception) {
                        emptyList()
                    }
                }.orEmpty()
                val songs = (listOfNotNull(seed) + related).distinctBy(YouTubeMusicTrack::videoId)
                if (songs.isNotEmpty()) playTracksQueue(songs, sourceLabel = title)
            } finally {
                _uiState.update { it.copy(launchingRadio = null) }
            }
        }
    }

    fun playPlaylistSummary(summary: YouTubePlaylistSummary) {
        viewModelScope.launch {
            val result = runCatching { innerTube.fetchPlaylist(summary.id, maxTracks = 100) }
                .getOrNull()
            val tracks = result?.tracks.orEmpty()
            if (tracks.isNotEmpty()) {
                val playable = tracks.map { it.toPlayableTrack() }
                musicPlayer.playQueue(playable, startIndex = 0, sourceLabel = summary.title)
            }
        }
    }

    fun handleQuickTileClick(tile: FeedQuickTile) {
        val videoId = tile.actionVideoId ?: return
        musicPlayer.play(
            PlayableTrack(
                title = tile.title,
                artist = tile.subtitle ?: "",
                artworkUrl = tile.artworkUrl,
                videoId = videoId.takeIf(String::isNotBlank),
            ),
            sourceLabel = "Quick Picks",
        )
    }

    private fun YouTubeMusicTrack.toPlayableTrack(): PlayableTrack = PlayableTrack(
        title = title,
        artist = artist,
        album = album,
        artworkUrl = artworkUrl,
        videoId = videoId.takeIf(String::isNotBlank),
    )

    private companion object {
        const val STALE_AFTER_MILLIS = 90_000L
    }
}
