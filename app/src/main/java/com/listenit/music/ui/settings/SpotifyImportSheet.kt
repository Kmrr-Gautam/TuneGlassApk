package com.listenit.music.ui.settings

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Message
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.listenit.music.data.playlist.SavedPlaylist
import com.listenit.music.data.playlist.SpotifyAuthManager
import com.listenit.music.data.playlist.SpotifyPlaylistFetcher
import com.listenit.music.data.playlist.SpotifyPlaylistResult
import com.listenit.music.data.playlist.SpotifyRawTrack
import com.listenit.music.data.playlist.SpotifyUserPlaylist
import com.listenit.music.ui.common.adaptiveContentWidth
import com.listenit.music.ui.common.safeDrawingBottomPadding
import kotlinx.coroutines.launch

private val SpotifyGreen = Color(0xFF1DB954)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpotifyImportSheet(
    onDismiss: () -> Unit,
    spotifyFetcher: SpotifyPlaylistFetcher,
    authManager: SpotifyAuthManager,
    onImportSuccess: (SavedPlaylist) -> Unit,
) {
    val clipboard = LocalClipboardManager.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val accountState by authManager.accountState.collectAsState()

    var showLoginWebView by remember { mutableStateOf(!accountState.isConnected) }
    var userPlaylists by remember { mutableStateOf<List<SpotifyUserPlaylist>>(emptyList()) }
    var isLoadingPlaylists by remember { mutableStateOf(false) }
    var playlistLoadError by remember { mutableStateOf<String?>(null) }
    var playlistSearchQuery by remember { mutableStateOf("") }

    var selectedPlaylistForDetail by remember { mutableStateOf<SpotifyUserPlaylist?>(null) }
    var loadedPlaylist by remember { mutableStateOf<SpotifyPlaylistResult?>(null) }
    var isLoadingTracks by remember { mutableStateOf(false) }
    var selectedTracks by remember { mutableStateOf<Set<SpotifyRawTrack>>(emptySet()) }

    var isImporting by remember { mutableStateOf(false) }
    var importProgress by remember { mutableFloatStateOf(0f) }
    var importProgressText by remember { mutableStateOf("") }
    var importError by remember { mutableStateOf<String?>(null) }

    var showUrlFallback by remember { mutableStateOf(false) }
    var urlInput by remember { mutableStateOf("") }

    fun loadUserPlaylists() {
        if (!accountState.isConnected) return
        isLoadingPlaylists = true
        playlistLoadError = null
        scope.launch {
            try {
                val list = authManager.fetchUserPlaylists()
                userPlaylists = list
            } catch (e: Exception) {
                playlistLoadError = e.message ?: "Failed to fetch Spotify playlists."
            } finally {
                isLoadingPlaylists = false
            }
        }
    }

    LaunchedEffect(accountState.isConnected) {
        if (accountState.isConnected) {
            showLoginWebView = false
            loadUserPlaylists()
        }
    }

    fun selectPlaylist(playlist: SpotifyUserPlaylist) {
        selectedPlaylistForDetail = playlist
        isLoadingTracks = true
        loadedPlaylist = null
        selectedTracks = emptySet()
        importError = null

        scope.launch {
            try {
                val result = authManager.fetchPlaylistTracks(playlist.id)
                loadedPlaylist = result
                selectedTracks = result.tracks.toSet()
            } catch (e: Exception) {
                importError = e.message ?: "Failed to load tracks for ${playlist.name}"
            } finally {
                isLoadingTracks = false
            }
        }
    }

    fun fetchFromUrl() {
        if (urlInput.isBlank()) return
        isLoadingTracks = true
        importError = null
        selectedPlaylistForDetail = null
        scope.launch {
            try {
                val result = spotifyFetcher.fetchPlaylist(urlInput.trim())
                loadedPlaylist = result
                selectedTracks = result.tracks.toSet()
            } catch (e: Exception) {
                importError = e.message ?: "Failed to fetch Spotify playlist. Ensure the link is valid."
            } finally {
                isLoadingTracks = false
            }
        }
    }

    fun executeImport() {
        val playlist = loadedPlaylist ?: return
        val toImport = playlist.tracks.filter { it in selectedTracks }
        if (toImport.isEmpty()) return

        isImporting = true
        importProgress = 0f
        importProgressText = "Matching 0 of ${toImport.size} tracks..."

        scope.launch {
            try {
                val saved = spotifyFetcher.importSpotifyPlaylist(
                    spotifyResult = playlist,
                    selectedTracks = toImport,
                    onProgress = { current, total ->
                        importProgress = current.toFloat() / total.toFloat()
                        importProgressText = "Matched $current of $total tracks..."
                    },
                )
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onImportSuccess(saved)
                onDismiss()
            } catch (e: Exception) {
                importError = e.message ?: "Import failed. Please try again."
                isImporting = false
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                Modifier
                    .padding(vertical = 10.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)),
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .adaptiveContentWidth(maxWidth = 640.dp)
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp + safeDrawingBottomPadding()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // ── WebView Login View (if active) ───────────────────────────────
            if (showLoginWebView) {
                var isWebViewLoading by remember { mutableStateOf(true) }
                var webViewError by remember { mutableStateOf<String?>(null) }
                var webViewInstance by remember { mutableStateOf<WebView?>(null) }
                var isCapturingSession by remember { mutableStateOf(false) }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = { showLoginWebView = false }) {
                        Icon(Icons.Filled.Clear, contentDescription = "Close sign-in")
                    }
                    Text(
                        "Sign in to Spotify",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(onClick = {
                        showLoginWebView = false
                        showUrlFallback = true
                    }) {
                        Text(
                            "Use public link",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(490.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface),
                ) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { ctx ->
                            WebView(ctx).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                )
                                @SuppressLint("SetJavaScriptEnabled")
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                settings.databaseEnabled = true
                                settings.javaScriptCanOpenWindowsAutomatically = true
                                settings.setSupportMultipleWindows(true)
                                settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                                settings.cacheMode = WebSettings.LOAD_DEFAULT
                                settings.useWideViewPort = true
                                settings.loadWithOverviewMode = true
                                settings.userAgentString =
                                    "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"

                                CookieManager.getInstance().setAcceptCookie(true)
                                CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

                                fun extractSessionIfPresent(v: WebView?) {
                                    if (isCapturingSession) return
                                    val cm = CookieManager.getInstance()
                                    val cookies = listOfNotNull(
                                        cm.getCookie("https://open.spotify.com"),
                                        cm.getCookie("https://accounts.spotify.com"),
                                        cm.getCookie("https://spotify.com"),
                                    ).joinToString("; ")
                                    if (cookies.contains("sp_dc=")) {
                                        isCapturingSession = true
                                        v?.evaluateJavascript(
                                            "fetch('https://open.spotify.com/get_access_token?reason=transport&productType=web_player').then(r => r.json()).then(d => JSON.stringify(d)).catch(e => '')",
                                        ) { tokenJson ->
                                            scope.launch {
                                                val clean = tokenJson?.trim('"', ' ', '\n')
                                                    ?.replace("\\\"", "\"")
                                                authManager.onSessionCaptured(cookies, clean)
                                                showLoginWebView = false
                                                loadUserPlaylists()
                                            }
                                        }
                                    }
                                }

                                webChromeClient = object : WebChromeClient() {
                                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                        super.onProgressChanged(view, newProgress)
                                        if (newProgress >= 85) {
                                            isWebViewLoading = false
                                        }
                                        extractSessionIfPresent(view)
                                    }

                                    override fun onCreateWindow(
                                        view: WebView?,
                                        isDialog: Boolean,
                                        isUserGesture: Boolean,
                                        resultMsg: Message?,
                                    ): Boolean {
                                        val newWebView = WebView(ctx).apply {
                                            settings.javaScriptEnabled = true
                                            settings.domStorageEnabled = true
                                            settings.userAgentString =
                                                "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"
                                            webViewClient = object : WebViewClient() {
                                                override fun shouldOverrideUrlLoading(v: WebView?, req: WebResourceRequest?): Boolean {
                                                    val u = req?.url?.toString() ?: return false
                                                    if (u.startsWith("http://") || u.startsWith("https://")) {
                                                        view?.loadUrl(u)
                                                        return true
                                                    }
                                                    return false
                                                }
                                            }
                                        }
                                        val transport = resultMsg?.obj as? WebView.WebViewTransport
                                        transport?.webView = newWebView
                                        resultMsg?.sendToTarget()
                                        return true
                                    }
                                }

                                webViewClient = object : WebViewClient() {
                                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                        super.onPageStarted(view, url, favicon)
                                        webViewError = null
                                        extractSessionIfPresent(view)
                                    }

                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        super.onPageFinished(view, url)
                                        isWebViewLoading = false
                                        extractSessionIfPresent(view)
                                    }

                                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                        val url = request?.url?.toString() ?: return false
                                        if (url.startsWith("http://") || url.startsWith("https://")) {
                                            return false
                                        }
                                        return try {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                            ctx.startActivity(intent)
                                            true
                                        } catch (e: Exception) {
                                            true
                                        }
                                    }

                                    override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                                        super.onReceivedError(view, request, error)
                                        if (request?.isForMainFrame == true) {
                                            isWebViewLoading = false
                                            webViewError = error?.description?.toString() ?: "Failed to load Spotify login."
                                        }
                                    }
                                }

                                webViewInstance = this
                                loadUrl("https://accounts.spotify.com/en/login?continue=https%3A%2F%2Fopen.spotify.com%2F")
                            }
                        },
                    )

                    if (isWebViewLoading && webViewError == null) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = SpotifyGreen)
                                Spacer(Modifier.height(14.dp))
                                Text(
                                    "Loading Spotify sign-in…",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }

                    if (webViewError != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(24.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Icon(
                                    Icons.Filled.Clear,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(36.dp),
                                )
                                Text(
                                    "Unable to open Spotify sign-in",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    webViewError ?: "Network or connection error",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                )
                                Spacer(Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Button(
                                        onClick = {
                                            webViewError = null
                                            isWebViewLoading = true
                                            webViewInstance?.reload()
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = SpotifyGreen,
                                            contentColor = Color.Black,
                                        ),
                                    ) {
                                        Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Retry")
                                    }
                                    FilledTonalButton(
                                        onClick = {
                                            showLoginWebView = false
                                            showUrlFallback = true
                                        },
                                    ) {
                                        Text("Paste URL instead")
                                    }
                                }
                            }
                        }
                    }
                }
                return@ModalBottomSheet
            }

            // ── Track Inspection & Import View ───────────────────────────────
            if (loadedPlaylist != null || isLoadingTracks) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = {
                        loadedPlaylist = null
                        selectedPlaylistForDetail = null
                        importError = null
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to playlists")
                    }
                    Column(Modifier.weight(1f)) {
                        Text(
                            loadedPlaylist?.title ?: selectedPlaylistForDetail?.name ?: "Loading playlist...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            "${selectedTracks.size} of ${loadedPlaylist?.tracks?.size ?: 0} tracks selected",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Clear, contentDescription = "Close")
                    }
                }

                if (isLoadingTracks) {
                    Box(
                        Modifier.fillMaxWidth().height(200.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = SpotifyGreen)
                            Spacer(Modifier.height(12.dp))
                            Text("Fetching playlist tracks...", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                } else if (loadedPlaylist != null) {
                    val pl = loadedPlaylist!!
                    // Playlist Header Card
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            if (!pl.artworkUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = pl.artworkUrl,
                                    contentDescription = pl.title,
                                    modifier = Modifier.size(54.dp).clip(RoundedCornerShape(12.dp)),
                                )
                                Spacer(Modifier.width(12.dp))
                            }
                            Column(Modifier.weight(1f)) {
                                Text(pl.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(
                                    "${pl.tracks.size} tracks · By ${pl.author ?: "Spotify"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            TextButton(
                                onClick = {
                                    selectedTracks = if (selectedTracks.size == pl.tracks.size) emptySet() else pl.tracks.toSet()
                                },
                            ) {
                                Text(if (selectedTracks.size == pl.tracks.size) "Deselect All" else "Select All")
                            }
                        }
                    }

                    // Track List
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 280.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        items(pl.tracks) { track ->
                            val isChecked = track in selectedTracks
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isChecked) MaterialTheme.colorScheme.surfaceContainerHighest else MaterialTheme.colorScheme.surfaceContainerLow,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedTracks = if (isChecked) selectedTracks - track else selectedTracks + track
                                    },
                            ) {
                                Row(
                                    Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = { checked ->
                                            selectedTracks = if (checked) selectedTracks + track else selectedTracks - track
                                        },
                                        colors = CheckboxDefaults.colors(checkedColor = SpotifyGreen),
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(track.title, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(
                                            track.artist,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Import Progress or Button
                    if (isImporting) {
                        Column(
                            Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            LinearProgressIndicator(
                                progress = { importProgress },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                                color = SpotifyGreen,
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(importProgressText, style = MaterialTheme.typography.bodySmall)
                        }
                    } else {
                        Button(
                            onClick = ::executeImport,
                            enabled = selectedTracks.isNotEmpty(),
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SpotifyGreen,
                                contentColor = Color.Black,
                            ),
                        ) {
                            Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Import ${selectedTracks.size} Tracks into TuneGlass", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                return@ModalBottomSheet
            }

            // ── Main View: Account & Playlists ───────────────────────────────
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SpotifyGreen),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Filled.LibraryMusic,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        "Spotify Playlists",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        if (accountState.isConnected) "Logged in as ${accountState.displayName}" else "Sign in to import your playlists",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Clear, contentDescription = "Close")
                }
            }

            if (!accountState.isConnected) {
                // Not Logged In: Show Connect Card
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            Modifier.size(60.dp).clip(CircleShape).background(SpotifyGreen.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Filled.AccountCircle, contentDescription = null, tint = SpotifyGreen, modifier = Modifier.size(34.dp))
                        }
                        Spacer(Modifier.height(14.dp))
                        Text(
                            "Connect Spotify Account",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Log in to view and select all your created and followed Spotify playlists for seamless import into TuneGlass.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        )
                        Spacer(Modifier.height(18.dp))
                        Button(
                            onClick = { showLoginWebView = true },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SpotifyGreen,
                                contentColor = Color.Black,
                            ),
                        ) {
                            Icon(Icons.Filled.Login, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Log In to Spotify", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Public URL Alternative
                Row(
                    Modifier.fillMaxWidth().clickable { showUrlFallback = !showUrlFallback }.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.Link, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Or import via public link", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                }

                if (showUrlFallback) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = urlInput,
                            onValueChange = { urlInput = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Paste open.spotify.com playlist URL...") },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        FilledTonalButton(
                            onClick = ::fetchFromUrl,
                            enabled = urlInput.isNotBlank(),
                            shape = RoundedCornerShape(14.dp),
                        ) {
                            Text("Fetch")
                        }
                    }
                }
            } else {
                // Logged In: Show Account Row & Playlists
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (!accountState.avatarUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = accountState.avatarUrl,
                            contentDescription = accountState.displayName,
                            modifier = Modifier.size(34.dp).clip(CircleShape),
                        )
                    } else {
                        Box(
                            Modifier.size(34.dp).clip(CircleShape).background(SpotifyGreen),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                accountState.displayName.take(1).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                fontSize = 14.sp,
                            )
                        }
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(accountState.displayName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text("${userPlaylists.size} playlists available", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = ::loadUserPlaylists) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh playlists", modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = { authManager.logout() }) {
                        Icon(Icons.Filled.Logout, contentDescription = "Log out", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                    }
                }

                // Search Bar
                OutlinedTextField(
                    value = playlistSearchQuery,
                    onValueChange = { playlistSearchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search your Spotify playlists...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    trailingIcon = {
                        if (playlistSearchQuery.isNotBlank()) {
                            IconButton(onClick = { playlistSearchQuery = "" }) {
                                Icon(Icons.Filled.Clear, contentDescription = "Clear search", modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    ),
                )

                // Playlist List
                val filtered = userPlaylists.filter {
                    playlistSearchQuery.isBlank() || it.name.contains(playlistSearchQuery, ignoreCase = true)
                }

                if (isLoadingPlaylists) {
                    Box(Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = SpotifyGreen)
                    }
                } else if (playlistLoadError != null) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(playlistLoadError.orEmpty(), color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.bodySmall)
                            Spacer(Modifier.height(8.dp))
                            FilledTonalButton(onClick = ::loadUserPlaylists) {
                                Text("Retry")
                            }
                        }
                    }
                } else if (filtered.isEmpty()) {
                    Box(Modifier.fillMaxWidth().height(140.dp), contentAlignment = Alignment.Center) {
                        Text("No playlists found.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 380.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(filtered) { playlist ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectPlaylist(playlist) },
                            ) {
                                Row(
                                    Modifier.fillMaxWidth().padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    if (!playlist.artworkUrl.isNullOrBlank()) {
                                        AsyncImage(
                                            model = playlist.artworkUrl,
                                            contentDescription = playlist.name,
                                            modifier = Modifier.size(52.dp).clip(RoundedCornerShape(12.dp)),
                                        )
                                    } else {
                                        Box(
                                            Modifier.size(52.dp).clip(RoundedCornerShape(12.dp)).background(SpotifyGreen.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Icon(Icons.Filled.QueueMusic, contentDescription = null, tint = SpotifyGreen)
                                        }
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(playlist.name, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(
                                            "${playlist.trackCount} tracks · ${if (playlist.isOwner) "Created by you" else "by " + (playlist.ownerName ?: "Spotify")}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                    FilledTonalButton(
                                        onClick = { selectPlaylist(playlist) },
                                        shape = RoundedCornerShape(12.dp),
                                    ) {
                                        Text("Select")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (importError != null) {
                Text(
                    importError.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }
        }
    }
}
