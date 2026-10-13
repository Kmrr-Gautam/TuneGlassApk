package com.listenit.music.data.repository

import android.content.Context
import androidx.compose.runtime.Immutable
import com.listenit.music.data.genre.GenreResolver
import com.listenit.music.data.model.ArtistPageData
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
@Immutable
data class FollowedArtist(
    val name: String,
    val browseId: String = "",
    val artworkUrl: String? = null,
    val genres: List<String> = emptyList(),
    val followedAt: Long = System.currentTimeMillis(),
)

@Singleton
class FollowedArtistsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val genreResolver: GenreResolver,
) {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }
    private val prefs = context.getSharedPreferences("followed_artists_prefs", Context.MODE_PRIVATE)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _followedArtists = MutableStateFlow<List<FollowedArtist>>(emptyList())
    val followedArtists: StateFlow<List<FollowedArtist>> = _followedArtists.asStateFlow()

    private val _followedGenres = MutableStateFlow<List<String>>(emptyList())
    val followedGenres: StateFlow<List<String>> = _followedGenres.asStateFlow()

    init {
        loadPersisted()
    }

    private fun loadPersisted() {
        val raw = prefs.getString("followed_artists_json", null)
        if (!raw.isNullOrBlank()) {
            val list = runCatching { json.decodeFromString<List<FollowedArtist>>(raw) }.getOrDefault(emptyList())
            _followedArtists.value = list
            recalculateGenres(list)
        }
    }

    private fun persist(list: List<FollowedArtist>) {
        _followedArtists.value = list
        recalculateGenres(list)
        scope.launch {
            val raw = json.encodeToString(list)
            prefs.edit().putString("followed_artists_json", raw).apply()
        }
    }

    private fun recalculateGenres(list: List<FollowedArtist>) {
        val genres = list.flatMap { it.genres }
            .map { it.trim().lowercase().replaceFirstChar { c -> c.uppercase() } }
            .filter { it.isNotBlank() && it.length > 1 }
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .map { it.key }
        _followedGenres.value = genres
    }

    fun isFollowed(name: String): Boolean {
        val clean = name.trim().lowercase()
        return _followedArtists.value.any { it.name.trim().lowercase() == clean }
    }

    fun unfollow(name: String) {
        val clean = name.trim()
        val updated = _followedArtists.value.filterNot { it.name.trim().equals(clean, ignoreCase = true) }
        persist(updated)
    }

    suspend fun toggleFollow(artist: ArtistPageData): Pair<Boolean, List<String>> = withContext(Dispatchers.IO) {
        val cleanName = artist.name.trim()
        val existing = _followedArtists.value.firstOrNull { it.name.trim().equals(cleanName, ignoreCase = true) }
        if (existing != null) {
            val updated = _followedArtists.value.filterNot { it.name.trim().equals(cleanName, ignoreCase = true) }
            persist(updated)
            return@withContext false to emptyList()
        }

        // Auto-fetch and resolve genres from artist tags and top tracks
        var resolvedGenres = artist.tags.filter { it.isNotBlank() }
        if (resolvedGenres.isEmpty()) {
            for (track in artist.topSongs.take(4)) {
                val g = runCatching { genreResolver.resolve(track.title, cleanName) }.getOrDefault("")
                if (g.isNotBlank()) {
                    resolvedGenres = resolvedGenres + g.split(",").map { it.trim() }
                }
            }
        }
        val cleanGenres = resolvedGenres.map { it.trim().lowercase().replaceFirstChar { c -> c.uppercase() } }
            .filter { it.isNotBlank() && it.length > 1 }
            .distinct()

        val newArtist = FollowedArtist(
            name = cleanName,
            browseId = artist.browseId,
            artworkUrl = artist.artworkUrl ?: artist.bannerUrl,
            genres = cleanGenres,
        )
        val updated = listOf(newArtist) + _followedArtists.value.filterNot { it.name.trim().equals(cleanName, ignoreCase = true) }
        persist(updated)
        return@withContext true to cleanGenres
    }
}
