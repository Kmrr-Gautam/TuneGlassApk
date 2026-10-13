package com.listenit.music.ui.common

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/*
 * "Lens" button vocabulary.
 *
 * Secondary actions are hairline rings (a thin circle with a glyph inside),
 * the way a lens barrel has fine engraved rings. The one primary action in the
 * app, play, is a solid disc inside its own progress ring (see PlayerHost's
 * LensPlayButton). Nothing else in the app uses a filled coloured circle, so
 * the primary action is always unmistakable.
 */

/**
 * A hairline ring icon button.
 *
 * @param active when true the ring and glyph switch to [activeColor], used for
 *   toggles (liked, shuffle on...) where state must read at a glance.
 * @param filled when true the ring is filled with [fillColor] (used on top of
 *   artwork, where a hairline alone would be lost).
 */
@Composable
fun LensRingButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    iconSize: Dp = 21.dp,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    ringColor: Color = tint.copy(alpha = 0.24f),
    active: Boolean = false,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    filled: Boolean = false,
    fillColor: Color = Color.Black.copy(alpha = 0.28f),
    enabled: Boolean = true,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.88f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "lensRingPress",
    )
    val glyph = if (active) activeColor else tint
    val ring = if (active) activeColor.copy(alpha = 0.7f) else ringColor

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = if (enabled) 1f else 0.38f
            }
            .clip(CircleShape)
            .then(if (filled) Modifier.background(fillColor) else Modifier)
            .border(1.dp, ring, CircleShape)
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = contentDescription, tint = glyph, modifier = Modifier.size(iconSize))
    }
}

/**
 * A bare glyph with no container, for transport-style controls (skip, shuffle,
 * repeat). When [active] a small dot sits under the glyph so a toggled state is
 * visible without adding a coloured blob.
 */
@Composable
fun LensGlyphButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    iconSize: Dp = 26.dp,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    active: Boolean = false,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    enabled: Boolean = true,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.84f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "lensGlyphPress",
    )
    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = if (enabled) 1f else 0.38f
            }
            .clip(CircleShape)
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            icon,
            contentDescription = contentDescription,
            tint = if (active) activeColor else tint,
            modifier = Modifier.size(iconSize),
        )
        if (active) {
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 4.dp)
                    .size(4.dp)
                    .background(activeColor, CircleShape),
            )
        }
    }
}
