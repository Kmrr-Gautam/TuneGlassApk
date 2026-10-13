package com.listenit.music.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.listenit.music.data.model.AuthState
import com.listenit.music.ui.auth.AuthViewModel
import com.listenit.music.ui.auth.LoginScreen
import com.listenit.music.ui.settings.SettingsScreen
import com.listenit.music.ui.search.SearchScreen
import com.listenit.music.ui.discover.DiscoverScreen
import com.listenit.music.ui.genres.GenresScreen
import com.listenit.music.ui.shell.MainShell
import com.listenit.music.ui.common.PredictiveBackScreen
import com.listenit.music.ui.common.ExpressiveLoadingIndicator
import com.listenit.music.ui.common.ExpressiveMotion
import com.listenit.music.ui.genres.GenreExplorer
import com.listenit.music.ui.onboarding.OnboardingScreen
import com.listenit.music.data.local.SettingsPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/** Thin bridge so TuneGlassNavHost (a plain composable, no direct Hilt
 *  singleton access) can observe GenreExplorer's pending genre and
 *  navigate — same reasoning as MainShellViewModel's bridge to
 *  MixLauncher. */
@HiltViewModel
class GenreExplorerNavBridge @Inject constructor(genreExplorer: GenreExplorer) : androidx.lifecycle.ViewModel() {
    val pendingGenre = genreExplorer.pendingGenre
}

@HiltViewModel
class OnboardingNavBridge @Inject constructor(val settingsPreferences: SettingsPreferences) : androidx.lifecycle.ViewModel() {
    val settings = settingsPreferences.settings
}

@HiltViewModel
class MixLauncherNavBridge @Inject constructor(val mixLauncher: com.listenit.music.ui.generate.MixLauncher) : androidx.lifecycle.ViewModel()

@HiltViewModel
class ArtistAlbumNavBridge @Inject constructor(val navigator: ArtistAlbumNavigator) : androidx.lifecycle.ViewModel()

@HiltViewModel
class AppRouteNavBridge @Inject constructor(val routeNavigator: AppRouteNavigator) : androidx.lifecycle.ViewModel()

@Composable
fun TuneGlassNavHost(
    navController: NavHostController = rememberNavController(),
) {
    val onboardingNavBridge: OnboardingNavBridge = hiltViewModel()
    val miscSettings by onboardingNavBridge.settings.collectAsStateWithLifecycle(initialValue = null)
    val mixNavBridge: MixLauncherNavBridge = hiltViewModel()
    val pendingMixSeed by mixNavBridge.mixLauncher.pendingSeed.collectAsStateWithLifecycle()
    LaunchedEffect(pendingMixSeed) {
        if (pendingMixSeed != null) {
            navController.navigate(Screen.Create.route) {
                launchSingleTop = true
            }
        }
    }
    val appRouteBridge: AppRouteNavBridge = hiltViewModel()
    val pendingAppRoute by appRouteBridge.routeNavigator.pendingRoute.collectAsStateWithLifecycle()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    LaunchedEffect(pendingAppRoute, currentRoute) {
        val route = pendingAppRoute
        if (route != null && currentRoute != null &&
            currentRoute != Screen.Splash.route && currentRoute != Screen.Login.route
        ) {
            if (currentRoute != route) {
                navController.navigate(route) {
                    launchSingleTop = true
                }
            }
            appRouteBridge.routeNavigator.consumeRoute()
        }
    }
    // "Explore this genre" from any track's context menu, anywhere in the
    // app — Home, Discover, Playlist, Search — routes here since Genres
    // is a pushed destination on THIS nav controller and none of those
    // screens hold a reference to it. See GenreExplorer's doc comment.
    val genreExplorerBridge: GenreExplorerNavBridge = hiltViewModel()
    val pendingGenre by genreExplorerBridge.pendingGenre.collectAsStateWithLifecycle()
    LaunchedEffect(pendingGenre) {
        if (pendingGenre != null) {
            navController.navigate(Screen.Genres.route) {
                launchSingleTop = true
            }
        }
    }

    val navBridge: ArtistAlbumNavBridge = hiltViewModel()
    LaunchedEffect(Unit) {
        navBridge.navigator.events.collect { target ->
            // Drill-down destinations MUST push a fresh back-stack entry every
            // time: launchSingleTop would collapse Artist A -> Artist B (or
            // Album X -> Album Y) into the top entry since they share one
            // destination node, leaving taps on similar artists / more-by
            // albums visibly dead and the detail ViewModel showing stale data.
            // Rapid double-tap dedup already lives in ArtistAlbumNavigator.
            when (target) {
                is ArtistAlbumNavTarget.Artist -> {
                    navController.navigate(Screen.ArtistDetail.createRoute(target.name, target.browseId))
                }
                is ArtistAlbumNavTarget.Album -> {
                    navController.navigate(Screen.AlbumDetail.createRoute(target.title, target.artist, target.browseId))
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        enterTransition = { ExpressiveMotion.forwardEnter() },
        exitTransition = { ExpressiveMotion.forwardExit() },
        popEnterTransition = { ExpressiveMotion.backEnter() },
        popExitTransition = { ExpressiveMotion.backExit() },
    ) {

        composable(Screen.Splash.route) {
            val settings = miscSettings
            LaunchedEffect(settings) {
                if (settings != null) {
                    val target = if (!settings.onboardingCompleted) {
                        Screen.Onboarding.route
                    } else {
                        Screen.MainShell.route
                    }
                    navController.navigate(target) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            }

            LaunchGate()
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinished = {
                    navController.navigate(Screen.MainShell.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            OnboardingScreen(
                onFinished = {
                    navController.navigate(Screen.MainShell.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        // Post-login container: Material3 bottom nav with Home/Generate/
        // Playlists as swipeable tabs. Settings, Search, and Discover are
        // NOT tabs — they're pushed screens reached from Home's top app bar
        // (profile icon / search icon / discover icon), on this same root
        // nav controller, so they can pop back cleanly and (on log out /
        // clear session) return all the way to Login.
        composable(Screen.MainShell.route) {
            MainShell(
                onOpenSettings = {
                    navController.navigate(Screen.Settings.route) { launchSingleTop = true }
                },
                onOpenSearch = { navController.navigate(Screen.Search.route) },
                onOpenDiscover = { navController.navigate(Screen.Discover.route) },
                onOpenGenres = { navController.navigate(Screen.Genres.route) },
                onOpenFriends = { navController.navigate(Screen.Friends.route) },
                onOpenFriendProfile = { username, displayName, avatarUrl ->
                    navController.navigate(Screen.FriendProfile.createRoute(username, displayName, avatarUrl))
                },
                onOpenPlaylist = { playlistId ->
                    navController.navigate(Screen.PlaylistDetail.createRoute(playlistId))
                },
                onOpenFeedPlaylist = { playlistId ->
                    navController.navigate(Screen.FeedPlaylistDetail.createRoute(playlistId))
                },
                onOpenGenerator = {
                    navController.navigate(Screen.Create.route) { launchSingleTop = true }
                },
                onOpenNewReleases = {
                    navController.navigate(Screen.NewReleases.route)
                },
                onOpenArtist = { name, browseId ->
                    navController.navigate(Screen.ArtistDetail.createRoute(name, browseId))
                },
                onOpenAlbum = { title, artist, browseId ->
                    navController.navigate(Screen.AlbumDetail.createRoute(title, artist, browseId))
                },
                onOpenChooseApps = { navController.navigate(Screen.ScrobblerApps.route) },
                onOpenDownloads = { navController.navigate(Screen.Downloads.route) },
                onOpenExcludedSongs = { navController.navigate(Screen.ExcludedSongs.route) },
                onOpenYouTubeImport = { navController.navigate(Screen.YouTubeImport.route) },
                onOpenYouTubeLogin = { navController.navigate(Screen.YouTubeLogin.route) },
                onLoggedOut = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.MainShell.route) { inclusive = true }
                    }
                },
            )
        }

        composable(Screen.Create.route) {
            PredictiveBackScreen(onBack = { navController.popBackStack() }) {
                com.listenit.music.ui.generate.GenerateScreen(
                    onNavigateToPlaylist = { playlistId ->
                        navController.popBackStack()
                        if (playlistId != null) {
                            navController.navigate(Screen.PlaylistDetail.createRoute(playlistId))
                        }
                    },
                    onBack = { navController.popBackStack() },
                )
            }
        }

        // Friends is a real pushed screen (not a Dialog/ModalBottomSheet) —
        // both of those were tried first and both had the same underlying
        // problem: getting a Compose overlay to genuinely claim the full
        // real window (not just "as tall as its own content measured
        // itself to be", which is all either API reliably guarantees
        // without extra low-level workarounds) turned out to be fragile
        // enough, even after direct fixes, that it kept regressing. A
        // normal NavHost destination gets full-screen sizing for free —
        // every other pushed screen here (Settings, Search, Discover,
        // Genres, ScrobblerApps) already renders correctly edge-to-edge —
        // so Friends now works exactly like those instead of being a
        // special case. It shares Home's own HomeViewModel (scoped to
        // MainShell's back stack entry) rather than getting a fresh one,
        // since the friends list / switch-profile state already lives
        // there and switching profile needs to affect the Home tab
        // underneath once you pop back.
        composable(Screen.Friends.route) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Screen.MainShell.route)
            }
            val homeViewModel: com.listenit.music.ui.home.HomeViewModel = hiltViewModel(parentEntry)
            PredictiveBackScreen(onBack = { navController.popBackStack() }) {
                com.listenit.music.ui.home.FriendsScreen(
                    viewModel = homeViewModel,
                    onBack = { navController.popBackStack() },
                    onOpenFriendProfile = { username, displayName, avatarUrl ->
                        navController.navigate(Screen.FriendProfile.createRoute(username, displayName, avatarUrl))
                    },
                )
            }
        }

        composable(
            route = Screen.FriendProfile.route,
            arguments = listOf(
                androidx.navigation.navArgument("username") { type = androidx.navigation.NavType.StringType },
                androidx.navigation.navArgument("displayName") {
                    type = androidx.navigation.NavType.StringType
                    defaultValue = ""
                },
                androidx.navigation.navArgument("avatarUrl") {
                    type = androidx.navigation.NavType.StringType
                    defaultValue = ""
                },
            ),
        ) { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username").orEmpty()
            val displayName = backStackEntry.arguments?.getString("displayName")?.takeIf(String::isNotBlank)
            val avatarUrl = backStackEntry.arguments?.getString("avatarUrl")?.takeIf(String::isNotBlank)
            PredictiveBackScreen(onBack = { navController.popBackStack() }) {
                com.listenit.music.ui.home.FriendProfileScreen(
                    username = username,
                    initialDisplayName = displayName,
                    initialAvatarUrl = avatarUrl,
                    onBack = { navController.popBackStack() },
                    onOpenArtist = { artistName ->
                        navController.navigate(Screen.ArtistDetail.createRoute(artistName))
                    },
                    onOpenAlbum = { albumTitle, artistName ->
                        navController.navigate(Screen.AlbumDetail.createRoute(albumTitle, artistName))
                    },
                )
            }
        }

        // Every pushed-on-top destination below is wrapped in
        // PredictiveBackScreen so Android's predictive back gesture (drag
        // in from the edge) scales/rounds/dims the screen in real time
        // instead of using a canned pop transition — see its doc comment
        // for exactly what it does and does not animate. The screen's own
        // onBack (its toolbar back button) still pops directly; the wrap
        // only adds the gesture-driven path, it doesn't replace the
        // button's.
        composable(Screen.Genres.route) {
            PredictiveBackScreen(onBack = { navController.popBackStack() }) {
                GenresScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToPlaylist = {
                        navController.popBackStack()
                        // Playlist tab already re-reads on resume (PlaylistViewModel's
                        // LifecycleResumeEffect), so popping back to MainShell is
                        // sufficient — no separate "switch tab" signal is needed here
                        // since MainShell always shows Playlists as one of its tabs
                        // and the user lands back wherever they left MainShell.
                    },
                )
            }
        }

        composable(Screen.Settings.route) {
            PredictiveBackScreen(onBack = { navController.popBackStack() }) {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    onLoggedOut = {
                        navController.navigate(Screen.Onboarding.route) {
                            popUpTo(Screen.MainShell.route) { inclusive = true }
                        }
                    },
                    onOpenChooseApps = { navController.navigate(Screen.ScrobblerApps.route) },
                    onOpenDownloads = { navController.navigate(Screen.Downloads.route) },
                    onOpenExcludedSongs = { navController.navigate(Screen.ExcludedSongs.route) },
                    onOpenYouTubeImport = { navController.navigate(Screen.YouTubeImport.route) },
                    onOpenYouTubeLogin = { navController.navigate(Screen.YouTubeLogin.route) },
                )
            }
        }

        composable(Screen.YouTubeImport.route) {
            PredictiveBackScreen(onBack = { navController.popBackStack() }) {
                com.listenit.music.ui.settings.YouTubePlaylistImportScreen(
                    onBack = { navController.popBackStack() },
                    onImportSuccess = {
                        navController.popBackStack()
                    },
                )
            }
        }

        composable(Screen.YouTubeLogin.route) {
            PredictiveBackScreen(onBack = { navController.popBackStack() }) {
                com.listenit.music.ui.settings.YouTubeLoginScreen(
                    onBack = { navController.popBackStack() },
                    onConnected = { navController.popBackStack() },
                )
            }
        }

        composable(Screen.Downloads.route) {
            PredictiveBackScreen(onBack = { navController.popBackStack() }) {
                com.listenit.music.ui.settings.DownloadsScreen(onBack = { navController.popBackStack() })
            }
        }

        composable(Screen.ExcludedSongs.route) {
            PredictiveBackScreen(onBack = { navController.popBackStack() }) {
                com.listenit.music.ui.settings.ExcludedSongsScreen(onBack = { navController.popBackStack() })
            }
        }

        composable(Screen.ScrobblerApps.route) {
            PredictiveBackScreen(onBack = { navController.popBackStack() }) {
                com.listenit.music.ui.settings.ScrobblerAppsScreen(onBack = { navController.popBackStack() })
            }
        }

        composable(Screen.Search.route) {
            PredictiveBackScreen(onBack = { navController.popBackStack() }) {
                SearchScreen(
                    onBack = { navController.popBackStack() },
                    onOpenArtist = { name, browseId ->
                        navController.navigate(Screen.ArtistDetail.createRoute(name, browseId))
                    },
                    onOpenAlbum = { title, artist, browseId ->
                        navController.navigate(Screen.AlbumDetail.createRoute(title, artist, browseId))
                    },
                    onOpenPlaylist = { playlistId ->
                        navController.navigate(Screen.FeedPlaylistDetail.createRoute(playlistId))
                    },
                )
            }
        }

        composable(Screen.Discover.route) {
            PredictiveBackScreen(onBack = { navController.popBackStack() }) {
                DiscoverScreen(onBack = { navController.popBackStack() })
            }
        }

        composable(Screen.NewReleases.route) {
            PredictiveBackScreen(onBack = { navController.popBackStack() }) {
                com.listenit.music.ui.newreleases.NewReleasesScreen(
                    onBack = { navController.popBackStack() },
                )
            }
        }

        composable(
            route = Screen.FeedPlaylistDetail.route,
            arguments = listOf(
                androidx.navigation.navArgument("playlistId") {
                    type = androidx.navigation.NavType.StringType
                },
            ),
        ) { backStackEntry ->
            val playlistId = backStackEntry.arguments?.getString("playlistId").orEmpty()
            PredictiveBackScreen(onBack = { navController.popBackStack() }) {
                com.listenit.music.ui.feed.FeedPlaylistDetailScreen(
                    playlistId = playlistId,
                    onBack = { navController.popBackStack() },
                )
            }
        }

        composable(
            route = Screen.PlaylistDetail.route,
            arguments = listOf(
                androidx.navigation.navArgument("playlistId") {
                    type = androidx.navigation.NavType.LongType
                },
            ),
        ) { backStackEntry ->
            val playlistId = backStackEntry.arguments?.getLong("playlistId") ?: return@composable
            PredictiveBackScreen(onBack = { navController.popBackStack() }) {
                com.listenit.music.ui.playlist.PlaylistDetailScreen(
                    playlistId = playlistId,
                    onBack = { navController.popBackStack() },
                    onOpenPlaylist = { newId ->
                        navController.navigate(Screen.PlaylistDetail.createRoute(newId))
                    },
                )
            }
        }

        composable(
            route = Screen.ArtistDetail.route,
            arguments = listOf(
                androidx.navigation.navArgument("artistName") { type = androidx.navigation.NavType.StringType },
                androidx.navigation.navArgument("browseId") {
                    type = androidx.navigation.NavType.StringType
                    defaultValue = ""
                },
            ),
        ) { backStackEntry ->
            val artistName = backStackEntry.arguments?.getString("artistName").orEmpty()
            val browseId = backStackEntry.arguments?.getString("browseId")?.takeIf(String::isNotBlank)
            PredictiveBackScreen(onBack = { navController.popBackStack() }) {
                com.listenit.music.ui.artist.ArtistDetailScreen(
                    artistName = artistName,
                    browseId = browseId,
                    onBack = { navController.popBackStack() },
                    onOpenAlbum = { title, artist, id ->
                        navController.navigate(Screen.AlbumDetail.createRoute(title, artist, id))
                    },
                    onOpenArtist = { name, id ->
                        navController.navigate(Screen.ArtistDetail.createRoute(name, id))
                    },
                )
            }
        }

        composable(
            route = Screen.AlbumDetail.route,
            arguments = listOf(
                androidx.navigation.navArgument("albumTitle") { type = androidx.navigation.NavType.StringType },
                androidx.navigation.navArgument("artistName") {
                    type = androidx.navigation.NavType.StringType
                    defaultValue = ""
                },
                androidx.navigation.navArgument("browseId") {
                    type = androidx.navigation.NavType.StringType
                    defaultValue = ""
                },
            ),
        ) { backStackEntry ->
            val albumTitle = backStackEntry.arguments?.getString("albumTitle").orEmpty()
            val artistName = backStackEntry.arguments?.getString("artistName").orEmpty()
            val browseId = backStackEntry.arguments?.getString("browseId")?.takeIf(String::isNotBlank)
            PredictiveBackScreen(onBack = { navController.popBackStack() }) {
                com.listenit.music.ui.album.AlbumDetailScreen(
                    albumTitle = albumTitle,
                    artistName = artistName,
                    browseId = browseId,
                    onBack = { navController.popBackStack() },
                    onOpenArtist = { name, id ->
                        navController.navigate(Screen.ArtistDetail.createRoute(name, id))
                    },
                    onOpenAlbum = { title, artist, id ->
                        navController.navigate(Screen.AlbumDetail.createRoute(title, artist, id))
                    },
                )
            }
        }
    }
}

@Composable
private fun LaunchGate() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                tonalElevation = 6.dp,
                shadowElevation = 10.dp,
                modifier = Modifier.size(88.dp),
            ) {
                androidx.compose.foundation.Image(
                    painter = painterResource(com.listenit.music.R.drawable.ic_launcher_foreground),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            Spacer(Modifier.height(18.dp))
            Text("TuneGlass", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(20.dp))
            ExpressiveLoadingIndicator(message = "Preparing your music")
        }
    }
}

@Composable
fun ListenItNavHost(
    navController: NavHostController = rememberNavController(),
) {
    TuneGlassNavHost(navController = navController)
}
