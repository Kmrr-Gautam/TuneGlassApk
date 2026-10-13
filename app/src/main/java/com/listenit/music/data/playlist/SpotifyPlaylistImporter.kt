package com.listenit.music.data.playlist

import com.listenit.music.data.generate.GeneratedTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

data class SpotifyPlaylistData(
    val id: String,
    val name: String,
    val description: String?,
    val coverArtUrl: String?,
    val tracks: List<GeneratedTrack>,
)

@Singleton
class SpotifyPlaylistImporter @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val playlistRepository: PlaylistRepository,
) {

    private val playlistIdPattern = Pattern.compile("(?:playlist[/:]|playlist\\?list=)([a-zA-Z0-9]+)")
    private val bareIdPattern = Pattern.compile("^[a-zA-Z0-9]{22}$")

    fun extractPlaylistId(urlOrId: String): String? {
        val trimmed = urlOrId.trim()
        val matcher = playlistIdPattern.matcher(trimmed)
        if (matcher.find()) {
            return matcher.group(1)
        }
        if (bareIdPattern.matcher(trimmed).matches()) {
            return trimmed
        }
        return null
    }

    suspend fun fetchPlaylist(urlOrId: String): SpotifyPlaylistData = withContext(Dispatchers.IO) {
        val playlistId = extractPlaylistId(urlOrId)
            ?: throw IllegalArgumentException("Invalid Spotify playlist URL. Please provide a valid public Spotify playlist link.")

        val embedUrl = "https://open.spotify.com/embed/playlist/$playlistId"
        val request = Request.Builder()
            .url(embedUrl)
            .header(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36",
            )
            .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .build()

        val html = okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Failed to load Spotify playlist (HTTP ${response.code}). Ensure the playlist is public.")
            }
            response.body?.string() ?: throw IOException("Empty response from Spotify")
        }

        val scriptPattern = Pattern.compile("<script id=\"__NEXT_DATA__\" type=\"application/json\">(.*?)</script>", Pattern.DOTALL)
        val scriptMatcher = scriptPattern.matcher(html)
        if (!scriptMatcher.find()) {
            throw IOException("Could not parse Spotify playlist data. The playlist may be private or restricted.")
        }

        val jsonStr = scriptMatcher.group(1) ?: throw IOException("Empty Spotify data script block")
        val root = JSONObject(jsonStr)
        val entity = root.optJSONObject("props")
            ?.optJSONObject("pageProps")
            ?.optJSONObject("state")
            ?.optJSONObject("data")
            ?.optJSONObject("entity")
            ?: throw IOException("Spotify playlist data structure not recognized.")

        val playlistName = entity.optString("name").ifBlank {
            entity.optString("title", "Imported Spotify Playlist")
        }
        val description = entity.optString("subtitle").ifBlank { null }
        val coverArtUrl = entity.optJSONObject("coverArt")
            ?.optJSONArray("sources")
            ?.optJSONObject(0)
            ?.optString("url")

        val trackList = entity.optJSONArray("trackList")
            ?: throw IOException("No track list found in this Spotify playlist.")

        val tracks = mutableListOf<GeneratedTrack>()
        for (i in 0 until trackList.length()) {
            val item = trackList.optJSONObject(i) ?: continue
            val title = item.optString("title").trim()
            val artist = item.optString("subtitle").trim()
            if (title.isBlank() || artist.isBlank()) continue

            val durationMs = item.optLong("duration", 0L).takeIf { it > 0 }
            val trackUri = item.optString("uri")
            val trackId = trackUri.removePrefix("spotify:track:")

            tracks.add(
                GeneratedTrack(
                    name = title,
                    artist = artist,
                    album = null,
                    artworkUrl = coverArtUrl,
                    url = if (trackId.isNotBlank()) "https://open.spotify.com/track/$trackId" else "",
                ),
            )
        }

        if (tracks.isEmpty()) {
            throw IOException("This Spotify playlist contains no tracks to import.")
        }

        SpotifyPlaylistData(
            id = playlistId,
            name = playlistName,
            description = description,
            coverArtUrl = coverArtUrl,
            tracks = tracks,
        )
    }

    suspend fun importPlaylist(urlOrId: String): SavedPlaylist = withContext(Dispatchers.IO) {
        val data = fetchPlaylist(urlOrId)
        playlistRepository.save(
            title = data.name,
            subtitle = "Spotify • ${data.tracks.size} tracks",
            mode = "custom",
            tracks = data.tracks,
        )
    }
}
