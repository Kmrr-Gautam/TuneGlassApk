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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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

        // App update prompt banner (only shown on app open when an update is available and not dismissed)
        AnimatedVisibility(
            visible = showUpdateBanner,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .adaptiveContentWidth(maxWidth = 600.dp)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .zIndex(10f),
        ) {
            UpdatePromptCard(
                version = updateInfo.latestVersion,
                isDownloading = updateInfo.isDownloading,
                downloadProgress = updateInfo.downloadProgress,
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
private fun UpdatePromptCard(
    version: String,
    isDownloading: Boolean,
    downloadProgress: Float,
    onUpdate: () -> Unit,
    onDismiss: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = 8.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                Modifier
                    .size(40.dp)
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.CloudDownload,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isDownloading) "Downloading update..." else "Update available",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = if (isDownloading) {
                        "${(downloadProgress * 100).toInt()}% • Installing soon"
                    } else {
                        "TuneGlass v$version is ready to install"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = onUpdate),
            ) {
                Text(
                    text = if (isDownloading) "${(downloadProgress * 100).toInt()}%" else "Update",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
                )
            }
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Dismiss update",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
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
