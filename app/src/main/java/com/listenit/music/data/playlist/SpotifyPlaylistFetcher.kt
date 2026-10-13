package com.listenit.music.data.playlist

import com.listenit.music.data.generate.GeneratedTrack
import com.listenit.music.data.music.InnerTubeMusicApi
import com.listenit.music.data.music.YouTubeMusicTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

data class SpotifyRawTrack(
    val title: String,
    val artist: String,
    val album: String? = null,
    val durationMs: Long = 0L,
    val uri: String? = null,
    val artworkUrl: String? = null,
)

data class SpotifyPlaylistResult(
    val id: String,
    val title: String,
    val description: String? = null,
    val artworkUrl: String? = null,
    val author: String? = null,
    val totalTracks: Int = 0,
    val tracks: List<SpotifyRawTrack> = emptyList(),
)

@Singleton
class SpotifyPlaylistFetcher @Inject constructor(
    private val innerTube: InnerTubeMusicApi,
    private val playlistRepository: PlaylistRepository,
) {

    /**
     * Extracts a clean 22-character alphanumeric Spotify playlist ID from URLs or URIs:
     * - https://open.spotify.com/playlist/37i9dQZF1DXcBWIGoYBM5M?si=...
     * - https://open.spotify.com/embed/playlist/37i9dQZF1DXcBWIGoYBM5M
     * - spotify:playlist:37i9dQZF1DXcBWIGoYBM5M
     * - 37i9dQZF1DXcBWIGoYBM5M
     */
    fun extractPlaylistId(input: String): String? {
        val trimmed = input.trim()
        val regex = Regex("""(?:spotify(?:\.com)?/(?:embed/)?playlist/|spotify:playlist:|^)([a-zA-Z0-9]{15,30})""")
        val match = regex.find(trimmed)
        return match?.groupValues?.getOrNull(1) ?: if (trimmed.matches(Regex("""^[a-zA-Z0-9]{15,30}$"""))) trimmed else null
    }

    /**
     * Fetches public Spotify playlist metadata and tracks without requiring Spotify login.
     * Uses Spotify's official public embed payload (__NEXT_DATA__).
     */
    suspend fun fetchPlaylist(inputUrlOrId: String): SpotifyPlaylistResult = withContext(Dispatchers.IO) {
        val playlistId = extractPlaylistId(inputUrlOrId)
            ?: throw IllegalArgumentException("Invalid Spotify playlist URL or ID: $inputUrlOrId")

        val embedUrl = "https://open.spotify.com/embed/playlist/$playlistId"
        val connection = (URL(embedUrl).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 12000
            readTimeout = 15000
            setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36",
            )
            setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            setRequestProperty("Accept-Language", "en-US,en;q=0.9")
        }

        val html = connection.inputStream.use { stream ->
            BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).readText()
        }

        val scriptMarker = "<script id=\"__NEXT_DATA__\" type=\"application/json\">"
        val scriptStart = html.indexOf(scriptMarker)
        if (scriptStart == -1) {
            throw IllegalStateException("Unable to read Spotify playlist data. The playlist might be private or removed.")
        }

        val jsonStart = scriptStart + scriptMarker.length
        val jsonEnd = html.indexOf("</script>", jsonStart)
        if (jsonEnd == -1) {
            throw IllegalStateException("Corrupted Spotify embed response.")
        }

        val jsonStr = html.substring(jsonStart, jsonEnd)
        val root = JSONObject(jsonStr)

        val props = root.optJSONObject("props")
        val pageProps = props?.optJSONObject("pageProps")
        val state = pageProps?.optJSONObject("state")
        val data = state?.optJSONObject("data")
        val entity = data?.optJSONObject("entity")
            ?: throw IllegalStateException("Spotify playlist data not found.")

        val title = entity.optString("title").ifBlank {
            entity.optString("name").ifBlank { "Spotify Playlist" }
        }

        // Cover art
        val visualIdentity = entity.optJSONObject("visualIdentity")
        val images = visualIdentity?.optJSONArray("image")
        var coverArt: String? = null
        if (images != null && images.length() > 0) {
            // Find highest resolution image
            var maxW = 0
            for (i in 0 until images.length()) {
                val imgObj = images.optJSONObject(i)
                val w = imgObj?.optInt("maxWidth", 0) ?: 0
                val url = imgObj?.optString("url")
                if (!url.isNullOrBlank() && w >= maxW) {
                    maxW = w
                    coverArt = url
                }
            }
        }
        if (coverArt == null) {
            val coverSources = entity.optJSONObject("coverArt")?.optJSONArray("sources")
            if (coverSources != null && coverSources.length() > 0) {
                coverArt = coverSources.optJSONObject(0)?.optString("url")
            }
        }

        // Extract track list
        val trackList = entity.optJSONArray("trackList")
        val parsedTracks = mutableListOf<SpotifyRawTrack>()

        if (trackList != null) {
            for (i in 0 until trackList.length()) {
                val item = trackList.optJSONObject(i) ?: continue
                val trackTitle = item.optString("title").trim()
                val subtitle = item.optString("subtitle").trim() // Usually artists joined with commas
                val duration = item.optLong("duration", 0L)
                val uri = item.optString("uri")

                if (trackTitle.isNotBlank()) {
                    parsedTracks.add(
                        SpotifyRawTrack(
                            title = trackTitle,
                            artist = subtitle.ifBlank { "Unknown Artist" },
                            durationMs = duration,
                            uri = uri,
                            artworkUrl = coverArt,
                        )
                    )
                }
            }
        }

        SpotifyPlaylistResult(
            id = playlistId,
            title = title,
            artworkUrl = coverArt,
            author = "Spotify",
            totalTracks = parsedTracks.size,
            tracks = parsedTracks,
        )
    }

    /**
     * Resolves Spotify tracks to YouTube Music tracks using fuzzy search & matching,
     * then saves to local SavedPlaylist.
     */
    suspend fun importSpotifyPlaylist(
        spotifyResult: SpotifyPlaylistResult,
        selectedTracks: List<SpotifyRawTrack> = spotifyResult.tracks,
        onProgress: (current: Int, total: Int) -> Unit = { _, _ -> },
    ): SavedPlaylist = withContext(Dispatchers.IO) {
        val limiter = Semaphore(4)
        var completedCount = 0

        val matchedTracks = coroutineScope {
            selectedTracks.map { raw ->
                async {
                    limiter.withPermit {
                        val query = "${raw.title} ${raw.artist}".trim()
                        val candidates = runCatching {
                            innerTube.searchSongs(query, limit = 5, prefetchStreams = false)
                        }.getOrDefault(emptyList())

                        // Find best matching track
                        val best = findBestMatch(raw, candidates)
                        synchronized(this@SpotifyPlaylistFetcher) {
                            completedCount++
                            onProgress(completedCount, selectedTracks.size)
                        }

                        best?.let { yt ->
                            GeneratedTrack(
                                name = raw.title.ifBlank { yt.title },
                                artist = raw.artist.ifBlank { yt.artist },
                                album = yt.album,
                                artworkUrl = yt.artworkUrl ?: raw.artworkUrl,
                                url = "https://music.youtube.com/watch?v=${yt.videoId}",
                            )
                        }
                    }
                }
            }.awaitAll().filterNotNull()
        }

        require(matchedTracks.isNotEmpty()) {
            "Could not match tracks from this Spotify playlist on YouTube Music."
        }

        playlistRepository.save(
            title = spotifyResult.title,
            subtitle = "Spotify Import \u2022 ${matchedTracks.size} tracks",
            mode = "custom",
            tracks = matchedTracks,
        )
    }

    private fun findBestMatch(raw: SpotifyRawTrack, candidates: List<YouTubeMusicTrack>): YouTubeMusicTrack? {
        if (candidates.isEmpty()) return null
        val rawTitleClean = cleanString(raw.title)
        val rawArtistClean = cleanString(raw.artist)

        // Exact match
        val exact = candidates.firstOrNull { cand ->
            val candTitleClean = cleanString(cand.title)
            val candArtistClean = cleanString(cand.artist)
            candTitleClean.contains(rawTitleClean) && candArtistClean.contains(rawArtistClean.take(10))
        }
        if (exact != null) return exact

        // Title match
        val titleMatch = candidates.firstOrNull { cand ->
            val candTitleClean = cleanString(cand.title)
            candTitleClean.contains(rawTitleClean) || rawTitleClean.contains(candTitleClean)
        }
        return titleMatch ?: candidates.firstOrNull()
    }

    private fun cleanString(str: String): String =
        str.lowercase().replace(Regex("""[^a-z0-9]"""), "")
}
