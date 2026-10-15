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
 * Charcoal: TuneGlass Design Language (Redesign).
 *
 * Dark warm-charcoal backgrounds, muted sage green accent, dusty blue,
 * warm beige and terracotta secondaries, fine warm-gray hairlines, and
 * warm-white typography. Album artwork is the only saturated element.
 *
 * Token names are intentionally preserved from the previous "Liquid Frequency"
 * palette so that every reference site across the codebase continues to
 * compile without changes — only the colour values are updated.
 */
object LiquidFrequency {
    // ── Core Charcoal Background Layers ───────────────────────────────────
    /** Deepest background — main scaffold background. */
    val Obsidian = Color(0xFF191B19)

    /** Card/surface layer — slightly lighter charcoal. */
    val MidnightIndigo = Color(0xFF242624)

    // ── Accent Palette ────────────────────────────────────────────────────
    /** Primary accent: muted sage green. */
    val ElectricCyan = Color(0xFF91A68B)

    /** Secondary accent: dusty muted blue. */
    val Ultraviolet = Color(0xFF718D9B)

    /** Tertiary accent: warm terracotta. */
    val SoftCoral = Color(0xFFAD7561)

    /** Quaternary accent: warm beige (used for featured/golden elements). */
    val WarmBeige = Color(0xFFC6AE86)

    // ── Typography ────────────────────────────────────────────────────────
    /** Primary text — warm white. */
    val FrostedWhite = Color(0xFFF3F1EB)

    /** Secondary text — muted gray-green. */
    val MutedLavender = Color(0xFFA5A8A2)

    // ── Elevated Surface Tints ────────────────────────────────────────────
    /** Slightly elevated card surface. */
    val GlassSubstrate = Color(0xFF2D302D)

    /** Mid-elevation surface. */
    val GlassSurface = Color(0xFF333736)

    /** Highest card elevation surface. */
    val GlassSurfaceElevated = Color(0xFF3A3D3A)

    // ── Subtle Gradient Brushes ───────────────────────────────────────────
    /** Warm charcoal top-to-bottom gradient for ambient depth. */
    val DeepObsidianGradient: Brush = Brush.verticalGradient(
        colors = listOf(
            Obsidian,
            Color(0xFF1E211E),
            Obsidian,
        ),
    )

    /** Subtle atmospheric charcoal gradient. */
    val SubtleAtmosphericGradient: Brush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF191B19),
            Color(0xFF1F221F),
            Color(0xFF191B19),
        ),
    )

    /**
     * Warm sage-tinted multi-stop gradient — replaces the old cyan/violet/coral
     * iridescent gradient. Used for accent highlights and decorative strokes.
     */
    val IridescentGradient: Brush = Brush.linearGradient(
        colors = listOf(ElectricCyan, Ultraviolet, SoftCoral),
        start = Offset.Zero,
        end = Offset(400f, 400f),
    )

    val FrequencyHorizontalGradient: Brush = Brush.horizontalGradient(
        colors = listOf(ElectricCyan, Ultraviolet, SoftCoral),
    )

    // ── Hairline Border Brushes ───────────────────────────────────────────
    /**
     * Default hairline: warm-gray top, fading to transparent — matches the
     * reference image's thin, restrained card borders.
     */
    val GlassBorderBrush: Brush = Brush.verticalGradient(
        0f to FrostedWhite.copy(alpha = 0.18f),
        0.40f to MutedLavender.copy(alpha = 0.10f),
        1f to Color.Transparent,
    )

    /**
     * Active hairline: sage green to dusty blue — used on the selected
     * nav item and focused interactive elements.
     */
    val GlassBorderActiveBrush: Brush = Brush.linearGradient(
        0f to ElectricCyan.copy(alpha = 0.70f),
        0.5f to Ultraviolet.copy(alpha = 0.50f),
        1f to SoftCoral.copy(alpha = 0.30f),
    )

    fun glassBorder(active: Boolean = false, strokeWidth: Dp = 1.dp): BorderStroke =
        BorderStroke(
            width = strokeWidth,
            brush = if (active) GlassBorderActiveBrush else GlassBorderBrush,
        )
}

// ── Modifier Helpers ──────────────────────────────────────────────────────────

/**
 * Charcoal card panel: replaces the old glass-blur panel. Uses a solid
 * charcoal background with a warm hairline border — matches the reference
 * image's matte card aesthetic rather than the previous glossy glass look.
 */
fun Modifier.liquidGlassPanel(
    shape: Shape = RoundedCornerShape(18.dp),
    isActive: Boolean = false,
    alpha: Float = 1f,
    borderWidth: Dp = 1.dp,
): Modifier = this
    .clip(shape)
    .background(
        Brush.verticalGradient(
            0f to LiquidFrequency.GlassSurfaceElevated.copy(alpha = alpha),
            1f to LiquidFrequency.GlassSubstrate.copy(alpha = alpha),
        )
    )
    .border(
        width = borderWidth,
        brush = if (isActive) LiquidFrequency.GlassBorderActiveBrush else LiquidFrequency.GlassBorderBrush,
        shape = shape,
    )
