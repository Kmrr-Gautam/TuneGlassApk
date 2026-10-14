package com.listenit.music.ui.player

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.listenit.music.ui.common.LocalAppHaptics
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.listenit.music.playback.MusicPlayerState
import kotlin.math.PI
import kotlin.math.min
import kotlin.math.sin

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import com.listenit.music.data.local.MusicBarStyle

/** Warm paper white used for the played portion when drawn over artwork. */
private val Paper = Color(0xFFF6F2EC)

private fun smoothstep(t: Float): Float {
    val c = t.coerceIn(0f, 1f)
    return c * c * (3f - 2f * c)
}

/**
 * Expressive seekbar supporting 4 customizable styles:
 * 1. LENS - Minimal hairline track with glass ring indicator
 * 2. NEON - Cyberpunk electric cyan/violet gradient with glowing pulse
 * 3. VINYL - Analog wavy groove track with warm bronze thumb
 * 4. PILLBAR - Modern rounded capsule track with pill indicator
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WavySeekBar(
    positionMs: Long,
    durationMs: Long,
    isPlaying: Boolean,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier,
    isTranslucent: Boolean = false,
    trackKey: String? = null,
    showTimeLabels: Boolean = true,
    wavy: Boolean = true,
    style: MusicBarStyle = MusicBarStyle.LENS,
    crossfadeEnabled: Boolean = false,
    crossfadeSeconds: Int = 5,
) {
    val appHaptics = LocalAppHaptics.current
    val interactionSource = remember(trackKey) { MutableInteractionSource() }
    val isDragged by interactionSource.collectIsDraggedAsState()
    var isInteracting by remember(trackKey) { mutableStateOf(false) }
    var dragPositionMs by remember(trackKey) { mutableFloatStateOf(0f) }
    var lastHapticSecond by remember(trackKey) { mutableLongStateOf(-1L) }
    val dragging = isDragged || isInteracting

    val boundedDurationMs = durationMs.coerceAtLeast(0L)
    val boundedPositionMs = if (boundedDurationMs > 0L) {
        positionMs.coerceIn(0L, boundedDurationMs)
    } else {
        0L
    }
    val shownMs = if (dragging) {
        dragPositionMs.toLong().coerceIn(0L, boundedDurationMs)
    } else {
        boundedPositionMs
    }
    val shownFraction = if (boundedDurationMs > 0L) {
        (shownMs.toDouble() / boundedDurationMs.toDouble()).toFloat().coerceIn(0f, 1f)
    } else {
        0f
    }

    val activeColor = if (isTranslucent) Paper else MaterialTheme.colorScheme.primary
    val inactiveColor = if (isTranslucent) {
        Color.White.copy(alpha = 0.26f)
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.16f)
    }
    val textColor = if (isTranslucent) {
        Color.White.copy(alpha = 0.78f)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    val density = LocalDensity.current
    val infiniteTransition = rememberInfiniteTransition(label = "barAnimation")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "barWavePhase",
    )
    val neonPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "neonPulse",
    )

    val rippleAmplitudeDp = if (wavy && isPlaying && !dragging) 3.2f else 0f
    val amplitude by animateFloatAsState(
        targetValue = with(density) { rippleAmplitudeDp.dp.toPx() },
        animationSpec = tween(durationMillis = 420),
        label = "barRippleAmp",
    )
    val thumbGrow by animateFloatAsState(
        targetValue = if (dragging) 1f else 0f,
        animationSpec = tween(durationMillis = 160),
        label = "barThumbGrow",
    )



    val ripplePath = remember { Path() }
    val inset = if (style == MusicBarStyle.PILLBAR) 6.dp else 8.dp

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val insetPx = inset.toPx()
                val startX = insetPx
                val endX = (size.width - insetPx).coerceAtLeast(startX)
                val cy = size.height / 2f
                val thumbX = startX + (endX - startX) * shownFraction

                when (style) {
                    MusicBarStyle.LENS -> {
                        val hairline = 2.dp.toPx()
                        // Track
                        drawLine(
                            color = inactiveColor,
                            start = Offset(startX, cy),
                            end = Offset(endX, cy),
                            strokeWidth = hairline,
                            cap = StrokeCap.Round,
                        )
                        // Played portion
                        if (thumbX > startX + 0.5f) {
                            if (amplitude < 0.4f) {
                                drawLine(
                                    color = activeColor,
                                    start = Offset(startX, cy),
                                    end = Offset(thumbX, cy),
                                    strokeWidth = 2.8.dp.toPx(),
                                    cap = StrokeCap.Round,
                                )
                            } else {
                                val length = thumbX - startX
                                val taper = min(44.dp.toPx(), length * 0.5f).coerceAtLeast(1f)
                                val wavelength = 62.dp.toPx()
                                val k = (2 * PI / wavelength).toFloat()
                                val step = 2.dp.toPx()
                                ripplePath.reset()
                                ripplePath.moveTo(startX, cy)
                                var x = startX
                                while (x < thumbX) {
                                    val env = smoothstep((x - startX) / taper) * smoothstep((thumbX - x) / taper)
                                    val y = cy - sin((x - startX) * k - phase) * amplitude * env
                                    ripplePath.lineTo(x, y)
                                    x += step
                                }
                                ripplePath.lineTo(thumbX, cy)
                                drawPath(
                                    path = ripplePath,
                                    color = activeColor,
                                    style = Stroke(width = 2.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
                                )
                            }
                        }
                        // Ring thumb
                        if (boundedDurationMs > 0L) {
                            val ringRadius = (6.5f + 3.5f * thumbGrow).dp.toPx()
                            drawCircle(
                                color = activeColor.copy(alpha = 0.55f),
                                radius = ringRadius,
                                center = Offset(thumbX, cy),
                                style = Stroke(width = 1.4.dp.toPx()),
                            )
                            drawCircle(
                                color = activeColor,
                                radius = (3.2f + 1.2f * thumbGrow).dp.toPx(),
                                center = Offset(thumbX, cy),
                            )
                        }
                    }

                    MusicBarStyle.NEON -> {
                        // Cyberpunk Neon Glow Track
                        val neonCyan = Color(0xFF00E5FF)
                        val neonPurple = Color(0xFF9D46FF)

                        // Inactive track with dark subtle cyan hue
                        drawLine(
                            color = neonCyan.copy(alpha = 0.20f),
                            start = Offset(startX, cy),
                            end = Offset(endX, cy),
                            strokeWidth = 2.dp.toPx(),
                            cap = StrokeCap.Round,
                        )

                        if (thumbX > startX + 0.5f) {
                            val neonBrush = Brush.horizontalGradient(
                                colors = listOf(neonCyan, neonPurple, neonCyan),
                                startX = startX,
                                endX = thumbX,
                            )
                            // Outer glow layer
                            drawLine(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(neonCyan.copy(alpha = 0.35f), neonPurple.copy(alpha = 0.35f)),
                                    startX = startX,
                                    endX = thumbX,
                                ),
                                start = Offset(startX, cy),
                                end = Offset(thumbX, cy),
                                strokeWidth = (6f + 2f * thumbGrow).dp.toPx(),
                                cap = StrokeCap.Round,
                            )
                            // Core crisp line
                            drawLine(
                                brush = neonBrush,
                                start = Offset(startX, cy),
                                end = Offset(thumbX, cy),
                                strokeWidth = 3.dp.toPx(),
                                cap = StrokeCap.Round,
                            )
                        }

                        // Electric pulse thumb
                        if (boundedDurationMs > 0L) {
                            val pulseRadius = (10f * neonPulse + 4f * thumbGrow).dp.toPx()
                            drawCircle(
                                color = neonCyan.copy(alpha = 0.30f),
                                radius = pulseRadius,
                                center = Offset(thumbX, cy),
                            )
                            drawCircle(
                                color = neonCyan,
                                radius = (5.5f + 1.5f * thumbGrow).dp.toPx(),
                                center = Offset(thumbX, cy),
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 2.5.dp.toPx(),
                                center = Offset(thumbX, cy),
                            )
                        }
                    }

                    MusicBarStyle.VINYL -> {
                        // Analog vinyl warm groove track
                        val vinylGold = Color(0xFFFFB300)
                        val vinylMuted = Color(0xFF8D7B68)

                        // 3 subtle microgroove lines
                        val grooveSpacing = 2.2.dp.toPx()
                        for (offsetMultiplier in listOf(-1, 0, 1)) {
                            val gy = cy + offsetMultiplier * grooveSpacing
                            drawLine(
                                color = vinylMuted.copy(alpha = if (offsetMultiplier == 0) 0.35f else 0.18f),
                                start = Offset(startX, gy),
                                end = Offset(endX, gy),
                                strokeWidth = 1.2.dp.toPx(),
                                cap = StrokeCap.Round,
                            )
                        }

                        // Played portion on center groove
                        if (thumbX > startX + 0.5f) {
                            if (amplitude < 0.4f) {
                                drawLine(
                                    color = vinylGold,
                                    start = Offset(startX, cy),
                                    end = Offset(thumbX, cy),
                                    strokeWidth = 2.8.dp.toPx(),
                                    cap = StrokeCap.Round,
                                )
                            } else {
                                val length = thumbX - startX
                                val taper = min(44.dp.toPx(), length * 0.5f).coerceAtLeast(1f)
                                val wavelength = 55.dp.toPx()
                                val k = (2 * PI / wavelength).toFloat()
                                val step = 2.dp.toPx()
                                ripplePath.reset()
                                ripplePath.moveTo(startX, cy)
                                var x = startX
                                while (x < thumbX) {
                                    val env = smoothstep((x - startX) / taper) * smoothstep((thumbX - x) / taper)
                                    val y = cy - sin((x - startX) * k - phase) * amplitude * env
                                    ripplePath.lineTo(x, y)
                                    x += step
                                }
                                ripplePath.lineTo(thumbX, cy)
                                drawPath(
                                    path = ripplePath,
                                    color = vinylGold,
                                    style = Stroke(width = 2.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
                                )
                            }
                        }

                        // Vinyl turntable brass spindle & record disc thumb
                        if (boundedDurationMs > 0L) {
                            val outerRadius = (8.5f + 3f * thumbGrow).dp.toPx()
                            drawCircle(
                                color = vinylGold.copy(alpha = 0.35f),
                                radius = outerRadius,
                                center = Offset(thumbX, cy),
                            )
                            drawCircle(
                                color = Color(0xFF221F1C),
                                radius = (6f + 2f * thumbGrow).dp.toPx(),
                                center = Offset(thumbX, cy),
                            )
                            drawCircle(
                                color = vinylGold,
                                radius = (2.8f + 0.8f * thumbGrow).dp.toPx(),
                                center = Offset(thumbX, cy),
                            )
                        }
                    }

                    MusicBarStyle.PILLBAR -> {
                        // Modern rounded capsule track & thumb
                        val pillH = 8.dp.toPx()
                        val pillR = pillH / 2f
                        val trackW = (endX - startX).coerceAtLeast(1f)

                        // Inactive pill track
                        drawRoundRect(
                            color = inactiveColor.copy(alpha = if (isTranslucent) 0.30f else 0.22f),
                            topLeft = Offset(startX, cy - pillR),
                            size = Size(trackW, pillH),
                            cornerRadius = CornerRadius(pillR, pillR),
                        )

                        // Active filled pill track
                        val filledW = (thumbX - startX).coerceAtLeast(0f)
                        if (filledW > 0.5f) {
                            val pillBrush = Brush.horizontalGradient(
                                colors = listOf(activeColor.copy(alpha = 0.85f), activeColor),
                                startX = startX,
                                endX = thumbX,
                            )
                            drawRoundRect(
                                brush = pillBrush,
                                topLeft = Offset(startX, cy - pillR),
                                size = Size(filledW, pillH),
                                cornerRadius = CornerRadius(pillR, pillR),
                            )
                        }

                        // Capsule thumb
                        if (boundedDurationMs > 0L) {
                            val thumbW = (12f + 4f * thumbGrow).dp.toPx()
                            val thumbH = (16f + 4f * thumbGrow).dp.toPx()
                            drawRoundRect(
                                color = Color.White,
                                topLeft = Offset(thumbX - thumbW / 2f, cy - thumbH / 2f),
                                size = Size(thumbW, thumbH),
                                cornerRadius = CornerRadius(thumbW / 2f, thumbW / 2f),
                            )
                        }
                    }
                }


            }

            // Invisible interaction layer: reliable seeking semantics.
            Slider(
                value = shownMs.toFloat(),
                onValueChange = {
                    isInteracting = true
                    dragPositionMs = it
                    val currentSec = (it / 1000f).toLong()
                    if (currentSec != lastHapticSecond) {
                        lastHapticSecond = currentSec
                        appHaptics.seekTick()
                    }
                },
                onValueChangeFinished = {
                    isInteracting = false
                    appHaptics.seekRelease()
                    onSeek(dragPositionMs.toLong().coerceIn(0L, boundedDurationMs))
                },
                valueRange = 0f..boundedDurationMs.coerceAtLeast(1L).toFloat(),
                enabled = boundedDurationMs > 0L,
                interactionSource = interactionSource,
                thumb = { Spacer(Modifier.size(inset * 2)) },
                modifier = Modifier
                    .matchParentSize()
                    .alpha(0f),
            )
        }

        if (showTimeLabels) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = formatTime(shownMs),
                    style = MaterialTheme.typography.labelMedium.copy(fontFeatureSettings = "tnum"),
                    color = textColor,
                )
                Text(
                    text = "−${formatTime((boundedDurationMs - shownMs).coerceAtLeast(0))}",
                    style = MaterialTheme.typography.labelMedium.copy(fontFeatureSettings = "tnum"),
                    color = textColor,
                )
            }
        }
    }
}

@Composable
fun WavySeekBar(
    state: MusicPlayerState,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier,
    isTranslucent: Boolean = false,
    style: MusicBarStyle = MusicBarStyle.LENS,
    crossfadeEnabled: Boolean = false,
    crossfadeSeconds: Int = 5,
) {
    WavySeekBar(
        positionMs = state.positionMs,
        durationMs = state.durationMs,
        isPlaying = state.isPlaying,
        onSeek = onSeek,
        modifier = modifier,
        isTranslucent = isTranslucent,
        trackKey = state.current?.let { it.videoId ?: "${it.artist}|${it.title}" },
        style = style,
        crossfadeEnabled = crossfadeEnabled,
        crossfadeSeconds = crossfadeSeconds,
    )
}
