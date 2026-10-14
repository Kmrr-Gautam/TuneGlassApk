package com.listenit.music.ui.shell

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.listenit.music.ui.common.ExpressiveMotion
import com.listenit.music.ui.common.LensRingButton
import com.listenit.music.ui.common.PredictiveBackScreen
import com.listenit.music.ui.common.adaptiveContentWidth
import com.listenit.music.ui.feed.FeedScreen
import com.listenit.music.ui.home.HomeScreen
import com.listenit.music.ui.player.LocalMiniPlayerScrollClearance
import com.listenit.music.ui.playlist.PlaylistScreen
import com.listenit.music.ui.search.SearchScreen
import com.listenit.music.ui.settings.SettingsScreen
import com.listenit.music.ui.theme.LiquidGlassPreset
import com.listenit.music.ui.theme.LocalLiquidGlass
import com.listenit.music.ui.theme.isLiquidGlassBackdropSupported
import com.listenit.music.ui.theme.liquidGlassChrome
import com.listenit.music.ui.theme.liquidGlassContainerColor
import com.listenit.music.ui.theme.liquidGlassSource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

/** Thin bridge exposing AppUpdateManager's live update state to MainShell */
@HiltViewModel
class MainShellViewModel @Inject constructor(
    val appUpdateManager: com.listenit.music.data.update.AppUpdateManager,
) : ViewModel() {
    val updateInfo = appUpdateManager.updateInfo

    fun dismissUpdate(version: String) {
        appUpdateManager.dismissUpdate(version)
    }

    fun openUpdate(context: android.content.Context) {
        appUpdateManager.openUpdate(context)
    }

    fun downloadAndInstall(context: android.content.Context) {
        appUpdateManager.downloadAndInstall(context)
    }
}

private enum class MainTab(val label: String) {
    FEED("Home"),
    SEARCH("Search"),
    PLAYLISTS("Library"),
    SETTINGS("Settings"),
}

/** Shared with any screen hosted inside [MainShell] so their scrolling
 *  lists know how much bottom content padding to reserve — the nav
 *  overlays content (it's not a Scaffold bottomBar reserving space), so
 *  each screen leaves this much room for its last item to clear it. */
object FloatingNavDefaults {
    val ContentBottomPadding = 112.dp

    /**
     * Full bottom clearance for edge-to-edge scrolling content: the floating
     * dock's visual height + margins ([ContentBottomPadding]) PLUS the live
     * navigation-bar (gesture area) inset. Screens that let their list draw
     * beneath the transparent gesture area must use this instead of the raw
     * constant, otherwise the last row hides behind the dock/gesture bar.
     */
    @Composable
    fun contentBottomPadding(): Dp =
        ContentBottomPadding +
            LocalMiniPlayerScrollClearance.current +
            WindowInsets.safeDrawing.asPaddingValues().calculateBottomPadding()
}

private val DockShape: Shape = RoundedCornerShape(26.dp)
private val NavItemShape: Shape = RoundedCornerShape(20.dp)

private fun <T> navSpring() = ExpressiveMotion.spatialSpring<T>()

@Composable
fun MainShell(
    onOpenSettings: () -> Unit = {},
    onOpenSearch: () -> Unit = {},
    onOpenDiscover: () -> Unit = {},
    onOpenGenres: () -> Unit = {},
    onOpenFriends: () -> Unit = {},
    onOpenFriendProfile: (username: String, displayName: String?, avatarUrl: String?) -> Unit = { _, _, _ -> },
    onOpenFeedPlaylist: (String) -> Unit,
    onOpenPlaylist: (Long) -> Unit = {},
    onOpenGenerator: () -> Unit = {},
    onOpenNewReleases: () -> Unit = {},
    onOpenArtist: (name: String, browseId: String?) -> Unit = { _, _ -> },
    onOpenAlbum: (title: String, artist: String, browseId: String?) -> Unit = { _, _, _ -> },
    onOpenChooseApps: () -> Unit = {},
    onOpenDownloads: () -> Unit = {},
    onOpenExcludedSongs: () -> Unit = {},
    onOpenYouTubeImport: () -> Unit = {},
    onOpenYouTubeLogin: () -> Unit = {},
    onLoggedOut: () -> Unit = {},
    mainShellViewModel: MainShellViewModel = hiltViewModel(),
) {
    val tabs = MainTab.entries
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val updateInfo by mainShellViewModel.updateInfo.collectAsStateWithLifecycle()
    val showUpdateBanner = updateInfo.isUpdateAvailable && !updateInfo.isDismissed
    val navigationBackdrop = if (isLiquidGlassBackdropSupported()) rememberLayerBackdrop() else null

    Box(Modifier.fillMaxSize()) {
        val feedIndex = tabs.indexOf(MainTab.FEED)
        HorizontalPager(
            state = pagerState,
            beyondViewportPageCount = 0,
            modifier = Modifier.fillMaxSize().liquidGlassSource(navigationBackdrop),
        ) { page ->
            val isCurrent = page == pagerState.currentPage
            PredictiveBackScreen(
                enabled = isCurrent && tabs[page] != MainTab.FEED,
                onBack = { scope.launch { pagerState.animateScrollToPage(feedIndex) } },
            ) {
                when (tabs[page]) {
                    MainTab.FEED -> FeedScreen(
                        onOpenSettings = { scope.launch { pagerState.animateScrollToPage(tabs.indexOf(MainTab.SETTINGS)) } },
                        onOpenSearch = { scope.launch { pagerState.animateScrollToPage(tabs.indexOf(MainTab.SEARCH)) } },
                        onOpenDiscover = onOpenDiscover,
                        onOpenPlaylist = onOpenPlaylist,
                        onOpenFeedPlaylist = onOpenFeedPlaylist,
                        onOpenGenerator = onOpenGenerator,
                        onOpenFriends = onOpenFriends,
                        onOpenFriendProfile = onOpenFriendProfile,
                        onOpenNewReleases = onOpenNewReleases,
                    )
                    MainTab.SEARCH -> SearchScreen(
                        onBack = null,
                        onOpenArtist = onOpenArtist,
                        onOpenAlbum = onOpenAlbum,
                        onOpenPlaylist = onOpenFeedPlaylist,
                    )
                    MainTab.PLAYLISTS -> PlaylistScreen(
                        onOpenPlaylist = onOpenPlaylist,
                        onOpenArtist = onOpenArtist,
                    )
                    MainTab.SETTINGS -> SettingsScreen(
                        onBack = null,
                        onLoggedOut = onLoggedOut,
                        onOpenChooseApps = onOpenChooseApps,
                        onOpenDownloads = onOpenDownloads,
                        onOpenExcludedSongs = onOpenExcludedSongs,
                        onOpenYouTubeImport = onOpenYouTubeImport,
                        onOpenYouTubeLogin = onOpenYouTubeLogin,
                    )
                }
            }
        }

        // App update popup modal (automatically shown when a new update is released)
        if (showUpdateBanner) {
            UpdateAvailableDialog(
                updateInfo = updateInfo,
                onUpdate = { mainShellViewModel.downloadAndInstall(context) },
                onDismiss = { mainShellViewModel.dismissUpdate(updateInfo.latestVersion) },
            )
        }

        FloatingNavBar(
            backdrop = navigationBackdrop,
            tabs = tabs,
            selectedIndex = pagerState.currentPage,
            onSelect = { index -> scope.launch { pagerState.animateScrollToPage(index) } },
            onOpenGenerator = onOpenGenerator,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun UpdateAvailableDialog(
    updateInfo: com.listenit.music.data.update.UpdateInfo,
    onUpdate: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color(0xFF131318),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
            shadowElevation = 24.dp,
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .fillMaxWidth()
                .wrapContentHeight(),
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Top icon
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.10f))
                        .border(1.dp, Color.White.copy(alpha = 0.18f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.CloudDownload,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp),
                    )
                }

                // Title & Version tag
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Update Available",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = Color.White,
                    )
                    Spacer(Modifier.height(4.dp))
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.12f),
                    ) {
                        Text(
                            text = "Version ${updateInfo.latestVersion}",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                        )
                    }
                }

                // What's New & Bug Fixes Box
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF1B1B22))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "What's New & Bug Fixes:",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                        ),
                        color = Color.White.copy(alpha = 0.7f),
                    )

                    val notes = updateInfo.releaseNotes.ifBlank {
                        "Performance improvements, bug fixes, and new features are ready to install."
                    }
                    val bulletPoints = notes.split(Regex("[\n,;]"))
                        .map { it.trim().removePrefix("-").removePrefix("•").trim() }
                        .filter { it.isNotBlank() }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 140.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        if (bulletPoints.isNotEmpty()) {
                            bulletPoints.forEach { point ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.Top,
                                ) {
                                    Text(
                                        text = "•",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFF22C55E),
                                    )
                                    Text(
                                        text = point,
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
                                        color = Color.White.copy(alpha = 0.85f),
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = notes,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = Color.White.copy(alpha = 0.85f),
                            )
                        }
                    }
                }

                // Downloading Progress
                if (updateInfo.isDownloading) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        LinearProgressIndicator(
                            progress = { updateInfo.downloadProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(CircleShape),
                            color = Color(0xFF22C55E),
                            trackColor = Color.White.copy(alpha = 0.15f),
                            strokeCap = StrokeCap.Round,
                        )
                        Text(
                            text = "${(updateInfo.downloadProgress * 100).toInt()}% • Downloading update...",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.6f),
                        )
                    }
                }

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(
                            text = "Later",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White.copy(alpha = 0.6f),
                        )
                    }

                    Button(
                        onClick = onUpdate,
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black,
                        ),
                        modifier = Modifier.weight(1.3f),
                    ) {
                        Text(
                            text = if (updateInfo.isDownloading) "Downloading..." else "Update Now",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        )
                    }
                }
            }
        }
    }
}

/**
 * Signature Liquid Frequency Navigation Dock.
 * Translucent floating glass surface with animated illuminated active capsule
 * and minimal iconography.
 */
@Composable
private fun FloatingNavBar(
    backdrop: LayerBackdrop?,
    tabs: List<MainTab>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    onOpenGenerator: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val liquidGlass = LocalLiquidGlass.current
    Box(
        modifier = modifier
            .windowInsetsPadding(
                WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal),
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 520.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = DockShape,
                color = com.listenit.music.ui.theme.LiquidFrequency.GlassSurfaceElevated.copy(alpha = if (liquidGlass) 0.85f else 0.96f),
                border = com.listenit.music.ui.theme.LiquidFrequency.glassBorder(active = true),
                tonalElevation = 0.dp,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .weight(1f)
                    .animateContentSize(animationSpec = navSpring())
                    .liquidGlassChrome(DockShape, liquidGlass, LiquidGlassPreset.BottomNavigation, backdrop),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    tabs.forEachIndexed { index, tab ->
                        val onClick = remember(index) { { onSelect(index) } }
                        val icon = when (tab) {
                            MainTab.FEED -> Icons.Filled.Home
                            MainTab.SEARCH -> Icons.Filled.Search
                            MainTab.PLAYLISTS -> Icons.AutoMirrored.Filled.QueueMusic
                            MainTab.SETTINGS -> Icons.Filled.Settings
                        }
                        LiquidFrequencyNavItem(
                            label = tab.label,
                            icon = icon,
                            selected = selectedIndex == index,
                            onClick = onClick,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            // Companion generator button (only visible on Playlists tab).
            AnimatedVisibility(
                visible = selectedIndex == tabs.indexOf(MainTab.PLAYLISTS),
                enter = fadeIn(animationSpec = tween(180)) +
                    scaleIn(initialScale = 0.35f, animationSpec = navSpring()) +
                    expandHorizontally(animationSpec = navSpring(), expandFrom = Alignment.End),
                exit = fadeOut(animationSpec = tween(120)) +
                    scaleOut(targetScale = 0.35f, animationSpec = navSpring()) +
                    shrinkHorizontally(animationSpec = navSpring(), shrinkTowards = Alignment.End),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(Modifier.width(10.dp))
                    LensRingButton(
                        icon = Icons.Filled.AutoAwesome,
                        contentDescription = "Create / Generate Playlist",
                        onClick = onOpenGenerator,
                        size = 56.dp,
                        iconSize = 22.dp,
                        tint = com.listenit.music.ui.theme.LiquidFrequency.ElectricCyan,
                        ringColor = com.listenit.music.ui.theme.LiquidFrequency.ElectricCyan.copy(alpha = 0.65f),
                        filled = true,
                        fillColor = com.listenit.music.ui.theme.LiquidFrequency.GlassSurfaceElevated,
                    )
                }
            }
        }
    }
}

@Composable
private fun LiquidFrequencyNavItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentColor by animateColorAsState(
        targetValue = if (selected) com.listenit.music.ui.theme.LiquidFrequency.FrostedWhite else com.listenit.music.ui.theme.LiquidFrequency.MutedLavender.copy(alpha = 0.75f),
        animationSpec = navSpring(),
        label = "navItemContent",
    )
    val iconTint by animateColorAsState(
        targetValue = if (selected) com.listenit.music.ui.theme.LiquidFrequency.ElectricCyan else com.listenit.music.ui.theme.LiquidFrequency.MutedLavender.copy(alpha = 0.65f),
        animationSpec = navSpring(),
        label = "navItemIcon",
    )

    Box(
        modifier = modifier
            .height(50.dp)
            .clip(NavItemShape)
            .then(
                if (selected) {
                    Modifier
                        .background(
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                listOf(
                                    com.listenit.music.ui.theme.LiquidFrequency.ElectricCyan.copy(alpha = 0.16f),
                                    com.listenit.music.ui.theme.LiquidFrequency.Ultraviolet.copy(alpha = 0.10f),
                                )
                            )
                        )
                        .border(
                            1.dp,
                            androidx.compose.ui.graphics.Brush.horizontalGradient(
                                listOf(
                                    com.listenit.music.ui.theme.LiquidFrequency.ElectricCyan.copy(alpha = 0.45f),
                                    com.listenit.music.ui.theme.LiquidFrequency.Ultraviolet.copy(alpha = 0.35f),
                                )
                            ),
                            NavItemShape,
                        )
                } else Modifier
            )
            .selectable(selected = selected, role = Role.Tab, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier.size(19.dp),
            )
            Spacer(Modifier.height(3.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    ),
                    color = contentColor,
                    maxLines = 1,
                    softWrap = false,
                )
                if (selected) {
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(com.listenit.music.ui.theme.LiquidFrequency.ElectricCyan),
                    )
                }
            }
        }
    }
}
