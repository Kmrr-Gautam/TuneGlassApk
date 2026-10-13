package com.listenit.music.ui.common

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.listenit.music.R
import com.listenit.music.ui.theme.LiquidFrequency

/**
 * Signature animated visual motif for TuneGlass: Animated TuneGlass Logo.
 *
 * Replaces the abstract geometric ball with an animated living emblem of the app logo:
 * - Living acoustic pulse & rhythmic breathing
 * - Outward expanding soundwave ripple / shockwave rings
 * - Atmospheric chromatic glow aura
 * - Diagonal liquid-glass specular light sheen across the logo
 */
@Composable
fun FrequencyOrb(
    modifier: Modifier = Modifier,
    size: Dp = 220.dp,
    isPlaying: Boolean = false,
    isAnimated: Boolean = true,
    primaryColor: Color = LiquidFrequency.ElectricCyan,
    secondaryColor: Color = LiquidFrequency.Ultraviolet,
    accentColor: Color = LiquidFrequency.SoftCoral,
    intensity: Float = 1f,
) {
    if (!isAnimated) {
        StaticFrequencyOrb(
            modifier = modifier.size(size),
            primaryColor = primaryColor,
            secondaryColor = secondaryColor,
            accentColor = accentColor,
        )
        return
    }

    val infiniteTransition = rememberInfiniteTransition(label = "TuneGlassLogoMotion")

    // 1. Organic breathing / acoustic pulse
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = if (isPlaying) 0.94f else 0.97f,
        targetValue = if (isPlaying) 1.06f else 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isPlaying) 1100 else 2800,
                easing = FastOutSlowInEasing,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "LogoPulseScale",
    )

    // 2. Weightless floating elevation
    val floatOffsetDp by infiniteTransition.animateFloat(
        initialValue = -3.5f,
        targetValue = 3.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isPlaying) 1900 else 3800,
                easing = FastOutSlowInEasing,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "LogoFloatOffset",
    )

    // 3. Acoustic Shockwave Ring 1 (expanding sound pressure wave)
    val rippleProgress1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isPlaying) 1600 else 3200,
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Restart,
        ),
        label = "Shockwave1",
    )

    // 4. Acoustic Shockwave Ring 2 (offset phase)
    val rippleProgress2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isPlaying) 1600 else 3200,
                delayMillis = if (isPlaying) 800 else 1600,
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Restart,
        ),
        label = "Shockwave2",
    )

    // 5. Specular liquid glass sheen sweep (repeats periodically)
    val sheenProgress by infiniteTransition.animateFloat(
        initialValue = -0.6f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isPlaying) 2600 else 4500,
                easing = FastOutSlowInEasing,
            ),
            repeatMode = RepeatMode.Restart,
        ),
        label = "GlassSheen",
    )

    // 6. Ambient halo breathing
    val haloAlpha by infiniteTransition.animateFloat(
        initialValue = 0.28f,
        targetValue = if (isPlaying) 0.65f else 0.42f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isPlaying) 1200 else 2400,
                easing = FastOutSlowInEasing,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "HaloAlpha",
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        // Background Acoustic Shockwaves & Ambient Glow Aura
        Canvas(modifier = Modifier.matchParentSize()) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val baseSize = minOf(this.size.width, this.size.height)
            val logoRadius = baseSize * 0.38f * pulseScale * intensity

            // Ambient Chromatic Atmospheric Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = haloAlpha * 0.55f),
                        secondaryColor.copy(alpha = haloAlpha * 0.28f),
                        Color.Transparent,
                    ),
                    center = center,
                    radius = logoRadius * 1.6f,
                ),
                radius = logoRadius * 1.6f,
                center = center,
            )

            // Shockwave 1: Radiating acoustic wavefront
            val r1Radius = logoRadius * (1f + 0.55f * rippleProgress1)
            val r1Alpha = ((1f - rippleProgress1) * (if (isPlaying) 0.55f else 0.30f)).coerceIn(0f, 1f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = r1Alpha * 0.45f),
                        secondaryColor.copy(alpha = r1Alpha * 0.20f),
                        Color.Transparent,
                    ),
                    center = center,
                    radius = r1Radius,
                ),
                radius = r1Radius,
                center = center,
            )
            drawCircle(
                color = primaryColor.copy(alpha = r1Alpha),
                radius = r1Radius,
                center = center,
                style = Stroke(width = (1.5f * (1f - rippleProgress1 * 0.5f)).dp.toPx()),
            )

            // Shockwave 2: Secondary offset wavefront
            val r2Radius = logoRadius * (1f + 0.65f * rippleProgress2)
            val r2Alpha = ((1f - rippleProgress2) * (if (isPlaying) 0.45f else 0.22f)).coerceIn(0f, 1f)
            drawCircle(
                color = secondaryColor.copy(alpha = r2Alpha),
                radius = r2Radius,
                center = center,
                style = Stroke(width = (1.2f * (1f - rippleProgress2 * 0.5f)).dp.toPx()),
            )
        }

        // Animated TuneGlass Logo Emblem with Specular Sheen
        val logoSize = size * 0.76f
        val squircleCorner = logoSize * 0.22f

        Box(
            modifier = Modifier
                .size(logoSize)
                .offset(y = floatOffsetDp.dp)
                .graphicsLayer {
                    scaleX = pulseScale
                    scaleY = pulseScale
                }
                .clip(RoundedCornerShape(squircleCorner))
                .drawWithContent {
                    drawContent()
                    // Liquid Glass Specular Shimmer Sweep
                    val w = this.size.width
                    val h = this.size.height
                    val sweepX = w * sheenProgress
                    val sweepBrush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = if (isPlaying) 0.35f else 0.22f),
                            Color.Transparent,
                        ),
                        start = Offset(sweepX - w * 0.25f, 0f),
                        end = Offset(sweepX + w * 0.25f, h),
                    )
                    drawRect(
                        brush = sweepBrush,
                        blendMode = BlendMode.Screen,
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(id = R.drawable.tuneglass_logo),
                contentDescription = "TuneGlass Animated Logo",
                modifier = Modifier.matchParentSize(),
            )
        }
    }
}

/**
 * Static TuneGlass Logo representation for low-power or static cards.
 */
@Composable
fun StaticFrequencyOrb(
    modifier: Modifier = Modifier,
    primaryColor: Color = LiquidFrequency.ElectricCyan,
    secondaryColor: Color = LiquidFrequency.Ultraviolet,
    accentColor: Color = LiquidFrequency.SoftCoral,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = minOf(size.width, size.height) * 0.42f

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.25f),
                        secondaryColor.copy(alpha = 0.10f),
                        Color.Transparent,
                    ),
                    center = center,
                    radius = baseRadius * 1.4f,
                ),
                radius = baseRadius * 1.4f,
                center = center,
            )
        }

        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(22.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(id = R.drawable.tuneglass_logo),
                contentDescription = "TuneGlass Logo",
                modifier = Modifier.matchParentSize(),
            )
        }
    }
}
