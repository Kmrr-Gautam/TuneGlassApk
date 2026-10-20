package com.listenit.music.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

enum class LyricsUiVersion(val id: String, val title: String) {
    CLASSIC("classic", "Classic"),
    MODERN("modern", "Modern");

    companion object {
        fun fromId(id: String?): LyricsUiVersion =
            entries.firstOrNull { it.id == id } ?: MODERN
    }
}

/** Visual design variant for the mini music bar (bottom progress bar / mini player). */
enum class MusicBarStyle(val id: String, val title: String, val description: String) {
    LENS("lens", "Lens", "Clean hairline track with soft ring thumb & live ripple"),
    NEON("neon", "Neon Glow", "Glowing gradient bar with electric pulse animation"),
    VINYL("vinyl", "Vinyl Groove", "Warm wavy groove with analog warmth feel"),
    PILLBAR("pillbar", "Pill Bar", "Bold rounded capsule fill with spring snap");

    companion object {
        fun fromId(id: String?): MusicBarStyle =
            entries.firstOrNull { it.id == id } ?: LENS
    }
}

enum class LyricsAnimation(val id: String, val title: String, val description: String) {
    APPLE_FLUID("apple_fluid", "Apple Fluid", "Smooth spring scaling with dynamic focal tracking"),
    KARAOKE_PULSE("karaoke_pulse", "Karaoke Pulse", "Rhythmic scale pop with energetic spring bounce"),
    KINETIC_SLIDE("kinetic_slide", "Kinetic Slide", "Active line glides smoothly from leading edge"),
    CINEMATIC_BLUR("cinematic_blur", "Cinematic Focus", "Soft background blur & vertical drift on past lines"),
    LOSSLESS_GLOW("lossless_glow", "Lossless Glow", "Vibrant gradient text with frosted glass reflection"),
    CARD_POP("card_pop", "Glass Elevation", "3D floating glass card lift with specular highlights"),
    APPLE_ZOOM("apple_zoom", "Dynamic Focus Zoom", "Expanded focal magnification with fluid spring push"),
    MINIMAL_WAVE("minimal_wave", "Minimal Clean", "Pure low-latency opacity transitions without distortion");

    companion object {
        fun fromId(id: String?): LyricsAnimation =
            entries.firstOrNull { it.id == id } ?: APPLE_FLUID
    }
}

data class MiscSettings(
    /** When on, the app's accent color follows the dominant color of the
     *  currently-scrobbling track's artwork (Home's "now playing" track),
     *  updating live as that track changes. Falls back to the user's
     *  regular selected accent whenever nothing is playing or artwork
     *  colors can't be extracted — see ThemeRepository.updateNowPlayingArtwork. */
    val dynamicNowPlayingEnabled: Boolean = false,
    /** "Use Application Font" — on: the bundled Google Sans Flex variable
     *  font (see ui/theme/Type.kt); off: the device's own system font.
     *  Defaults on so the app ships with its own identity out of the box. */
    val useCustomFont: Boolean = true,
    /** Last.fm usernames pinned to the top of Home's friend-switcher sheet
     *  (long-press a friend row to toggle). Order among pinned friends
     *  follows whatever order user.getfriends itself returns them in —
     *  just filtered to the front, not independently reorderable. */
    val pinnedFriends: Set<String> = emptySet(),
    /** When true, the player attempts to resolve and stream lossless / Hi-Res audio
     *  directly from lossless CDN when a high-confidence match exists. Falls back to YouTube Music. */
    val preferLosslessStreaming: Boolean = true,
    /** Preferred quality preset for lossless streaming (27: 24/192, 7: 24/96, 6: 16/44.1, 5: 320k).
     *  If a track does not support the requested quality, the worker automatically selects the highest available. */
    val losslessQuality: Int = 27,
    /** Preferred quality preset for downloads (27: 24/192, 7: 24/96, 6: 16/44.1, 5: 320k, -1: YouTube Music). */
    val downloadQuality: Int = 27,
    /** Optional studio-clarity curve. Disabled by default because fixed tone
     *  shaping cannot be neutral on every speaker, headset and OEM spatializer. */
    val isStudioMasterClarityEnabled: Boolean = false,
    /** When true, completely bypasses DSP, EQ, tone effects, and software volume ducking for bit-exact audio. */
    val isBitPerfectEnabled: Boolean = false,
    /** Lyrics UI layout version (Classic or Modern). */
    val lyricsUiVersion: LyricsUiVersion = LyricsUiVersion.MODERN,
    val wordByWordLyrics: Boolean = true,
    /** Experimental lyrics animation style (Settings -> Experimental -> Lyrics Animation). */
    val lyricsAnimation: LyricsAnimation = LyricsAnimation.APPLE_FLUID,
    /** Blend the end of one queued track into the beginning of the next. */
    val crossfadeEnabled: Boolean = false,
    /** Crossfade length in seconds; kept within the native settings slider range. */
    val crossfadeSeconds: Int = 5,
    /** When true (default), uses the multi-layer dynamic wavy seekbar.
     *  When false, uses the classic standard progress slider in the player tab. */
    val wavySeekbarEnabled: Boolean = true,
    /** When true (default), downloads fetch and save synced lyrics (.lrc companion files and embedded tags). */
    val downloadLyrics: Boolean = true,
    /** User's personalized profile display name */
    val userProfileName: String = "",
    /** User's personalized profile avatar (gallery URI, image URL, or mini doodle id) */
    val userProfileAvatar: String = "",
    /** User's chosen music genres (e.g. Punjabi, DHH, Hindi, Haryanvi, English) */
    val userGenres: Set<String> = emptySet(),
    /** Whether the user has completed the initial onboarding setup */
    val onboardingCompleted: Boolean = false,
    /** Design variant for the mini music / progress bar shown on the player screen. */
    val musicBarStyle: MusicBarStyle = MusicBarStyle.LENS,
    /** When true, automatically saves tracks for offline listening upon starting playback. */
    val autoOfflineSaveEnabled: Boolean = false,
    /** When true (default), displays album artwork as a full-bleed full screen poster in the music player.
     *  When false, displays the album artwork normally as a square poster. */
    val fullScreenPoster: Boolean = true,
    /** When true, the full-screen player poster tilts with device movement. */
    val tiltPosterAnimationEnabled: Boolean = true,
    /** When true (default), shows a floating interactive dynamic island at the top on all Android devices. */
    val dynamicIslandEnabled: Boolean = true,
    /** When true (default), enables rich tactile haptic feedback on playback controls, sliders, and gestures. */
    val hapticFeedbackEnabled: Boolean = true,
)

/** Small dedicated prefs object for settings that don't fit ThemePreferences
 *  or SessionPreferences semantically — shares the app's single DataStore. */
@Singleton
class SettingsPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    private object Keys {
        val DYNAMIC_NOW_PLAYING = booleanPreferencesKey("lw_dynamic_now_playing")
        val USE_CUSTOM_FONT = booleanPreferencesKey("lw_use_custom_font")
        val PINNED_FRIENDS = stringSetPreferencesKey("lw_pinned_friends")
        val PREFER_LOSSLESS_STREAMING = booleanPreferencesKey("lw_prefer_lossless_streaming")
        val LOSSLESS_QUALITY = intPreferencesKey("lw_lossless_quality")
        val DOWNLOAD_QUALITY = intPreferencesKey("lw_download_quality")
        val MUSIC_ENHANCER = booleanPreferencesKey("lw_music_enhancer")
        val BIT_PERFECT_ENABLED = booleanPreferencesKey("lw_bit_perfect_enabled")
        val LYRICS_UI_VERSION = stringPreferencesKey("lw_lyrics_ui_version")
        val WORD_BY_WORD_LYRICS = booleanPreferencesKey("lw_word_by_word_lyrics")
        val LYRICS_ANIMATION = stringPreferencesKey("lw_lyrics_animation")
        val CROSSFADE_ENABLED = booleanPreferencesKey("lw_crossfade_enabled")
        val CROSSFADE_SECONDS = intPreferencesKey("lw_crossfade_seconds")
        val WAVY_SEEKBAR_ENABLED = booleanPreferencesKey("lw_wavy_seekbar_enabled")
        val DOWNLOAD_LYRICS = booleanPreferencesKey("lw_download_lyrics")
        val USER_PROFILE_NAME = stringPreferencesKey("listenit_user_profile_name")
        val USER_PROFILE_AVATAR = stringPreferencesKey("listenit_user_profile_avatar")
        val USER_GENRES = stringSetPreferencesKey("listenit_user_genres")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("listenit_onboarding_completed")
        val MUSIC_BAR_STYLE = stringPreferencesKey("lw_music_bar_style")
        val AUTO_OFFLINE_SAVE_ENABLED = booleanPreferencesKey("lw_auto_offline_save_enabled")
        val FULL_SCREEN_POSTER = booleanPreferencesKey("lw_full_screen_poster")
        val TILT_POSTER_ANIMATION_ENABLED = booleanPreferencesKey("lw_tilt_poster_animation_enabled")
        val DYNAMIC_ISLAND = booleanPreferencesKey("tuneglass_dynamic_island")
        val HAPTIC_FEEDBACK_ENABLED = booleanPreferencesKey("lw_haptic_feedback_enabled")
    }

    val settings: Flow<MiscSettings> = dataStore.data
        .recoverPreferences("SettingsPreferences")
        .map { p ->
            MiscSettings(
                dynamicNowPlayingEnabled = p.readSafely(Keys.DYNAMIC_NOW_PLAYING) ?: false,
                useCustomFont = p.readSafely(Keys.USE_CUSTOM_FONT) ?: true,
                pinnedFriends = p.readSafely(Keys.PINNED_FRIENDS) ?: emptySet(),
                preferLosslessStreaming = p.readSafely(Keys.PREFER_LOSSLESS_STREAMING) ?: true,
                losslessQuality = p.readSafely(Keys.LOSSLESS_QUALITY)?.takeIf { it in LOSSLESS_QUALITIES } ?: 27,
                downloadQuality = p.readSafely(Keys.DOWNLOAD_QUALITY)?.takeIf { it in DOWNLOAD_QUALITIES } ?: 27,
                isStudioMasterClarityEnabled = p.readSafely(Keys.MUSIC_ENHANCER) ?: false,
                isBitPerfectEnabled = p.readSafely(Keys.BIT_PERFECT_ENABLED) ?: false,
                lyricsUiVersion = LyricsUiVersion.fromId(p.readSafely(Keys.LYRICS_UI_VERSION)),
                wordByWordLyrics = p.readSafely(Keys.WORD_BY_WORD_LYRICS) ?: true,
                lyricsAnimation = LyricsAnimation.fromId(p.readSafely(Keys.LYRICS_ANIMATION)),
                crossfadeEnabled = p.readSafely(Keys.CROSSFADE_ENABLED) ?: false,
                crossfadeSeconds = (p.readSafely(Keys.CROSSFADE_SECONDS) ?: 5).coerceIn(1, 12),
                wavySeekbarEnabled = p.readSafely(Keys.WAVY_SEEKBAR_ENABLED) ?: true,
                downloadLyrics = p.readSafely(Keys.DOWNLOAD_LYRICS) ?: true,
                userProfileName = p.readSafely(Keys.USER_PROFILE_NAME).orEmpty(),
                userProfileAvatar = p.readSafely(Keys.USER_PROFILE_AVATAR).orEmpty(),
                userGenres = p.readSafely(Keys.USER_GENRES) ?: emptySet(),
                onboardingCompleted = p.readSafely(Keys.ONBOARDING_COMPLETED) ?: false,
                musicBarStyle = MusicBarStyle.fromId(p.readSafely(Keys.MUSIC_BAR_STYLE)),
                autoOfflineSaveEnabled = p.readSafely(Keys.AUTO_OFFLINE_SAVE_ENABLED) ?: false,
                fullScreenPoster = p.readSafely(Keys.FULL_SCREEN_POSTER) ?: true,
                tiltPosterAnimationEnabled = p.readSafely(Keys.TILT_POSTER_ANIMATION_ENABLED) ?: true,
                dynamicIslandEnabled = p.readSafely(Keys.DYNAMIC_ISLAND) ?: true,
                hapticFeedbackEnabled = p.readSafely(Keys.HAPTIC_FEEDBACK_ENABLED) ?: true,
            )
        }

    suspend fun setUserProfileAvatar(avatar: String) {
        dataStore.edit { it[Keys.USER_PROFILE_AVATAR] = avatar }
    }

    suspend fun setHapticFeedbackEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.HAPTIC_FEEDBACK_ENABLED] = enabled }
    }

    suspend fun setDynamicIsland(enabled: Boolean) {
        dataStore.edit { it[Keys.DYNAMIC_ISLAND] = enabled }
    }

    suspend fun setFullScreenPoster(enabled: Boolean) {
        dataStore.edit { it[Keys.FULL_SCREEN_POSTER] = enabled }
    }

    suspend fun setTiltPosterAnimationEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.TILT_POSTER_ANIMATION_ENABLED] = enabled }
    }

    suspend fun setDynamicNowPlaying(enabled: Boolean) {
        dataStore.edit { it[Keys.DYNAMIC_NOW_PLAYING] = enabled }
    }

    suspend fun setUseCustomFont(enabled: Boolean) {
        dataStore.edit { it[Keys.USE_CUSTOM_FONT] = enabled }
    }

    suspend fun setPreferLosslessStreaming(enabled: Boolean) {
        dataStore.edit {
            it[Keys.PREFER_LOSSLESS_STREAMING] = enabled
        }
    }

    suspend fun setLosslessQuality(quality: Int) {
        dataStore.edit {
            val q = quality.takeIf { it in LOSSLESS_QUALITIES } ?: 27
            it[Keys.LOSSLESS_QUALITY] = q
        }
    }

    suspend fun setDownloadQuality(quality: Int) {
        dataStore.edit {
            val q = quality.takeIf { it in DOWNLOAD_QUALITIES } ?: 27
            it[Keys.DOWNLOAD_QUALITY] = q
        }
    }

    suspend fun setStudioMasterClarity(enabled: Boolean) {
        dataStore.edit { it[Keys.MUSIC_ENHANCER] = enabled }
    }

    suspend fun setLyricsUiVersion(version: LyricsUiVersion) {
        dataStore.edit { it[Keys.LYRICS_UI_VERSION] = version.id }
    }

    suspend fun setBitPerfectEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.BIT_PERFECT_ENABLED] = enabled }
    }

    suspend fun setWordByWordLyrics(enabled: Boolean) {
        dataStore.edit { it[Keys.WORD_BY_WORD_LYRICS] = enabled }
    }

    suspend fun setLyricsAnimation(animation: LyricsAnimation) {
        dataStore.edit { it[Keys.LYRICS_ANIMATION] = animation.id }
    }

    suspend fun setCrossfadeEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.CROSSFADE_ENABLED] = enabled }
    }

    suspend fun setCrossfadeSeconds(seconds: Int) {
        dataStore.edit { it[Keys.CROSSFADE_SECONDS] = seconds.coerceIn(1, 12) }
    }

    suspend fun setWavySeekbarEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.WAVY_SEEKBAR_ENABLED] = enabled }
    }

    suspend fun setDownloadLyrics(enabled: Boolean) {
        dataStore.edit { it[Keys.DOWNLOAD_LYRICS] = enabled }
    }

    suspend fun setAutoOfflineSaveEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.AUTO_OFFLINE_SAVE_ENABLED] = enabled }
    }

    suspend fun setMusicBarStyle(style: MusicBarStyle) {
        dataStore.edit { it[Keys.MUSIC_BAR_STYLE] = style.id }
    }

    suspend fun setUserProfileName(name: String) {
        dataStore.edit { it[Keys.USER_PROFILE_NAME] = name.trim() }
    }

    suspend fun setUserGenres(genres: Set<String>) {
        dataStore.edit { it[Keys.USER_GENRES] = genres }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { it[Keys.ONBOARDING_COMPLETED] = completed }
    }

    suspend fun saveUserProfileAndGenres(name: String, genres: Set<String>, avatar: String = "") {
        dataStore.edit {
            it[Keys.USER_PROFILE_NAME] = name.trim()
            it[Keys.USER_GENRES] = genres
            if (avatar.isNotBlank()) {
                it[Keys.USER_PROFILE_AVATAR] = avatar.trim()
            }
            it[Keys.ONBOARDING_COMPLETED] = true
        }
    }

    suspend fun toggleFriendPinned(username: String) {
        dataStore.edit { prefs ->
            val current = prefs.readSafely(Keys.PINNED_FRIENDS) ?: emptySet()
            prefs[Keys.PINNED_FRIENDS] = if (username in current) current - username else current + username
        }
    }

    private companion object {
        val LOSSLESS_QUALITIES = setOf(-1, 5, 6, 7, 27)
        val DOWNLOAD_QUALITIES = setOf(-1, 5, 6, 7, 27)
    }
}
