package com.listenit.music.ui.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.listenit.music.playback.PlaybackChromeState
import com.listenit.music.playback.PlaybackProgressState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow

/**
 * Universal Dynamic Island for Android.
 * Compatible with ALL Android smartphone designs (Samsung One UI, OnePlus OxygenOS,
 * Oppo ColorOS, Vivo FuntouchOS, Xiaomi HyperOS, Google Pixel, Motorola, etc.).
 *
 * Positions comfortably over the status bar notch / punch hole area with smooth
 * morphing between compact floating pill and interactive expanded media control card.
 */
@Composable
fun DynamicIsland(
    state: PlaybackChromeState,
    progressState: StateFlow<PlaybackProgressState>,
    onToggle: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onOpenPlayer: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val track = state.current ?: return
    val progress by progressState.collectAsStateWithLifecycle()
    val haptics = LocalHapticFeedback.current
    var isExpanded by remember { mutableStateOf(false) }

    // Auto-collapse expanded state after 6 seconds of idle
    LaunchedEffect(isExpanded, state.isPlaying, track.videoId) {
        if (isExpanded) {
            delay(6000)
            isExpanded = false
        }
    }

    Box(
        modifier = modifier
            .statusBarsPadding()
            .padding(top = 4.dp)
            .zIndex(100f),
        contentAlignment = Alignment.TopCenter,
    ) {
        Surface(
            shape = if (isExpanded) RoundedCornerShape(26.dp) else CircleShape,
            color = Color(0xFF0C0C0F).copy(alpha = 0.96f),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isExpanded) Color.White.copy(alpha = 0.16f) else Color.White.copy(alpha = 0.13f),
            ),
            shadowElevation = if (isExpanded) 18.dp else 12.dp,
            modifier = Modifier
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = 0.8f,
                        stiffness = 380f,
                    ),
                )
                .shadow(
                    elevation = if (isExpanded) 20.dp else 14.dp,
                    shape = if (isExpanded) RoundedCornerShape(26.dp) else CircleShape,
                    spotColor = Color.Black.copy(alpha = 0.75f),
                    ambientColor = Color.Black.copy(alpha = 0.5f),
                )
                .pointerInput(Unit) {
                    var totalDragX = 0f
                    detectHorizontalDragGestures(
                        onDragStart = { totalDragX = 0f },
                        onDragEnd = {
                            if (totalDragX > 70f) {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onNext()
                            } else if (totalDragX < -70f) {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onPrevious()
                            }
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            totalDragX += dragAmount
                        },
                    )
                },
        ) {
            if (!isExpanded) {
                // ── COMPACT FLOATING PILL ──
                Row(
                    modifier = Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                isExpanded = true
                            },
                        )
                        .padding(horizontal = 7.dp, vertical = 5.dp)
                        .height(30.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    // Left: Miniature album artwork
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E1E24)),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (!track.artworkUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = track.artworkUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize(),
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.MusicNote,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(12.dp),
                            )
                        }
                    }

                    // Center: Track title and artist
                    Column(
                        modifier = Modifier.widthIn(max = 140.dp),
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = track.title,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.2).sp,
                            ),
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = track.artist,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Normal,
                            ),
                            color = Color.White.copy(alpha = 0.6f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    // Right: Animated live Equalizer Waveform bars
                    MiniIslandEqualizer(isPlaying = state.isPlaying)
                }
            } else {
                // ── EXPANDED INTERACTIVE ISLAND CARD ──
                Column(
                    modifier = Modifier
                        .widthIn(min = 310.dp, max = 345.dp)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    // Top row: Art + Metadata + Tap to FullPlayer
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onOpenPlayer,
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF1E1E24)),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (!track.artworkUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = track.artworkUrl,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.MusicNote,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = track.title,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                ),
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = track.artist,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                ),
                                color = Color.White.copy(alpha = 0.65f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }

                        // Close / collapse button
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.10f))
                                .clickable { isExpanded = false },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "✕",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = Color.White.copy(alpha = 0.8f),
                            )
                        }
                    }

                    // Progress bar
                    val currentFraction = if (progress.durationMs > 0) {
                        (progress.positionMs.toFloat() / progress.durationMs.toFloat()).coerceIn(0f, 1f)
                    } else 0f

                    LinearProgressIndicator(
                        progress = { currentFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .clip(CircleShape),
                        color = Color.White.copy(alpha = 0.9f),
                        trackColor = Color.White.copy(alpha = 0.15f),
                        strokeCap = StrokeCap.Round,
                    )

                    // Bottom row: Playback Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        IconButton(
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onPrevious()
                            },
                            modifier = Modifier.size(36.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.SkipPrevious,
                                contentDescription = "Previous",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp),
                            )
                        }

                        // Play / Pause prominent disc
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .clickable {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onToggle()
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = if (state.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = if (state.isPlaying) "Pause" else "Play",
                                tint = Color.Black,
                                modifier = Modifier.size(20.dp),
                            )
                        }

                        IconButton(
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onNext()
                            },
                            modifier = Modifier.size(36.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.SkipNext,
                                contentDescription = "Next",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 3-bar animated micro-equalizer that dances while audio playback is active.
 */
@Composable
private fun MiniIslandEqualizer(isPlaying: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "island_eq")

    val bar1 by infiniteTransition.animateFloat(
        initialValue = 4f,
        targetValue = if (isPlaying) 15f else 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(420, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "eq_bar1",
    )
    val bar2 by infiniteTransition.animateFloat(
        initialValue = if (isPlaying) 14f else 4f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(310, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "eq_bar2",
    )
    val bar3 by infiniteTransition.animateFloat(
        initialValue = 6f,
        targetValue = if (isPlaying) 16f else 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(480, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "eq_bar3",
    )

    Row(
        modifier = Modifier.height(18.dp).padding(end = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val barColor = Color(0xFF22C55E) // Modern calm emerald pulse
        Box(
            modifier = Modifier
                .width(2.5.dp)
                .height(bar1.dp)
                .clip(CircleShape)
                .background(barColor),
        )
        Box(
            modifier = Modifier
                .width(2.5.dp)
                .height(bar2.dp)
                .clip(CircleShape)
                .background(barColor),
        )
        Box(
            modifier = Modifier
                .width(2.5.dp)
                .height(bar3.dp)
                .clip(CircleShape)
                .background(barColor),
        )
    }
}
