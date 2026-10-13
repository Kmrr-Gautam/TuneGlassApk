package com.listenit.music.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * "Lens" shape language: calm, evenly-rounded plates. Radii step up with the
 * size of the surface (a chip is tighter than a sheet) instead of every
 * element sharing one corner. Buttons are circles/rings (see LensRingButton);
 * cards and rows are softly-rounded plates separated by hairlines.
 */
val TuneGlassShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

val ListenItShapes = TuneGlassShapes

val ExpressiveHeroShape = RoundedCornerShape(22.dp)
val ExpressivePillShape = RoundedCornerShape(14.dp)
val HeroInnerShape = RoundedCornerShape(16.dp)
val StatPillShape = RoundedCornerShape(12.dp)
val ListContainerShape = RoundedCornerShape(18.dp)
val BadgePillShape = RoundedCornerShape(8.dp)
val NowPlayingCardShape = RoundedCornerShape(18.dp)
val TrackRowShape = RoundedCornerShape(12.dp)
val ArtworkShape = RoundedCornerShape(10.dp)
