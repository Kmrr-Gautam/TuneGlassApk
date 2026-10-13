package com.listenit.music.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * "Lens" palette. Rebuilds the app's dark scheme from a single source hue.
 *
 * Design intent: the chrome is quiet ink and warm paper, so album artwork is
 * the only loud colour on screen. Surface steps are deliberately tiny (a few
 * lightness points apart) and separated by hairlines rather than by heavy
 * tonal contrast; the accent is a luminous pastel used sparingly, with deep,
 * muted containers behind it.
 *
 * The public API (hueOf / buildScheme / buildMonochromeScheme) and the
 * hue-seeded accent behaviour are unchanged, so the accent picker, AMOLED
 * mode, wallpaper colour and now-playing colour all keep working.
 */
object Md3SchemeBuilder {

    /** Saturation levels (HSL %) per tonal family. */
    private const val CHROMA_PRIMARY = 54
    private const val CHROMA_SECONDARY = 16
    private const val CHROMA_TERTIARY = 36
    private const val CHROMA_NEUTRAL = 7
    private const val CHROMA_NEUTRAL_VARIANT = 9

    /** Extracts just the hue (0-360) from a hex color — the only component
     *  app.js's _hexToHsl() result that _applyMaterialYouScheme() actually uses. */
    fun hueOf(hex: String): Int {
        val clean = hex.removePrefix("#")
        if (clean.length < 6) return 0
        val rgb = clean.take(6).toIntOrNull(16) ?: return 0
        val r = ((rgb ushr 16) and 0xFF) / 255f
        val g = ((rgb ushr 8) and 0xFF) / 255f
        val b = (rgb and 0xFF) / 255f
        val max = maxOf(r, g, b)
        val min = minOf(r, g, b)
        if (max == min) return 0
        val d = max - min
        val h = when (max) {
            r -> ((g - b) / d + (if (g < b) 6f else 0f))
            g -> ((b - r) / d + 2f)
            else -> ((r - g) / d + 4f)
        } / 6f
        return (h * 360f).roundToInt().let { ((it % 360) + 360) % 360 }
    }

    /** Full dynamic scheme, seeded from [hex]'s hue. Matches _applyMaterialYouScheme(). */
    @Suppress("UNUSED_PARAMETER")
    fun buildScheme(hex: String, amoled: Boolean, liquidGlass: Boolean = false): ColorScheme {
        val h = hueOf(hex)
        val hT = (h + 60) % 360
        return build(
            primaryHue = h,
            secondaryHue = h,
            tertiaryHue = hT,
            neutralHue = h,
            amoled = amoled,
        )
    }

    /** Pure grayscale scheme (all chroma = 0). Matches _applyMonochromeScheme(). */
    @Suppress("UNUSED_PARAMETER")
    fun buildMonochromeScheme(amoled: Boolean, liquidGlass: Boolean = false): ColorScheme = build(
        primaryHue = 0,
        secondaryHue = 0,
        tertiaryHue = 0,
        neutralHue = 0,
        amoled = amoled,
        monochrome = true,
    )

    private fun build(
        primaryHue: Int,
        secondaryHue: Int,
        tertiaryHue: Int,
        neutralHue: Int,
        amoled: Boolean,
        monochrome: Boolean = false,
    ): ColorScheme {
        val cP = if (monochrome) 0 else CHROMA_PRIMARY
        val cS = if (monochrome) 0 else CHROMA_SECONDARY
        val cT = if (monochrome) 0 else CHROMA_TERTIARY
        val cN = if (monochrome) 0 else CHROMA_NEUTRAL
        val cNV = if (monochrome) 0 else CHROMA_NEUTRAL_VARIANT

        // Ink: surfaces climb in very small steps so rows and cards read as
        // faint plates on the page rather than as boxes.
        val bgL = if (amoled) 0 else 4
        val sc1L = if (amoled) 5 else 8
        val sc2L = if (amoled) 8 else 11
        val sc3L = if (amoled) 11 else 14

        val backgroundCol = if (amoled) Color.Black else LiquidFrequency.Obsidian
        val surfaceCol = if (amoled) Color.Black else LiquidFrequency.Obsidian
        val surfaceLowCol = if (amoled) Color.Black else LiquidFrequency.GlassSubstrate
        val surfaceLowestCol = if (amoled) Color.Black else LiquidFrequency.Obsidian

        return darkColorScheme(
            // Luminous frequency accents
            primary = hsl(primaryHue, cP, 82),
            onPrimary = hsl(primaryHue, (cP - 14).coerceAtLeast(0), 10),
            primaryContainer = hsl(primaryHue, (cP / 2).coerceAtLeast(0), if (amoled) 16 else 19),
            onPrimaryContainer = hsl(primaryHue, (cP + 8).coerceAtMost(100), 92),

            secondary = hsl(secondaryHue, cS, 78),
            onSecondary = hsl(secondaryHue, cS, 12),
            secondaryContainer = hsl(secondaryHue, cS, if (amoled) 15 else 18),
            onSecondaryContainer = hsl(secondaryHue, (cS + 4).coerceAtMost(100), 90),

            tertiary = hsl(tertiaryHue, cT, 80),
            onTertiary = hsl(tertiaryHue, cT, 12),
            tertiaryContainer = hsl(tertiaryHue, (cT / 2).coerceAtLeast(0), if (amoled) 16 else 19),
            onTertiaryContainer = hsl(tertiaryHue, (cT + 6).coerceAtMost(100), 92),

            error = hsl(2, 62, 72),
            onError = hsl(2, 50, 12),
            errorContainer = hsl(2, 36, 22),
            onErrorContainer = hsl(2, 60, 90),

            // Liquid Frequency surfaces: Obsidian backdrop, Frosted White typography, Muted Lavender details
            background = backgroundCol,
            onBackground = LiquidFrequency.FrostedWhite,
            surface = surfaceCol,
            onSurface = LiquidFrequency.FrostedWhite,
            surfaceContainerLowest = surfaceLowestCol,
            surfaceContainerLow = surfaceLowCol,
            surfaceContainer = if (amoled) Color(0xFF0A0C16) else LiquidFrequency.MidnightIndigo,
            surfaceContainerHigh = if (amoled) Color(0xFF101322) else LiquidFrequency.GlassSurface,
            surfaceContainerHighest = if (amoled) Color(0xFF151A30) else LiquidFrequency.GlassSurfaceElevated,
            surfaceVariant = hsl(neutralHue, cNV, 16),
            onSurfaceVariant = LiquidFrequency.MutedLavender,

            // Hairlines are first-class luminous strokes in Liquid Frequency
            outline = LiquidFrequency.FrostedWhite.copy(alpha = 0.22f),
            outlineVariant = LiquidFrequency.ElectricCyan.copy(alpha = 0.14f),

            inverseSurface = hsl(neutralHue, cNV, 90),
            inverseOnSurface = hsl(neutralHue, cNV, 12),
            inversePrimary = hsl(primaryHue, cP, 38),
            scrim = Color.Black,
        )
    }

    /**
     * HSL(h in 0-360, s in 0-100, l in 0-100) -> Compose Color.
     * Standard conversion — must match browsers' CSS hsl() exactly so every
     * token lines up with the values app.css previously rendered.
     */
    private fun hsl(h: Int, s: Int, l: Int): Color {
        val hf = ((h % 360) + 360) % 360 / 360f
        val sf = (s.coerceIn(0, 100)) / 100f
        val lf = (l.coerceIn(0, 100)) / 100f

        if (sf == 0f) return Color(lf, lf, lf)

        val q = if (lf < 0.5f) lf * (1 + sf) else lf + sf - lf * sf
        val p = 2 * lf - q

        fun hueToRgb(t: Float): Float {
            var tt = t
            if (tt < 0f) tt += 1f
            if (tt > 1f) tt -= 1f
            return when {
                tt < 1f / 6f -> p + (q - p) * 6f * tt
                tt < 1f / 2f -> q
                tt < 2f / 3f -> p + (q - p) * (2f / 3f - tt) * 6f
                else -> p
            }
        }

        val r = hueToRgb(hf + 1f / 3f)
        val g = hueToRgb(hf)
        val b = hueToRgb(hf - 1f / 3f)
        return Color(r, g, b)
    }
}

/** Kept for readability at call sites that only care about hue distance. */
internal fun hueDelta(a: Int, b: Int): Int = abs(a - b).let { minOf(it, 360 - it) }
