package com.listenit.music.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Liquid Frequency: TuneGlass Design Language.
 *
 * Combines deep atmospheric obsidian darkness, translucent liquid-glass surfaces,
 * iridescent frequency gradients (Cyan, Ultraviolet, Coral), and fine luminous contours.
 */
object LiquidFrequency {
    // Core controlled palette
    val Obsidian = Color(0xFF080911)
    val MidnightIndigo = Color(0xFF11152B)
    val ElectricCyan = Color(0xFF69F0F0)
    val Ultraviolet = Color(0xFF9875FF)
    val SoftCoral = Color(0xFFFF8F9D)
    val FrostedWhite = Color(0xFFF4F3FF)
    val MutedLavender = Color(0xFFA7A6C4)

    // Derived surface tints
    val GlassSubstrate = Color(0xFF0D1022).copy(alpha = 0.55f)
    val GlassSurface = Color(0xFF121630).copy(alpha = 0.65f)
    val GlassSurfaceElevated = Color(0xFF181C3D).copy(alpha = 0.75f)

    // Iridescent Gradients
    val IridescentGradient = Brush.linearGradient(
        colors = listOf(ElectricCyan, Ultraviolet, SoftCoral),
        start = Offset.Zero,
        end = Offset(400f, 400f),
    )

    val FrequencyHorizontalGradient = Brush.horizontalGradient(
        colors = listOf(ElectricCyan, Ultraviolet, SoftCoral),
    )

    val DeepObsidianGradient = Brush.verticalGradient(
        colors = listOf(
            Obsidian,
            MidnightIndigo.copy(alpha = 0.85f),
            Obsidian,
        ),
    )

    val SubtleAtmosphericGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0A0C16),
            Color(0xFF10142A),
            Color(0xFF080911),
        ),
    )

    // Glass Hairline Border Gradients
    val GlassBorderBrush = Brush.verticalGradient(
        0f to FrostedWhite.copy(alpha = 0.28f),
        0.35f to ElectricCyan.copy(alpha = 0.18f),
        0.75f to Ultraviolet.copy(alpha = 0.10f),
        1f to Color.Transparent,
    )

    val GlassBorderActiveBrush = Brush.linearGradient(
        0f to ElectricCyan.copy(alpha = 0.65f),
        0.5f to Ultraviolet.copy(alpha = 0.45f),
        1f to SoftCoral.copy(alpha = 0.35f),
    )

    fun glassBorder(active: Boolean = false, strokeWidth: Dp = 1.dp): BorderStroke {
        return BorderStroke(
            width = strokeWidth,
            brush = if (active) GlassBorderActiveBrush else GlassBorderBrush,
        )
    }
}

/**
 * Modifier to apply a liquid-frequency frosted glass panel background with optional active border.
 */
fun Modifier.liquidGlassPanel(
    shape: Shape = RoundedCornerShape(22.dp),
    isActive: Boolean = false,
    alpha: Float = 0.62f,
    borderWidth: Dp = 1.dp,
): Modifier = this
    .clip(shape)
    .background(
        Brush.verticalGradient(
            0f to LiquidFrequency.GlassSurfaceElevated.copy(alpha = alpha),
            1f to LiquidFrequency.GlassSubstrate.copy(alpha = alpha * 0.85f),
        )
    )
    .border(
        width = borderWidth,
        brush = if (isActive) LiquidFrequency.GlassBorderActiveBrush else LiquidFrequency.GlassBorderBrush,
        shape = shape,
    )
