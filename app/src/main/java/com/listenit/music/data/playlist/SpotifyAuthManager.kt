package com.listenit.music.data.playlist

import android.content.Context
import android.util.Log
import android.webkit.CookieManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpotifyAuthManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val okHttpClient: OkHttpClient,
    private val fallbackFetcher: SpotifyPlaylistFetcher,
) {
    private val prefs = context.getSharedPreferences("spotify_auth_prefs", Context.MODE_PRIVATE)

    private val _accountState = MutableStateFlow(loadInitialAccount())
    val accountState: StateFlow<SpotifyAccount> = _accountState.asStateFlow()

    val isConnected: Boolean
        get() = _accountState.value.isConnected

    private fun loadInitialAccount(): SpotifyAccount {
        val cookies = prefs.getString(KEY_COOKIES, null)
        val name = prefs.getString(KEY_DISPLAY_NAME, null)
        val id = prefs.getString(KEY_USER_ID, null)
        val avatar = prefs.getString(KEY_AVATAR_URL, null)
        val hasSession = !cookies.isNullOrBlank() && cookies.contains("sp_dc=")
        return SpotifyAccount(
            displayName = name.orEmpty().ifBlank { if (hasSession) "Spotify User" else "" },
            userId = id.orEmpty(),
            avatarUrl = avatar,
            isConnected = hasSession,
        )
    }

    suspend fun onSessionCaptured(cookies: String, directTokenJson: String? = null): Boolean =
        withContext(Dispatchers.IO) {
            try {
                prefs.edit().putString(KEY_COOKIES, cookies).apply()

                var token: String? = null
                var expiry: Long = 0L

                // If direct token was retrieved from WebView JS
                if (!directTokenJson.isNullOrBlank()) {
                    runCatching {
                        val obj = JSONObject(directTokenJson)
                        token = obj.optString("accessToken").ifBlank { null }
                        expiry = obj.optLong("accessTokenExpirationTimestampMs", 0L)
                    }
                }

                // If no direct token, request via endpoint with cookies
                if (token.isNullOrBlank()) {
                    val tokenResult = requestFreshAccessToken(cookies)
                    token = tokenResult?.first
                    expiry = tokenResult?.second ?: 0L
                }

                if (!token.isNullOrBlank()) {
                    prefs.edit()
                        .putString(KEY_ACCESS_TOKEN, token)
                        .putLong(KEY_TOKEN_EXPIRY, expiry)
                        .apply()
                }

                // Fetch user profile info
                val profile = fetchProfile(token)
                val account = SpotifyAccount(
                    displayName = profile?.first.orEmpty().ifBlank { "Spotify User" },
                    userId = profile?.second.orEmpty(),
                    avatarUrl = profile?.third,
                    isConnected = true,
                )

                prefs.edit()
                    .putString(KEY_DISPLAY_NAME, account.displayName)
                    .putString(KEY_USER_ID, account.userId)
                    .putString(KEY_AVATAR_URL, account.avatarUrl)
                    .apply()

                _accountState.value = account
                true
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize Spotify session", e)
                false
            }
        }

    suspend fun getAccessToken(): String? = withContext(Dispatchers.IO) {
        val cached = prefs.getString(KEY_ACCESS_TOKEN, null)
        val expiry = prefs.getLong(KEY_TOKEN_EXPIRY, 0L)
        val now = System.currentTimeMillis()

        if (!cached.isNullOrBlank() && expiry > now + 60_000L) {
            return@withContext cached
        }

        val cookies = prefs.getString(KEY_COOKIES, null)
        if (cookies.isNullOrBlank()) return@withContext null

        val fresh = requestFreshAccessToken(cookies)
        if (fresh != null) {
            prefs.edit()
                .putString(KEY_ACCESS_TOKEN, fresh.first)
                .putLong(KEY_TOKEN_EXPIRY, fresh.second)
                .apply()
            return@withContext fresh.first
        }
        cached
    }

    private fun requestFreshAccessToken(cookies: String): Pair<String, Long>? {
        val request = Request.Builder()
            .url("https://open.spotify.com/get_access_token?reason=transport&productType=web_player")
            .addHeader("Cookie", cookies)
            .addHeader("User-Agent", USER_AGENT)
            .addHeader("Referer", "https://open.spotify.com/")
            .build()

        return runCatching {
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                val body = response.body?.string() ?: return null
                val obj = JSONObject(body)
                val isAnon = obj.optBoolean("isAnonymous", false)
                val token = obj.optString("accessToken")
                val expiry = obj.optLong("accessTokenExpirationTimestampMs", 0L)
                if (token.isNotBlank() && !isAnon) {
                    token to expiry
                } else null
            }
        }.getOrNull()
    }

    private fun fetchProfile(token: String?): Triple<String, String, String?>? {
        if (token.isNullOrBlank()) return null
        val request = Request.Builder()
            .url("https://api.spotify.com/v1/me")
            .addHeader("Authorization", "Bearer $token")
            .addHeader("User-Agent", USER_AGENT)
            .build()

        return runCatching {
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                val body = response.body?.string() ?: return null
                val obj = JSONObject(body)
                val name = obj.optString("display_name")
                val id = obj.optString("id")
                val images = obj.optJSONArray("images")
                val avatar = images?.optJSONObject(0)?.optString("url")
                Triple(name, id, avatar)
            }
        }.getOrNull()
    }

    suspend fun fetchUserPlaylists(): List<SpotifyUserPlaylist> = withContext(Dispatchers.IO) {
        val token = getAccessToken()
            ?: throw IllegalStateException("Not logged into Spotify. Please connect your account.")

        val request = Request.Builder()
            .url("https://api.spotify.com/v1/me/playlists?limit=50")
            .addHeader("Authorization", "Bearer $token")
            .addHeader("User-Agent", USER_AGENT)
            .build()

        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IllegalStateException("Spotify API returned error: HTTP ${response.code}")
            }
            val body = response.body?.string()
                ?: throw IllegalStateException("Empty response from Spotify")
            val root = JSONObject(body)
            val items = root.optJSONArray("items") ?: return@withContext emptyList()
            val list = mutableListOf<SpotifyUserPlaylist>()

            val myUserId = _accountState.value.userId

            for (i in 0 until items.length()) {
                val pl = items.optJSONObject(i) ?: continue
                val id = pl.optString("id")
                val name = pl.optString("name")
                val description = pl.optString("description").takeIf { it.isNotBlank() }
                val images = pl.optJSONArray("images")
                val artwork = images?.optJSONObject(0)?.optString("url")
                val tracksObj = pl.optJSONObject("tracks")
                val total = tracksObj?.optInt("total", 0) ?: 0
                val ownerObj = pl.optJSONObject("owner")
                val ownerName = ownerObj?.optString("display_name")?.ifBlank { "Spotify" }
                val ownerId = ownerObj?.optString("id")
                val isMine = myUserId.isNotBlank() && ownerId == myUserId

                if (id.isNotBlank() && name.isNotBlank()) {
                    list.add(
                        SpotifyUserPlaylist(
                            id = id,
                            name = name,
                            description = description,
                            trackCount = total,
                            artworkUrl = artwork,
                            ownerName = ownerName,
                            isOwner = isMine,
                        )
                    )
                }
            }
            list
        }
    }

    suspend fun fetchPlaylistTracks(playlistId: String): SpotifyPlaylistResult = withContext(Dispatchers.IO) {
        val token = getAccessToken()

        if (token != null) {
            val result = runCatching { fetchPlaylistViaApi(playlistId, token) }.getOrNull()
            if (result != null && result.tracks.isNotEmpty()) {
                return@withContext result
            }
        }

        // Fallback to public embed scraper
        fallbackFetcher.fetchPlaylist(playlistId)
    }

    private fun fetchPlaylistViaApi(playlistId: String, token: String): SpotifyPlaylistResult {
        val request = Request.Builder()
            .url("https://api.spotify.com/v1/playlists/$playlistId?fields=id,name,description,images,owner(display_name),tracks(total,items(track(name,duration_ms,uri,artists(name),album(name,images))))")
            .addHeader("Authorization", "Bearer $token")
            .addHeader("User-Agent", USER_AGENT)
            .build()

        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IllegalStateException("API error: ${response.code}")
            }
            val body = response.body?.string() ?: throw IllegalStateException("Empty body")
            val obj = JSONObject(body)
            val title = obj.optString("name").ifBlank { "Spotify Playlist" }
            val desc = obj.optString("description")
            val images = obj.optJSONArray("images")
            val artwork = images?.optJSONObject(0)?.optString("url")
            val owner = obj.optJSONObject("owner")?.optString("display_name") ?: "Spotify"

            val tracksObj = obj.optJSONObject("tracks")
            val total = tracksObj?.optInt("total", 0) ?: 0
            val items = tracksObj?.optJSONArray("items")

            val parsedTracks = mutableListOf<SpotifyRawTrack>()
            if (items != null) {
                for (i in 0 until items.length()) {
                    val wrapper = items.optJSONObject(i) ?: continue
                    val track = wrapper.optJSONObject("track") ?: continue
                    val trackName = track.optString("name")
                    if (trackName.isBlank()) continue

                    val duration = track.optLong("duration_ms", 0L)
                    val uri = track.optString("uri")

                    val artistsArr = track.optJSONArray("artists")
                    val artistNames = mutableListOf<String>()
                    if (artistsArr != null) {
                        for (a in 0 until artistsArr.length()) {
                            artistsArr.optJSONObject(a)?.optString("name")?.let { artistNames.add(it) }
                        }
                    }
                    val artist = artistNames.joinToString(", ").ifBlank { "Unknown Artist" }

                    val albumObj = track.optJSONObject("album")
                    val albumName = albumObj?.optString("name")
                    val albumArtwork = albumObj?.optJSONArray("images")?.optJSONObject(0)?.optString("url") ?: artwork

                    parsedTracks.add(
                        SpotifyRawTrack(
                            title = trackName,
                            artist = artist,
                            album = albumName,
                            durationMs = duration,
                            uri = uri,
                            artworkUrl = albumArtwork,
                        )
                    )
                }
            }

            return SpotifyPlaylistResult(
                id = playlistId,
                title = title,
                description = desc,
                artworkUrl = artwork,
                author = owner,
                totalTracks = total,
                tracks = parsedTracks,
            )
        }
    }

    fun logout() {
        prefs.edit().clear().apply()
        _accountState.value = SpotifyAccount()
        runCatching {
            val cm = CookieManager.getInstance()
            cm.removeAllCookies(null)
            cm.flush()
        }
    }

    companion object {
        private const val TAG = "SpotifyAuthManager"
        private const val KEY_COOKIES = "spotify_cookies"
        private const val KEY_ACCESS_TOKEN = "spotify_access_token"
        private const val KEY_TOKEN_EXPIRY = "spotify_token_expiry"
        private const val KEY_DISPLAY_NAME = "spotify_display_name"
        private const val KEY_USER_ID = "spotify_user_id"
        private const val KEY_AVATAR_URL = "spotify_avatar_url"
        private const val USER_AGENT =
            "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"
    }
}
