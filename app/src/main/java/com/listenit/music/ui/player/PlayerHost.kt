@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.listenit.music.ui.player

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.listenit.music.ui.common.PredictiveBackScreen
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material3.LocalContentColor
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import com.listenit.music.ui.common.LensGlyphButton
import com.listenit.music.ui.common.LensRingButton
import com.listenit.music.ui.common.LocalAppHaptics
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Downloading
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.ui.graphics.Shape
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import kotlin.math.roundToInt
import kotlin.math.PI
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import com.listenit.music.ui.common.adaptiveContentWidth
import com.listenit.music.ui.common.isTabletOrWideScreen
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import com.listenit.music.data.local.MusicBarStyle
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.palette.graphics.Palette
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import androidx.media3.common.Player
import com.listenit.music.data.generate.GeneratedTrack
import com.listenit.music.data.lyrics.LyricsRepository
import com.listenit.music.data.lyrics.LyricsResult
import com.listenit.music.data.playlist.PlaylistRepository
import com.listenit.music.data.playlist.SavedPlaylist
import com.listenit.music.data.playlist.LIKED_SONGS_MODE
import com.listenit.music.data.local.LyricsAnimation
import com.listenit.music.data.local.LyricsUiVersion
import com.listenit.music.playback.MusicPlayer
import com.listenit.music.playback.MusicPlayerState
import com.listenit.music.playback.PlaybackChromeState
import com.listenit.music.playback.PlaybackProgressState
import com.listenit.music.playback.PlayableTrack
import com.listenit.music.ui.common.ArtworkImage
import com.listenit.music.ui.common.ExpressiveInlineLoadingIndicator
import com.listenit.music.ui.common.ExpressiveMotion
import com.listenit.music.ui.common.PlaylistCover
import com.listenit.music.ui.common.TrackContextMenuSheet
import com.listenit.music.ui.common.TrackMenuCapabilities
import com.listenit.music.ui.common.TrackMenuTarget
import com.listenit.music.ui.theme.LocalLiquidGlass
import com.listenit.music.ui.theme.LiquidGlassSurface
import com.listenit.music.ui.theme.liquidGlassChrome
import com.listenit.music.ui.theme.liquidGlassContainerColor
import com.listenit.music.ui.theme.liquidGlassSource
import com.listenit.music.ui.theme.isLiquidGlassBackdropSupported
import com.listenit.music.ui.theme.LocalLiquidGlassBackdrop
import com.listenit.music.ui.theme.LocalLiquidGlassOverlayBackdrop
import com.listenit.music.ui.theme.LiquidGlassPreset
import com.listenit.music.ui.theme.BackdropBlur
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.math.abs

enum class FullPlayerTab {
    NOW_PLAYING,
    LYRICS,
    QUEUE
}

val LocalMusicPlayer = staticCompositionLocalOf<MusicPlayer> {
    error("MusicPlayer is only available inside PlayerHost")
}

val LocalAddToPlaylist = staticCompositionLocalOf<(PlayableTrack) -> Unit> {
    error("Add-to-playlist is only available inside PlayerHost")
}

/**
 * Extra list-end clearance needed while the collapsed player overlays a
 * screen. The Material 3 bar is about 86dp tall; 88dp clears it without
 * leaving a large empty band at the end of short lists.
 */
val LocalMiniPlayerScrollClearance = staticCompositionLocalOf { 0.dp }

@HiltViewModel
class PlayerViewModel @Inject constructor(
    val player: MusicPlayer,
    private val playlistRepository: PlaylistRepository,
    private val lyricsRepository: LyricsRepository,
    private val settingsPreferences: com.listenit.music.data.local.SettingsPreferences,
    val navigator: com.listenit.music.ui.navigation.ArtistAlbumNavigator,
    val genreExplorer: com.listenit.music.ui.genres.GenreExplorer,
    val mixLauncher: com.listenit.music.ui.generate.MixLauncher,
    private val ytMusicLibraryManager: com.listenit.music.data.ytmusic.YtMusicLibraryManager,
    private val likedSongsManager: com.listenit.music.data.playlist.LikedSongsManager,
    private val scrobbleRepository: com.listenit.music.data.repository.ScrobbleRepository,
    val downloadManager: com.listenit.music.data.download.TrackDownloadManager,
    private val downloadedTrackDao: com.listenit.music.data.local.db.DownloadedTrackDao,
) : ViewModel() {
    val navEvents = navigator.events
    val state = player.state
    val chromeState = player.chromeState
    val progressState = player.progressState
    val activeDownloads = downloadManager.downloads
    val downloadedTracks = downloadedTrackDao.getAll().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList(),
    )

    fun downloadTrack(track: PlayableTrack) {
        downloadManager.downloadTrack(
            title = track.title,
            artist = track.artist,
            album = track.album,
            artworkUrl = track.artworkUrl,
        )
    }
    val fullPlayerState = player.state
        .map { it.copy(positionMs = 0L, bufferedPositionMs = 0L) }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.Eagerly, player.state.value.copy(positionMs = 0L, bufferedPositionMs = 0L))
    val settings: StateFlow<com.listenit.music.data.local.MiscSettings> = settingsPreferences.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), com.listenit.music.data.local.MiscSettings())

    fun toggleCrossfade() {
        viewModelScope.launch {
            val current = settings.value.crossfadeEnabled
            settingsPreferences.setCrossfadeEnabled(!current)
        }
    }
    private val _customPlaylists = MutableStateFlow<List<SavedPlaylist>>(emptyList())
    val customPlaylists = _customPlaylists.asStateFlow()
    private var customPlaylistsLoaded = false
    val likedTrackKeys = likedSongsManager.likedTrackKeys

    fun openArtist(name: String, browseId: String? = null) {
        navigator.openArtist(name, browseId)
    }

    fun openAlbum(title: String, artist: String = "", browseId: String? = null) {
        navigator.openAlbum(title, artist, browseId)
    }

    private val _lyricsState = MutableStateFlow<LyricsUiState>(LyricsUiState.Idle)
    val lyricsState = _lyricsState.asStateFlow()

    private var currentTrackLyricsKey: String? = null
    private var lyricsJob: Job? = null

    init {
        viewModelScope.launch {
            playlistRepository.changes.collect {
                if (customPlaylistsLoaded) refreshCustomPlaylists()
            }
        }
        viewModelScope.launch {
            ytMusicLibraryManager.playlists.collect {
                if (customPlaylistsLoaded) refreshCustomPlaylists()
            }
        }
        viewModelScope.launch {
            combine(
                player.chromeState.map { it.current }.distinctUntilChanged(),
                settingsPreferences.settings.map { it.wordByWordLyrics }.distinctUntilChanged(),
            ) { track, wordByWord -> track to wordByWord }.collect { (track, wordByWord) ->
                val key = track?.let { "${it.artist}|${it.title}|$wordByWord" }
                if (key != currentTrackLyricsKey) {
                    currentTrackLyricsKey = key
                    if (track != null) {
                        loadLyrics(track, forceRefresh = false)
                    } else {
                        lyricsJob?.cancel()
                        _lyricsState.value = LyricsUiState.Idle
                    }
                }
            }
        }
    }

    private suspend fun refreshCustomPlaylists() {
        try {
            _customPlaylists.value = (playlistRepository.getAll()
                .filter { it.mode == "custom" || it.mode == LIKED_SONGS_MODE }
                .sortedByDescending { it.mode == LIKED_SONGS_MODE } + ytMusicLibraryManager.playlists.value)
                .distinctBy { it.id }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            customPlaylistsLoaded = false
            android.util.Log.e("PlayerViewModel", "Couldn't refresh playlists", error)
        }
    }

    fun prepareCustomPlaylists() {
        if (customPlaylistsLoaded) return
        customPlaylistsLoaded = true
        viewModelScope.launch { refreshCustomPlaylists() }
    }

    fun loadLyrics(track: PlayableTrack, forceRefresh: Boolean = false) {
        lyricsJob?.cancel()
        lyricsJob = viewModelScope.launch {
            _lyricsState.value = LyricsUiState.Loading
            val durationSeconds = if (player.state.value.durationMs > 0) {
                (player.state.value.durationMs / 1000).toInt()
            } else null

            val result = try {
                lyricsRepository.getLyrics(
                    track.title, track.artist, track.album, durationSeconds, forceRefresh,
                    wordByWord = settingsPreferences.settings.first().wordByWordLyrics,
                    onPartialResult = { partial ->
                        withContext(Dispatchers.Main.immediate) {
                            coroutineContext.ensureActive()
                            publishLyrics(partial)
                        }
                    },
                )
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                LyricsResult.Error(error.message ?: "Couldn't load lyrics")
            }
            coroutineContext.ensureActive()
            if (result is LyricsResult.Success || _lyricsState.value !is LyricsUiState.Success) {
                publishLyrics(result)
            }
        }
    }

    private fun publishLyrics(result: LyricsResult) {
        when (result) {
            is LyricsResult.Success -> {
                _lyricsState.value = LyricsUiState.Success(
                    lines = result.lines,
                    isSynced = result.isSynced,
                    isWordSynced = result.isWordSynced,
                    plainLyrics = result.plainLyrics,
                    isInstrumental = result.isInstrumental,
                    source = result.source,
                )
            }
            is LyricsResult.Empty -> {
                _lyricsState.value = LyricsUiState.Empty
            }
            is LyricsResult.Error -> {
                _lyricsState.value = LyricsUiState.Error(result.message)
            }
        }
    }

    fun retryLyrics() {
        val track = player.state.value.current ?: return
        loadLyrics(track, forceRefresh = true)
    }

    fun addToPlaylists(
        playlistIds: Set<Long>,
        duplicatePlaylistIds: Set<Long>,
        track: PlayableTrack,
    ) {
        if (playlistIds.isEmpty()) return
        viewModelScope.launch {
            runCatching {
                val generatedTrack = track.toGeneratedTrack()
                var remoteChanged = false
                playlistIds.forEach { playlistId ->
                    val allowDuplicate = playlistId in duplicatePlaylistIds
                    if (playlistId < 0L) {
                        val added = ytMusicLibraryManager.addTrack(playlistId, generatedTrack, allowDuplicate)
                        remoteChanged = remoteChanged || added
                    } else {
                        playlistRepository.addTrack(
                            id = playlistId,
                            track = generatedTrack,
                            allowDuplicate = allowDuplicate,
                        )
                    }
                }
                if (remoteChanged) {
                    ytMusicLibraryManager.refresh()
                    refreshCustomPlaylists()
                }
            }
        }
    }

    suspend fun findDuplicatePlaylistIds(playlistIds: Set<Long>, track: PlayableTrack): Set<Long> {
        val generatedTrack = track.toGeneratedTrack()
        val localDuplicates = _customPlaylists.value
            .filter { playlist ->
                playlist.id in playlistIds &&
                    playlist.remotePlaylistId == null &&
                    playlist.tracks.any {
                        it.key == generatedTrack.key ||
                            (it.name.equals(generatedTrack.name, ignoreCase = true) && it.artist.equals(generatedTrack.artist, ignoreCase = true))
                    }
            }
            .mapTo(mutableSetOf(), SavedPlaylist::id)
        val remoteIds = playlistIds.filterTo(mutableSetOf()) { it < 0L }
        return localDuplicates + ytMusicLibraryManager.findDuplicatePlaylistIds(remoteIds, generatedTrack)
    }

    suspend fun findCachedDuplicatePlaylistIds(playlistIds: Set<Long>, track: PlayableTrack): Set<Long> {
        val generatedTrack = track.toGeneratedTrack()
        val remoteIds = playlistIds.filterTo(mutableSetOf()) { it < 0L }
        return ytMusicLibraryManager.findCachedDuplicatePlaylistIds(remoteIds, generatedTrack)
    }

    fun createPlaylistAndAdd(title: String, track: PlayableTrack) {
        if (title.isBlank()) return
        viewModelScope.launch {
            runCatching {
                val playlist = playlistRepository.createCustom(title)
                playlistRepository.addTrack(playlist.id, track.toGeneratedTrack())
            }
        }
    }

    fun toggleLiked(track: PlayableTrack) {
        viewModelScope.launch {
            val loved = likedSongsManager.toggle(track.toGeneratedTrack())
            mirrorLastFmLove(track, loved)
        }
    }

    fun like(track: PlayableTrack) {
        viewModelScope.launch {
            likedSongsManager.like(track.toGeneratedTrack())
            mirrorLastFmLove(track, loved = true)
        }
    }

    private suspend fun mirrorLastFmLove(track: PlayableTrack, loved: Boolean) {
        when (val result = scrobbleRepository.setTrackLoved(track.artist, track.title, loved)) {
            com.listenit.music.data.repository.ScrobbleRepository.Result.Success,
            com.listenit.music.data.repository.ScrobbleRepository.Result.NoSessionKey -> Unit
            is com.listenit.music.data.repository.ScrobbleRepository.Result.Failed ->
                android.util.Log.w("PlayerViewModel", "Last.fm love sync failed: ${result.message}")
        }
    }
}

/** App-wide collapsed + maximized player layered over every navigation route. */
@Composable
fun PlayerHost(
    viewModel: PlayerViewModel = hiltViewModel(),
    hasBottomNavigation: Boolean = false,
    isPlayerAllowed: Boolean = true,
    content: @Composable () -> Unit,
) {
    val state by viewModel.chromeState.collectAsStateWithLifecycle()
    var expanded by rememberSaveable { mutableStateOf(false) }
    var currentTab by rememberSaveable { mutableStateOf(FullPlayerTab.NOW_PLAYING) }
    var playlistTrack by remember { mutableStateOf<PlayableTrack?>(null) }
    val requestAddToPlaylist = remember(viewModel) {
        { track: PlayableTrack ->
            viewModel.prepareCustomPlaylists()
            playlistTrack = track
        }
    }
    val trackKey = state.current?.let { it.videoId ?: "${it.artist}|${it.title}" }
    LaunchedEffect(trackKey) {
        if (trackKey == null) {
            expanded = false
            currentTab = FullPlayerTab.NOW_PLAYING
        }
    }
    LaunchedEffect(expanded) {
        if (!expanded) {
            currentTab = FullPlayerTab.NOW_PLAYING
        }
    }
    LaunchedEffect(Unit) {
        viewModel.navEvents.collect {
            expanded = false
            currentTab = FullPlayerTab.NOW_PLAYING
        }
    }
    LaunchedEffect(Unit) {
        viewModel.mixLauncher.requests.collect {
            expanded = false
            currentTab = FullPlayerTab.NOW_PLAYING
        }
    }
    LaunchedEffect(Unit) {
        viewModel.genreExplorer.pendingGenre.collect { genre ->
            if (genre != null) {
                expanded = false
                currentTab = FullPlayerTab.NOW_PLAYING
            }
        }
    }
    val miniPlayerVisible = isPlayerAllowed && state.current != null && !expanded
    val playerSettings by viewModel.settings.collectAsStateWithLifecycle()

    CompositionLocalProvider(
        LocalMusicPlayer provides viewModel.player,
        LocalAddToPlaylist provides requestAddToPlaylist,
        LocalMiniPlayerScrollClearance provides if (isPlayerAllowed && state.current != null) 88.dp else 0.dp,
    ) {
        Box(Modifier.fillMaxSize()) {
            content()
            if (miniPlayerVisible) {
                MiniPlayer(
                    state = state,
                    progressState = viewModel.progressState,
                    onExpand = { expanded = true },
                    onToggle = viewModel.player::togglePlayPause,
                    onPrevious = viewModel.player::previous,
                    onNext = viewModel.player::next,
                    onClose = viewModel.player::stopAndClear,
                    bottomPadding = if (hasBottomNavigation) 92.dp else 12.dp,
                    edgeToEdge = !hasBottomNavigation,
                    backdrop = null,
                    musicBarStyle = playerSettings.musicBarStyle,
                    fullScreenPoster = playerSettings.fullScreenPoster,
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }
            AnimatedVisibility(
                visible = isPlayerAllowed && expanded && state.current != null,
                enter = slideInVertically(
                    animationSpec = ExpressiveMotion.smoothSpring(),
                    initialOffsetY = { it },
                ) + fadeIn(tween(180)),
                exit = slideOutVertically(
                    animationSpec = ExpressiveMotion.smoothSpring(),
                    targetOffsetY = { it },
                ) + fadeOut(tween(150)),
            ) {
                PredictiveBackScreen(
                    enabled = expanded && state.current != null,
                    onBack = {
                        if (currentTab != FullPlayerTab.NOW_PLAYING) {
                            currentTab = FullPlayerTab.NOW_PLAYING
                        } else {
                            expanded = false
                        }
                    },
                ) {
                    ExpandedPlayer(
                        viewModel = viewModel,
                        currentTab = currentTab,
                        onTabChange = { currentTab = it },
                        onRetryLyrics = viewModel::retryLyrics,
                        onCollapse = { expanded = false },
                        onOpenArtist = { artist ->
                            expanded = false
                            viewModel.openArtist(artist)
                        },
                    )
                }
            }
            playlistTrack?.let { track ->
                AddToPlaylistDialogHost(
                    viewModel = viewModel,
                    track = track,
                    onDismiss = { playlistTrack = null },
                    onAdd = { playlistIds, duplicatePlaylistIds ->
                        viewModel.addToPlaylists(playlistIds, duplicatePlaylistIds, track)
                        playlistTrack = null
                    },
                    onFindDuplicates = { playlistIds ->
                        viewModel.findDuplicatePlaylistIds(playlistIds, track)
                    },
                    onFindCachedDuplicates = { playlistIds ->
                        viewModel.findCachedDuplicatePlaylistIds(playlistIds, track)
                    },
                    onCreate = { title ->
                        viewModel.createPlaylistAndAdd(title, track)
                        playlistTrack = null
                    },
                )
            }
        }
    }
}

@Composable
private fun ExpandedPlayer(
    viewModel: PlayerViewModel,
    currentTab: FullPlayerTab,
    onTabChange: (FullPlayerTab) -> Unit,
    onRetryLyrics: () -> Unit,
    onCollapse: () -> Unit,
    onOpenArtist: (String) -> Unit,
) {
    val state by viewModel.fullPlayerState.collectAsStateWithLifecycle()
    val lyricsState by viewModel.lyricsState.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val likedTrackKeys by viewModel.likedTrackKeys.collectAsStateWithLifecycle()
    val activeDownloads by viewModel.activeDownloads.collectAsStateWithLifecycle()
    val downloadedTracks by viewModel.downloadedTracks.collectAsStateWithLifecycle()
    val currentTrack = state.current
    val isLiked = currentTrack != null &&
        "${currentTrack.title}|${currentTrack.artist}".lowercase() in likedTrackKeys

    val downloadKey = currentTrack?.let {
        com.listenit.music.data.download.TrackDownloadManager.makeDownloadKey(it.title, it.artist)
    }
    val isDownloading = downloadKey != null && activeDownloads[downloadKey]?.let { !it.isFinished && it.error == null } == true
    var isDownloadedOnDisk by remember(currentTrack?.title, currentTrack?.artist) { mutableStateOf(false) }
    LaunchedEffect(currentTrack?.title, currentTrack?.artist) {
        val t = currentTrack ?: return@LaunchedEffect
        isDownloadedOnDisk = withContext(Dispatchers.IO) {
            viewModel.downloadManager.isTrackDownloaded(t.title, t.artist)
        }
    }
    val isDownloaded = isDownloadedOnDisk || (currentTrack != null && downloadedTracks.any {
        it.trackKey == downloadKey ||
        (it.title.equals(currentTrack.title, ignoreCase = true) && it.artist.equals(currentTrack.artist, ignoreCase = true))
    })

    FullPlayer(
        state = state,
        progressState = viewModel.progressState,
        player = viewModel.player,
        lyricsState = lyricsState,
        lyricsUiVersion = settings.lyricsUiVersion,
        lyricsAnimation = settings.lyricsAnimation,
        wavySeekbarEnabled = settings.wavySeekbarEnabled,
        musicBarStyle = settings.musicBarStyle,
        crossfadeEnabled = settings.crossfadeEnabled,
        crossfadeSeconds = settings.crossfadeSeconds,
        onToggleCrossfade = viewModel::toggleCrossfade,
        currentTab = currentTab,
        onTabChange = onTabChange,
        onRetryLyrics = onRetryLyrics,
        onCollapse = onCollapse,
        onOpenArtist = onOpenArtist,
        isLiked = isLiked,
        onToggleLiked = { currentTrack?.let(viewModel::toggleLiked) },
        onDoubleTapLike = { currentTrack?.let(viewModel::like) },
        isDownloaded = isDownloaded,
        isDownloading = isDownloading,
        onDownload = { currentTrack?.let(viewModel::downloadTrack) },
        fullScreenPoster = settings.fullScreenPoster,
        tiltPosterAnimationEnabled = settings.tiltPosterAnimationEnabled,
    )
}

@Composable
private fun AddToPlaylistDialogHost(
    viewModel: PlayerViewModel,
    track: PlayableTrack,
    onDismiss: () -> Unit,
    onAdd: (Set<Long>, Set<Long>) -> Unit,
    onFindDuplicates: suspend (Set<Long>) -> Set<Long>,
    onFindCachedDuplicates: suspend (Set<Long>) -> Set<Long>,
    onCreate: (String) -> Unit,
) {
    val playlists by viewModel.customPlaylists.collectAsStateWithLifecycle()
    AddToPlaylistDialog(
        track = track,
        playlists = playlists,
        onDismiss = onDismiss,
        onAdd = onAdd,
        onFindDuplicates = onFindDuplicates,
        onFindCachedDuplicates = onFindCachedDuplicates,
        onCreate = onCreate,
    )
}

@Composable
internal fun AnimatedPlayPauseIcon(isPlaying: Boolean, modifier: Modifier = Modifier) {
    AnimatedContent(
        targetState = isPlaying,
        transitionSpec = {
            (fadeIn(tween(ExpressiveMotion.Quick)) +
                scaleIn(ExpressiveMotion.spatialSpring(), initialScale = 0.7f)) togetherWith
                (fadeOut(tween(ExpressiveMotion.Quick)) +
                    scaleOut(tween(ExpressiveMotion.Quick), targetScale = 0.7f))
        },
        label = "playPauseIcon",
        modifier = modifier,
    ) { playing ->
        Icon(
            if (playing) Icons.Filled.Pause else Icons.Filled.PlayArrow,
            contentDescription = "Play or pause",
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun MiniPlayer(
    state: PlaybackChromeState,
    progressState: StateFlow<PlaybackProgressState>,
    onExpand: () -> Unit,
    onToggle: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onClose: () -> Unit,
    bottomPadding: androidx.compose.ui.unit.Dp,
    edgeToEdge: Boolean,
    backdrop: Backdrop? = LocalLiquidGlassBackdrop.current,
    musicBarStyle: MusicBarStyle = MusicBarStyle.LENS,
    fullScreenPoster: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val track = state.current ?: return
    // Liquid Glass dressing for the floating mini player (no-op when the
    // experimental setting is off — see ui/theme/LiquidGlass.kt).
    val liquidGlass = LocalLiquidGlass.current
    val appHaptics = LocalAppHaptics.current
    var dragX by remember(track.videoId, track.title) { mutableFloatStateOf(0f) }
    var dragY by remember(track.videoId, track.title) { mutableFloatStateOf(0f) }
    val shownX by animateFloatAsState(dragX, ExpressiveMotion.spatialSpring(), label = "miniPlayerX")
    val shownY by animateFloatAsState(dragY, ExpressiveMotion.spatialSpring(), label = "miniPlayerY")
    val threshold = with(LocalDensity.current) { 72.dp.toPx() }
    val isTablet = isTabletOrWideScreen()
    val shape = if (edgeToEdge && !isTablet) {
        RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)
    } else {
        RoundedCornerShape(26.dp)
    }
    val positionedModifier = if (edgeToEdge && !isTablet) {
        modifier.fillMaxWidth()
    } else {
        modifier
            .windowInsetsPadding(
                WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal),
            )
            .padding(horizontal = 14.dp)
            .padding(bottom = if (isTablet && edgeToEdge) 14.dp else bottomPadding)
            .widthIn(max = 680.dp)
            .fillMaxWidth()
    }
    Box(
        modifier = positionedModifier
            .graphicsLayer {
                translationX = shownX
                translationY = shownY.coerceAtLeast(0f)
                alpha = (1f - (abs(shownX) + shownY.coerceAtLeast(0f)) / (threshold * 4f)).coerceIn(0.55f, 1f)
            }
            .pointerInput(track.videoId, track.title) {
                detectDragGestures(
                    onDragCancel = { dragX = 0f; dragY = 0f },
                    onDragEnd = {
                        when {
                            dragY > threshold -> onClose()
                            dragY < -threshold -> onExpand()
                            dragX < -threshold -> {
                                appHaptics.next()
                                onNext()
                            }
                            dragX > threshold -> {
                                appHaptics.previous()
                                onPrevious()
                            }
                        }
                        dragX = 0f
                        dragY = 0f
                    },
                ) { change, amount ->
                    change.consume()
                    if (abs(dragX + amount.x) > abs(dragY + amount.y)) dragX += amount.x
                    else dragY += amount.y
                }
            }
            .clickable(onClick = onExpand),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            shape = shape,
            color = com.listenit.music.ui.theme.LiquidFrequency.GlassSurfaceElevated,
            border = com.listenit.music.ui.theme.LiquidFrequency.glassBorder(active = true),
            tonalElevation = 0.dp,
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth().liquidGlassChrome(shape, liquidGlass, LiquidGlassPreset.MiniPlayer, backdrop),
        ) {
            Box {
                // The cover itself, zoomed into a soft strip, is the mini player's
                // background, so the bar always carries the current record's colour.
                if (fullScreenPoster) {
                    PlayerArtwork(
                        track,
                        Modifier.matchParentSize().graphicsLayer {
                            scaleX = 1.8f
                            scaleY = 1.8f
                            alpha = 0.5f
                        },
                        0.dp,
                        decodeSizePx = 160,
                    )
                }
                Box(
                    Modifier.matchParentSize().background(
                        Brush.horizontalGradient(
                            0f to Ink.copy(alpha = 0.90f),
                            1f to Ink.copy(alpha = 0.62f),
                        ),
                    ),
                )
                Column(
                    modifier = if (edgeToEdge) {
                        Modifier.windowInsetsPadding(
                            WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal),
                        )
                    } else {
                        Modifier
                    },
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(start = 12.dp, end = 10.dp, top = 10.dp, bottom = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, OnArt.copy(alpha = 0.25f), RoundedCornerShape(12.dp)),
                        ) {
                            PlayerArtwork(track, Modifier.fillMaxSize(), 0.dp)
                        }
                        Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
                            Text(
                                track.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = OnArt,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                track.artist,
                                style = MaterialTheme.typography.bodyMedium,
                                color = OnArt.copy(alpha = 0.68f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        LensGlyphButton(
                            icon = Icons.Filled.SkipNext,
                            contentDescription = "Next",
                            onClick = {
                                appHaptics.next()
                                onNext()
                            },
                            size = 44.dp,
                            iconSize = 26.dp,
                            tint = OnArt,
                            enabled = state.queueSize > 1,
                        )
                        LensPlayButton(
                            isPlaying = state.isPlaying,
                            isBuffering = state.isBuffering,
                            progressState = progressState,
                            onClick = onToggle,
                            diameter = 56.dp,
                        )
                    }
                    val progress by progressState.collectAsStateWithLifecycle()
                    val progressFraction = if (progress.durationMs > 0L) {
                        (progress.positionMs.toFloat() / progress.durationMs.toFloat()).coerceIn(0f, 1f)
                    } else 0f
                    if (progressFraction > 0f) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(2.5.dp)
                                .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
                                .background(Color.White.copy(alpha = 0.12f))
                        ) {
                            Box(
                                Modifier
                                    .fillMaxWidth(progressFraction)
                                    .fillMaxHeight()
                                    .background(
                                        when (musicBarStyle) {
                                            MusicBarStyle.NEON -> Brush.horizontalGradient(listOf(Color(0xFF91A68B), Color(0xFF7895A2)))
                                            MusicBarStyle.VINYL -> Brush.horizontalGradient(listOf(Color(0xFFFFB300), Color(0xFFFF8F00)))
                                            MusicBarStyle.PILLBAR -> Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary))
                                            MusicBarStyle.LENS -> Brush.horizontalGradient(listOf(OnArt.copy(alpha = 0.85f), OnArt))
                                        }
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}

/** The dimmed half-screen panel shown while double-tap seeking. */
@Composable
private fun SeekOverlayPanel(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    shape: androidx.compose.ui.graphics.Shape,
) {
    Box(
        Modifier
            .fillMaxSize()
            .clip(shape)
            .background(Color.Black.copy(alpha = 0.50f)),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier.size(56.dp).border(1.dp, OnArt.copy(alpha = 0.7f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = OnArt, modifier = Modifier.size(28.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(label, style = MaterialTheme.typography.titleMedium, color = OnArt)
        }
    }
}

/** Warm paper white: the one text/glyph colour used over artwork. */
private val OnArt = Color(0xFFF6F2EC)
private val Ink = Color(0xFF0A0909)

/**
 * The app's single primary action. A solid paper disc inside a hairline ring;
 * the ring doubles as the track-progress arc, so "where am I in this song" is
 * always visible right where the thumb rests. Used large in the player and
 * small in the mini player.
 */
@Composable
private fun LensPlayButton(
    isPlaying: Boolean,
    isBuffering: Boolean,
    progressState: StateFlow<PlaybackProgressState>?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    diameter: androidx.compose.ui.unit.Dp = 84.dp,
    discColor: Color = OnArt,
    glyphColor: Color = Ink,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (pressed) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "lensPlayScale",
    )
    val appHaptics = LocalAppHaptics.current
    val progress = progressState?.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .size(diameter)
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .drawBehind {
                val stroke = 2.5.dp.toPx()
                val arcTopLeft = Offset(stroke / 2f, stroke / 2f)
                val arcSize = Size(size.width - stroke, size.height - stroke)
                drawArc(
                    color = discColor.copy(alpha = 0.24f),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = Stroke(width = stroke),
                )
                val p = progress?.value?.let {
                    if (it.durationMs > 0) (it.positionMs.toFloat() / it.durationMs).coerceIn(0f, 1f) else 0f
                } ?: 0f
                if (p > 0f) {
                    drawArc(
                        color = discColor,
                        startAngle = -90f,
                        sweepAngle = 360f * p,
                        useCenter = false,
                        topLeft = arcTopLeft,
                        size = arcSize,
                        style = Stroke(width = stroke, cap = StrokeCap.Round),
                    )
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            onClick = {
                appHaptics.playPause()
                onClick()
            },
            shape = CircleShape,
            color = discColor,
            contentColor = glyphColor,
            interactionSource = interaction,
            modifier = Modifier.size(diameter * 0.81f),
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (isBuffering) {
                    ExpressiveInlineLoadingIndicator(
                        size = diameter * 0.33f,
                        color = androidx.compose.material3.LocalContentColor.current,
                        strokeWidth = 3.dp,
                    )
                } else {
                    AnimatedPlayPauseIcon(isPlaying, Modifier.size(diameter * 0.40f))
                }
            }
        }
    }
}

@Composable
fun PlayingWaveBars(
    modifier: Modifier = Modifier,
    waveColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    shape: Shape = CircleShape,
) {
    val transition = rememberInfiniteTransition(label = "miniArtworkWave")
    val first = transition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 480, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "miniWaveFirst",
    )
    val second = transition.animateFloat(
        initialValue = 0.95f,
        targetValue = 0.30f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 640, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "miniWaveSecond",
    )
    val third = transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 530, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "miniWaveThird",
    )

    val content: @Composable () -> Unit = {
        // Fixed small badge content so it can never expand to fill the cover.
        // Outer Box is 26x22 (16+5+5, 12+5+5) — always tiny, bottom-end aligned by caller.
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .padding(horizontal = 5.dp, vertical = 5.dp)
                .size(16.dp, 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            androidx.compose.foundation.Canvas(
                Modifier.fillMaxSize(),
            ) {
            if (size.width <= 0f || size.height <= 0f) return@Canvas
            val barCount = 3
            val barWidth = (size.width / 5.2f).coerceAtLeast(1.5f)
            val barGap = barWidth * 0.9f
            val totalContentWidth = barCount * barWidth + (barCount - 1) * barGap
            val startX = ((size.width - totalContentWidth) / 2f).coerceAtLeast(0f)

            repeat(barCount) { index ->
                val fraction = when (index) {
                    0 -> first.value
                    1 -> second.value
                    else -> third.value
                }
                val barHeight = (size.height * fraction).coerceIn(minOf(barWidth, size.height), size.height)
                drawRoundRect(
                    color = waveColor,
                    topLeft = androidx.compose.ui.geometry.Offset(
                        x = startX + index * (barWidth + barGap),
                        y = size.height - barHeight,
                    ),
                    size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth / 2f, barWidth / 2f),
                )
            }
            }
        }
    }

    if (containerColor == Color.Transparent) {
        Box(
            modifier = modifier.defaultMinSize(minWidth = 24.dp, minHeight = 18.dp),
            contentAlignment = Alignment.Center,
        ) {
            content()
        }
    } else {
        Surface(
            shape = shape,
            color = containerColor,
            modifier = modifier.defaultMinSize(minWidth = 26.dp, minHeight = 22.dp),
        ) {
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddToPlaylistDialog(
    track: PlayableTrack,
    playlists: List<SavedPlaylist>,
    onDismiss: () -> Unit,
    onAdd: (Set<Long>, Set<Long>) -> Unit,
    onFindDuplicates: suspend (Set<Long>) -> Set<Long>,
    onFindCachedDuplicates: suspend (Set<Long>) -> Set<Long>,
    onCreate: (String) -> Unit,
) {
    val sanitizedPlaylists = remember(playlists) { playlists.distinctBy { it.id } }
    var newPlaylistName by remember(track) { mutableStateOf("") }
    var selectedPlaylistIds by remember(track) { mutableStateOf(emptySet<Long>()) }
    var duplicateConfirmation by remember(track) { mutableStateOf<Set<Long>?>(null) }
    var duplicatePlaylistIds by remember(track) { mutableStateOf(emptySet<Long>()) }
    var knownDuplicatePlaylistIds by remember(track) { mutableStateOf(emptySet<Long>()) }
    var isCheckingDuplicates by remember(track) { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val trackKey = remember(track.title, track.artist) { track.toGeneratedTrack().key }
    val selectedPlaylists = sanitizedPlaylists.filter { it.id in selectedPlaylistIds }
    val playlistListMaxHeight = (LocalConfiguration.current.screenHeightDp.dp - 360.dp)
        .coerceIn(180.dp, 410.dp)

    LaunchedEffect(sanitizedPlaylists.map(SavedPlaylist::id)) {
        selectedPlaylistIds = selectedPlaylistIds.intersect(sanitizedPlaylists.mapTo(mutableSetOf(), SavedPlaylist::id))
    }

    LaunchedEffect(trackKey, sanitizedPlaylists.map(SavedPlaylist::id)) {
        runCatching {
            val remoteIds = sanitizedPlaylists
                .filterTo(mutableListOf()) { it.remotePlaylistId != null }
                .mapTo(mutableSetOf(), SavedPlaylist::id)
            knownDuplicatePlaylistIds = knownDuplicatePlaylistIds + onFindCachedDuplicates(remoteIds)
        }
    }

    fun requestAdd(playlistIds: Set<Long>) {
        if (playlistIds.isEmpty() || isCheckingDuplicates) return
        val immediateDuplicates = playlistIds.filter { id ->
            id in knownDuplicatePlaylistIds ||
                sanitizedPlaylists.firstOrNull { it.id == id }?.tracks?.any {
                    it.key == trackKey ||
                        (it.name.equals(track.title, ignoreCase = true) && it.artist.equals(track.artist, ignoreCase = true))
                } == true
        }.toSet()
        if (immediateDuplicates.isNotEmpty()) {
            duplicatePlaylistIds = emptySet()
            duplicateConfirmation = immediateDuplicates
            return
        }
        scope.launch {
            runCatching {
                isCheckingDuplicates = true
                val duplicates = onFindDuplicates(playlistIds)
                isCheckingDuplicates = false
                knownDuplicatePlaylistIds = (knownDuplicatePlaylistIds - playlistIds) + duplicates
                if (duplicates.isEmpty()) {
                    onAdd(playlistIds, emptySet())
                } else {
                    duplicatePlaylistIds = emptySet()
                    duplicateConfirmation = duplicates
                }
            }.onFailure {
                isCheckingDuplicates = false
                onAdd(playlistIds, emptySet())
            }
        }
    }

    duplicateConfirmation?.let { duplicates ->
        val duplicatePlaylists = sanitizedPlaylists.filter { it.id in duplicates }
        if (duplicatePlaylists.size == 1) {
            val playlist = duplicatePlaylists.first()
            AlertDialog(
                onDismissRequest = { duplicateConfirmation = null },
                title = { Text("Song already in playlist") },
                text = {
                    Text(
                        "${track.title} by ${track.artist} is already present in ${playlist.title}. " +
                            "You can leave the playlist unchanged or add another copy.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onAdd(selectedPlaylistIds, setOf(playlist.id))
                            duplicateConfirmation = null
                        },
                    ) {
                        Text("Add anyway")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { duplicateConfirmation = null }) {
                        Text("Leave unchanged")
                    }
                },
            )
            return
        }

        val missingCount = selectedPlaylistIds.size - duplicates.size
        AlertDialog(
            onDismissRequest = { duplicateConfirmation = null },
            title = {
                Text("${duplicates.size} playlists already have this song")
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Existing copies stay unchanged. Select only the playlists where you want another copy.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 260.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        itemsIndexed(
                            duplicatePlaylists,
                            key = { index, playlist -> "${playlist.id}_${playlist.remotePlaylistId ?: ""}_$index" },
                        ) { _, playlist ->
                            val addAgain = playlist.id in duplicatePlaylistIds
                            val canAddAnother = playlist.mode != LIKED_SONGS_MODE
                            Surface(
                                onClick = {
                                    duplicatePlaylistIds = if (addAgain) {
                                        duplicatePlaylistIds - playlist.id
                                    } else {
                                        duplicatePlaylistIds + playlist.id
                                    }
                                },
                                enabled = canAddAnother,
                                shape = RoundedCornerShape(14.dp),
                                color = if (addAgain) {
                                    MaterialTheme.colorScheme.secondaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainerHigh
                                },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            playlist.title,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                        Text(
                                            if (canAddAnother) "Add another copy" else "Already liked",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                    Checkbox(checked = addAgain, onCheckedChange = null, enabled = canAddAnother)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onAdd(selectedPlaylistIds, duplicatePlaylistIds)
                        duplicateConfirmation = null
                    },
                ) {
                    Text(
                        when {
                            duplicatePlaylistIds.isNotEmpty() -> "Add anyway"
                            missingCount > 0 -> "Add missing"
                            else -> "Leave unchanged"
                        },
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { duplicateConfirmation = null }) {
                    Text("Leave unchanged")
                }
            },
        )
        return
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier.liquidGlassChrome(
            androidx.compose.material3.BottomSheetDefaults.ExpandedShape,
            LocalLiquidGlass.current,
            LiquidGlassPreset.ModalSheet,
            LocalLiquidGlassOverlayBackdrop.current,
        ),
        containerColor = liquidGlassContainerColor(
            MaterialTheme.colorScheme.surfaceContainer,
            backdrop = LocalLiquidGlassOverlayBackdrop.current,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .adaptiveContentWidth(maxWidth = 640.dp)
                .align(Alignment.CenterHorizontally)
                .padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(52.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.PlaylistAdd, contentDescription = null, modifier = Modifier.size(27.dp))
                    }
                }
                Column(Modifier.weight(1f).padding(start = 14.dp)) {
                    Text(
                        "Add to playlist",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        if (selectedPlaylistIds.isEmpty()) "Choose one or more playlists" else "${selectedPlaylistIds.size} selected",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text(
                        track.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        track.artist,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            if (sanitizedPlaylists.isEmpty()) {
                Text(
                    "No playlists yet. Create your first playlist below.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = playlistListMaxHeight),
                    verticalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    itemsIndexed(
                        sanitizedPlaylists,
                        key = { index, playlist -> "${playlist.id}_${playlist.remotePlaylistId ?: ""}_$index" },
                    ) { _, playlist ->
                        val selected = playlist.id in selectedPlaylistIds
                        val alreadyAdded = playlist.id in knownDuplicatePlaylistIds ||
                            playlist.tracks.any {
                                it.key == trackKey ||
                                    (it.name.equals(track.title, ignoreCase = true) && it.artist.equals(track.artist, ignoreCase = true))
                            }
                        val trackCount = playlist.remoteTrackCount
                            ?: playlist.tracks.size.takeIf { playlist.remotePlaylistId == null }
                        val countLabel = when (trackCount) {
                            null -> "Track count unavailable"
                            1 -> "1 track"
                            else -> "$trackCount tracks"
                        }
                        val rowColor by animateColorAsState(
                            targetValue = if (selected) {
                                MaterialTheme.colorScheme.secondaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerHigh
                            },
                            animationSpec = tween(ExpressiveMotion.Quick),
                            label = "playlistSelectionColor",
                        )
                        Surface(
                            onClick = {
                                selectedPlaylistIds = if (selected) {
                                    selectedPlaylistIds - playlist.id
                                } else {
                                    selectedPlaylistIds + playlist.id
                                }
                            },
                            shape = RoundedCornerShape(18.dp),
                            color = rowColor,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                PlaylistCover(
                                    playlist = playlist,
                                    modifier = Modifier.size(52.dp),
                                    cornerRadius = 14.dp,
                                )
                                Column(Modifier.weight(1f).padding(horizontal = 13.dp)) {
                                    Text(
                                        playlist.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Text(
                                        when {
                                            isCheckingDuplicates && selected -> "$countLabel · Checking contents…"
                                            alreadyAdded -> "$countLabel · Already added"
                                            else -> countLabel
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (alreadyAdded) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        },
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                                Checkbox(checked = selected, onCheckedChange = null)
                            }
                        }
                    }
                }
            }

            OutlinedTextField(
                value = newPlaylistName,
                onValueChange = { newPlaylistName = it },
                label = { Text("New playlist name") },
                singleLine = true,
                trailingIcon = {
                    IconButton(
                        enabled = newPlaylistName.isNotBlank(),
                        onClick = {
                            val cleanName = newPlaylistName.trim()
                            val existingPlaylist = sanitizedPlaylists.firstOrNull {
                                it.mode == "custom" && it.title.equals(cleanName, ignoreCase = true)
                            }
                            if (existingPlaylist == null) {
                                onCreate(cleanName)
                            } else {
                                selectedPlaylistIds = setOf(existingPlaylist.id)
                                requestAdd(setOf(existingPlaylist.id))
                            }
                        },
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Create playlist and add track")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onDismiss) { Text("Cancel") }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = { requestAdd(selectedPlaylistIds) },
                    enabled = selectedPlaylistIds.isNotEmpty() && !isCheckingDuplicates,
                ) {
                    if (isCheckingDuplicates) {
                        ExpressiveInlineLoadingIndicator(
                            size = 18.dp,
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    } else {
                        Icon(Icons.Filled.PlaylistAdd, contentDescription = null, modifier = Modifier.size(19.dp))
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        when {
                            selectedPlaylistIds.isEmpty() -> "Add"
                            isCheckingDuplicates -> "Checking"
                            else -> "Add to ${selectedPlaylists.size}"
                        },
                    )
                }
            }
        }
    }
}

private enum class SeekDirection { REWIND, FORWARD }

@Composable
private fun rememberPosterTilt(enabled: Boolean): Offset {
    val context = LocalContext.current
    var tilt by remember { mutableStateOf(Offset.Zero) }

    DisposableEffect(context, enabled) {
        if (!enabled) {
            tilt = Offset.Zero
            onDispose {}
        } else {
            val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
            val tiltSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
                ?: sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            var gravityX = 0f
            var gravityY = 0f
            var gravityZ = 0f
            var hasGravity = false
            var neutralPitch: Float? = null
            var neutralRoll: Float? = null
            var filteredPitch = 0f
            var filteredRoll = 0f
            val rotationMatrix = FloatArray(9)
            val orientation = FloatArray(3)
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    val pitch: Float
                    val roll: Float
                    if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                        if (!hasGravity) {
                            gravityX = event.values[0]
                            gravityY = event.values[1]
                            gravityZ = event.values[2]
                            hasGravity = true
                        } else {
                            gravityX += (event.values[0] - gravityX) * 0.22f
                            gravityY += (event.values[1] - gravityY) * 0.22f
                            gravityZ += (event.values[2] - gravityZ) * 0.22f
                        }
                        pitch = Math.toDegrees(
                            kotlin.math.atan2(
                                -gravityY.toDouble(),
                                kotlin.math.sqrt(gravityX * gravityX + gravityZ * gravityZ).toDouble(),
                            ),
                        ).toFloat()
                        roll = Math.toDegrees(
                            kotlin.math.atan2(
                                gravityX.toDouble(),
                                kotlin.math.sqrt(gravityY * gravityY + gravityZ * gravityZ).toDouble(),
                            ),
                        ).toFloat()
                    } else if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                        SensorManager.getOrientation(rotationMatrix, orientation)
                        pitch = orientation[1] * 180f / PI.toFloat()
                        roll = orientation[2] * 180f / PI.toFloat()
                    } else {
                        return
                    }

                    val pitchBaseline = neutralPitch ?: pitch.also { neutralPitch = it }
                    val rollBaseline = neutralRoll ?: roll.also { neutralRoll = it }
                    val relativePitch = ((pitch - pitchBaseline + 540f) % 360f) - 180f
                    val relativeRoll = ((roll - rollBaseline + 540f) % 360f) - 180f
                    filteredPitch += (relativePitch - filteredPitch) * 0.28f
                    filteredRoll += (relativeRoll - filteredRoll) * 0.28f
                    tilt = Offset(
                        x = (-filteredPitch * 1.15f).coerceIn(-16f, 16f),
                        y = (filteredRoll * 1.15f).coerceIn(-16f, 16f),
                    )
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
            }

            if (sensorManager != null && tiltSensor != null) {
                sensorManager.registerListener(listener, tiltSensor, SensorManager.SENSOR_DELAY_GAME)
            }
            onDispose {
                sensorManager?.unregisterListener(listener)
                tilt = Offset.Zero
            }
        }
    }
    return tilt
}

@Composable
private fun FullPlayer(
    state: MusicPlayerState,
    progressState: StateFlow<PlaybackProgressState>,
    player: MusicPlayer,
    lyricsState: LyricsUiState,
    lyricsUiVersion: LyricsUiVersion = LyricsUiVersion.MODERN,
    lyricsAnimation: LyricsAnimation = LyricsAnimation.APPLE_FLUID,
    wavySeekbarEnabled: Boolean = true,
    musicBarStyle: MusicBarStyle = MusicBarStyle.LENS,
    crossfadeEnabled: Boolean = false,
    crossfadeSeconds: Int = 5,
    onToggleCrossfade: () -> Unit = {},
    tiltPosterAnimationEnabled: Boolean = true,
    currentTab: FullPlayerTab,
    onTabChange: (FullPlayerTab) -> Unit,
    onRetryLyrics: () -> Unit,
    onCollapse: () -> Unit,
    onOpenArtist: (String) -> Unit = {},
    isLiked: Boolean = false,
    onToggleLiked: () -> Unit = {},
    onDoubleTapLike: () -> Unit = {},
    isDownloaded: Boolean = false,
    isDownloading: Boolean = false,
    onDownload: () -> Unit = {},
    fullScreenPoster: Boolean = true,
) {
    val track = state.current ?: return
    // Full-bleed (crop to screen) vs whole-cover (full width, cover dissolves downward).
    var artFill by remember(fullScreenPoster) { mutableStateOf(fullScreenPoster) }
    var lyricsFullscreen by remember(currentTab) { mutableStateOf(false) }
    BackHandler(enabled = lyricsFullscreen) { lyricsFullscreen = false }
    val view = LocalView.current
    DisposableEffect(view, lyricsFullscreen) {
        val fullscreenActive = lyricsFullscreen
        val activity = generateSequence(view.context) { (it as? android.content.ContextWrapper)?.baseContext }
            .filterIsInstance<android.app.Activity>().firstOrNull()
        val controller = activity?.window?.let { WindowCompat.getInsetsController(it, view) }
        val previousBehavior = controller?.systemBarsBehavior
        if (fullscreenActive) {
            controller?.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller?.hide(WindowInsetsCompat.Type.systemBars())
        }
        onDispose {
            if (fullscreenActive) {
                controller?.show(WindowInsetsCompat.Type.systemBars())
                previousBehavior?.let { controller?.systemBarsBehavior = it }
            }
        }
    }
    var showTrackMenu by remember(track.videoId, track.title) { mutableStateOf(false) }
    var artworkDragX by remember(track.videoId, track.title) { mutableFloatStateOf(0f) }
    var dismissDragY by remember(track.videoId, track.title) { mutableFloatStateOf(0f) }
    var isDismissDragging by remember { mutableStateOf(false) }
    var seekOverlayDirection by remember(track.videoId, track.title) { mutableStateOf<SeekDirection?>(null) }
    var seekOverlaySeconds by remember(track.videoId, track.title) { mutableIntStateOf(0) }
    var lastTapTimestamp by remember(track.videoId, track.title) { mutableLongStateOf(0L) }
    var lastTapSide by remember(track.videoId, track.title) { mutableStateOf<SeekDirection?>(null) }
    var lastLikeTapTimestamp by remember(track.videoId, track.title) { mutableLongStateOf(0L) }
    var seekResetJob by remember(track.videoId, track.title) { mutableStateOf<Job?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val shownArtworkX by animateFloatAsState(
        artworkDragX,
        ExpressiveMotion.spatialSpring(),
        label = "fullPlayerArtworkX",
    )
    val shownDismissY by animateFloatAsState(
        targetValue = dismissDragY,
        animationSpec = if (isDismissDragging) snap() else ExpressiveMotion.spatialSpring(),
        label = "fullPlayerDismissY",
    )
    val swipeThreshold = with(LocalDensity.current) { 88.dp.toPx() }

    // Dominant cover-art color for the ambient background glow. Extracted
    // once per track through the shared Coil loader (normally a cache hit)
    // with Palette; any failure leaves the standard surface gradient.
    val context = LocalContext.current
    var ambientPrimary by remember(track.videoId, track.artworkUrl, track.title, track.artist) { mutableStateOf<Color?>(null) }
    var ambientSecondary by remember(track.videoId, track.artworkUrl, track.title, track.artist) { mutableStateOf<Color?>(null) }
    var ambientTertiary by remember(track.videoId, track.artworkUrl, track.title, track.artist) { mutableStateOf<Color?>(null) }
    LaunchedEffect(track.videoId, track.artworkUrl, track.title, track.artist) {
        val url = track.artworkUrl?.takeIf { it.isNotBlank() } ?: return@LaunchedEffect
        withContext(Dispatchers.IO) {
            runCatching {
                val request = ImageRequest.Builder(context)
                    .data(url)
                    .allowHardware(false)
                    .size(128)
                    .build()
                val bitmap = ((context.imageLoader.execute(request) as? SuccessResult)?.drawable as? BitmapDrawable)?.bitmap
                    ?: return@runCatching
                val palette = Palette.from(bitmap).clearFilters().generate()
                val p = palette.vibrantSwatch ?: palette.dominantSwatch ?: palette.mutedSwatch
                val s = palette.lightVibrantSwatch ?: palette.darkVibrantSwatch ?: palette.mutedSwatch ?: palette.dominantSwatch
                val t = palette.darkVibrantSwatch ?: palette.darkMutedSwatch ?: palette.dominantSwatch
                if (p != null) ambientPrimary = Color(p.rgb)
                if (s != null) ambientSecondary = Color(s.rgb)
                if (t != null) ambientTertiary = Color(t.rgb)
            }
        }
    }
    val ambientColor by animateColorAsState(
        targetValue = ambientPrimary ?: MaterialTheme.colorScheme.primary,
        animationSpec = tween(700),
        label = "playerAmbientColor",
    )
    val ambientCompanion by animateColorAsState(
        targetValue = ambientSecondary ?: androidx.compose.ui.graphics.lerp(MaterialTheme.colorScheme.tertiary, ambientColor, 0.42f),
        animationSpec = tween(700),
        label = "playerAmbientCompanion",
    )
    val ambientDeep by animateColorAsState(
        targetValue = ambientTertiary ?: androidx.compose.ui.graphics.lerp(MaterialTheme.colorScheme.secondary, ambientColor, 0.25f),
        animationSpec = tween(700),
        label = "playerAmbientDeep",
    )

    fun Modifier.playerVerticalSwipe(enabled: Boolean): Modifier = if (!enabled) this else pointerInput(track.videoId, track.title, currentTab) {
        detectVerticalDragGestures(
            onDragStart = { isDismissDragging = true },
            onDragCancel = {
                isDismissDragging = false
                dismissDragY = 0f
            },
            onDragEnd = {
                isDismissDragging = false
                when {
                    currentTab != FullPlayerTab.NOW_PLAYING && dismissDragY > swipeThreshold -> onTabChange(FullPlayerTab.NOW_PLAYING)
                    currentTab == FullPlayerTab.NOW_PLAYING && dismissDragY < -swipeThreshold -> onTabChange(FullPlayerTab.QUEUE)
                    currentTab == FullPlayerTab.NOW_PLAYING && dismissDragY > swipeThreshold -> onCollapse()
                }
                dismissDragY = 0f
            },
        ) { change, amount ->
            val updatedDrag = if (currentTab != FullPlayerTab.NOW_PLAYING) {
                (dismissDragY + amount).coerceAtLeast(0f)
            } else {
                dismissDragY + amount
            }
            if (updatedDrag != dismissDragY) change.consume()
            dismissDragY = updatedDrag
        }
    }

    val fillT by animateFloatAsState(
        targetValue = if (artFill) 1f else 0f,
        animationSpec = tween(420, easing = FastOutSlowInEasing),
        label = "playerArtFill",
    )
    val artScale by animateFloatAsState(
        targetValue = if (state.isPlaying) 1.05f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow),
        label = "playerArtScale",
    )
    val posterTilt = rememberPosterTilt(
        enabled = tiltPosterAnimationEnabled && fullScreenPoster && currentTab == FullPlayerTab.NOW_PLAYING,
    )
    val shownPosterTiltX by animateFloatAsState(
        targetValue = posterTilt.x,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "posterTiltX",
    )
    val shownPosterTiltY by animateFloatAsState(
        targetValue = posterTilt.y,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "posterTiltY",
    )
    // The cover dissolves into its own deepest colour, never into flat black.
    val plateColor = androidx.compose.ui.graphics.lerp(Ink, ambientDeep, 0.38f)

    val playerBackdrop = if (isLiquidGlassBackdropSupported()) rememberLayerBackdrop() else null
    CompositionLocalProvider(
        LocalLiquidGlassBackdrop provides playerBackdrop,
        LocalLiquidGlassOverlayBackdrop provides playerBackdrop,
    ) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                translationY = shownDismissY.coerceAtLeast(0f)
                val playerHeight = size.height.coerceAtLeast(1f)
                alpha = (1f - shownDismissY.coerceAtLeast(0f) / (playerHeight * 1.5f)).coerceIn(0.72f, 1f)
            }
            .playerVerticalSwipe(enabled = currentTab == FullPlayerTab.NOW_PLAYING),
    ) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val bgWidth = constraints.maxWidth.toFloat()
            val bgHeight = constraints.maxHeight.toFloat()
            val bgMaxDimension = maxOf(bgWidth, bgHeight, 1f)

            val artHeightDp = with(LocalDensity.current) {
                (bgWidth + (bgHeight - bgWidth) * fillT).toDp()
            }
            val fadeStart = 0.74f - 0.34f * fillT

            Box(Modifier.matchParentSize().liquidGlassSource(playerBackdrop)) {
                Box(Modifier.fillMaxSize().background(plateColor))

                if (fullScreenPoster) {
                    // The cover, edge to edge, under the status bar.
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(artHeightDp)
                            .align(Alignment.TopCenter)
                            .graphicsLayer {
                                scaleX = artScale * 1.10f
                                scaleY = artScale * 1.10f
                                translationX = shownArtworkX * 0.5f
                                rotationX = shownPosterTiltX
                                rotationY = shownPosterTiltY
                                cameraDistance = 32f * density
                            },
                    ) {
                        LiquidCrossfadePoster(
                            currentTrack = track,
                            state = state,
                            crossfadeEnabled = crossfadeEnabled,
                            modifier = Modifier.fillMaxSize(),
                            corner = 0.dp,
                        )
                    }

                    // Top scrim (status bar / header legibility) and the dissolve into the plate.
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(artHeightDp)
                            .align(Alignment.TopCenter)
                            .background(
                                Brush.verticalGradient(
                                    0f to Color.Black.copy(alpha = 0.50f),
                                    0.14f to Color.Black.copy(alpha = 0.16f),
                                    0.28f to Color.Transparent,
                                ),
                            )
                            .background(
                                Brush.verticalGradient(
                                    0f to Color.Transparent,
                                    fadeStart to Color.Transparent,
                                    fadeStart + (1f - fadeStart) * 0.45f to plateColor.copy(alpha = 0.62f),
                                    fadeStart + (1f - fadeStart) * 0.80f to plateColor.copy(alpha = 0.94f),
                                    1f to plateColor,
                                ),
                            ),
                    )
                } else {
                    // Spotify-like subtle ambient background glow from the album art
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    0f to ambientColor.copy(alpha = 0.40f),
                                    0.45f to ambientDeep.copy(alpha = 0.22f),
                                    1f to plateColor,
                                ),
                            ),
                    )
                }

                // Lyrics / Queue: a calm, deeply blurred veil of the same cover.
                androidx.compose.animation.AnimatedVisibility(
                    visible = currentTab != FullPlayerTab.NOW_PLAYING,
                    enter = fadeIn(tween(320)),
                    exit = fadeOut(tween(260)),
                    modifier = Modifier.matchParentSize(),
                ) {
                    Box(Modifier.fillMaxSize()) {
                        BackdropBlur(radius = 40.dp, modifier = Modifier.fillMaxSize()) {
                            LiquidCrossfadePoster(
                                currentTrack = track,
                                state = state,
                                crossfadeEnabled = crossfadeEnabled,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer {
                                        scaleX = 1.35f
                                        scaleY = 1.35f
                                        alpha = 0.8f
                                    },
                                corner = 0.dp,
                                decodeSizePx = 200,
                            )
                        }
                        Box(
                            Modifier.fillMaxSize().background(
                                Brush.verticalGradient(
                                    0f to plateColor.copy(alpha = 0.78f),
                                    1f to plateColor.copy(alpha = 0.96f),
                                ),
                            ),
                        )
                    }
                }
            }

            // Gesture layer over the whole cover: swipe = skip, centre tap = lyrics,
            // centre double-tap = like, edge double-tap = seek (logic unchanged).
            androidx.compose.animation.AnimatedVisibility(
                visible = currentTab == FullPlayerTab.NOW_PLAYING,
                enter = fadeIn(tween(1)),
                exit = fadeOut(tween(300)),
                modifier = Modifier.matchParentSize(),
            ) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .pointerInput(track.videoId, track.title) {
                                                                    awaitEachGesture {
                                                                            val down = awaitFirstDown(requireUnconsumed = false)
                                                                            var isDrag = false
                                                                            val touchSlop = viewConfiguration.touchSlop
                                                                            val initialX = down.position.x
                                                                            val initialY = down.position.y

                                                                            while (true) {
                                                                                val event = awaitPointerEvent()
                                                                                val change = event.changes.firstOrNull { it.id == down.id } ?: break

                                                                                if (!change.pressed) {
                                                                                    if (isDrag) {
                                                                                        when {
                                                                                            artworkDragX < -swipeThreshold -> player.next()
                                                                                            artworkDragX > swipeThreshold -> player.previous()
                                                                                        }
                                                                                        artworkDragX = 0f
                                                                                    } else if (down.position.y > size.height * 0.60f) {
                        // Controls zone: a stray tap here must not open lyrics / like / seek.
                        lastTapSide = null
                        lastTapTimestamp = 0L
                        lastLikeTapTimestamp = 0L
                    } else {
                                                                                        val now = SystemClock.elapsedRealtime()
                                                                                        val side = when {
                                                                                            initialX < size.width * 0.34f -> SeekDirection.REWIND
                                                                                            initialX > size.width * 0.66f -> SeekDirection.FORWARD
                                                                                            else -> null
                                                                                        }

                                                                                        if (side == null) {
                                                                                            lastTapSide = null
                                                                                            lastTapTimestamp = 0L
                                                                                            if (lastLikeTapTimestamp != 0L && now - lastLikeTapTimestamp < 450L) {
                                                                                                lastLikeTapTimestamp = 0L
                                                                                                onDoubleTapLike()
                                                                                            } else {
                                                                                                lastLikeTapTimestamp = now
                                                                                                onTabChange(FullPlayerTab.LYRICS)
                                                                                            }
                                                                                        } else {
                                                                                            // Preserve the existing edge double-tap seek behavior.
                                                                                            // An edge tap cannot complete a center Like sequence.
                                                                                            lastLikeTapTimestamp = 0L
                                                                                            if (lastTapSide != side) {
                                                                                                seekResetJob?.cancel()
                                                                                                seekOverlayDirection = null
                                                                                                lastTapSide = side
                                                                                                lastTapTimestamp = now
                                                                                            } else if (now - lastTapTimestamp < 450L) {
                                                                                                val newSeconds = if (seekOverlayDirection == side) seekOverlaySeconds + 5 else 5
                                                                                                seekOverlaySeconds = newSeconds
                                                                                                seekOverlayDirection = side
                                                                                                lastTapTimestamp = now
                                                                                                val deltaMs = if (side == SeekDirection.FORWARD) 5_000L else -5_000L
                                                                                                val newPos = (player.state.value.positionMs + deltaMs).coerceIn(0L, player.state.value.durationMs.coerceAtLeast(0L))
                                                                                                player.seekTo(newPos)

                                                                                                seekResetJob?.cancel()
                                                                                                seekResetJob = coroutineScope.launch {
                                                                                                    delay(700L)
                                                                                                    seekOverlayDirection = null
                                                                                                    lastTapSide = null
                                                                                                }
                                                                                            } else {
                                                                                                lastTapTimestamp = now
                                                                                                lastTapSide = side
                                                                                                seekOverlayDirection = null
                                                                                            }
                                                                                        }
                                                                                    }
                                                                                    break
                                                                                }

                                                                                if (change.isConsumed) {
                                                                                    artworkDragX = 0f
                                                                                    lastLikeTapTimestamp = 0L
                                                                                    lastTapTimestamp = 0L
                                                                                    lastTapSide = null
                                                                                    break
                                                                                }

                                                                                val dx = change.position.x - initialX
                                                                                val dy = change.position.y - initialY
                                                                                if (!isDrag) {
                                                                                    if (kotlin.math.abs(dx) > touchSlop && kotlin.math.abs(dx) > kotlin.math.abs(dy)) {
                                                                                        isDrag = true
                                                                                        lastLikeTapTimestamp = 0L
                                                                                        lastTapTimestamp = 0L
                                                                                        lastTapSide = null
                                                                                        change.consume()
                                                                                    }
                                                                                } else {
                                                                                    change.consume()
                                                                                    artworkDragX = dx
                                                                                }
                                                                            }
                                                                        }
                        },
                ) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = seekOverlayDirection == SeekDirection.REWIND,
                        enter = fadeIn(tween(120)),
                        exit = fadeOut(tween(260)),
                        modifier = Modifier.align(Alignment.CenterStart).fillMaxHeight().fillMaxWidth(0.5f),
                    ) {
                        SeekOverlayPanel(
                            icon = Icons.Filled.FastRewind,
                            label = "−${seekOverlaySeconds}s",
                            shape = RoundedCornerShape(topEnd = 160.dp, bottomEnd = 160.dp),
                        )
                    }
                    androidx.compose.animation.AnimatedVisibility(
                        visible = seekOverlayDirection == SeekDirection.FORWARD,
                        enter = fadeIn(tween(120)),
                        exit = fadeOut(tween(260)),
                        modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight().fillMaxWidth(0.5f),
                    ) {
                        SeekOverlayPanel(
                            icon = Icons.Filled.FastForward,
                            label = "+${seekOverlaySeconds}s",
                            shape = RoundedCornerShape(topStart = 160.dp, bottomStart = 160.dp),
                        )
                    }
                }
            }
            Column(
                Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // ── Header: ring buttons over the cover ─────────────────
                if (!lyricsFullscreen) Row(
                    Modifier
                        .fillMaxWidth()
                        .adaptiveContentWidth(maxWidth = 640.dp)
                        .height(56.dp)
                        .padding(horizontal = 16.dp)
                        .playerVerticalSwipe(enabled = currentTab != FullPlayerTab.NOW_PLAYING),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LensRingButton(
                        icon = if (currentTab != FullPlayerTab.NOW_PLAYING) Icons.AutoMirrored.Filled.ArrowBack else Icons.Filled.ExpandMore,
                        contentDescription = if (currentTab != FullPlayerTab.NOW_PLAYING) "Back to player" else "Minimize player",
                        onClick = {
                            if (currentTab != FullPlayerTab.NOW_PLAYING) {
                                onTabChange(FullPlayerTab.NOW_PLAYING)
                            } else {
                                onCollapse()
                            }
                        },
                        size = 44.dp,
                        iconSize = 24.dp,
                        tint = OnArt,
                        filled = true,
                    )
                    Column(
                        Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            when (currentTab) {
                                FullPlayerTab.NOW_PLAYING -> "Now playing"
                                FullPlayerTab.LYRICS -> "Lyrics"
                                FullPlayerTab.QUEUE -> "Queue"
                            },
                            style = MaterialTheme.typography.labelLarge,
                            color = OnArt,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            if (currentTab == FullPlayerTab.LYRICS) "${track.title} \u2022 ${track.artist}" else state.sourceLabel.ifBlank { "TuneGlass" },
                            style = MaterialTheme.typography.bodySmall,
                            color = OnArt.copy(alpha = 0.66f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (currentTab == FullPlayerTab.NOW_PLAYING) {
                            LensRingButton(
                                icon = when {
                                    isDownloaded -> Icons.Filled.CheckCircle
                                    isDownloading -> Icons.Filled.Downloading
                                    else -> Icons.Filled.Download
                                },
                                contentDescription = when {
                                    isDownloaded -> "Song downloaded"
                                    isDownloading -> "Downloading song"
                                    else -> "Download song"
                                },
                                onClick = {
                                    when {
                                        isDownloaded -> {
                                            android.widget.Toast.makeText(
                                                context,
                                                "Track is already downloaded",
                                                android.widget.Toast.LENGTH_SHORT,
                                            ).show()
                                        }
                                        isDownloading -> {
                                            android.widget.Toast.makeText(
                                                context,
                                                "Download is in progress\u2026",
                                                android.widget.Toast.LENGTH_SHORT,
                                            ).show()
                                        }
                                        else -> {
                                            onDownload()
                                            android.widget.Toast.makeText(
                                                context,
                                                "Downloading \u201c${track.title}\u201d\u2026",
                                                android.widget.Toast.LENGTH_SHORT,
                                            ).show()
                                        }
                                    }
                                },
                                size = 44.dp,
                                iconSize = 20.dp,
                                tint = OnArt,
                                active = isDownloaded || isDownloading,
                                activeColor = if (isDownloaded) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                                filled = true,
                            )
                        }
                        LensRingButton(
                            icon = Icons.Filled.MoreVert,
                            contentDescription = "Song options",
                            onClick = { showTrackMenu = true },
                            size = 44.dp,
                            iconSize = 22.dp,
                            tint = OnArt,
                            filled = true,
                        )
                    }
                }
                if (!lyricsFullscreen) Spacer(Modifier.height(6.dp))

                AnimatedContent(
                    targetState = currentTab,
                    modifier = Modifier.weight(1f).adaptiveContentWidth(maxWidth = 680.dp),
                    transitionSpec = {
                        if (targetState != FullPlayerTab.NOW_PLAYING) {
                            (slideInVertically(animationSpec = ExpressiveMotion.smoothSpring()) { it / 6 } +
                                fadeIn(tween(ExpressiveMotion.Standard))) togetherWith
                                (slideOutVertically(animationSpec = tween(ExpressiveMotion.Quick)) { -it / 6 } +
                                    fadeOut(tween(ExpressiveMotion.Quick)))
                        } else {
                            (slideInVertically(animationSpec = ExpressiveMotion.smoothSpring()) { -it / 6 } +
                                fadeIn(tween(ExpressiveMotion.Standard))) togetherWith
                                (slideOutVertically(animationSpec = tween(ExpressiveMotion.Quick)) { it / 6 } +
                                    fadeOut(tween(ExpressiveMotion.Quick)))
                        }
                    },
                    label = "playerTabContent",
                ) { tab ->
                    when (tab) {
                        FullPlayerTab.LYRICS -> {
                            if (lyricsUiVersion == LyricsUiVersion.MODERN) {
                                ModernLyricsPanel(
                                    state = state,
                                    player = player,
                                    lyricsState = lyricsState,
                                    progressState = progressState,
                                    wavySeekbarEnabled = wavySeekbarEnabled,
                                    musicBarStyle = musicBarStyle,
                                    onRetry = onRetryLyrics,
                                    onToggleFullscreen = { lyricsFullscreen = !lyricsFullscreen },
                                    isFullscreen = lyricsFullscreen,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .adaptiveContentWidth(maxWidth = 720.dp),
                                )
                            } else {
                                LyricsPanel(
                                    state = state,
                                    progressState = progressState,
                                    player = player,
                                    lyricsState = lyricsState,
                                    lyricsAnimation = lyricsAnimation,
                                    wavySeekbarEnabled = wavySeekbarEnabled,
                                    musicBarStyle = musicBarStyle,
                                    onRetry = onRetryLyrics,
                                    onToggleFullscreen = { lyricsFullscreen = !lyricsFullscreen },
                                    isFullscreen = lyricsFullscreen,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .adaptiveContentWidth(maxWidth = 720.dp),
                                )
                            }
                        }

                        FullPlayerTab.QUEUE -> {
                            QueuePanel(
                                state,
                                player,
                                Modifier
                                    .fillMaxSize()
                                    .adaptiveContentWidth(maxWidth = 720.dp)
                                    .padding(horizontal = 20.dp),
                            )
                        }

                        FullPlayerTab.NOW_PLAYING -> {
                            Column(
                                Modifier.fillMaxSize().padding(horizontal = 24.dp).padding(bottom = 12.dp),
                                verticalArrangement = if (fullScreenPoster) Arrangement.Bottom else Arrangement.SpaceBetween,
                            ) {
                                if (!fullScreenPoster) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxWidth()
                                            .padding(top = 8.dp, bottom = 16.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 6.dp)
                                                .aspectRatio(1f)
                                                .shadow(
                                                    elevation = 28.dp,
                                                    shape = RoundedCornerShape(18.dp),
                                                    clip = false,
                                                    spotColor = Color.Black.copy(alpha = 0.75f),
                                                    ambientColor = Color.Black.copy(alpha = 0.50f),
                                                )
                                                .clip(RoundedCornerShape(18.dp))
                                                .graphicsLayer {
                                                    scaleX = artScale
                                                    scaleY = artScale
                                                    translationX = shownArtworkX * 0.5f
                                                },
                                        ) {
                                            LiquidCrossfadePoster(
                                                currentTrack = track,
                                                state = state,
                                                crossfadeEnabled = crossfadeEnabled,
                                                modifier = Modifier.fillMaxSize(),
                                                corner = 18.dp,
                                            )
                                        }
                                    }
                                }
                                Column(Modifier.fillMaxWidth()) {
                                    Row(
                                        Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.Bottom,
                                    ) {
                                        Column(Modifier.weight(1f)) {
                                            Text(
                                                track.title,
                                                style = MaterialTheme.typography.headlineMedium.copy(
                                                    fontSize = 30.sp,
                                                    lineHeight = 36.sp,
                                                ),
                                                color = OnArt,
                                                maxLines = 1,
                                                modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                                            )
                                            Spacer(Modifier.height(4.dp))
                                            val splitArtists = remember(track.artist) {
                                                com.listenit.music.util.ArtistHelper.splitArtists(track.artist)
                                            }
                                            FlowRow(
                                                horizontalArrangement = Arrangement.spacedBy(0.dp),
                                                verticalArrangement = Arrangement.spacedBy(2.dp),
                                            ) {
                                                splitArtists.forEachIndexed { index, artName ->
                                                    Text(
                                                        text = artName,
                                                        style = MaterialTheme.typography.titleMedium.copy(
                                                            fontSize = 17.sp,
                                                            fontWeight = FontWeight.Medium,
                                                        ),
                                                        color = OnArt.copy(alpha = 0.80f),
                                                        modifier = Modifier
                                                            .clickable(
                                                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                                                indication = null,
                                                            ) { onOpenArtist(artName) },
                                                    )
                                                    if (index < splitArtists.lastIndex) {
                                                        Text(
                                                            text = ", ",
                                                            style = MaterialTheme.typography.titleMedium.copy(
                                                                fontSize = 17.sp,
                                                                fontWeight = FontWeight.Normal,
                                                            ),
                                                            color = OnArt.copy(alpha = 0.55f),
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        Spacer(Modifier.width(12.dp))
                                        LensRingButton(
                                            icon = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                            contentDescription = if (isLiked) "Unlike song" else "Like song",
                                            onClick = onToggleLiked,
                                            size = 48.dp,
                                            iconSize = 24.dp,
                                            tint = OnArt,
                                            active = isLiked,
                                            filled = true,
                                        )
                                    }
                                    Spacer(Modifier.height(14.dp))
                                    SeekBar(
                                        progressState = progressState,
                                        isPlaying = state.isPlaying,
                                        trackKey = track.videoId ?: "${track.artist}|${track.title}",
                                        wavyEnabled = wavySeekbarEnabled,
                                        musicBarStyle = musicBarStyle,
                                        crossfadeEnabled = crossfadeEnabled,
                                        crossfadeSeconds = crossfadeSeconds,
                                        onSeek = player::seekTo,
                                        isTranslucent = true,
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    MainControls(state, player, progressState)
                                    Spacer(Modifier.height(10.dp))
                                    PlayerUtilityControls(
                                        state = state,
                                        crossfadeEnabled = crossfadeEnabled,
                                        onToggleCrossfade = onToggleCrossfade,
                                        onLyrics = { onTabChange(FullPlayerTab.LYRICS) },
                                        onQueue = { onTabChange(FullPlayerTab.QUEUE) },
                                    )
                                }
                            }
                        }
                    }
                }
                state.error?.takeUnless { lyricsFullscreen }?.let { message ->
                    Surface(
                        onClick = player::retry,
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    ) {
                        Row(
                            Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Filled.ErrorOutline, null, modifier = Modifier.size(21.dp))
                            Column(Modifier.weight(1f).padding(horizontal = 10.dp)) {
                                Text(
                                    message,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    "Tap to retry",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
                                )
                            }
                            IconButton(
                                onClick = player::clearError,
                                modifier = Modifier.size(28.dp),
                            ) {
                                Icon(Icons.Filled.Close, "Dismiss error", modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
    if (showTrackMenu) {
        TrackContextMenuSheet(
            target = TrackMenuTarget.Track(track.title, track.artist, ""),
            capabilities = TrackMenuCapabilities(
                showCopyActions = true,
                showDeleteScrobble = false,
            ),
            playableTrack = track,
            onDismiss = { showTrackMenu = false },
            onPlayInTuneGlass = { player.play(track, sourceLabel = state.sourceLabel) },
        )
    }
}
}

@Composable
internal fun PlayerProgressSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val primary = MaterialTheme.colorScheme.primary
    val tertiary = MaterialTheme.colorScheme.tertiary
    val inactive = MaterialTheme.colorScheme.onSurface.copy(alpha = if (enabled) 0.20f else 0.12f)
    val range = (valueRange.endInclusive - valueRange.start).coerceAtLeast(0.0001f)
    val fraction = ((value - valueRange.start) / range).coerceIn(0f, 1f)

    Slider(
        value = value,
        onValueChange = onValueChange,
        onValueChangeFinished = onValueChangeFinished,
        valueRange = valueRange,
        enabled = enabled,
        modifier = modifier.drawBehind {
            val inset = 10.dp.toPx()
            val startX = inset
            val endX = (size.width - inset).coerceAtLeast(startX)
            val activeEndX = startX + ((endX - startX) * fraction)
            val centerY = size.height / 2f
            drawLine(
                color = inactive,
                start = androidx.compose.ui.geometry.Offset(startX, centerY),
                end = androidx.compose.ui.geometry.Offset(endX, centerY),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round,
            )
            if (activeEndX > startX) {
                drawLine(
                    color = primary.copy(alpha = if (enabled) 1f else 0.42f),
                    start = androidx.compose.ui.geometry.Offset(startX, centerY),
                    end = androidx.compose.ui.geometry.Offset(activeEndX, centerY),
                    strokeWidth = 2.8.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }
        },
        colors = SliderDefaults.colors(
            thumbColor = primary,
            activeTrackColor = Color.Transparent,
            inactiveTrackColor = Color.Transparent,
            activeTickColor = Color.Transparent,
            inactiveTickColor = Color.Transparent,
            disabledThumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.34f),
            disabledActiveTrackColor = Color.Transparent,
            disabledInactiveTrackColor = Color.Transparent,
            disabledActiveTickColor = Color.Transparent,
            disabledInactiveTickColor = Color.Transparent,
        ),
    )
}

@Composable
private fun SeekBar(
    progressState: StateFlow<PlaybackProgressState>,
    isPlaying: Boolean,
    trackKey: String?,
    wavyEnabled: Boolean = true,
    musicBarStyle: MusicBarStyle = MusicBarStyle.LENS,
    crossfadeEnabled: Boolean = false,
    crossfadeSeconds: Int = 5,
    onSeek: (Long) -> Unit,
    isTranslucent: Boolean = false,
) {
    val progress by progressState.collectAsStateWithLifecycle()
    // "Wavy seekbar" off = the same hairline, perfectly straight.
    WavySeekBar(
        positionMs = progress.positionMs,
        durationMs = progress.durationMs,
        isPlaying = isPlaying,
        onSeek = onSeek,
        isTranslucent = isTranslucent,
        trackKey = trackKey,
        wavy = wavyEnabled,
        style = musicBarStyle,
        crossfadeEnabled = crossfadeEnabled,
        crossfadeSeconds = crossfadeSeconds,
    )
}

/**
 * Transport. Five items on one line: shuffle, previous, [play], next, repeat.
 * Skip/shuffle/repeat are bare glyphs (a dot marks an active toggle); the play
 * disc with its progress ring is the only filled control on screen.
 */
@Composable
private fun MainControls(
    state: MusicPlayerState,
    player: MusicPlayer,
    progressState: StateFlow<PlaybackProgressState>,
) {
    val appHaptics = LocalAppHaptics.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LensGlyphButton(
            icon = Icons.Filled.Shuffle,
            contentDescription = if (state.shuffleEnabled) "Shuffle on" else "Shuffle off",
            onClick = {
                appHaptics.toggle()
                player.toggleShuffle()
            },
            tint = OnArt.copy(alpha = 0.72f),
            activeColor = OnArt,
            active = state.shuffleEnabled,
            iconSize = 22.dp,
        )
        LensGlyphButton(
            icon = Icons.Filled.SkipPrevious,
            contentDescription = "Previous",
            onClick = {
                appHaptics.previous()
                player.previous()
            },
            tint = OnArt,
            size = 56.dp,
            iconSize = 34.dp,
        )
        LensPlayButton(
            isPlaying = state.isPlaying,
            isBuffering = state.isBuffering,
            progressState = progressState,
            onClick = player::togglePlayPause,
            diameter = 84.dp,
        )
        LensGlyphButton(
            icon = Icons.Filled.SkipNext,
            contentDescription = "Next",
            onClick = {
                appHaptics.next()
                player.next()
            },
            tint = OnArt,
            size = 56.dp,
            iconSize = 34.dp,
        )
        LensGlyphButton(
            icon = if (state.repeatMode == Player.REPEAT_MODE_ONE) Icons.Filled.RepeatOne else Icons.Filled.Repeat,
            contentDescription = when (state.repeatMode) {
                Player.REPEAT_MODE_ONE -> "Repeat one"
                Player.REPEAT_MODE_ALL -> "Repeat all"
                else -> "Repeat off"
            },
            onClick = {
                appHaptics.toggle()
                player.cycleRepeatMode()
            },
            tint = OnArt.copy(alpha = 0.72f),
            activeColor = OnArt,
            active = state.repeatMode != Player.REPEAT_MODE_OFF,
            iconSize = 22.dp,
        )
    }
}

/** Lyrics ring, a hairline quality tag (centred), queue ring. */
@Composable
private fun PlayerUtilityControls(
    state: MusicPlayerState,
    crossfadeEnabled: Boolean,
    onToggleCrossfade: () -> Unit,
    onLyrics: () -> Unit,
    onQueue: () -> Unit,
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LensRingButton(
            icon = Icons.Filled.FormatQuote,
            contentDescription = "Show lyrics",
            onClick = onLyrics,
            size = 44.dp,
            tint = OnArt,
            filled = true,
        )
        CrossfadeControlButton(
            crossfadeEnabled = crossfadeEnabled,
            isCrossfading = state.isCrossfading,
            onToggleCrossfade = onToggleCrossfade,
        )
        Row(
            modifier = Modifier
                .height(32.dp)
                .clip(CircleShape)
                .border(1.dp, OnArt.copy(alpha = 0.30f), CircleShape)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Filled.HighQuality,
                contentDescription = null,
                tint = OnArt.copy(alpha = 0.9f),
                modifier = Modifier.size(15.dp),
            )
            Spacer(Modifier.width(7.dp))
            Text(
                qualityLabel(state),
                style = MaterialTheme.typography.labelMedium.copy(fontFeatureSettings = "tnum"),
                color = OnArt.copy(alpha = 0.9f),
                maxLines = 1,
            )
        }
        LensRingButton(
            icon = Icons.Filled.QueueMusic,
            contentDescription = "View queue",
            onClick = onQueue,
            size = 44.dp,
            tint = OnArt,
            filled = true,
        )
    }
}

private data class QueueItemHolder(
    val id: Long,
    val track: PlayableTrack,
)

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun QueuePanel(state: MusicPlayerState, player: MusicPlayer, modifier: Modifier = Modifier) {
    var nextId by remember { mutableLongStateOf(1L) }
    val visualQueue = remember { mutableStateListOf<QueueItemHolder>() }
    var draggingItemId by remember { mutableStateOf<Long?>(null) }
    var dragStartIndex by remember { mutableIntStateOf(-1) }
    var dragCurrentIndex by remember { mutableIntStateOf(-1) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }

    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current
    val listState = rememberLazyListState()
    val itemHeightPx = with(density) { 68.dp.toPx() }

    LaunchedEffect(state.queue, draggingItemId) {
        if (draggingItemId == null) {
            val oldItems = visualQueue.toMutableList()
            val newItems = state.queue.map { track ->
                val existing = oldItems.firstOrNull { it.track == track }
                if (existing != null) {
                    oldItems.remove(existing)
                    existing
                } else {
                    QueueItemHolder(id = nextId++, track = track)
                }
            }
            visualQueue.clear()
            visualQueue.addAll(newItems)
        }
    }

    fun startDrag(index: Int, holder: QueueItemHolder) {
        draggingItemId = holder.id
        dragStartIndex = index
        dragCurrentIndex = index
        dragOffsetY = 0f
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    fun onDragDelta(dragAmountY: Float) {
        if (draggingItemId == null) return
        val currentIdx = dragCurrentIndex
        if (currentIdx !in visualQueue.indices) return

        dragOffsetY += dragAmountY
        val offsetRows = (dragOffsetY / itemHeightPx).roundToInt()
        val targetIdx = (currentIdx + offsetRows).coerceIn(0, visualQueue.size - 1)

        if (targetIdx != currentIdx) {
            val item = visualQueue.removeAt(currentIdx)
            visualQueue.add(targetIdx, item)
            dragOffsetY -= (targetIdx - currentIdx) * itemHeightPx
            dragCurrentIndex = targetIdx
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    fun endDrag() {
        val from = dragStartIndex
        val to = dragCurrentIndex
        val activeId = draggingItemId
        draggingItemId = null
        dragStartIndex = -1
        dragCurrentIndex = -1
        dragOffsetY = 0f

        if (activeId != null && from >= 0 && to >= 0 && from != to) {
            player.moveQueueItem(from, to)
        }
    }

    fun cancelDrag() {
        draggingItemId = null
        dragStartIndex = -1
        dragCurrentIndex = -1
        dragOffsetY = 0f
        val oldItems = visualQueue.toMutableList()
        val newItems = state.queue.map { track ->
            val existing = oldItems.firstOrNull { it.track == track }
            if (existing != null) {
                oldItems.remove(existing)
                existing
            } else {
                QueueItemHolder(id = nextId++, track = track)
            }
        }
        visualQueue.clear()
        visualQueue.addAll(newItems)
    }

    Column(modifier) {
        Row(
            Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text("Up next", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    if (state.isEndlessQueue) {
                        "Unlimited songs · ${state.currentIndex.coerceAtLeast(0) + 1} playing · Hold to float & reorder"
                    } else {
                        "${visualQueue.size} songs · ${state.currentIndex.coerceAtLeast(0) + 1} playing · Hold to float & reorder"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            LensRingButton(
                icon = Icons.Filled.ClearAll,
                contentDescription = "Clear upcoming songs",
                onClick = player::clearUpcoming,
                enabled = state.currentIndex >= 0 && state.currentIndex + 1 < state.queue.size,
                size = 44.dp,
            )
        }
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            items(visualQueue, key = { it.id }) { holder ->
                val index = visualQueue.indexOf(holder)
                val item = holder.track
                val isCurrent = item == state.current
                val isDraggingThis = draggingItemId == holder.id
                val elevation by animateDpAsState(if (isDraggingThis) 28.dp else 0.dp, label = "queueDragElevation")
                val scale by animateFloatAsState(if (isDraggingThis) 1.045f else 1f, label = "queueDragScale")
                val zIndex = if (isDraggingThis) 100f else 1f
                val translationY = if (isDraggingThis) dragOffsetY else 0f

                LiquidGlassSurface(
                    glassModifier = Modifier.liquidGlassChrome(RoundedCornerShape(16.dp), LocalLiquidGlass.current),
                    onClick = {
                        if (draggingItemId == null) {
                            val originalIndex = state.queue.indexOfFirst { it == item }
                            if (originalIndex >= 0) {
                                player.seekToQueueItem(originalIndex)
                            } else if (index >= 0) {
                                player.seekToQueueItem(index)
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(
                        if (isDraggingThis) 2.dp else 1.dp,
                        if (isDraggingThis) MaterialTheme.colorScheme.primary
                        else if (isCurrent) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
                    ),
                    color = liquidGlassContainerColor(
                        if (isDraggingThis) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.90f)
                        else if (isCurrent) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                        else Color.Black.copy(alpha = 0.22f),
                    ),
                    contentColor = if (isDraggingThis || isCurrent) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .animateItem()
                        .zIndex(zIndex)
                        .graphicsLayer {
                            this.translationY = translationY
                            this.scaleX = scale
                            this.scaleY = scale
                            this.shadowElevation = elevation.toPx()
                        }
                        .pointerInput(holder.id) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = {
                                    val idx = visualQueue.indexOfFirst { it.id == holder.id }
                                    if (idx >= 0) startDrag(idx, holder)
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    onDragDelta(dragAmount.y)
                                },
                                onDragEnd = { endDrag() },
                                onDragCancel = { cancelDrag() },
                            )
                        },
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        PlayerArtwork(item, Modifier.size(50.dp), 13.dp)
                        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                            Text(
                                item.title,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = if (isCurrent || isDraggingThis) FontWeight.Bold else FontWeight.SemiBold,
                            )
                            Text(
                                item.artist,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isCurrent || isDraggingThis) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        if (isCurrent && !isDraggingThis) {
                            Icon(
                                Icons.Filled.GraphicEq,
                                "Currently playing",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(start = 4.dp),
                        ) {
                            LensRingButton(
                                icon = Icons.Filled.DeleteOutline,
                                contentDescription = "Remove from queue",
                                onClick = {
                                    val originalIndex = state.queue.indexOfFirst { it == item }
                                    if (originalIndex >= 0) {
                                        player.removeQueueItem(originalIndex)
                                    } else if (index >= 0) {
                                        player.removeQueueItem(index)
                                    }
                                },
                                size = 32.dp,
                                iconSize = 16.dp,
                                tint = LocalContentColor.current.copy(alpha = 0.70f),
                            )
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .pointerInput(holder.id) {
                                        detectDragGestures(
                                            onDragStart = {
                                                val idx = visualQueue.indexOfFirst { it.id == holder.id }
                                                if (idx >= 0) startDrag(idx, holder)
                                            },
                                            onDrag = { change, dragAmount ->
                                                change.consume()
                                                onDragDelta(dragAmount.y)
                                            },
                                            onDragEnd = { endDrag() },
                                            onDragCancel = { cancelDrag() },
                                        )
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    Icons.Filled.DragHandle,
                                    contentDescription = "Hold and float to reorder",
                                    tint = if (isDraggingThis) MaterialTheme.colorScheme.primary else LocalContentColor.current.copy(alpha = 0.60f),
                                    modifier = Modifier.size(22.dp),
                                )
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(12.dp)) }
        }
    }
}

@Composable
private fun CrossfadeControlButton(
    crossfadeEnabled: Boolean,
    isCrossfading: Boolean,
    onToggleCrossfade: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val appHaptics = LocalAppHaptics.current
    val burstAnim = remember { androidx.compose.animation.core.Animatable(1f) }
    val scope = rememberCoroutineScope()

    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (pressed) 0.86f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "crossfadePress",
    )

    val infiniteTransition = rememberInfiniteTransition(label = "crossfadeGlow")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.70f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "ringPulse",
    )
    val eqPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "eqPhase",
    )

    Box(
        modifier = modifier
            .size(48.dp)
            .clickable(
                interactionSource = interaction,
                indication = null,
                role = androidx.compose.ui.semantics.Role.Button,
                onClick = {
                    appHaptics.toggle()
                    val nextState = !crossfadeEnabled
                    if (nextState) {
                        scope.launch {
                            burstAnim.snapTo(0f)
                            burstAnim.animateTo(
                                targetValue = 1f,
                                animationSpec = tween(650, easing = FastOutSlowInEasing),
                            )
                        }
                    }
                    onToggleCrossfade()
                    android.widget.Toast.makeText(
                        context,
                        if (nextState) "Crossfade will apply on next song" else "Crossfade Turned Off",
                        android.widget.Toast.LENGTH_SHORT,
                    ).show()
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        // Expanding shockwave ring on tap
        if (burstAnim.value < 1f) {
            val progress = burstAnim.value
            val ringRadius = 22.dp + 24.dp * progress
            val ringAlpha = (1f - progress) * 0.9f
            Box(
                Modifier
                    .size(ringRadius * 2)
                    .clip(CircleShape)
                    .border(
                        1.5.dp,
                        com.listenit.music.ui.theme.LiquidFrequency.ElectricCyan.copy(alpha = ringAlpha),
                        CircleShape,
                    ),
            )
        }

        // Active breathing halo
        if (crossfadeEnabled) {
            val auraScale by animateFloatAsState(
                targetValue = if (isCrossfading) 1.25f else 1.08f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "auraScale",
            )
            Box(
                Modifier
                    .size(44.dp * auraScale)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                com.listenit.music.ui.theme.LiquidFrequency.ElectricCyan.copy(alpha = if (isCrossfading) pulseAlpha * 0.5f else 0.20f),
                                Color.Transparent,
                            ),
                        ),
                    ),
            )
        }

        // Main button surface
        val ringColor = if (crossfadeEnabled) {
            com.listenit.music.ui.theme.LiquidFrequency.ElectricCyan.copy(alpha = if (isCrossfading) 0.95f else 0.70f)
        } else {
            OnArt.copy(alpha = 0.24f)
        }
        val fillColor = if (crossfadeEnabled) {
            com.listenit.music.ui.theme.LiquidFrequency.MidnightIndigo.copy(alpha = 0.85f)
        } else {
            Color.Black.copy(alpha = 0.28f)
        }

        Box(
            modifier = Modifier
                .size(44.dp)
                .graphicsLayer {
                    scaleX = pressScale
                    scaleY = pressScale
                }
                .clip(CircleShape)
                .background(fillColor)
                .border(
                    width = if (crossfadeEnabled) 1.5.dp else 1.dp,
                    color = ringColor,
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (crossfadeEnabled) {
                // Live animated 4-bar equalizer Canvas
                Canvas(Modifier.size(20.dp)) {
                    val barWidth = 2.5.dp.toPx()
                    val gap = 2.dp.toPx()
                    val totalW = (barWidth * 4) + (gap * 3)
                    val startX = (size.width - totalW) / 2f
                    val maxH = size.height * 0.80f
                    val minH = size.height * 0.25f

                    val heights = listOf(
                        minH + (maxH - minH) * ((kotlin.math.sin(eqPhase + 0.0) + 1.0) / 2.0).toFloat(),
                        minH + (maxH - minH) * ((kotlin.math.sin(eqPhase + 1.4) + 1.0) / 2.0).toFloat(),
                        minH + (maxH - minH) * ((kotlin.math.sin(eqPhase + 2.8) + 1.0) / 2.0).toFloat(),
                        minH + (maxH - minH) * ((kotlin.math.sin(eqPhase + 4.2) + 1.0) / 2.0).toFloat(),
                    )

                    for (i in 0 until 4) {
                        val barH = heights[i]
                        val x = startX + i * (barWidth + gap)
                        val y = (size.height - barH) / 2f
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                listOf(
                                    com.listenit.music.ui.theme.LiquidFrequency.ElectricCyan,
                                    com.listenit.music.ui.theme.LiquidFrequency.Ultraviolet,
                                ),
                                startY = y,
                                endY = y + barH,
                            ),
                            topLeft = Offset(x, y),
                            size = Size(barWidth, barH),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(1.5.dp.toPx(), 1.5.dp.toPx()),
                        )
                    }
                }
            } else {
                Icon(
                    imageVector = Icons.Filled.GraphicEq,
                    contentDescription = "Turn on crossfade",
                    tint = OnArt.copy(alpha = 0.65f),
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}



/**
 * Smooth Liquid Cover Artwork.
 * Clean, seamless crossfade between tracks without distracting band cuts, wave slices, or HUD overlays.
 */
@Composable
private fun LiquidCrossfadePoster(
    currentTrack: PlayableTrack,
    state: MusicPlayerState,
    crossfadeEnabled: Boolean,
    modifier: Modifier = Modifier,
    corner: androidx.compose.ui.unit.Dp = 0.dp,
    decodeSizePx: Int? = null,
) {
    var primaryTrack by remember { mutableStateOf(currentTrack) }
    var outgoingTrackState by remember { mutableStateOf<PlayableTrack?>(null) }
    val manualProgressAnim = remember { androidx.compose.animation.core.Animatable(1f) }

    val trackChanged = (currentTrack.videoId != primaryTrack.videoId ||
        currentTrack.title != primaryTrack.title ||
        currentTrack.artist != primaryTrack.artist)

    if (trackChanged) {
        outgoingTrackState = primaryTrack
        primaryTrack = currentTrack
    }

    LaunchedEffect(primaryTrack.videoId, primaryTrack.title, primaryTrack.artist) {
        if (outgoingTrackState != null) {
            val duration = if (crossfadeEnabled) 500 else 250
            manualProgressAnim.snapTo(0f)
            manualProgressAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = duration, easing = FastOutSlowInEasing),
            )
            outgoingTrackState = null
        }
    }

    val isPlayerCrossfading = state.isCrossfading && state.outgoingTrack != null
    val effectiveOutgoing = if (isPlayerCrossfading) state.outgoingTrack else outgoingTrackState
    val progress = if (isPlayerCrossfading) {
        state.crossfadeProgress.coerceIn(0f, 1f)
    } else {
        manualProgressAnim.value.coerceIn(0f, 1f)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(corner))
            .background(com.listenit.music.ui.theme.LiquidFrequency.GlassSubstrate),
        contentAlignment = Alignment.Center,
    ) {
        if (effectiveOutgoing != null && progress < 1f) {
            PlayerArtwork(
                track = effectiveOutgoing,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = 1f - progress },
                corner = corner,
                decodeSizePx = decodeSizePx,
            )
        }
        PlayerArtwork(
            track = primaryTrack,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    alpha = if (effectiveOutgoing != null && progress < 1f) progress else 1f
                },
            corner = corner,
            decodeSizePx = decodeSizePx,
        )
    }
}

@Composable
private fun PlayerArtwork(
    track: PlayableTrack,
    modifier: Modifier,
    corner: androidx.compose.ui.unit.Dp,
    decodeSizePx: Int? = null,
) {
    Box(
        modifier
            .clip(RoundedCornerShape(corner))
            .background(com.listenit.music.ui.theme.LiquidFrequency.GlassSubstrate),
        contentAlignment = Alignment.Center,
    ) {
        if (track.artworkUrl.isNullOrBlank()) {
            com.listenit.music.ui.common.FrequencyOrb(
                size = 180.dp,
                isPlaying = true,
                isAnimated = true,
            )
        } else {
            ArtworkImage(
                name = track.title,
                artist = track.artist,
                embeddedUrl = track.artworkUrl,
                fallbackIcon = Icons.Filled.MusicNote,
                modifier = Modifier.fillMaxSize(),
                decodeSizePx = decodeSizePx,
            )
        }
    }
}

internal fun formatTime(ms: Long): String {
    val total = (ms.coerceAtLeast(0) / 1000)
    return "%d:%02d".format(total / 60, total % 60)
}

private fun qualityLabel(state: MusicPlayerState): String = when {
    // Lossless with known bit depth / sampling rate → show precise format
    state.isLossless && state.bitDepth != null && state.samplingRateKHz != null -> {
        val rate = if (state.samplingRateKHz % 1.0 == 0.0) state.samplingRateKHz.toInt().toString()
        else state.samplingRateKHz.toString()
        "FLAC ${state.bitDepth}/$rate"
    }
    state.isLossless && state.audioCodec == "MP3 320k" -> "MP3 320k"
    state.isLossless -> state.audioCodec ?: "LOSSLESS"
    // YouTube with known codec + bitrate → e.g. "OPUS 160k" or "AAC 256k"
    state.audioCodec != null && state.bitrateKbps != null -> "${state.audioCodec} ${state.bitrateKbps}k"
    state.audioCodec != null -> state.audioCodec
    state.bitrateKbps != null -> "${state.bitrateKbps} kbps"
    else -> "AUDIO"
}

private fun PlayableTrack.toGeneratedTrack() = com.listenit.music.data.generate.GeneratedTrack(
    name = title,
    artist = artist,
    artworkUrl = artworkUrl,
    album = album,
    url = videoId?.let { "https://music.youtube.com/watch?v=$it" }.orEmpty(),
)
