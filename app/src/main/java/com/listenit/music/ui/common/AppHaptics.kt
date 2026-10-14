package com.listenit.music.ui.common

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView

val LocalHapticFeedbackEnabled = staticCompositionLocalOf { true }

val LocalAppHaptics = staticCompositionLocalOf {
    AppHaptics(
        hapticFeedback = object : HapticFeedback {
            override fun performHapticFeedback(hapticFeedbackType: HapticFeedbackType) {}
        },
        view = null,
        isEnabled = { true },
    )
}

/**
 * A wrapper around Compose's [HapticFeedback] that checks the user's preference
 * before triggering any tactile vibration.
 */
class ConditionalHapticFeedback(
    private val delegate: HapticFeedback,
    private val isEnabled: () -> Boolean,
) : HapticFeedback {
    override fun performHapticFeedback(hapticFeedbackType: HapticFeedbackType) {
        if (isEnabled()) {
            delegate.performHapticFeedback(hapticFeedbackType)
        }
    }
}

/**
 * High-performance tactile haptic feedback controller tailored for TuneGlass audio player.
 * Uses native low-latency View haptics with Compose fallbacks for rich, expressive tactile responses.
 */
class AppHaptics(
    private val hapticFeedback: HapticFeedback,
    private val view: View?,
    private val isEnabled: () -> Boolean,
) {
    /** Crisp mechanical click for Play / Pause. */
    fun playPause() {
        if (!isEnabled()) return
        val handled = view?.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP) ?: false
        if (!handled) {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    /** Responsive skip / tap feedback for Next track. */
    fun next() {
        if (!isEnabled()) return
        val handled = view?.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP) ?: false
        if (!handled) {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    /** Responsive skip / tap feedback for Previous track. */
    fun previous() {
        if (!isEnabled()) return
        val handled = view?.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP) ?: false
        if (!handled) {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    /** Subtle mechanical detent micro-tick when scrubbing or adjusting sliders (e.g. crossfade duration). */
    fun sliderTick() {
        if (!isEnabled()) return
        val handled = view?.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK) ?: false
        if (!handled) {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    /** High-frequency micro tick while dragging seekbar timeline. */
    fun seekTick() {
        if (!isEnabled()) return
        val handled = if (Build.VERSION.SDK_INT >= 34) {
            view?.performHapticFeedback(HapticFeedbackConstants.SEGMENT_FREQUENT_TICK) ?: false
        } else {
            view?.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK) ?: false
        }
        if (!handled) {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    /** Clean tactile snap when releasing seekbar thumb to start playback at new position. */
    fun seekRelease() {
        if (!isEnabled()) return
        val handled = view?.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP) ?: false
        if (!handled) {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    /** Confident tactile confirmation bump for toggles (crossfade button, repeat, shuffle). */
    fun toggle() {
        if (!isEnabled()) return
        val handled = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view?.performHapticFeedback(HapticFeedbackConstants.CONFIRM) ?: false
        } else {
            view?.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP) ?: false
        }
        if (!handled) {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    fun perform(type: HapticFeedbackType) {
        if (isEnabled()) {
            hapticFeedback.performHapticFeedback(type)
        }
    }
}

@Composable
fun rememberAppHaptics(
    enabled: Boolean = LocalHapticFeedbackEnabled.current,
): AppHaptics {
    val hapticFeedback = LocalHapticFeedback.current
    val view = LocalView.current
    return remember(hapticFeedback, view, enabled) {
        AppHaptics(hapticFeedback, view) { enabled }
    }
}
