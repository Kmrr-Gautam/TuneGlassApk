package com.listenit.music.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Charcoal shape language: softly-rounded plates inspired by the reference
 * four-screen design. Cards are slightly more rounded than the previous Lens
 * system; chips are fully pill-shaped; album artwork has modest rounding.
 *
 * Buttons remain as rings (see LensRingButton); cards and rows are plates
 * separated by hairlines.
 */
val TuneGlassShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small      = RoundedCornerShape(12.dp),
    medium     = RoundedCornerShape(16.dp),
    large      = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

val ListenItShapes = TuneGlassShapes

// ── Named shape tokens ────────────────────────────────────────────────────────

/** Hero / featured card: matches the large rounded cards in the reference. */
val ExpressiveHeroShape = RoundedCornerShape(20.dp)

/** Pill-shaped filter chips and category badges. */
val ExpressivePillShape = RoundedCornerShape(50)

/** Inner rounded container inside a hero card. */
val HeroInnerShape = RoundedCornerShape(14.dp)

/** Stat chips and metadata pills. */
val StatPillShape = RoundedCornerShape(12.dp)

/** Top-rounding only: used for the track list container that sits flush to its parent. */
val ListContainerShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)

/** Badge pill: username label, "Top song" badges. */
val BadgePillShape = RoundedCornerShape(8.dp)

/** Now-playing card and playlist cards: generous rounding per reference. */
val NowPlayingCardShape = RoundedCornerShape(18.dp)

/** Track rows inside a list container. */
val TrackRowShape = RoundedCornerShape(14.dp)

/** Album artwork: moderate rounding per reference image. */
val ArtworkShape = RoundedCornerShape(10.dp)
