package com.listenit.music.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.geometry.Size
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import com.listenit.music.util.BatteryOptimizationHelper
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Colorize
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.BubbleChart
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Waves
import com.listenit.music.data.local.LyricsAnimation
import com.listenit.music.data.local.LyricsUiVersion
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import com.listenit.music.ui.theme.LocalLiquidGlass
import com.listenit.music.ui.theme.LocalLiquidGlassOverlayBackdrop
import com.listenit.music.ui.theme.LiquidGlassPreset
import com.listenit.music.ui.theme.liquidGlassContainerColor
import com.listenit.music.ui.theme.LiquidGlassSurface
import com.listenit.music.ui.theme.liquidGlassChrome
import com.listenit.music.ui.player.LocalMiniPlayerScrollClearance
import com.listenit.music.ui.shell.FloatingNavDefaults
import com.listenit.music.R
import com.listenit.music.data.local.AccentMode
import com.listenit.music.data.local.EQ_BAND_FREQS_HZ
import com.listenit.music.data.local.EQ_MAX_GAIN_DB
import com.listenit.music.data.local.EqualizerPresets
import com.listenit.music.data.local.EqualizerSettings
import com.listenit.music.data.local.MusicBarStyle
import com.listenit.music.data.local.eqBandLabel
import com.listenit.music.ui.common.ExpressiveHeader
import com.listenit.music.ui.common.safeDrawingBottomPadding
import com.listenit.music.ui.common.safeHorizontalContentPadding
import com.listenit.music.ui.common.adaptiveContentWidth
import com.listenit.music.ui.theme.ExpressivePillShape
import kotlin.math.roundToInt

private data class AccentPreset(val name: String, val hex: String)
private val ACCENT_PRESETS = listOf(
    AccentPreset("Cyan", "#69F0F0"),
    AccentPreset("Ultraviolet", "#9875FF"),
    AccentPreset("Coral", "#FF8F9D"),
    AccentPreset("Ocean", "#2196C6"),
    AccentPreset("Violet", "#7C4DFF"),
    AccentPreset("Amber", "#E0A030"),
)

// -- Expressive shape scale used only within this screen --
private val CardOuterShape = RoundedCornerShape(14.dp)
private val IconBadgeShape = RoundedCornerShape(14.dp)

/** Where a row sits within a visually-connected group of settings rows —
 *  drives per-row corner radii so a multi-row group reads as one premium
 *  surface split into rows, not a stack of separate cards (see groupShape
 *  below and the GROUP_GAP spacing used between rows in a group's Column). */
private enum class GroupPosition { SINGLE, TOP, MIDDLE, BOTTOM }

private val GROUP_OUTER_RADIUS = 26.dp
private val GROUP_INNER_RADIUS = 14.dp
private val GROUP_GAP = 6.dp

private fun groupShape(position: GroupPosition): RoundedCornerShape = when (position) {
    GroupPosition.SINGLE -> RoundedCornerShape(GROUP_OUTER_RADIUS)
    GroupPosition.TOP -> RoundedCornerShape(
        topStart = GROUP_OUTER_RADIUS, topEnd = GROUP_OUTER_RADIUS,
        bottomStart = GROUP_INNER_RADIUS, bottomEnd = GROUP_INNER_RADIUS,
    )
    GroupPosition.MIDDLE -> RoundedCornerShape(GROUP_INNER_RADIUS)
    GroupPosition.BOTTOM -> RoundedCornerShape(
        topStart = GROUP_INNER_RADIUS, topEnd = GROUP_INNER_RADIUS,
        bottomStart = GROUP_OUTER_RADIUS, bottomEnd = GROUP_OUTER_RADIUS,
    )
}

/** Wraps a fixed list of settings rows and assigns each one its
 *  GroupPosition automatically — SINGLE for a lone row, TOP/BOTTOM for the
 *  ends of a longer group, MIDDLE for everything between. Rows are stacked
 *  with a tiny GROUP_GAP rather than normal item spacing, so the group
 *  reads as one connected surface with rows peeking through a hairline gap
 *  rather than a list of separate cards. */
@Composable
private fun SettingsGroup(rowCount: Int, content: @Composable (index: Int, position: GroupPosition) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(GROUP_GAP)) {
        for (i in 0 until rowCount) {
            val position = when {
                rowCount == 1 -> GroupPosition.SINGLE
                i == 0 -> GroupPosition.TOP
                i == rowCount - 1 -> GroupPosition.BOTTOM
                else -> GroupPosition.MIDDLE
            }
            content(i, position)
        }
    }
}

/**
 * Faithful port of settings.js (par 8): Last.fm account management, appearance
 * (AMOLED / Dynamic Color / Monochrome / accent presets / custom color
 * wheel), iTunes/ListenBrainz artwork toggles, data management (clear
 * recommendation exclusions, clear all data), backup & restore, and app info.
 *
 * Visuals only: restyled into a Material 3 Expressive presentation
 * (larger touch targets, per-row cards, tonal icon badges, spring-based
 * press feedback). Every setting, callback, and piece of state below is
 * unchanged from the original implementation.
 */
private enum class SettingsCategory(val title: String, val icon: ImageVector) {
    AUDIO("Sound & Hi-Res", Icons.Filled.VolumeUp),
    APPEARANCE("Glass & Style", Icons.Filled.Palette),
    CLOUD("Cloud & Sync", Icons.Filled.CloudSync),
    STORAGE("Offline Listening", Icons.Filled.CloudDownload),
    SYSTEM("System & About", Icons.Filled.Info),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: (() -> Unit)? = null,
    onLoggedOut: () -> Unit = {},
    onOpenChooseApps: () -> Unit = {},
    onOpenDownloads: () -> Unit = {},
    onOpenExcludedSongs: () -> Unit = {},
    onOpenYouTubeImport: () -> Unit = {},
    onOpenYouTubeLogin: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val session by viewModel.session.collectAsStateWithLifecycle()
    val theme by viewModel.theme.collectAsStateWithLifecycle()
    val misc by viewModel.misc.collectAsStateWithLifecycle()
    val scrobbler by viewModel.scrobbler.collectAsStateWithLifecycle()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val downloadCount by viewModel.downloadCount.collectAsStateWithLifecycle()
    val downloadTotalBytes by viewModel.downloadTotalBytes.collectAsStateWithLifecycle()
    val ytConnection by viewModel.ytConnection.collectAsStateWithLifecycle()
    val ytSyncEnabled by viewModel.ytSyncEnabled.collectAsStateWithLifecycle()
    val ytHistorySyncEnabled by viewModel.ytHistorySyncEnabled.collectAsStateWithLifecycle()
    val ytSyncState by viewModel.ytSyncState.collectAsStateWithLifecycle()
    val ytLastSyncAt by viewModel.ytLastSyncAt.collectAsStateWithLifecycle()
    val syncedPlaylistIds by viewModel.syncedPlaylistIds.collectAsStateWithLifecycle()
    val allPlaylists by viewModel.allPlaylists.collectAsStateWithLifecycle()
    val ytAccountPlaylists by viewModel.ytAccountPlaylists.collectAsStateWithLifecycle()
    val hiddenYtLibraryPlaylistIds by viewModel.hiddenYtLibraryPlaylistIds.collectAsStateWithLifecycle()
    val eq by viewModel.equalizer.collectAsStateWithLifecycle()
    val updateInfo by viewModel.updateInfo.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showQualityDialog by remember { mutableStateOf(false) }
    var showDownloadQualityDialog by remember { mutableStateOf(false) }
    var showEqSheet by remember { mutableStateOf(false) }
    var showLyricsAnimationSheet by remember { mutableStateOf(false) }
    var showSyncPlaylistsSheet by remember { mutableStateOf(false) }
    var showYtLibraryVisibilitySheet by remember { mutableStateOf(false) }
    var showYtDisconnectConfirm by remember { mutableStateOf(false) }

    // Sends the user to Android's own Notification Listener access screen
    // — the one permission this feature needs that the app can never grant
    // itself, only deep-link to. There's no reliable "is it already
    // granted for THIS app" API pre-33 short of parsing a settings string,
    // so this always opens the picker rather than guessing; picking
    // TuneGlass again there if it's already on is harmless.
    fun openNotificationAccessSettings() {
        val opened = startActivitySafely(
            context,
            Intent(android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS),
        ) || startActivitySafely(context, Intent(android.provider.Settings.ACTION_SETTINGS))
        if (!opened) viewModel.showToast("Android Settings is unavailable on this ROM")
    }

    // "*/*" rather than "application/json": many document providers (Drive,
    // Downloads, some file managers) report a .json file as
    // application/octet-stream or text/plain, and GetContent's mime filter
    // hides anything that doesn't match — the user's own backup file would
    // silently not show up. Real validation happens right after the file is
    // read (stagePendingRestore), so being permissive here is safe.
    val restoreLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            viewModel.handleRestorePicked(uri)
        }
    }

    val csvPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            viewModel.handleCsvPicked(uri)
        }
    }

    var showYouTubeImportSheet by remember { mutableStateOf(false) }
    var showSpotifyImportSheet by remember { mutableStateOf(false) }

    // Lets the user pick exactly where the backup file is saved (SAF), so
    // it's guaranteed to be somewhere restoreLauncher's picker can browse
    // back to later — unlike a silent write into app-private storage.
    val backupLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) viewModel.exportBackup(uri, appVersionName(context))
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .adaptiveContentWidth(maxWidth = 760.dp),
        ) {
        // ── Premium Settings Header ──────────────────────────────────────────
        ExpressiveHeader(title = "Control Center", onBack = onBack)

        // ── Fluid Morphism Category Selector Dock ───────────────────────
        var activeCategory by remember { mutableStateOf(SettingsCategory.AUDIO) }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SettingsCategory.entries.forEach { cat ->
                val selected = cat == activeCategory
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.65f),
                    border = BorderStroke(
                        1.dp,
                        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(22.dp))
                        .clickable { activeCategory = cat },
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = cat.icon,
                            contentDescription = cat.title,
                            tint = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            text = cat.title,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        // ── Active Category Surface (Structured Lens View) ───────────────
        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 12.dp,
                bottom = FloatingNavDefaults.contentBottomPadding(),
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.safeHorizontalContentPadding(),
        ) {
            when (activeCategory) {
                SettingsCategory.AUDIO -> {
                    // Audio Engine Bento summary
                    item {
                        val audioQualityLabel = when (misc.losslessQuality) {
                            27 -> "Hi-Res 192k"
                            7 -> "Hi-Res 96k"
                            6 -> "CD Lossless"
                            5 -> "320 kbps"
                            else -> "HD Audio"
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            SettingsBentoTile(
                                icon = Icons.Filled.GraphicEq,
                                iconContainer = MaterialTheme.colorScheme.tertiaryContainer,
                                iconTint = MaterialTheme.colorScheme.tertiary,
                                title = "Equalizer",
                                subtitle = if (eq.enabled) eq.presetName else "15-Band Inactive",
                                tag = if (eq.enabled) "ON" else "OFF",
                                tagColor = if (eq.enabled) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline,
                                onClick = { showEqSheet = true },
                                modifier = Modifier.weight(1f),
                            )
                            SettingsBentoTile(
                                icon = Icons.Filled.HighQuality,
                                iconContainer = MaterialTheme.colorScheme.primaryContainer,
                                iconTint = MaterialTheme.colorScheme.primary,
                                title = "Audio Engine",
                                subtitle = audioQualityLabel,
                                tag = "Hi-Res",
                                tagColor = MaterialTheme.colorScheme.primary,
                                onClick = { showQualityDialog = true },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            NeonSectionHeader("Sound & Audio Master", Icons.Filled.VolumeUp)
                            val qualitySubtitle = when (misc.losslessQuality) {
                                27 -> "Max (Up to 24-bit / 192 kHz)"
                                7 -> "Hi-Res (24-bit / 96 kHz)"
                                6 -> "CD Lossless (16-bit / 44.1 kHz FLAC)"
                                5 -> "Standard (320 kbps MP3)"
                                else -> "HD Stream (Opus / AAC)"
                            }
                            SettingsGroup(rowCount = 4) { index, position ->
                                when (index) {
                                    0 -> SettingsActionCard(
                                        icon = Icons.Filled.HighQuality,
                                        iconContainer = MaterialTheme.colorScheme.primaryContainer,
                                        iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        title = "Hi-Res Streaming Quality",
                                        subtitle = "$qualitySubtitle • Native master",
                                        onClick = { showQualityDialog = true },
                                        position = position,
                                    )
                                    1 -> SettingsToggleCard(
                                        icon = Icons.Filled.AutoAwesome,
                                        iconContainer = MaterialTheme.colorScheme.primaryContainer,
                                        iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        title = "Studio Master Clarity",
                                        subtitle = if (misc.isStudioMasterClarityEnabled) "Acoustic stage enhancement active" else "Natural unprocessed output",
                                        checked = misc.isStudioMasterClarityEnabled,
                                        onCheckedChange = viewModel::setStudioMasterClarity,
                                        position = position,
                                    )
                                    2 -> SettingsToggleCard(
                                        icon = Icons.Filled.Tune,
                                        iconContainer = MaterialTheme.colorScheme.secondaryContainer,
                                        iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
                                        title = "Bit-Perfect Mode (Direct Passthrough)",
                                        subtitle = if (misc.isBitPerfectEnabled) "Direct bit-exact audio (bypasses DSP)" else "Bypass all DSP for bit-exact reproduction",
                                        checked = misc.isBitPerfectEnabled,
                                        onCheckedChange = viewModel::setBitPerfectEnabled,
                                        position = position,
                                    )
                                    3 -> SettingsToggleCard(
                                        icon = Icons.Filled.GraphicEq,
                                        iconContainer = MaterialTheme.colorScheme.tertiaryContainer,
                                        iconTint = MaterialTheme.colorScheme.onTertiaryContainer,
                                        title = "Smooth Playback Crossfade",
                                        subtitle = if (misc.crossfadeEnabled) "Blends track transitions • ${misc.crossfadeSeconds}s" else "Gapless track transition",
                                        checked = misc.crossfadeEnabled,
                                        onCheckedChange = { enabled ->
                                            viewModel.setCrossfadeEnabled(enabled)
                                            if (enabled) {
                                                android.widget.Toast.makeText(
                                                    context,
                                                    "Crossfade will apply on next song",
                                                    android.widget.Toast.LENGTH_SHORT,
                                                ).show()
                                            }
                                        },
                                        position = position,
                                    )
                                }
                            }
                            if (misc.crossfadeEnabled) {
                                Spacer(Modifier.height(4.dp))
                                CrossfadeDurationRow(
                                    seconds = misc.crossfadeSeconds,
                                    onSecondsChange = viewModel::setCrossfadeSeconds,
                                    position = GroupPosition.SINGLE,
                                )
                            }
                        }
                    }
                }

                SettingsCategory.APPEARANCE -> {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            NeonSectionHeader("Glass Morphism & Atmosphere", Icons.Filled.Palette)
                            SettingsGroup(rowCount = 4) { index, position ->
                                when (index) {
                                    0 -> SettingsToggleCard(
                                        icon = Icons.Filled.BubbleChart,
                                        iconContainer = MaterialTheme.colorScheme.primaryContainer,
                                        iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        title = "Liquid Glass Morphism",
                                        subtitle = "Translucent frosted substrate & refractive lens borders",
                                        checked = theme?.liquidGlass ?: false,
                                        onCheckedChange = viewModel::setLiquidGlass,
                                        position = position,
                                    )
                                    1 -> SettingsToggleCard(
                                        icon = Icons.Filled.Contrast,
                                        iconContainer = MaterialTheme.colorScheme.tertiaryContainer,
                                        iconTint = MaterialTheme.colorScheme.onTertiaryContainer,
                                        title = "AMOLED Obsidian Mode",
                                        subtitle = "Deep true-black obsidian substrate for OLED screens",
                                        checked = theme?.amoled ?: false,
                                        onCheckedChange = viewModel::setAmoled,
                                        position = position,
                                    )
                                    2 -> SettingsToggleCard(
                                        icon = Icons.Filled.Album,
                                        iconContainer = MaterialTheme.colorScheme.secondaryContainer,
                                        iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
                                        title = "Dynamic Artwork Atmosphere",
                                        subtitle = "Fluidly adapt glow and UI colors from current track",
                                        checked = misc.dynamicNowPlayingEnabled,
                                        onCheckedChange = viewModel::setDynamicNowPlaying,
                                        position = position,
                                    )
                                    3 -> SettingsActionCard(
                                        icon = Icons.Filled.Lyrics,
                                        iconContainer = MaterialTheme.colorScheme.secondaryContainer,
                                        iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
                                        title = "Lyrics Styling & Animation",
                                        subtitle = if (misc.lyricsUiVersion == LyricsUiVersion.MODERN) "Modern Kinetic Layout" else "${misc.lyricsAnimation.title} Mode",
                                        onClick = { showLyricsAnimationSheet = true },
                                        position = position,
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Column {
                            NeonSectionHeader("Accent Color & Iridescence", Icons.Filled.Colorize)
                            Spacer(Modifier.height(10.dp))
                            Card(
                                shape = CardOuterShape,
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                            ) {
                                Column(Modifier.padding(20.dp)) {
                                    AccentPresetGrid(
                                        currentMode = theme?.mode ?: AccentMode.MANUAL,
                                        selectedHex = theme?.accentColorHex,
                                        onPickPreset = { hex -> viewModel.setManualAccent(Color(android.graphics.Color.parseColor(hex))) },
                                        onPickMono = { viewModel.setAccentMode(AccentMode.MONOCHROME) },
                                        onPickCustom = viewModel::openColorWheel,
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            NeonSectionHeader("Music Bar & Poster Style", Icons.Filled.GraphicEq)
                            SettingsToggleCard(
                                icon = if (misc.fullScreenPoster) Icons.Filled.Fullscreen else Icons.Filled.FullscreenExit,
                                iconContainer = MaterialTheme.colorScheme.secondaryContainer,
                                iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
                                title = "Full Screen Poster",
                                subtitle = if (misc.fullScreenPoster) "Full screen poster enabled" else "Normal poster enabled",
                                checked = misc.fullScreenPoster,
                                onCheckedChange = viewModel::setFullScreenPoster,
                                position = GroupPosition.TOP,
                            )
                            SettingsToggleCard(
                                icon = Icons.Filled.BubbleChart,
                                iconContainer = MaterialTheme.colorScheme.secondaryContainer,
                                iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
                                title = "Dynamic Island",
                                subtitle = if (misc.dynamicIslandEnabled) "Active on supported devices when app is closed (Oppo, Vivo, OnePlus, Xiaomi, Samsung)" else "Dynamic island disabled",
                                checked = misc.dynamicIslandEnabled,
                                onCheckedChange = viewModel::setDynamicIsland,
                                position = GroupPosition.BOTTOM,
                            )
                            Spacer(Modifier.height(4.dp))
                            PlayerBarStylePicker(
                                current = misc.musicBarStyle,
                                onSelect = viewModel::setMusicBarStyle,
                            )
                        }
                    }
                }

                SettingsCategory.CLOUD -> {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            NeonSectionHeader("Cloud Account & Synchronization", Icons.Filled.CloudSync)
                            val ytConnected = ytConnection.isConnected
                            if (ytConnected) {
                                YouTubeAccountRow(
                                    accountName = ytConnection.accountName,
                                    channelHandle = ytConnection.channelHandle,
                                    onDisconnect = { showYtDisconnectConfirm = true },
                                    position = GroupPosition.SINGLE,
                                )
                                Spacer(Modifier.height(6.dp))
                                SettingsGroup(rowCount = 2) { index, position ->
                                    when (index) {
                                        0 -> SettingsToggleCard(
                                            icon = Icons.Filled.CloudSync,
                                            iconContainer = MaterialTheme.colorScheme.secondaryContainer,
                                            iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
                                            title = "24/7 Cloud Playlist Mirroring",
                                            subtitle = if (ytSyncEnabled) "Playlists continuously mirror to YouTube Music" else "Sync is disabled",
                                            checked = ytSyncEnabled,
                                            onCheckedChange = viewModel::setYtSyncEnabled,
                                            position = position,
                                        )
                                        1 -> SettingsToggleCard(
                                            icon = Icons.Filled.History,
                                            iconContainer = MaterialTheme.colorScheme.secondaryContainer,
                                            iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
                                            title = "YouTube Watch History Sync",
                                            subtitle = "Record playback to your YouTube account history",
                                            checked = ytHistorySyncEnabled,
                                            onCheckedChange = viewModel::setYtHistorySyncEnabled,
                                            position = position,
                                        )
                                    }
                                }
                            } else {
                                SettingsActionCard(
                                    icon = Icons.Filled.CloudSync,
                                    iconContainer = MaterialTheme.colorScheme.primaryContainer,
                                    iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    title = "Connect YouTube Music",
                                    subtitle = "Sign in to synchronize playlists, favorites, and history",
                                    onClick = onOpenYouTubeLogin,
                                    position = GroupPosition.SINGLE,
                                )
                            }
                        }
                    }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            NeonSectionHeader("Library & Playlist Importers", Icons.Filled.FileDownload)
                            SettingsGroup(rowCount = 3) { index, position ->
                                when (index) {
                                    0 -> SettingsActionCard(
                                        icon = Icons.Filled.FileDownload,
                                        iconContainer = MaterialTheme.colorScheme.secondaryContainer,
                                        iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
                                        title = "Import Spotify Playlists",
                                        subtitle = "Transfer public Spotify tracks into TuneGlass",
                                        onClick = { showSpotifyImportSheet = true },
                                        position = position,
                                    )
                                    1 -> SettingsActionCard(
                                        icon = Icons.Filled.FileDownload,
                                        iconContainer = MaterialTheme.colorScheme.primaryContainer,
                                        iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        title = "Import YouTube Playlists",
                                        subtitle = "Import playlists via YouTube URL or ID",
                                        onClick = { showYouTubeImportSheet = true },
                                        position = position,
                                    )
                                    2 -> SettingsActionCard(
                                        icon = Icons.Filled.FormatListBulleted,
                                        iconContainer = MaterialTheme.colorScheme.tertiaryContainer,
                                        iconTint = MaterialTheme.colorScheme.onTertiaryContainer,
                                        title = "Import CSV / M3U Tracklist",
                                        subtitle = "Load playlist files from device storage",
                                        onClick = { csvPickerLauncher.launch(arrayOf("*/*")) },
                                        position = position,
                                    )
                                }
                            }
                        }
                    }
                }

                SettingsCategory.STORAGE -> {
                    item {
                        val mb = (downloadTotalBytes ?: 0L).toDouble() / (1024 * 1024)
                        val formattedStorage = if (mb >= 1000) "%.1f GB".format(mb / 1024) else "%.1f MB".format(mb)
                        val dlSubtitle = if (downloadCount > 0) "$downloadCount tracks • $formattedStorage" else "0 tracks saved"
                        val downloadQualitySubtitle = when (misc.downloadQuality) {
                            27 -> "Max (24-bit / 192 kHz FLAC)"
                            7 -> "Hi-Res (24-bit / 96 kHz FLAC)"
                            6 -> "CD Lossless (16-bit / 44.1 kHz FLAC)"
                            5 -> "Standard (320 kbps MP3)"
                            else -> "Standard (MP3/AAC)"
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            NeonSectionHeader("Offline Listening", Icons.Filled.CloudDownload)
                            SettingsBentoTile(
                                icon = Icons.Filled.CloudDownload,
                                iconContainer = MaterialTheme.colorScheme.primaryContainer,
                                iconTint = MaterialTheme.colorScheme.primary,
                                title = "Offline Listening Library",
                                subtitle = dlSubtitle,
                                tag = if (downloadCount > 0) "$downloadCount" else "Empty",
                                tagColor = MaterialTheme.colorScheme.primary,
                                onClick = onOpenDownloads,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            SettingsGroup(rowCount = 3) { index, position ->
                                when (index) {
                                    0 -> SettingsToggleCard(
                                        icon = Icons.Filled.AutoAwesome,
                                        iconContainer = MaterialTheme.colorScheme.primaryContainer,
                                        iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        title = "Auto-Save to Offline Listening",
                                        subtitle = if (misc.autoOfflineSaveEnabled) "Automatically save tracks to offline storage when playing" else "Disabled • Only save manually",
                                        checked = misc.autoOfflineSaveEnabled,
                                        onCheckedChange = viewModel::setAutoOfflineSaveEnabled,
                                        position = position,
                                    )
                                    1 -> SettingsActionCard(
                                        icon = Icons.Filled.HighQuality,
                                        iconContainer = MaterialTheme.colorScheme.secondaryContainer,
                                        iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
                                        title = "Offline Audio Quality Format",
                                        subtitle = "$downloadQualitySubtitle • Lossless audio",
                                        onClick = { showDownloadQualityDialog = true },
                                        position = position,
                                    )
                                    2 -> SettingsToggleCard(
                                        icon = Icons.Filled.Lyrics,
                                        iconContainer = MaterialTheme.colorScheme.tertiaryContainer,
                                        iconTint = MaterialTheme.colorScheme.onTertiaryContainer,
                                        title = "Save Synced Lyrics (.LRC)",
                                        subtitle = "Embed timed lyrics into offline audio files",
                                        checked = misc.downloadLyrics,
                                        onCheckedChange = viewModel::setDownloadLyrics,
                                        position = position,
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            NeonSectionHeader("Backup & Data Management", Icons.Filled.Backup)
                            SettingsGroup(rowCount = 3) { index, position ->
                                when (index) {
                                    0 -> SettingsActionCard(
                                        icon = Icons.Filled.CloudDownload,
                                        iconContainer = MaterialTheme.colorScheme.primaryContainer,
                                        iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        title = "Export Library Backup",
                                        subtitle = "Save all playlists, settings, and favorites to JSON",
                                        onClick = { backupLauncher.launch("tuneglass-backup-${System.currentTimeMillis()}.json") },
                                        position = position,
                                    )
                                    1 -> SettingsActionCard(
                                        icon = Icons.Filled.Backup,
                                        iconContainer = MaterialTheme.colorScheme.secondaryContainer,
                                        iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
                                        title = "Restore Library Backup",
                                        subtitle = "Restore playlists and library from a JSON backup",
                                        onClick = { restoreLauncher.launch(arrayOf("*/*")) },
                                        position = position,
                                    )
                                    2 -> SettingsActionCard(
                                        icon = Icons.Filled.RestartAlt,
                                        iconContainer = MaterialTheme.colorScheme.surfaceVariant,
                                        iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        title = "Clear Recommendation Exclusions",
                                        subtitle = "Reset skipped songs and exclusion filters",
                                        onClick = viewModel::clearRecommendationExclusions,
                                        position = position,
                                    )
                                }
                            }
                        }
                    }
                }

                SettingsCategory.SYSTEM -> {
                    item {
                        AccountCard(
                            isSignedIn = session.username.isNotBlank(),
                            username = session.username,
                            onLogOut = { viewModel.logOut(onLoggedOut) },
                        )
                    }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            NeonSectionHeader("About & System", Icons.Filled.Info)
                            if (updateInfo.isUpdateAvailable) {
                                Surface(
                                    shape = CardOuterShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shadowElevation = 3.dp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(CardOuterShape)
                                        .clickable { viewModel.downloadAndInstallUpdate(context) },
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(46.dp)
                                                .background(MaterialTheme.colorScheme.primary, CircleShape),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.CloudDownload,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onPrimary,
                                                modifier = Modifier.size(24.dp),
                                            )
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = if (updateInfo.isDownloading) "Downloading Update..." else "Update Available!",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            )
                                            Text(
                                                text = when {
                                                    updateInfo.isDownloading -> "${(updateInfo.downloadProgress * 100).toInt()}% • Tap to install when done"
                                                    else -> "Version ${updateInfo.latestVersion} is ready to install • Tap to update"
                                                },
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                                            )
                                        }
                                    }
                                }
                            }

                            SettingsGroup(rowCount = 3) { index, position ->
                                when (index) {
                                    0 -> SettingsActionCard(
                                        icon = if (updateInfo.isDownloading) Icons.Filled.CloudDownload else Icons.Filled.CloudSync,
                                        iconContainer = MaterialTheme.colorScheme.primaryContainer,
                                        iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        title = if (updateInfo.isDownloading) "Downloading Update..." else "Check for Updates",
                                        subtitle = when {
                                            updateInfo.isDownloading -> "${(updateInfo.downloadProgress * 100).toInt()}% • Downloading..."
                                            updateInfo.isChecking -> "Checking for latest updates..."
                                            updateInfo.isUpdateAvailable -> "Version ${updateInfo.latestVersion} available • Tap to install"
                                            else -> "TuneGlass v${appVersionName(context)} • Up to date"
                                        },
                                        onClick = {
                                            if (updateInfo.isUpdateAvailable) viewModel.downloadAndInstallUpdate(context)
                                            else viewModel.checkForUpdates()
                                        },
                                        position = position,
                                    )
                                    1 -> SettingsActionCard(
                                        icon = Icons.Filled.Info,
                                        iconContainer = MaterialTheme.colorScheme.secondaryContainer,
                                        iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
                                        title = "TuneGlass Music Player",
                                        subtitle = "v${appVersionName(context)} • Liquid Frequency Lens UI",
                                        onClick = {
                                            viewModel.showToast("TuneGlass v${appVersionName(context)} • Lens Morphism UI")
                                        },
                                        position = position,
                                    )
                                    2 -> SettingsActionCard(
                                        icon = Icons.Filled.Delete,
                                        iconContainer = MaterialTheme.colorScheme.errorContainer,
                                        iconTint = MaterialTheme.colorScheme.onErrorContainer,
                                        title = "Clear All App Data",
                                        subtitle = "Erase cache, credentials, and local saved data",
                                        onClick = viewModel::requestClearAllData,
                                        position = position,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    } // Closes Column
    } // Closes Box

    // -- Custom color wheel dialog (par 8.4) --
    if (state.showColorWheel) {
        ColorWheelSheet(onDismiss = viewModel::dismissColorWheel, onApply = viewModel::applyCustomColor)
    }

    // -- Clear-all-data confirm --
    if (state.showClearAllConfirm) {
        AlertDialog(
            onDismissRequest = viewModel::dismissClearAllConfirm,
            title = { Text("Clear all data?") },
            text = { Text("This removes your credentials, playlists, and cached data. This can't be undone.") },
            confirmButton = { TextButton(onClick = { viewModel.confirmClearAllData(onLoggedOut) }) { Text("Clear Everything") } },
            dismissButton = { TextButton(onClick = viewModel::dismissClearAllConfirm) { Text("Cancel") } },
        )
    }

    // -- YouTube Music disconnect confirm --
    if (showYtDisconnectConfirm) {
        AlertDialog(
            onDismissRequest = { showYtDisconnectConfirm = false },
            title = { Text("Disconnect Cloud Account?") },
            text = { Text("Playlists already mirrored to your account stay there, but TuneGlass stops syncing and forgets the connection.") },
            confirmButton = {
                TextButton(onClick = {
                    showYtDisconnectConfirm = false
                    viewModel.disconnectYouTube()
                }) { Text("Disconnect") }
            },
            dismissButton = {
                TextButton(onClick = { showYtDisconnectConfirm = false }) { Text("Cancel") }
            },
        )
    }

    // -- Restore confirm --
    if (state.showRestoreConfirm) {        val isPlaylistMirror = state.pendingRestoreKind == PendingRestoreKind.PLAYLIST_MIRROR
        AlertDialog(
            onDismissRequest = viewModel::dismissRestoreConfirm,
            title = { Text(if (isPlaylistMirror) "Sync playlist JSON?" else "Restore backup?") },
            text = {
                Text(
                    if (isPlaylistMirror) {
                        "This will merge ${state.pendingRestorePlaylistCount ?: 0} playlist(s) from the local JSON file and reconnect automatic syncing."
                    } else {
                        "This will replace your current data with ${state.pendingRestorePlaylistCount ?: 0} playlist(s) and all settings from the backup file."
                    },
                )
            },
            confirmButton = { TextButton(onClick = { viewModel.confirmRestore(onBack ?: {}) }) { Text(if (isPlaylistMirror) "Sync" else "Restore") } },
            dismissButton = { TextButton(onClick = viewModel::dismissRestoreConfirm) { Text("Cancel") } },
        )
    }

    // -- Enable Scrobbling password dialog --
    if (state.showSessionKeyDialog) {
        var password by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = viewModel::dismissSessionKeyDialog,
            title = { Text("Enable scrobbling") },
            text = {
                Column {
                    Text(
                        "Last.fm only allows scrobbling through a signed session, and the only way to get one without a browser is with your password. It's sent once, directly to Last.fm over HTTPS, and never stored.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(Modifier.height(16.dp))
                    androidx.compose.material3.OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Last.fm password") },
                        singleLine = true,
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        isError = state.sessionKeyError != null,
                        supportingText = state.sessionKeyError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.submitPassword(password) },
                    enabled = !state.sessionKeyLoading,
                ) {
                    if (state.sessionKeyLoading) {
                        com.listenit.music.ui.common.ExpressiveInlineLoadingIndicator(size = 18.dp)
                    } else {
                        Text("Enable")
                    }
                }
            },
            dismissButton = { TextButton(onClick = viewModel::dismissSessionKeyDialog) { Text("Cancel") } },
        )
    }

    // -- Experimental 15-band equalizer --
    if (showEqSheet) {
        EqualizerSheet(
            eq = eq,
            onDismiss = { showEqSheet = false },
            onSetEnabled = viewModel::setEqualizerEnabled,
            onPickPreset = viewModel::applyEqPreset,
            onBandPreview = viewModel::previewEqBandGain,
            onBandChange = viewModel::setEqBandGain,
        )
    }

    // -- Experimental Lyrics Animation Sheet --
    if (showLyricsAnimationSheet) {
        LyricsAnimationSheet(
            version = misc.lyricsUiVersion,
            wordByWord = misc.wordByWordLyrics,
            onWordByWordChange = viewModel::setWordByWordLyrics,
            onSelectVersion = viewModel::setLyricsUiVersion,
            current = misc.lyricsAnimation,
            onSelect = {
                viewModel.setLyricsAnimation(it)
                showLyricsAnimationSheet = false
            },
            onDismiss = { showLyricsAnimationSheet = false },
        )
    }

    // -- Selective Playlist Sync sheet --
    if (showSyncPlaylistsSheet) {
        SyncPlaylistsSheet(
            playlists = allPlaylists,
            syncedIds = syncedPlaylistIds,
            onToggleSync = viewModel::togglePlaylistSync,
            onSelectAll = viewModel::selectAllPlaylistsForSync,
            onDismiss = { showSyncPlaylistsSheet = false },
        )
    }

    if (showYtLibraryVisibilitySheet) {
        YouTubeLibraryVisibilitySheet(
            playlists = ytAccountPlaylists,
            hiddenIds = hiddenYtLibraryPlaylistIds,
            onSetVisible = viewModel::setYtLibraryPlaylistVisible,
            onSetAllVisible = viewModel::setAllYtLibraryPlaylistsVisible,
            onDismiss = { showYtLibraryVisibilitySheet = false },
        )
    }

    if (showQualityDialog) {
        val tiers = listOf(
            Triple(27, "Max Quality", "Up to 24-bit / 192 kHz • Lossless Studio FLAC" to "24-BIT / 192k"),
            Triple(7, "Hi-Res Audio", "24-bit / 96 kHz • Lossless Studio FLAC" to "24-BIT / 96k"),
            Triple(6, "CD Lossless", "16-bit / 44.1 kHz • Lossless CD FLAC" to "16-BIT / 44.1k"),
            Triple(5, "Standard Quality", "320 kbps • MP3 (Data Saver)" to "320 kbps"),
            Triple(-1, "Cloud Stream", "128-256 kbps • YouTube Music AAC / Opus stream" to "YOUTUBE"),
        )
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

        ModalBottomSheet(
            onDismissRequest = { showQualityDialog = false },
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            dragHandle = {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                    modifier = Modifier
                        .padding(top = 12.dp, bottom = 8.dp)
                        .size(width = 36.dp, height = 4.dp),
                ) {}
            },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .adaptiveContentWidth(maxWidth = 640.dp)
                    .align(Alignment.CenterHorizontally)
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 24.dp + safeDrawingBottomPadding()),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Filled.HighQuality,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(
                            "Streaming Quality",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            "Select preferred audio resolution & bit depth",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Text(
                    "Lossless streams provide bit-exact studio quality (FLAC/MP3). If your chosen quality is unavailable, TuneGlass automatically streams the higher quality tier above it (or falls back to Standard Stream if unavailable in lossless).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    tiers.forEach { (qualityId, title, meta) ->
                        val (subtitle, badge) = meta
                        val isSelected = misc.losslessQuality == qualityId
                        Surface(
                            onClick = {
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                viewModel.setLosslessQuality(qualityId)
                                showQualityDialog = false
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                            shadowElevation = if (isSelected) 3.dp else 0.dp,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            title,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest,
                                        ) {
                                            Text(
                                                badge,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }

                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            Icons.Filled.Check,
                                            contentDescription = "Selected",
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(18.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDownloadQualityDialog) {
        val downloadTiers = listOf(
            Triple(27, "Max Quality", "Up to 24-bit / 192 kHz • Studio Master FLAC" to "24-BIT / 192k"),
            Triple(7, "Hi-Res Audio", "24-bit / 96 kHz • Studio FLAC" to "24-BIT / 96k"),
            Triple(6, "CD Lossless", "16-bit / 44.1 kHz • Bit-Exact CD FLAC" to "16-BIT / 44.1k"),
            Triple(5, "Standard Quality", "320 kbps • High-Bitrate MP3" to "320 kbps"),
            Triple(-1, "Cloud Stream", "128-256 kbps • YouTube Music AAC / Opus stream" to "YOUTUBE"),
        )
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

        ModalBottomSheet(
            onDismissRequest = { showDownloadQualityDialog = false },
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            dragHandle = {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                    modifier = Modifier
                        .padding(top = 12.dp, bottom = 8.dp)
                        .size(width = 36.dp, height = 4.dp),
                ) {}
            },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .adaptiveContentWidth(maxWidth = 640.dp)
                    .align(Alignment.CenterHorizontally)
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 24.dp + safeDrawingBottomPadding()),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Filled.CloudDownload,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(
                            "Download Quality",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            "Select offline audio resolution & bit depth",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Text(
                    "Lossless downloads provide bit-exact studio quality (FLAC/MP3). If your chosen quality is unavailable, TuneGlass automatically downloads the higher quality tier above it (or falls back to Standard Stream if unavailable in lossless).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    downloadTiers.forEach { (qualityId, title, meta) ->
                        val (subtitle, badge) = meta
                        val isSelected = misc.downloadQuality == qualityId
                        Surface(
                            onClick = {
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                viewModel.setDownloadQuality(qualityId)
                                showDownloadQualityDialog = false
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                            shadowElevation = if (isSelected) 3.dp else 0.dp,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            title,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest,
                                        ) {
                                            Text(
                                                badge,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }

                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            Icons.Filled.Check,
                                            contentDescription = "Selected",
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(18.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showYouTubeImportSheet) {
        YouTubeImportSheet(
            onDismiss = { showYouTubeImportSheet = false },
            innerTube = viewModel.innerTube,
            importManager = viewModel.playlistImportManager,
            onImportSuccess = { saved ->
                viewModel.showToast("Imported \"${saved.title}\" (${saved.tracks.size} tracks)")
            },
        )
    }

    if (showSpotifyImportSheet) {
        SpotifyImportSheet(
            onDismiss = { showSpotifyImportSheet = false },
            spotifyFetcher = viewModel.spotifyPlaylistFetcher,
            authManager = viewModel.spotifyAuthManager,
            onImportSuccess = { saved ->
                viewModel.showToast("Imported \"${saved.title}\" (${saved.tracks.size} tracks)")
            },
        )
    }

    state.toastMessage?.let { msg ->
        LaunchedEffect(msg) {
            kotlinx.coroutines.delay(3000)
            viewModel.dismissToast()
        }
        Box(
            Modifier
                .fillMaxSize()
                .safeHorizontalContentPadding()
                .padding(
                    bottom = 24.dp +
                        LocalMiniPlayerScrollClearance.current +
                        safeDrawingBottomPadding(),
                ),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Surface(shape = ExpressivePillShape, color = MaterialTheme.colorScheme.inverseSurface, tonalElevation = 6.dp) {
                Text(msg, color = MaterialTheme.colorScheme.inverseOnSurface, modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp))
            }
        }
    }
}

private fun appVersionName(context: android.content.Context): String = try {
    context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0.0"
} catch (error: Exception) {
    "1.0.0"
} catch (error: LinkageError) {
    "1.0.0"
}

/** Small tap-scale used across the row-style cards on this screen for a
 *  softer, springier press response than the plain ripple alone gives. */
@Composable
private fun rememberPressScale(interactionSource: MutableInteractionSource): Float {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "pressScale",
    )
    return scale
}

// ── NeonSectionHeader ─────────────────────────────────────────────────────
/** Premium section header with gradient icon badge and an accent rule underline. */
@Composable
private fun NeonSectionHeader(title: String, icon: ImageVector) {
    val primary = MaterialTheme.colorScheme.primary
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 2.dp),
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.72f),
                        )
                    )
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(18.dp),
            )
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(4.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(1.5.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.horizontalGradient(
                            listOf(primary, primary.copy(alpha = 0f)),
                        )
                    )
            )
        }
    }
}

// ── PlayerBarStylePicker ───────────────────────────────────────────────────
/** 4-design music bar style picker with live visual preview and spring selection. */
@Composable
private fun PlayerBarStylePicker(
    current: MusicBarStyle,
    onSelect: (MusicBarStyle) -> Unit,
) {
    val styles = MusicBarStyle.entries
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            styles.take(2).forEach { style ->
                PlayerBarStyleCard(
                    style = style,
                    selected = style == current,
                    onClick = { onSelect(style) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            styles.drop(2).forEach { style ->
                PlayerBarStyleCard(
                    style = style,
                    selected = style == current,
                    onClick = { onSelect(style) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun PlayerBarStyleCard(
    style: MusicBarStyle,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.03f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "barCardScale",
    )
    val borderAlpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "barCardBorderAlpha",
    )
    val liquidGlass = LocalLiquidGlass.current
    val shape = RoundedCornerShape(18.dp)
    val primary = MaterialTheme.colorScheme.primary

    Card(
        onClick = {
            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
            onClick()
        },
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = liquidGlassContainerColor(
                if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = if (liquidGlass) 0.85f else 1f)
                else MaterialTheme.colorScheme.surfaceContainerHigh
            )
        ),
        border = BorderStroke(2.dp, primary.copy(alpha = borderAlpha)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        interactionSource = interactionSource,
        modifier = modifier
            .scale(scale)
            .liquidGlassChrome(shape, liquidGlass),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Mini preview of the bar style
            PlayerBarMiniPreview(style = style, selected = selected)

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = style.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = style.description,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    )
                }
                androidx.compose.animation.AnimatedVisibility(
                    visible = selected,
                    enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.scaleIn(),
                    exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.scaleOut(),
                ) {
                    Box(
                        Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(12.dp),
                        )
                    }
                }
            }
        }
    }
}

/**
 * Mini non-interactive visual preview of each PlayerBarStyle.
 * Drawn entirely with Canvas for maximum fidelity with zero runtime overhead.
 */
@Composable
private fun PlayerBarMiniPreview(style: MusicBarStyle, selected: Boolean) {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer

    // Animated progress fraction
    val infiniteTransition = rememberInfiniteTransition(label = "barPreviewAnim")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.78f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = androidx.compose.animation.core.FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "previewProgress",
    )
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "wavePhase",
    )

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(surfaceVariant.copy(alpha = 0.5f)),
    ) {
        val w = size.width
        val h = size.height
        val cx = h / 2f
        val progressPx = progress * w

        when (style) {
            MusicBarStyle.LENS -> {
                // Hairline track
                drawLine(
                    color = primary.copy(alpha = 0.2f),
                    start = Offset(0f, cx),
                    end = Offset(w, cx),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round,
                )
                // Filled progress
                drawLine(
                    color = primary,
                    start = Offset(0f, cx),
                    end = Offset(progressPx, cx),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round,
                )
                // Ring thumb
                drawCircle(
                    color = primary,
                    radius = 5.dp.toPx(),
                    center = Offset(progressPx, cx),
                    style = Stroke(width = 2.dp.toPx()),
                )
                drawCircle(
                    color = primary.copy(alpha = 0.2f),
                    radius = 8.dp.toPx(),
                    center = Offset(progressPx, cx),
                )
            }
            MusicBarStyle.NEON -> {
                // Glow track
                val glowBrush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF00E5FF).copy(alpha = 0.9f),
                        Color(0xFF7C4DFF).copy(alpha = 0.9f),
                        Color(0xFF00E5FF).copy(alpha = 0.4f),
                    ),
                    startX = 0f,
                    endX = progressPx,
                )
                drawLine(
                    brush = glowBrush,
                    start = Offset(0f, cx),
                    end = Offset(progressPx, cx),
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round,
                )
                drawLine(
                    color = primary.copy(alpha = 0.15f),
                    start = Offset(progressPx, cx),
                    end = Offset(w, cx),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round,
                )
                // Electric pulse thumb
                drawCircle(
                    color = Color(0xFF00E5FF).copy(alpha = 0.3f),
                    radius = 9.dp.toPx(),
                    center = Offset(progressPx, cx),
                )
                drawCircle(
                    color = Color(0xFF00E5FF),
                    radius = 4.dp.toPx(),
                    center = Offset(progressPx, cx),
                )
            }
            MusicBarStyle.VINYL -> {
                // Wavy groove track
                val path = Path()
                path.moveTo(0f, cx)
                val segments = 40
                for (i in 0..segments) {
                    val x = w * i / segments
                    val amplitude = if (x <= progressPx) 4.dp.toPx() else 2.dp.toPx()
                    val y = cx + (amplitude * kotlin.math.sin((wavePhase + x * 0.04f).toDouble())).toFloat()
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(
                    path = path,
                    color = primary.copy(alpha = 0.35f),
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round),
                )
                val filledPath = Path()
                for (i in 0..segments) {
                    val x = w * i / segments
                    if (x > progressPx) break
                    val y = cx + (4.dp.toPx() * kotlin.math.sin((wavePhase + x * 0.04f).toDouble())).toFloat()
                    if (i == 0) filledPath.moveTo(x, y) else filledPath.lineTo(x, y)
                }
                drawPath(
                    path = filledPath,
                    color = primary,
                    style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round),
                )
                // Warm thumb
                drawCircle(
                    color = secondary.copy(alpha = 0.25f),
                    radius = 8.dp.toPx(),
                    center = Offset(progressPx, cx),
                )
                drawCircle(
                    color = secondary,
                    radius = 4.dp.toPx(),
                    center = Offset(progressPx, cx),
                )
            }
            MusicBarStyle.PILLBAR -> {
                val pillH = 8.dp.toPx()
                val pillR = pillH / 2f
                // Background pill
                drawRoundRect(
                    color = primary.copy(alpha = 0.15f),
                    topLeft = Offset(0f, cx - pillR),
                    size = Size(w, pillH),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(pillR),
                )
                // Filled progress pill
                if (progressPx > 0f) {
                    drawRoundRect(
                        brush = Brush.horizontalGradient(
                            listOf(primaryContainer, primary),
                            startX = 0f,
                            endX = progressPx,
                        ),
                        topLeft = Offset(0f, cx - pillR),
                        size = Size(progressPx, pillH),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(pillR),
                    )
                }
                // Capsule thumb
                val thumbW = 14.dp.toPx()
                val thumbH = 14.dp.toPx()
                drawRoundRect(
                    color = primary,
                    topLeft = Offset(progressPx - thumbW / 2f, cx - thumbH / 2f),
                    size = Size(thumbW, thumbH),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(thumbH / 2f),
                )
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp, bottom = 2.dp),
    )
}

@Composable
private fun IconBadge(icon: ImageVector, container: Color, tint: Color, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(44.dp)
            .clip(IconBadgeShape)
            .background(container),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun SettingsBentoTile(
    icon: ImageVector,
    iconContainer: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    tag: String? = null,
    tagColor: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val scale = rememberPressScale(interactionSource)
    val shape = RoundedCornerShape(20.dp)
    val liquidGlass = LocalLiquidGlass.current

    Card(
        onClick = onClick,
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = liquidGlassContainerColor(MaterialTheme.colorScheme.surfaceContainerHigh)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        interactionSource = interactionSource,
        modifier = modifier
            .scale(scale)
            .liquidGlassChrome(shape, liquidGlass),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(iconContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp),
                    )
                }
                if (!tag.isNullOrBlank()) {
                    Surface(
                        shape = ExpressivePillShape,
                        color = tagColor.copy(alpha = 0.15f),
                        border = BorderStroke(0.5.dp, tagColor.copy(alpha = 0.3f)),
                    ) {
                        Text(
                            tag,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = tagColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        )
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun SettingsToggleCard(
    icon: ImageVector,
    iconContainer: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    position: GroupPosition = GroupPosition.SINGLE,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val scale = rememberPressScale(interactionSource)
    val shape = groupShape(position)
    val liquidGlass = LocalLiquidGlass.current

    Card(
        onClick = { onCheckedChange(!checked) },
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = liquidGlassContainerColor(MaterialTheme.colorScheme.surfaceContainerHigh)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.12f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        interactionSource = interactionSource,
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .liquidGlassChrome(shape, liquidGlass),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconBadge(icon, iconContainer, iconTint)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.width(8.dp))
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                thumbContent = if (checked) {
                    {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier.size(SwitchDefaults.IconSize),
                        )
                    }
                } else null,
            )
        }
    }
}

/** The percent-of-track threshold before a scrobble is submitted — same
 *  idea as Pano Scrobbler's "Percent" slider, minus its separate parallel
 *  "Minutes" slider: Last.fm's own scrobble rule already caps the wait at
 *  4 minutes regardless of percent, so that second slider would only ever
 *  matter for tracks over 8 minutes long, a genuine edge case not worth
 *  the extra UI here. */
@Composable
private fun ScrobbleThresholdRow(percent: Int, onPercentChange: (Int) -> Unit, position: GroupPosition = GroupPosition.SINGLE) {
    var sliderValue by remember(percent) { mutableStateOf(percent.coerceIn(25, 90).toFloat()) }
    val shape = groupShape(position)
    val liquidGlass = LocalLiquidGlass.current
    Card(
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = liquidGlassContainerColor(MaterialTheme.colorScheme.surfaceContainerHigh)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .liquidGlassChrome(shape, liquidGlass),
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(Icons.Filled.Timer, MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("Scrobble after", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                    Text(
                        "${sliderValue.toInt()}% played (capped at 4 min)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Slider(
                value = sliderValue,
                onValueChange = { sliderValue = it },
                onValueChangeFinished = { onPercentChange(sliderValue.toInt()) },
                valueRange = 25f..90f,
                steps = 12,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun CrossfadeDurationRow(
    seconds: Int,
    onSecondsChange: (Int) -> Unit,
    position: GroupPosition = GroupPosition.SINGLE,
) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    var sliderValue by remember { mutableStateOf(seconds.coerceIn(1, 12).toFloat()) }
    var isDragging by remember { mutableStateOf(false) }
    LaunchedEffect(seconds) {
        if (!isDragging) sliderValue = seconds.coerceIn(1, 12).toFloat()
    }
    val shape = groupShape(position)
    val liquidGlass = LocalLiquidGlass.current

    val blendStyle = when (sliderValue.roundToInt()) {
        in 1..2 -> "Quick DJ overlap"
        in 3..5 -> "Standard smooth blend"
        in 6..8 -> "Long musical transition"
        else -> "Extended cinematic crossfade"
    }

    Card(
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = liquidGlassContainerColor(MaterialTheme.colorScheme.surfaceContainerHigh)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .liquidGlassChrome(shape, liquidGlass),
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(
                    Icons.Filled.Tune,
                    MaterialTheme.colorScheme.secondaryContainer,
                    MaterialTheme.colorScheme.onSecondaryContainer,
                )
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("Crossfade duration", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                    Text(
                        "${sliderValue.roundToInt()}s \u2022 $blendStyle",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Text(
                        "${sliderValue.roundToInt()}s",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
            }
            Slider(
                value = sliderValue,
                onValueChange = { value ->
                    isDragging = true
                    val rounded = value.roundToInt().coerceIn(1, 12)
                    if (rounded.toFloat() != sliderValue) {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                        sliderValue = rounded.toFloat()
                        onSecondsChange(rounded)
                    }
                },
                onValueChangeFinished = {
                    isDragging = false
                },
                valueRange = 1f..12f,
                steps = 10,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("1s (Tight)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                Text("6s", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                Text("12s (Long)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}

@Composable
private fun SettingsActionCard(
    icon: ImageVector,
    iconContainer: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    danger: Boolean = false,
    onClick: () -> Unit,
    position: GroupPosition = GroupPosition.SINGLE,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val scale = rememberPressScale(interactionSource)
    val titleColor = if (danger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
    val shape = groupShape(position)
    val liquidGlass = LocalLiquidGlass.current

    Card(
        onClick = onClick,
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = liquidGlassContainerColor(MaterialTheme.colorScheme.surfaceContainerHigh)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.12f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        interactionSource = interactionSource,
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .liquidGlassChrome(shape, liquidGlass),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconBadge(icon, iconContainer, iconTint)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, color = titleColor)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = if (danger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun YouTubeAccountRow(
    accountName: String,
    channelHandle: String?,
    onDisconnect: () -> Unit,
    position: GroupPosition,
) {
    val shape = groupShape(position)
    val liquidGlass = LocalLiquidGlass.current
    Card(
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = liquidGlassContainerColor(MaterialTheme.colorScheme.surfaceContainerHigh)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.12f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .liquidGlassChrome(shape, liquidGlass),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconBadge(
                Icons.Filled.CloudSync,
                MaterialTheme.colorScheme.primaryContainer,
                MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "Cloud Stream Account",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    accountName.ifBlank { "Connected" },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                )
                if (!channelHandle.isNullOrBlank()) {
                    Text(
                        channelHandle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            FilledTonalIconButton(
                onClick = onDisconnect,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                ),
            ) {
                Icon(Icons.Filled.Logout, contentDescription = "Disconnect")
            }
        }
    }
}

private fun lastSyncSuffix(lastSyncAtMillis: Long): String {
    if (lastSyncAtMillis <= 0L) return ""
    return " \u2022 synced ${relativeTime(lastSyncAtMillis)}"
}

private fun relativeTime(timestampMillis: Long): String {
    if (timestampMillis <= 0L) return "never"
    val delta = System.currentTimeMillis() - timestampMillis
    val minutes = delta / 60_000L
    return when {
        minutes < 1 -> "just now"
        minutes < 60 -> "${minutes}m ago"
        minutes < 60 * 24 -> "${minutes / 60}h ago"
        else -> "${minutes / (60 * 24)}d ago"
    }
}

@Composable
private fun AccountCard(
    isSignedIn: Boolean,
    username: String,
    onLogOut: () -> Unit,
) {
    var showLogoutConfirm by remember { mutableStateOf(false) }
    val liquidGlass = LocalLiquidGlass.current
    val shape = RoundedCornerShape(24.dp)

    Card(
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = liquidGlassContainerColor(MaterialTheme.colorScheme.surfaceContainerHigh)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)),
        modifier = Modifier
            .fillMaxWidth()
            .liquidGlassChrome(shape, liquidGlass)
            .animateContentSize(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.tertiary,
                            )
                        )
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    if (isSignedIn && username.isNotBlank()) username.take(1).uppercase() else "L",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge,
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Surface(
                    shape = ExpressivePillShape,
                    color = if (isSignedIn) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Text(
                        if (isSignedIn) "TuneGlass Account" else "Guest Mode",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = if (isSignedIn) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    if (username.isNotBlank()) username else "Listener",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Spacer(Modifier.width(8.dp))
            FilledTonalIconButton(
                onClick = { showLogoutConfirm = true },
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f),
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                ),
            ) {
                Icon(Icons.Filled.Logout, contentDescription = "Log out")
            }
        }
    }

    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text("Log out from TuneGlass?") },
            text = { Text("Your playlists and cached data will be kept.") },
            confirmButton = { TextButton(onClick = { showLogoutConfirm = false; onLogOut() }) { Text("Log Out") } },
            dismissButton = { TextButton(onClick = { showLogoutConfirm = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun AccentPresetGrid(
    currentMode: AccentMode,
    selectedHex: String?,
    onPickPreset: (String) -> Unit,
    onPickMono: () -> Unit,
    onPickCustom: () -> Unit,
) {
    // A preset only reads as "selected" while the user is actually in
    // manual mode — Dynamic/Monochrome shouldn't light up whichever preset
    // happens to hex-match by coincidence.
    fun isPresetSelected(hex: String) =
        currentMode == AccentMode.MANUAL && selectedHex?.equals(hex, ignoreCase = true) == true
    val customSelected = currentMode == AccentMode.MANUAL &&
        selectedHex != null &&
        ACCENT_PRESETS.none { it.hex.equals(selectedHex, ignoreCase = true) }
    val monoSelected = currentMode == AccentMode.MONOCHROME

    // One unified 4-column tile grid — six color presets plus Mono and
    // Custom as tiles of their own, not a separate row of pill buttons.
    Row(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxWidth()) {
        ACCENT_PRESETS.take(4).forEach { preset ->
            ColorTile(
                label = preset.name,
                selected = isPresetSelected(preset.hex),
                modifier = Modifier.weight(1f),
                onClick = { onPickPreset(preset.hex) },
            ) {
                PaletteTilePreview(preset.hex)
            }
        }
    }
    Spacer(Modifier.height(14.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxWidth()) {
        ACCENT_PRESETS.drop(4).forEach { preset ->
            ColorTile(
                label = preset.name,
                selected = isPresetSelected(preset.hex),
                modifier = Modifier.weight(1f),
                onClick = { onPickPreset(preset.hex) },
            ) {
                PaletteTilePreview(preset.hex)
            }
        }
        ColorTile(
            label = "Mono",
            selected = monoSelected,
            modifier = Modifier.weight(1f),
            onClick = onPickMono,
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.Contrast,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
        ColorTile(
            label = "Custom",
            selected = customSelected,
            modifier = Modifier.weight(1f),
            onClick = onPickCustom,
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        androidx.compose.ui.graphics.Brush.sweepGradient(
                            listOf(Color(0xFFE03030), Color(0xFFE0A030), Color(0xFF6B9E6B), Color(0xFF2196C6), Color(0xFF7C4DFF), Color(0xFFE03030)),
                        ),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.Colorize,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
    }
}

/**
 * Four coordinated shades derived from one preset hex via HSV — Dark,
 * Medium, Light, and a punchier Accent tone — used to render a real
 * multi-tone palette preview inside each Accent tile instead of one flat
 * swatch. Order returned: [dark, medium, light, accent].
 *
 * Tuned for a muted, Material You / Monet feel rather than raw HSV
 * vibrance: saturation is capped well below 100% even for the "accent"
 * shade (Monet's HCT-derived tonal palettes rarely reach full chroma —
 * that's what read as neon here), and the value range is narrower so
 * "dark" and "light" stay closer to the preset's own character instead of
 * swinging to near-black/near-white extremes.
 */
private fun accentShades(hex: String): List<Color> {
    val argb = android.graphics.Color.parseColor(hex)
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(argb, hsv)
    val (h, s, v) = hsv
    val mutedBase = s * 0.72f // the single biggest lever for "less neon"

    fun shade(saturation: Float, value: Float): Color {
        val arr = floatArrayOf(h, saturation.coerceIn(0f, 0.82f), value.coerceIn(0.2f, 0.92f))
        return Color(android.graphics.Color.HSVToColor(arr))
    }

    return listOf(
        shade(mutedBase.coerceAtLeast(0.42f), v * 0.62f), // dark
        shade(mutedBase, v * 0.80f), // medium — closest to the preset's own tone
        shade((mutedBase * 0.55f), (v + (1f - v) * 0.45f).coerceAtLeast(0.78f)), // light
        shade((mutedBase * 1.15f), (v * 0.95f)), // accent — a touch richer, never maxed out
    )
}

/** Renders a preset's four shades as a 2x2 block grid filling the tile,
 *  so selecting a preset previews its whole coordinated palette rather
 *  than one flat color. */
@Composable
private fun PaletteTilePreview(hex: String) {
    val shades = remember(hex) { accentShades(hex) }
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.weight(1f).fillMaxWidth()) {
            Box(Modifier.weight(1f).fillMaxHeight().background(shades[0]))
            Box(Modifier.weight(1f).fillMaxHeight().background(shades[1]))
        }
        Row(Modifier.weight(1f).fillMaxWidth()) {
            Box(Modifier.weight(1f).fillMaxHeight().background(shades[2]))
            Box(Modifier.weight(1f).fillMaxHeight().background(shades[3]))
        }
    }
}


/**
 * One expressive accent tile: a real elevated square swatch (Modifier.shadow
 * — a true drop shadow, not Card's tonal-elevation color blend, which would
 * otherwise wash out the exact color a swatch is supposed to preview),
 * a spring scale/elevation lift on selection, a genuine ripple on tap, and
 * an animated check badge. [content] draws the tile's fill — a flat color
 * for presets, an icon-on-surface treatment for Mono/Custom.
 */
@Composable
private fun ColorTile(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable (Boolean) -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else if (selected) 1.04f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "tileScale",
    )
    val elevation by animateDpAsState(
        targetValue = if (selected) 8.dp else 2.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "tileElevation",
    )
    val borderColor by androidx.compose.animation.animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "tileBorder",
    )
    val tileShape = RoundedCornerShape(10.dp)

    Column(
        modifier = modifier.scale(scale),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                // Real shadow, not tonal elevation — keeps every tile's
                // color a true, undistorted preview of the accent it
                // represents (see GenerateScreen's ModeCard fix for why
                // Card's own elevation param is the wrong tool for this).
                .shadow(elevation = elevation, shape = tileShape, clip = false)
                .clip(tileShape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = LocalIndication.current,
                    onClick = onClick,
                )
                .border(2.5.dp, borderColor, tileShape),
            contentAlignment = Alignment.Center,
        ) {
            content(selected)
            androidx.compose.animation.AnimatedVisibility(
                visible = selected,
                enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.scaleIn(),
                exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.scaleOut(),
                modifier = Modifier.align(Alignment.TopEnd).padding(6.dp),
            ) {
                Box(
                    Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(13.dp),
                    )
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun AboutCard(versionName: String) {
    val liquidGlass = LocalLiquidGlass.current
    val shape = RoundedCornerShape(26.dp)

    Card(
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = liquidGlassContainerColor(MaterialTheme.colorScheme.surfaceContainerHigh)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)),
        modifier = Modifier
            .fillMaxWidth()
            .liquidGlassChrome(shape, liquidGlass),
    ) {
        Column(
            Modifier.fillMaxWidth().padding(vertical = 28.dp, horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                Color(0xFF231838),
                                Color(0xFF0C0816),
                            )
                        )
                    )
                    .border(1.5.dp, Brush.linearGradient(listOf(Color(0xFF8B5CF6), Color(0xFF00E5FF))), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_launcher_logo),
                    contentDescription = "TuneGlass",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(56.dp),
                )
            }
            Spacer(Modifier.height(14.dp))
            Text(
                "TuneGlass",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(8.dp))
            Surface(
                shape = ExpressivePillShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
            ) {
                Text(
                    "Version $versionName",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                "Prismatic Audio Experience \u2022 Stream & Offline",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

private fun openTelegramChannel(context: android.content.Context, handleOrUrl: String): Boolean {
    val username = handleOrUrl
        .removePrefix("https://t.me/")
        .removePrefix("http://t.me/")
        .removePrefix("@")
        .trim()
    val tgIntent = Intent(Intent.ACTION_VIEW, Uri.parse("tg://resolve?domain=$username")).apply {
        setPackage("org.telegram.messenger")
    }
    val genericTgIntent = Intent(Intent.ACTION_VIEW, Uri.parse("tg://resolve?domain=$username"))
    val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/$username"))
    return startActivitySafely(context, tgIntent) ||
        startActivitySafely(context, genericTgIntent) ||
        startActivitySafely(context, webIntent)
}

/** OEM Settings/browser components are optional and occasionally broken on
 * custom ROMs. Never let an external activity failure escape a click event. */
private fun startActivitySafely(context: android.content.Context, intent: Intent): Boolean {
    val safeIntent = Intent(intent).apply {
        if (context !is android.app.Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    return try {
        context.startActivity(safeIntent)
        true
    } catch (_: Exception) {
        false
    } catch (_: LinkageError) {
        false
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ColorWheelSheet(onDismiss: () -> Unit, onApply: (Color) -> Unit) {
    val sheetState = rememberModalBottomSheetState()
    var hue by remember { mutableStateOf(4f) }
    var saturation by remember { mutableStateOf(0.75f) }
    var lightness by remember { mutableStateOf(0.5f) }
    val previewColor = Color.hsl(hue, saturation, lightness)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .adaptiveContentWidth(maxWidth = 560.dp)
                .align(Alignment.CenterHorizontally)
                .padding(20.dp),
        ) {
            Text("Custom Color", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(previewColor),
            )
            Spacer(Modifier.height(20.dp))
            Text("Hue", style = MaterialTheme.typography.labelLarge)
            Slider(value = hue, onValueChange = { hue = it }, valueRange = 0f..360f)
            Text("Saturation", style = MaterialTheme.typography.labelLarge)
            Slider(value = saturation, onValueChange = { saturation = it }, valueRange = 0f..1f)
            Text("Lightness", style = MaterialTheme.typography.labelLarge)
            Slider(value = lightness, onValueChange = { lightness = it }, valueRange = 0.15f..0.85f)
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = onDismiss, shape = ExpressivePillShape, modifier = Modifier.weight(1f).height(48.dp)) { Text("Cancel") }
                Button(onClick = { onApply(previewColor) }, shape = ExpressivePillShape, modifier = Modifier.weight(1f).height(48.dp)) { Text("Apply") }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

// -- Experimental 15-band equalizer (Settings → Experimental → Equalizer) --

private const val EQ_MAX_DB = EQ_MAX_GAIN_DB

private fun eqBandCategory(hz: Int): String = when {
    hz <= 40 -> "SUB"
    hz <= 100 -> "BASS"
    hz <= 250 -> "LOW-MID"
    hz <= 1000 -> "MID"
    hz <= 2500 -> "HIGH-MID"
    hz <= 6300 -> "PRES"
    else -> "AIR"
}

@Composable
private fun EqualizerCurveGraph(
    gains: FloatArray,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val outlineVariant = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(130.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val paddingX = 14.dp.toPx()
            val availableW = w - paddingX * 2f
            val baselineY = h / 2f
            val maxDbPx = (h - 22.dp.toPx()) / 2f

            val topY = baselineY - maxDbPx
            val bottomY = baselineY + maxDbPx

            // Baseline (0 dB)
            drawLine(
                color = if (enabled) outlineVariant else outlineVariant.copy(alpha = 0.15f),
                start = Offset(paddingX, baselineY),
                end = Offset(w - paddingX, baselineY),
                strokeWidth = 1.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f),
            )
            // +8 dB line
            drawLine(
                color = gridColor,
                start = Offset(paddingX, topY),
                end = Offset(w - paddingX, topY),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 6f), 0f),
            )
            // -8 dB line
            drawLine(
                color = gridColor,
                start = Offset(paddingX, bottomY),
                end = Offset(w - paddingX, bottomY),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 6f), 0f),
            )

            if (gains.isEmpty()) return@Canvas

            val count = gains.size
            val stepX = availableW / (count - 1).coerceAtLeast(1)
            val points = List(count) { i ->
                val x = paddingX + i * stepX
                val gain = if (enabled) gains[i].coerceIn(-EQ_MAX_DB, EQ_MAX_DB) else 0f
                val y = baselineY - (gain / EQ_MAX_DB) * maxDbPx
                Offset(x, y)
            }

            val path = Path()
            val fillPath = Path()

            path.moveTo(points.first().x, points.first().y)
            fillPath.moveTo(points.first().x, baselineY)
            fillPath.lineTo(points.first().x, points.first().y)

            for (i in 0 until points.size - 1) {
                val p0 = points[i]
                val p1 = points[i + 1]
                val controlX1 = p0.x + (p1.x - p0.x) / 2f
                val controlY1 = p0.y
                val controlX2 = p0.x + (p1.x - p0.x) / 2f
                val controlY2 = p1.y
                path.cubicTo(controlX1, controlY1, controlX2, controlY2, p1.x, p1.y)
                fillPath.cubicTo(controlX1, controlY1, controlX2, controlY2, p1.x, p1.y)
            }

            fillPath.lineTo(points.last().x, baselineY)
            fillPath.close()

            if (enabled) {
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.35f),
                            primaryColor.copy(alpha = 0.05f),
                            Color.Transparent,
                        ),
                        startY = topY,
                        endY = bottomY,
                    ),
                )
            }

            drawPath(
                path = path,
                color = if (enabled) primaryColor else onSurfaceVariant.copy(alpha = 0.4f),
                style = Stroke(
                    width = 3.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round,
                ),
            )

            for (p in points) {
                val hasBoostOrCut = Math.abs(p.y - baselineY) > 2f && enabled
                drawCircle(
                    color = if (hasBoostOrCut) primaryColor else if (enabled) primaryColor.copy(alpha = 0.7f) else onSurfaceVariant.copy(alpha = 0.3f),
                    radius = if (hasBoostOrCut) 4.5.dp.toPx() else 3.dp.toPx(),
                    center = p,
                )
                if (hasBoostOrCut) {
                    drawCircle(
                        color = Color.White,
                        radius = 2.dp.toPx(),
                        center = p,
                    )
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxHeight().align(Alignment.CenterEnd).padding(end = 2.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.End,
        ) {
            Text("+12dB", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text("0dB", fontSize = 9.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
            Text("-12dB", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
        }
    }
}

/**
 * 100% natural, native Material 3 Equalizer:
 * - Edge-to-edge layout with status bar and navigation bar insets protection
 * - Live dynamic Bézier Spline frequency response visualizer
 * - Hardware acoustic fader board with real-time numeric dB readouts and 0 dB center detent haptics
 * - Standard ISO center frequencies
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun EqualizerSheet(
    eq: EqualizerSettings,
    onDismiss: () -> Unit,
    onSetEnabled: (Boolean) -> Unit,
    onPickPreset: (String) -> Unit,
    onBandPreview: (Int, Float) -> Unit,
    onBandChange: (Int, Float) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

    var gains by remember(eq.gainsDb) {
        mutableStateOf(
            FloatArray(EQ_BAND_FREQS_HZ.size) { index ->
                eq.gainsDb.getOrNull(index)
                    ?.takeIf { it.isFinite() }
                    ?.coerceIn(-EQ_MAX_DB, EQ_MAX_DB)
                    ?: 0f
            },
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        dragHandle = {
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .size(width = 36.dp, height = 4.dp),
            ) {}
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .adaptiveContentWidth(maxWidth = 640.dp)
                .align(Alignment.CenterHorizontally)
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp + safeDrawingBottomPadding()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Filled.GraphicEq,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(
                            "Equalizer",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            "15-band hardware acoustic tuning",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                        )
                    }
                }

                TextButton(
                    onClick = {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        onPickPreset(EqualizerPresets.FLAT.name)
                    },
                    enabled = eq.enabled,
                ) {
                    Icon(Icons.Filled.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Reset", style = MaterialTheme.typography.labelMedium)
                }
            }

            // Master On/Off Switch Card
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Enable Equalizer", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                        Text(
                            if (eq.enabled) "Shaping your music in real-time" else "Off \u2014 original audio passes through",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                        )
                    }
                    Switch(
                        checked = eq.enabled,
                        onCheckedChange = { enabled ->
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            onSetEnabled(enabled)
                        },
                    )
                }
            }

            // Real-Time Frequency Response Visualizer
            EqualizerCurveGraph(
                gains = gains,
                enabled = eq.enabled,
            )

            // Presets Horizontal Flow
            SectionLabel("Presets")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                EqualizerPresets.ALL.forEach { preset ->
                    FilterChip(
                        selected = eq.presetName.equals(preset.name, ignoreCase = true),
                        onClick = {
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            onPickPreset(preset.name)
                        },
                        label = { Text(preset.name, style = MaterialTheme.typography.labelMedium) },
                    )
                }
                if (eq.presetName == EqualizerPresets.CUSTOM_NAME) {
                    FilterChip(selected = true, onClick = {}, label = { Text("Custom", style = MaterialTheme.typography.labelMedium) })
                }
            }

            // Native Equalizer Fader Board
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                val curveAlpha by animateFloatAsState(
                    targetValue = if (eq.enabled) 1f else 0.38f,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "eqCurveAlpha",
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                        .alpha(curveAlpha),
                ) {
                    // Top Scale Label
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "+8 dB (Boost)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            "-8 dB (Cut)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    // Scrollable Horizontal Row of Native Equalizer Faders
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        EQ_BAND_FREQS_HZ.forEachIndexed { index, hz ->
                            EqNativeSlider(
                                gainDb = gains[index],
                                hz = hz,
                                enabled = eq.enabled,
                                onGainChange = { value ->
                                    gains = gains.copyOf().also { it[index] = value }
                                    onBandPreview(index, value)
                                },
                                onChangeFinished = { onBandChange(index, gains[index]) },
                            )
                        }
                    }
                }
            }

            Text(
                text = when {
                    !eq.enabled -> "Turn on to apply equalization live"
                    eq.presetName == EqualizerPresets.CUSTOM_NAME -> "Custom profile \u2022 touch and drag any bar up/down"
                    else -> "${eq.presetName} preset active \u2022 touch any bar to customize"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
            )
        }
    }
}

/**
 * Goated native hardware equalizer fader bar:
 * - Vertical capsule track with central 0 dB baseline notch and active level gradient beam
 * - Tactile hardware capsule thumb knob with double grip ridges
 * - Real-time continuous numeric dB badge on top with container coloring
 * - Standard frequency label + band category tag underneath
 */
@Composable
private fun EqNativeSlider(
    gainDb: Float,
    hz: Int,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onGainChange: (Float) -> Unit,
    onChangeFinished: () -> Unit,
) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val normalized = ((gainDb + EQ_MAX_DB) / (EQ_MAX_DB * 2f)).coerceIn(0f, 1f)
    var isDragging by remember { mutableStateOf(false) }
    val currentGain by rememberUpdatedState(gainDb)
    val currentOnGainChange by rememberUpdatedState(onGainChange)
    val currentOnChangeFinished by rememberUpdatedState(onChangeFinished)

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Numeric Gain on top in a small pill container
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = when {
                !enabled -> MaterialTheme.colorScheme.surfaceContainer
                gainDb > 0f -> MaterialTheme.colorScheme.primaryContainer
                gainDb < 0f -> MaterialTheme.colorScheme.tertiaryContainer
                else -> MaterialTheme.colorScheme.surfaceContainerHighest
            },
        ) {
            Text(
                text = if (gainDb > 0f) "+${"%.1f".format(gainDb)}" else "${"%.1f".format(gainDb)}",
                fontSize = 11.sp,
                fontWeight = if (gainDb != 0f) FontWeight.Bold else FontWeight.Medium,
                color = when {
                    !enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    gainDb > 0f -> MaterialTheme.colorScheme.onPrimaryContainer
                    gainDb < 0f -> MaterialTheme.colorScheme.onTertiaryContainer
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
                maxLines = 1,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            )
        }

        Spacer(Modifier.height(6.dp))

        // Vertical Track Box
        val trackHeight = 152.dp
        val thumbHeight = 24.dp
        val thumbWidth = 38.dp

        Box(
            modifier = Modifier
                .width(44.dp)
                .height(trackHeight)
                .clip(RoundedCornerShape(22.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                .pointerInput(enabled) {
                    if (!enabled) return@pointerInput
                    detectVerticalDragGestures(
                        onDragStart = { isDragging = true },
                        onDragEnd = {
                            isDragging = false
                            currentOnChangeFinished()
                        },
                        onDragCancel = {
                            isDragging = false
                            currentOnChangeFinished()
                        },
                    ) { change, dragAmount ->
                        change.consume()
                        val deltaFraction = -dragAmount / size.height.toFloat()
                        val currentFraction = ((currentGain + EQ_MAX_DB) / (EQ_MAX_DB * 2f))
                        val newFraction = (currentFraction + deltaFraction).coerceIn(0f, 1f)
                        val newGain = (newFraction * EQ_MAX_DB * 2f - EQ_MAX_DB).let {
                            if (it in -0.3f..0.3f) 0f else (Math.round(it * 2f) / 2f)
                        }
                        if (newGain != currentGain) {
                            if (newGain == 0f && currentGain != 0f) {
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                            }
                            currentOnGainChange(newGain)
                        }
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            // Center Baseline Line (0 dB)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            )

            // Active Level Fill (from baseline to thumb)
            val baselineFraction = 0.5f
            val topFraction = if (normalized >= baselineFraction) 1f - normalized else 1f - baselineFraction
            val heightFraction = Math.abs(normalized - baselineFraction)

            if (heightFraction > 0.01f && enabled) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.55f)
                        .fillMaxHeight(heightFraction)
                        .align(Alignment.TopCenter)
                        .graphicsLayer {
                            translationY = size.height * topFraction
                        }
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (gainDb > 0f) {
                                Brush.verticalGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.primaryContainer,
                                    ),
                                )
                            } else {
                                Brush.verticalGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.tertiaryContainer,
                                        MaterialTheme.colorScheme.tertiary,
                                    ),
                                )
                            },
                        ),
                )
            }

            // Tactile Hardware Capsule Thumb Knob
            Box(
                modifier = Modifier
                    .size(width = thumbWidth, height = thumbHeight)
                    .align(Alignment.TopCenter)
                    .graphicsLayer {
                        val maxTravel = (trackHeight - thumbHeight).toPx()
                        translationY = maxTravel * (1f - normalized)
                    }
                    .shadow(if (isDragging) 8.dp else 3.dp, RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                // Double grip ridges
                Column(
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 14.dp, height = 2.dp)
                            .clip(RoundedCornerShape(1.dp))
                            .background(
                                if (enabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            ),
                    )
                    Box(
                        modifier = Modifier
                            .size(width = 14.dp, height = 2.dp)
                            .clip(RoundedCornerShape(1.dp))
                            .background(
                                if (enabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            ),
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Frequency Label
        Text(
            text = eqBandLabel(hz),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (gainDb != 0f && enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
        )
        Text(
            text = eqBandCategory(hz),
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
            maxLines = 1,
        )
    }
}

/** Controls which connected-account playlists appear in TuneGlass. This is
 * intentionally independent from importing and two-way sync. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun YouTubeLibraryVisibilitySheet(
    playlists: List<com.listenit.music.data.music.YouTubePlaylistSummary>,
    hiddenIds: Set<String>,
    onSetVisible: (String, Boolean) -> Unit,
    onSetAllVisible: (Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val allShown = playlists.isNotEmpty() && playlists.all { it.id !in hiddenIds }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .adaptiveContentWidth(maxWidth = 640.dp)
                .align(Alignment.CenterHorizontally)
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp + safeDrawingBottomPadding()),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("YouTube Playlists Shown", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(
                        "Choose which account playlists appear in TuneGlass",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                TextButton(
                    onClick = {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        onSetAllVisible(!allShown)
                    },
                ) {
                    Text(if (allShown) "Hide All" else "Show All", fontWeight = FontWeight.Bold)
                }
            }

            if (playlists.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("No YouTube playlists found", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 480.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(playlists.size, key = { playlists[it].id }) { index ->
                        val playlist = playlists[index]
                        val isShown = playlist.id !in hiddenIds
                        Surface(
                            onClick = {
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                                onSetVisible(playlist.id, !isShown)
                            },
                            shape = RoundedCornerShape(16.dp),
                            color = if (isShown) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainer,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isShown) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHighest),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        Icons.Filled.QueueMusic,
                                        contentDescription = null,
                                        tint = if (isShown) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                Spacer(Modifier.width(14.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(playlist.title, fontWeight = FontWeight.SemiBold, maxLines = 1)
                                    Text(
                                        playlist.trackCountText ?: playlist.author ?: "YouTube Music playlist",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                    )
                                }
                                Checkbox(
                                    checked = isShown,
                                    onCheckedChange = { checked -> onSetVisible(playlist.id, checked) },
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Button(onClick = onDismiss, shape = CircleShape, modifier = Modifier.fillMaxWidth().height(48.dp)) {
                Text("Done", fontWeight = FontWeight.Bold)
            }
        }
    }
}

/** Bottom sheet allowing user to select which specific playlists to mirror to YouTube Music. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SyncPlaylistsSheet(
    playlists: List<com.listenit.music.data.playlist.SavedPlaylist>,
    syncedIds: Set<Long>?,
    onToggleSync: (Long, Boolean) -> Unit,
    onSelectAll: (Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val allSelected = playlists.isNotEmpty() && (syncedIds == null || (playlists.all { it.id in syncedIds } && syncedIds.isNotEmpty()))

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        dragHandle = {
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .size(width = 36.dp, height = 4.dp),
            ) {}
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .adaptiveContentWidth(maxWidth = 640.dp)
                .align(Alignment.CenterHorizontally)
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp + safeDrawingBottomPadding()),
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "Sync Playlists",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "Choose which playlists mirror to YouTube Music",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                TextButton(
                    onClick = {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        onSelectAll(!allSelected)
                    },
                ) {
                    Text(if (allSelected) "Deselect All" else "Select All", fontWeight = FontWeight.Bold)
                }
            }

            if (playlists.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "No playlists in your library yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(playlists.size, key = { playlists[it].id }) { idx ->
                        val playlist = playlists[idx]
                        val isChecked = syncedIds == null || playlist.id in syncedIds
                        Surface(
                            onClick = {
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                                onToggleSync(playlist.id, !isChecked)
                            },
                            shape = RoundedCornerShape(16.dp),
                            color = if (isChecked) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainer,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (isChecked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHighest
                                        ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        Icons.Filled.QueueMusic,
                                        contentDescription = null,
                                        tint = if (isChecked) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(22.dp),
                                    )
                                }

                                Spacer(Modifier.width(14.dp))

                                Column(Modifier.weight(1f)) {
                                    Text(
                                        playlist.title,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                    )
                                    Text(
                                        "${playlist.tracks.size} tracks",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }

                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { checked ->
                                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                                        onToggleSync(playlist.id, checked)
                                    },
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = onDismiss,
                shape = CircleShape,
                modifier = Modifier.fillMaxWidth().height(48.dp),
            ) {
                Text("Done", fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * Bottom sheet to pick from 8 experimental lyrics animation physics profiles.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LyricsAnimationSheet(
    wordByWord: Boolean,
    onWordByWordChange: (Boolean) -> Unit,
    version: LyricsUiVersion,
    onSelectVersion: (LyricsUiVersion) -> Unit,
    current: LyricsAnimation,
    onSelect: (LyricsAnimation) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val liquidGlass = LocalLiquidGlass.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier.liquidGlassChrome(
            RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            liquidGlass,
            LiquidGlassPreset.ModalSheet,
            LocalLiquidGlassOverlayBackdrop.current,
        ),
        containerColor = liquidGlassContainerColor(
            MaterialTheme.colorScheme.surfaceContainer,
            backdrop = LocalLiquidGlassOverlayBackdrop.current,
        ),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .adaptiveContentWidth(maxWidth = 640.dp)
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    modifier = Modifier.size(42.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Lyrics,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
                Column {
                    Text(
                        text = "Lyrics Animation",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "Real-time motion physics & optical tracking",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            SettingsToggleCard(
                icon = Icons.Filled.Lyrics,
                iconContainer = MaterialTheme.colorScheme.tertiaryContainer,
                iconTint = MaterialTheme.colorScheme.onTertiaryContainer,
                title = "Word-by-word lyrics",
                subtitle = "Turn off to use LRCLIB line-by-line lyrics",
                checked = wordByWord,
                onCheckedChange = onWordByWordChange,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val versions = listOf(
                    LyricsUiVersion.CLASSIC to "Classic",
                    LyricsUiVersion.MODERN to "New UI",
                )
                versions.forEach { (ver, label) ->
                    val isVerSelected = ver == version
                    val chipShape = RoundedCornerShape(14.dp)
                    LiquidGlassSurface(
                        glassModifier = Modifier.liquidGlassChrome(chipShape, liquidGlass),
                        onClick = {
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                            onSelectVersion(ver)
                        },
                        shape = chipShape,
                        color = liquidGlassContainerColor(if (isVerSelected) {
                            if (liquidGlass) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f)
                            else MaterialTheme.colorScheme.primaryContainer
                        } else {
                            if (liquidGlass) MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.60f)
                            else MaterialTheme.colorScheme.surfaceContainerHigh
                        }),
                        modifier = Modifier
                            .weight(1f)
                            .clip(chipShape),
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (isVerSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isVerSelected) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            )
                        }
                    }
                }
            }

            if (version == LyricsUiVersion.CLASSIC) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 440.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(LyricsAnimation.entries.toTypedArray(), key = { it.id }) { anim ->
                        val isSelected = anim == current
                        val cardShape = RoundedCornerShape(18.dp)

                        Surface(
                            shape = cardShape,
                            color = liquidGlassContainerColor(if (isSelected) {
                                if (liquidGlass) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f)
                                else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.70f)
                            } else {
                                if (liquidGlass) MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.90f)
                                else MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.50f)
                            }),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(cardShape)
                                .liquidGlassChrome(cardShape, liquidGlass)
                                .clickable {
                                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                                    onSelect(anim)
                                },
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceContainerLowest,
                                    modifier = Modifier.size(24.dp),
                                ) {
                                    if (isSelected) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Filled.Check,
                                                contentDescription = "Selected",
                                                tint = MaterialTheme.colorScheme.onPrimary,
                                                modifier = Modifier.size(16.dp),
                                            )
                                        }
                                    }
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = anim.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                        color = if (isSelected && liquidGlass) MaterialTheme.colorScheme.onPrimaryContainer
                                        else if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurface,
                                    )
                                    Text(
                                        text = anim.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isSelected && liquidGlass) {
                                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f)
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = liquidGlassContainerColor(if (liquidGlass) MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.70f)
                    else MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.40f)),
                    modifier = Modifier.fillMaxWidth().liquidGlassChrome(RoundedCornerShape(18.dp), liquidGlass),
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "New Lyrics UI",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "Modern lyrics rendering active",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}
