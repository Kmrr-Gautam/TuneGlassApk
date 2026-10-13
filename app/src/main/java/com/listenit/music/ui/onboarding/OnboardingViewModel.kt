package com.listenit.music.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.listenit.music.data.local.SessionPreferences
import com.listenit.music.data.local.SettingsPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GenreOption(
    val id: String,
    val name: String,
    val subtitle: String,
    val emoji: String,
)

data class OnboardingUiState(
    val name: String = "",
    val selectedGenres: Set<String> = setOf("Punjabi", "DHH", "Hindi", "Haryanvi", "English"),
    val isSaving: Boolean = false,
    val availableGenres: List<GenreOption> = listOf(
        GenreOption("Punjabi", "Punjabi", "Sidhu, Karan Aujla, Diljit, AP", "🔥"),
        GenreOption("DHH", "DHH", "Desi Hip Hop: KR\$NA, SM, DIVINE, Stan", "🎤"),
        GenreOption("Hindi", "Hindi", "Bollywood Hits, Arijit, Shreya", "🎵"),
        GenreOption("Haryanvi", "Haryanvi", "Gulzaar, Renuka, Masoom", "⚡"),
        GenreOption("English", "English", "Global Pop, The Weeknd, Drake, Taylor", "🌍"),
        GenreOption("Bollywood", "Bollywood", "Romantic & Classic Cinema", "✨"),
        GenreOption("Lo-Fi", "Lo-Fi", "Chill Beats & Aesthetic Vibes", "🌙"),
        GenreOption("Indie", "Indie", "Anuv Jain, Prateek Kuhad, Acoustic", "🎸"),
        GenreOption("Bhojpuri", "Bhojpuri", "Bhojpuri Beats & Folk Hits", "🎉"),
        GenreOption("South", "South Hits", "Telugu, Tamil & Malayalam Chartbusters", "🎬"),
        GenreOption("Rock", "Rock & Metal", "Queen, Linkin Park, Nirvana, AC/DC", "🎸"),
        GenreOption("EDM", "EDM / Dance", "Martin Garrix, Avicii, Alan Walker", "🎧"),
        GenreOption("R&B", "R&B / Soul", "SZA, Frank Ocean, The Weeknd, Bruno", "💜"),
        GenreOption("K-Pop", "K-Pop", "BTS, BLACKPINK, NewJeans, TWICE", "⭐"),
    ),
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val settingsPreferences: SettingsPreferences,
    private val sessionPreferences: SessionPreferences,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val misc = settingsPreferences.settings.first()
            val session = sessionPreferences.session.first()
            val initialName = misc.userProfileName.ifBlank { session.username }
            val initialGenres = if (misc.userGenres.isNotEmpty()) misc.userGenres else _uiState.value.selectedGenres
            _uiState.update {
                it.copy(
                    name = initialName,
                    selectedGenres = initialGenres,
                )
            }
        }
    }

    fun onNameChanged(newName: String) {
        _uiState.update { it.copy(name = newName) }
    }

    fun toggleGenre(genreId: String) {
        _uiState.update { state ->
            val current = state.selectedGenres
            val updated = if (genreId in current) {
                if (current.size > 1) current - genreId else current
            } else {
                current + genreId
            }
            state.copy(selectedGenres = updated)
        }
    }

    fun completeOnboarding(onSuccess: () -> Unit) {
        val state = _uiState.value
        val trimmedName = state.name.trim().ifBlank { "Listener" }
        val genres = state.selectedGenres.ifEmpty { setOf("Punjabi", "DHH", "Hindi", "English") }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            settingsPreferences.saveUserProfileAndGenres(trimmedName, genres)
            val session = sessionPreferences.session.first()
            if (session.username.isBlank()) {
                sessionPreferences.setSignedIn(trimmedName)
            }
            _uiState.update { it.copy(isSaving = false) }
            onSuccess()
        }
    }
}
