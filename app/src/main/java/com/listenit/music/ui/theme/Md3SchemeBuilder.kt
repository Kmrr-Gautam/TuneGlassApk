package com.listenit.music.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * "Charcoal" palette — redesigned from the old "Lens" indigo scheme.
 *
 * Design intent: warm charcoal chrome so album artwork dominates the screen.
 * Surfaces use tiny lightness steps separated by warm-gray hairlines; the
 * sage-green accent is used sparingly. The public API (hueOf / buildScheme /
 * buildMonochromeScheme) and hue-seeded accent behaviour are unchanged, so
 * the accent picker, AMOLED mode, wallpaper colour and now-playing colour
 * extraction all continue to work.
 */
object Md3SchemeBuilder {

    /** Saturation levels (HSL %) per tonal family. */
    private const val CHROMA_PRIMARY = 22      // Sage green — muted, not vivid
    private const val CHROMA_SECONDARY = 14
    private const val CHROMA_TERTIARY = 28
    private const val CHROMA_NEUTRAL = 5       // Warm-gray surfaces, barely tinted
    private const val CHROMA_NEUTRAL_VARIANT = 7

    /** Extracts just the hue (0-360) from a hex color. */
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

    /** Full dynamic scheme, seeded from [hex]'s hue. */
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

    /** Pure grayscale scheme (all chroma = 0). */
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

        // Charcoal surface steps: warm green-gray, climbing in tiny increments
        // AMOLED stays pure black; standard uses the warm charcoal base.
        val backgroundCol  = if (amoled) Color.Black else LiquidFrequency.Obsidian
        val surfaceCol     = if (amoled) Color.Black else LiquidFrequency.Obsidian
        val surfaceLowCol  = if (amoled) Color.Black else LiquidFrequency.MidnightIndigo
        val surfaceLowestCol = if (amoled) Color.Black else LiquidFrequency.Obsidian

        return darkColorScheme(
            // Primary accent: sage green (hue from accent picker, muted chroma)
            primary              = hsl(primaryHue, cP, 66),
            onPrimary            = hsl(primaryHue, (cP - 8).coerceAtLeast(0), 12),
            primaryContainer     = hsl(primaryHue, (cP / 2).coerceAtLeast(0), if (amoled) 18 else 22),
            onPrimaryContainer   = hsl(primaryHue, (cP + 6).coerceAtMost(100), 90),

            // Secondary accent: dusty blue hue
            secondary            = hsl(secondaryHue, cS, 64),
            onSecondary          = hsl(secondaryHue, cS, 12),
            secondaryContainer   = hsl(secondaryHue, cS, if (amoled) 16 else 20),
            onSecondaryContainer = hsl(secondaryHue, (cS + 4).coerceAtMost(100), 88),

            // Tertiary accent: warm beige/terracotta
            tertiary             = hsl(tertiaryHue, cT, 68),
            onTertiary           = hsl(tertiaryHue, cT, 12),
            tertiaryContainer    = hsl(tertiaryHue, (cT / 2).coerceAtLeast(0), if (amoled) 18 else 22),
            onTertiaryContainer  = hsl(tertiaryHue, (cT + 4).coerceAtMost(100), 90),

            // Error: accessible muted red
            error                = hsl(2, 50, 68),
            onError              = hsl(2, 44, 12),
            errorContainer       = hsl(2, 30, 22),
            onErrorContainer     = hsl(2, 48, 88),

            // Charcoal surfaces — the key visual shift
            background                 = backgroundCol,
            onBackground               = LiquidFrequency.FrostedWhite,
            surface                    = surfaceCol,
            onSurface                  = LiquidFrequency.FrostedWhite,
            surfaceContainerLowest     = surfaceLowestCol,
            surfaceContainerLow        = surfaceLowCol,
            surfaceContainer           = if (amoled) Color(0xFF0D0D0D) else LiquidFrequency.MidnightIndigo,
            surfaceContainerHigh       = if (amoled) Color(0xFF141414) else LiquidFrequency.GlassSubstrate,
            surfaceContainerHighest    = if (amoled) Color(0xFF1A1A1A) else LiquidFrequency.GlassSurface,
            surfaceVariant             = hsl(neutralHue, cNV, 20),
            onSurfaceVariant           = LiquidFrequency.MutedLavender,

            // Warm-gray hairlines
            outline                    = LiquidFrequency.FrostedWhite.copy(alpha = 0.16f),
            outlineVariant             = Color(0xFF454844),

            inverseSurface             = hsl(neutralHue, cNV, 88),
            inverseOnSurface           = hsl(neutralHue, cNV, 14),
            inversePrimary             = hsl(primaryHue, cP, 36),
            scrim                      = Color.Black,
        )
    }

    /**
     * HSL(h in 0–360, s in 0–100, l in 0–100) → Compose Color.
     * Standard conversion — unchanged from original.
     */
    private fun hsl(h: Int, s: Int, l: Int): Color {
        val hf = ((h % 360) + 360) % 360 / 360f
        val sf = s.coerceIn(0, 100) / 100f
        val lf = l.coerceIn(0, 100) / 100f

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
