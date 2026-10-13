@file:OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)

package com.listenit.music.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.listenit.music.R

/*
 * Type system: Fraunces (serif) for display/headline/titleLarge, Google Sans
 * Flex (sans, roundness axis off) for titles, body and labels. Both are
 * bundled variable fonts, so every device renders identically.
 */

/** Weights the app actually asks for; one variable-font instance per weight so an
 *  explicit `fontWeight = ExtraBold` at a call site lands on the real axis value
 *  instead of a synthesized (faux) bold. */
private val UiWeights = listOf(400, 500, 600, 700, 800)

private fun toFontWeight(w: Int) = when {
    w >= 800 -> FontWeight.ExtraBold
    w >= 700 -> FontWeight.Bold
    w >= 600 -> FontWeight.SemiBold
    w >= 500 -> FontWeight.Medium
    else -> FontWeight.Normal
}

/**
 * Sans: Google Sans Flex with the "ROND" roundness axis at 0, so UI text is
 * clean and neutral (the old scale pushed roundness to ~40 for a bubbly look).
 * Used for everything small: rows, buttons, labels, body copy.
 */
private fun sans(opticalSize: Float, width: Float = 100f): FontFamily = FontFamily(
    UiWeights.map { w ->
        Font(
            R.font.google_sans_flex,
            weight = toFontWeight(w),
            variationSettings = FontVariation.Settings(
                FontVariation.weight(w),
                FontVariation.width(width),
                FontVariation.Setting("ROND", 0f),
                FontVariation.Setting("opsz", opticalSize),
                FontVariation.Setting("GRAD", 0f),
            ),
        )
    },
)

/**
 * Serif: Fraunces (SIL OFL), bundled as res/font/fraunces.ttf. Subset to
 * Latin, "WONK" pinned off and "SOFT" pinned slightly soft, weight axis
 * 300-800, optical-size axis 9-144. Used only for display, headlines and
 * large titles, so song titles and screen names feel like liner notes while
 * the controls around them stay quiet and modern.
 */
private fun serif(opticalSize: Float): FontFamily = FontFamily(
    UiWeights.map { w ->
        Font(
            R.font.fraunces,
            weight = toFontWeight(w),
            variationSettings = FontVariation.Settings(
                FontVariation.weight(w),
                FontVariation.Setting("opsz", opticalSize),
            ),
        )
    },
)

private val SerifDisplay = serif(96f)
private val SerifHeadline = serif(60f)
private val SerifTitle = serif(32f)
private val SansTitle = sans(16f)
private val SansBody = sans(15f)
private val SansLabel = sans(12f)

val TuneGlassTypography = Typography(
    displayLarge = TextStyle(fontFamily = SerifDisplay, fontWeight = FontWeight.Medium, fontSize = 57.sp, lineHeight = 62.sp, letterSpacing = (-1.0).sp),
    displayMedium = TextStyle(fontFamily = SerifDisplay, fontWeight = FontWeight.Medium, fontSize = 45.sp, lineHeight = 50.sp, letterSpacing = (-0.8).sp),
    displaySmall = TextStyle(fontFamily = SerifDisplay, fontWeight = FontWeight.Medium, fontSize = 36.sp, lineHeight = 42.sp, letterSpacing = (-0.6).sp),
    headlineLarge = TextStyle(fontFamily = SerifHeadline, fontWeight = FontWeight.SemiBold, fontSize = 32.sp, lineHeight = 38.sp, letterSpacing = (-0.5).sp),
    headlineMedium = TextStyle(fontFamily = SerifHeadline, fontWeight = FontWeight.SemiBold, fontSize = 28.sp, lineHeight = 34.sp, letterSpacing = (-0.4).sp),
    headlineSmall = TextStyle(fontFamily = SerifHeadline, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 30.sp, letterSpacing = (-0.3).sp),
    titleLarge = TextStyle(fontFamily = SerifTitle, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 28.sp, letterSpacing = (-0.2).sp),
    titleMedium = TextStyle(fontFamily = SansTitle, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.1.sp),
    titleSmall = TextStyle(fontFamily = SansTitle, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
    bodyLarge = TextStyle(fontFamily = SansBody, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = SansBody, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 22.sp),
    bodySmall = TextStyle(fontFamily = SansBody, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 18.sp),
    labelLarge = TextStyle(fontFamily = SansLabel, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
    labelMedium = TextStyle(fontFamily = SansLabel, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.3.sp),
    labelSmall = TextStyle(fontFamily = SansLabel, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.4.sp),
)

/**
 * The "Use Application Font" toggle's off-state: plain platform default
 * (FontFamily.Default), same sizes/weights as [TuneGlassTypography] so
 * turning the toggle off changes ONLY the typeface, not the whole scale.
 */
val SystemTypography = Typography(
    displayLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal, fontSize = 57.sp, lineHeight = 64.sp),
    displayMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal, fontSize = 45.sp, lineHeight = 52.sp),
    displaySmall = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal, fontSize = 36.sp, lineHeight = 44.sp),
    headlineLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 40.sp),
    headlineMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 36.sp),
    headlineSmall = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 32.sp),
    titleLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium, fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 24.sp),
    titleSmall = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
    bodyLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 22.sp),
    bodySmall = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 18.sp),
    labelLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp),
)

val ListenItTypography = TuneGlassTypography
