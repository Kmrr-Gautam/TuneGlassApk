package com.listenit.music.ui.artist

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.listenit.music.data.model.ArtistPageData
import com.listenit.music.data.repository.ArtistRepository
import com.listenit.music.playback.MusicPlayer
import com.listenit.music.playback.PlayableTrack
import com.listenit.music.ui.generate.MixLauncher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ensureActive
import com.listenit.music.data.artwork.ArtworkNormalizer
import javax.inject.Inject

import com.listenit.music.data.local.MiscSettings
import com.listenit.music.data.local.SettingsPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

sealed interface ArtistUiState {
    data object Loading : ArtistUiState
    data class Success(val data: ArtistPageData) : ArtistUiState
    data class Error(val message: String) : ArtistUiState
}

@HiltViewModel
class ArtistViewModel @Inject constructor(
    private val repository: ArtistRepository,
    private val musicPlayer: MusicPlayer,
    private val mixLauncher: MixLauncher,
    private val settingsPreferences: SettingsPreferences,
    private val followedArtistsRepository: com.listenit.music.data.repository.FollowedArtistsRepository,
) : ViewModel() {

    val followedArtists = followedArtistsRepository.followedArtists
    private val _followFeedback = MutableStateFlow<String?>(null)
    val followFeedback: StateFlow<String?> = _followFeedback.asStateFlow()

    fun isArtistFollowed(name: String): Boolean = followedArtistsRepository.isFollowed(name)

    fun toggleFollowArtist() {
        val data = (_uiState.value as? ArtistUiState.Success)?.data ?: return
        viewModelScope.launch {
            val (isNowFollowed, genres) = followedArtistsRepository.toggleFollow(data)
            _followFeedback.value = if (isNowFollowed) {
                if (genres.isNotEmpty()) "Added to Library · Genres: ${genres.take(3).joinToString(", ")}"
                else "Added to Library"
            } else {
                "Removed from Library"
            }
        }
    }

    fun clearFollowFeedback() {
        _followFeedback.value = null
    }

    val settings: StateFlow<MiscSettings> = settingsPreferences.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MiscSettings())

    private val _uiState = MutableStateFlow<ArtistUiState>(ArtistUiState.Loading)
    val uiState: StateFlow<ArtistUiState> = _uiState.asStateFlow()

    private var currentArtistName: String = ""
    private var currentBrowseId: String? = null
    private var loadJob: Job? = null

    fun loadArtist(artistName: String, browseId: String? = null) {
        val cached = (_uiState.value as? ArtistUiState.Success)?.data
        if (artistName == currentArtistName && browseId == currentBrowseId &&
            (loadJob?.isActive == true || ArtworkNormalizer.isRealImage(cached?.artworkUrl))) {
            return
        }
        currentArtistName = artistName
        currentBrowseId = browseId

        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.value = ArtistUiState.Loading
            try {
                val data = repository.getArtistDetails(artistName, browseId) { initialData ->
                    coroutineContext.ensureActive()
                    _uiState.value = ArtistUiState.Success(initialData)
                }
                coroutineContext.ensureActive()
                _uiState.value = ArtistUiState.Success(data)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (_uiState.value !is ArtistUiState.Success) {
                    _uiState.value = ArtistUiState.Error(e.message ?: "Failed to load artist details")
                }
            }
        }
    }

    fun playAll(startIndex: Int = 0) {
        val state = _uiState.value as? ArtistUiState.Success ?: return
        val songs = state.data.topSongs
        if (songs.isNotEmpty()) {
            musicPlayer.playQueue(songs, startIndex.coerceIn(0, songs.lastIndex), sourceLabel = state.data.name)
        }
    }

    fun playShuffle() {
        val state = _uiState.value as? ArtistUiState.Success ?: return
        val songs = state.data.topSongs
        if (songs.isNotEmpty()) {
            val shuffled = songs.shuffled()
            musicPlayer.playQueue(shuffled, 0, sourceLabel = state.data.name)
        }
    }

    fun startArtistMix() {
        val state = _uiState.value as? ArtistUiState.Success ?: return
        val artistName = state.data.name
        val topSongs = state.data.topSongs
        val firstTrack = topSongs.firstOrNull()

        viewModelScope.launch {
            val radioTracks = try {
                repository.getArtistRadio(artistName, firstTrack)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                emptyList()
            }

            val finalQueue = if (radioTracks.isNotEmpty()) {
                radioTracks
            } else if (topSongs.isNotEmpty()) {
                topSongs.shuffled()
            } else {
                emptyList()
            }

            if (finalQueue.isNotEmpty()) {
                musicPlayer.playQueue(finalQueue, startIndex = 0, sourceLabel = "$artistName Radio")
            }
        }
    }
}
