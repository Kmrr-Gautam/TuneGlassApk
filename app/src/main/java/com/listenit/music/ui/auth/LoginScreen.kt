package com.listenit.music.ui.auth

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.listenit.music.R
import com.listenit.music.data.model.AuthState
import com.listenit.music.ui.common.FrequencyOrb
import com.listenit.music.ui.common.adaptiveContentWidth
import com.listenit.music.ui.theme.LiquidFrequency
import com.listenit.music.ui.theme.liquidGlassPanel

/**
 * Liquid Frequency Authentication & Account Connect Screen.
 *
 * Preserves the actual Last.fm Custom Tab auth exchange and backup restore logic
 * while upgrading the presentation into an atmospheric translucent experience.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    authState: AuthState,
    webAuthState: WebAuthState,
    onBeginSignIn: () -> Unit,
    onReturnedFromBrowser: () -> Unit,
    onCancelWebAuth: () -> Unit,
    onSignOut: () -> Unit,
    onRestoreBackupAndSignIn: (String) -> Unit,
    onDismissError: () -> Unit,
    onOpenDownloads: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    var restoreReadError by remember { mutableStateOf<String?>(null) }
    val restoreBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            val content = runCatching {
                context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                    ?: error("Could not read this backup")
            }
            content.onSuccess {
                restoreReadError = null
                onRestoreBackupAndSignIn(it)
            }.onFailure {
                restoreReadError = it.message ?: "Could not read this backup"
            }
        }
    }

    // The web flow returns its authorized token through the app callback.
    val awaitingUrl = (webAuthState as? WebAuthState.AwaitingApproval)?.authUrl
    LaunchedEffect(awaitingUrl) {
        val state = webAuthState as? WebAuthState.AwaitingApproval ?: return@LaunchedEffect
        CustomTabsIntent.Builder().build().launchUrl(context, android.net.Uri.parse(state.authUrl))
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) onReturnedFromBrowser()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LiquidFrequency.Obsidian),
        contentAlignment = Alignment.Center,
    ) {
        // Subtle ambient atmospheric glow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(LiquidFrequency.SubtleAtmosphericGradient),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .adaptiveContentWidth(maxWidth = 480.dp)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Frequency Orb Emblem
            FrequencyOrb(
                size = 130.dp,
                isPlaying = false,
                isAnimated = true,
                primaryColor = LiquidFrequency.ElectricCyan,
                secondaryColor = LiquidFrequency.Ultraviolet,
                accentColor = LiquidFrequency.SoftCoral,
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = "TUNEGLASS",
                style = MaterialTheme.typography.headlineMedium.copy(
                    letterSpacing = 4.sp,
                    fontWeight = FontWeight.Bold,
                ),
                color = LiquidFrequency.FrostedWhite,
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Connect with Last.fm or restore your local backup",
                style = MaterialTheme.typography.bodyMedium,
                color = LiquidFrequency.MutedLavender,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(32.dp))

            // Auth card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlassPanel(shape = RoundedCornerShape(24.dp), isActive = true),
                color = Color.Transparent,
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    if (webAuthState is WebAuthState.RestoringBackup) {
                        com.listenit.music.ui.common.ExpressiveLoadingIndicator(
                            message = "Restoring your music and settings…",
                        )
                    } else when (val state = authState) {
                        is AuthState.SignedIn -> SignedInCard(username = state.username, onSignOut = onSignOut)

                        AuthState.Unknown -> com.listenit.music.ui.common.ExpressiveLoadingIndicator()

                        else -> Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            when (webAuthState) {
                                is WebAuthState.AwaitingApproval -> {
                                    com.listenit.music.ui.common.ExpressiveLoadingIndicator(
                                        message = "Waiting for approval in the browser…",
                                    )
                                    Spacer(Modifier.height(18.dp))
                                    OutlinedButton(
                                        onClick = {
                                            CustomTabsIntent.Builder().build().launchUrl(
                                                context,
                                                android.net.Uri.parse(webAuthState.authUrl)
                                            )
                                        },
                                        shape = RoundedCornerShape(18.dp),
                                        border = LiquidFrequency.glassBorder(active = true),
                                    ) {
                                        Icon(
                                            Icons.Filled.OpenInBrowser,
                                            contentDescription = null,
                                            tint = LiquidFrequency.ElectricCyan,
                                            modifier = Modifier.padding(end = 8.dp),
                                        )
                                        Text("Open Last.fm Again", color = LiquidFrequency.FrostedWhite)
                                    }
                                    Spacer(Modifier.height(10.dp))
                                    TextButton(onClick = onCancelWebAuth) {
                                        Text("Cancel", color = LiquidFrequency.MutedLavender)
                                    }
                                }

                                else -> {
                                    val busy = state is AuthState.SigningIn || webAuthState is WebAuthState.CompletingSignIn

                                    Button(
                                        onClick = onBeginSignIn,
                                        enabled = !busy,
                                        shape = RoundedCornerShape(20.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = LiquidFrequency.ElectricCyan,
                                            contentColor = LiquidFrequency.Obsidian,
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(54.dp)
                                            .shadow(12.dp, RoundedCornerShape(20.dp), spotColor = LiquidFrequency.ElectricCyan),
                                    ) {
                                        if (busy) {
                                            com.listenit.music.ui.common.ExpressiveInlineLoadingIndicator(
                                                color = LiquidFrequency.Obsidian,
                                                size = 20.dp,
                                            )
                                        } else {
                                            Text(
                                                "Connect with Last.fm",
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            )
                                        }
                                    }

                                    Spacer(Modifier.height(14.dp))

                                    OutlinedButton(
                                        onClick = {
                                            restoreBackupLauncher.launch(
                                                arrayOf("application/json", "text/plain", "*/*")
                                            )
                                        },
                                        enabled = !busy,
                                        shape = RoundedCornerShape(20.dp),
                                        border = LiquidFrequency.glassBorder(),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(52.dp),
                                    ) {
                                        Icon(
                                            Icons.Filled.CloudDownload,
                                            contentDescription = null,
                                            tint = LiquidFrequency.ElectricCyan,
                                            modifier = Modifier.padding(end = 8.dp),
                                        )
                                        Text("Restore Backup & Login", color = LiquidFrequency.FrostedWhite)
                                    }

                                    Spacer(Modifier.height(14.dp))

                                    Text(
                                        text = "Choose a TuneGlass backup file to sync your local playlists and metadata.",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                        color = LiquidFrequency.MutedLavender,
                                        textAlign = TextAlign.Center,
                                    )

                                    if (onOpenDownloads != null) {
                                        Spacer(Modifier.height(14.dp))
                                        TextButton(onClick = onOpenDownloads) {
                                            Icon(
                                                imageVector = Icons.Filled.Download,
                                                contentDescription = null,
                                                tint = LiquidFrequency.ElectricCyan,
                                                modifier = Modifier.padding(end = 6.dp),
                                            )
                                            Text("Offline Library & Downloads", color = LiquidFrequency.ElectricCyan)
                                        }
                                    }
                                }
                            }

                            val errorMessage = (state as? AuthState.Error)?.message
                                ?: (webAuthState as? WebAuthState.Error)?.message
                                ?: restoreReadError
                            if (errorMessage != null) {
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    errorMessage,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error,
                                    textAlign = TextAlign.Center,
                                )
                                Spacer(Modifier.height(4.dp))
                                TextButton(onClick = onDismissError) {
                                    Text("Dismiss", color = LiquidFrequency.MutedLavender)
                                }
                            }
                        }
                    }

                    if (authState is AuthState.SignedIn && webAuthState is WebAuthState.Error) {
                        Spacer(Modifier.height(16.dp))
                        Text(
                            webAuthState.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                        )
                        TextButton(onClick = onDismissError) {
                            Text("Continue", color = LiquidFrequency.ElectricCyan)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SignedInCard(username: String, onSignOut: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(LiquidFrequency.ElectricCyan.copy(alpha = 0.15f))
                .border(1.dp, LiquidFrequency.ElectricCyan.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = LiquidFrequency.ElectricCyan,
                modifier = Modifier.size(28.dp),
            )
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text = "Connected as $username",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = LiquidFrequency.FrostedWhite,
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = "Scrobbling and library synchronizations active",
            style = MaterialTheme.typography.bodySmall,
            color = LiquidFrequency.MutedLavender,
        )

        Spacer(Modifier.height(20.dp))

        OutlinedButton(
            onClick = onSignOut,
            shape = RoundedCornerShape(18.dp),
            border = LiquidFrequency.glassBorder(),
        ) {
            Text("Sign Out", color = LiquidFrequency.SoftCoral)
        }
    }
}
