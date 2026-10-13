package com.listenit.music.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.listenit.music.R
import com.listenit.music.ui.common.FrequencyOrb
import com.listenit.music.ui.common.adaptiveContentWidth
import com.listenit.music.ui.player.LocalMusicPlayer
import com.listenit.music.ui.theme.LiquidFrequency
import com.listenit.music.ui.theme.liquidGlassPanel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Stages in the redesigned TuneGlass cinematic onboarding flow.
 */
private enum class OnboardingStage {
    WELCOME,
    IDENTITY,
    GENRES,
}

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val musicPlayer = LocalMusicPlayer.current
    LaunchedEffect(Unit) {
        musicPlayer.stopAndClear()
    }

    val focusManager = LocalFocusManager.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    var stage by remember { mutableStateOf(OnboardingStage.WELCOME) }
    var isTransitioning by remember { mutableStateOf(false) }

    // Frequency Orb dynamic reactive colors based on selected genres
    val orbPrimaryColor by animateColorAsState(
        targetValue = when {
            "EDM" in state.selectedGenres -> LiquidFrequency.ElectricCyan
            "Rock" in state.selectedGenres -> LiquidFrequency.SoftCoral
            "Lo-Fi" in state.selectedGenres || "R&B" in state.selectedGenres -> LiquidFrequency.Ultraviolet
            "Punjabi" in state.selectedGenres || "DHH" in state.selectedGenres -> Color(0xFFFF6E40)
            else -> LiquidFrequency.ElectricCyan
        },
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "OrbPrimaryColor",
    )

    val orbSecondaryColor by animateColorAsState(
        targetValue = when {
            "Indie" in state.selectedGenres -> Color(0xFF64FFDA)
            "K-Pop" in state.selectedGenres -> Color(0xFFFF80AB)
            else -> LiquidFrequency.Ultraviolet
        },
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "OrbSecondaryColor",
    )

    fun completeAndEnterApp() {
        if (state.isSaving || isTransitioning) return
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        focusManager.clearFocus()
        isTransitioning = true
        scope.launch {
            delay(500)
            viewModel.completeOnboarding(onSuccess = onFinished)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LiquidFrequency.Obsidian)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
        contentAlignment = Alignment.Center,
    ) {
        // Deep atmospheric background light
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(LiquidFrequency.SubtleAtmosphericGradient),
        )

        AnimatedContent(
            targetState = stage,
            transitionSpec = {
                if (targetState.ordinal > initialState.ordinal) {
                    (slideInHorizontally { it / 3 } + fadeIn(tween(400)))
                        .togetherWith(slideOutHorizontally { -it / 3 } + fadeOut(tween(300)))
                } else {
                    (slideInHorizontally { -it / 3 } + fadeIn(tween(400)))
                        .togetherWith(slideOutHorizontally { it / 3 } + fadeOut(tween(300)))
                }
            },
            label = "OnboardingStageTransition",
            modifier = Modifier.fillMaxSize(),
        ) { currentStage ->
            when (currentStage) {
                OnboardingStage.WELCOME -> {
                    WelcomeStageView(
                        onBegin = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            stage = OnboardingStage.IDENTITY
                        },
                        onQuickStart = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            completeAndEnterApp()
                        },
                    )
                }

                OnboardingStage.IDENTITY -> {
                    IdentityStageView(
                        name = state.name,
                        onNameChanged = viewModel::onNameChanged,
                        onBack = { stage = OnboardingStage.WELCOME },
                        onContinue = {
                            focusManager.clearFocus()
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            stage = OnboardingStage.GENRES
                        },
                    )
                }

                OnboardingStage.GENRES -> {
                    GenreSelectionStageView(
                        state = state,
                        orbPrimaryColor = orbPrimaryColor,
                        orbSecondaryColor = orbSecondaryColor,
                        onToggleGenre = { genreId ->
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.toggleGenre(genreId)
                        },
                        onBack = { stage = OnboardingStage.IDENTITY },
                        onFinish = { completeAndEnterApp() },
                        isSaving = state.isSaving || isTransitioning,
                    )
                }
            }
        }
    }
}

/**
 * Stage 1: Cinematic Welcome Screen.
 * Features central slowly rotating Frequency Orb, editorial typography,
 * and high-contrast liquid glass interaction surfaces.
 */
@Composable
private fun WelcomeStageView(
    onBegin: () -> Unit,
    onQuickStart: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .adaptiveContentWidth(maxWidth = 580.dp)
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        // Top branding row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(LiquidFrequency.ElectricCyan.copy(alpha = 0.15f))
                        .border(1.dp, LiquidFrequency.ElectricCyan.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_launcher_logo),
                        contentDescription = "TuneGlass Logo",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Text(
                    text = "TUNEGLASS",
                    style = MaterialTheme.typography.labelLarge.copy(
                        letterSpacing = 4.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = LiquidFrequency.FrostedWhite,
                )
            }

            TextButton(
                onClick = onQuickStart,
                shape = CircleShape,
            ) {
                Text(
                    text = "Skip to App",
                    style = MaterialTheme.typography.labelMedium,
                    color = LiquidFrequency.MutedLavender,
                )
            }
        }

        // Centerpiece: Majestic Frequency Orb with editorial headline
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.weight(1f),
        ) {
            FrequencyOrb(
                size = 240.dp,
                isPlaying = false,
                isAnimated = true,
                primaryColor = LiquidFrequency.ElectricCyan,
                secondaryColor = LiquidFrequency.Ultraviolet,
                accentColor = LiquidFrequency.SoftCoral,
            )

            Spacer(Modifier.height(32.dp))

            Text(
                text = "Find the frequency\nthat feels like you.",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Light,
                    lineHeight = 42.sp,
                    letterSpacing = (-0.8).sp,
                ),
                color = LiquidFrequency.FrostedWhite,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Liquid audio architecture crafted for pure acoustic clarity.",
                style = MaterialTheme.typography.bodyMedium,
                color = LiquidFrequency.MutedLavender,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp),
            )
        }

        // Bottom actions: frosted glass cards
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Button(
                onClick = onBegin,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LiquidFrequency.ElectricCyan,
                    contentColor = LiquidFrequency.Obsidian,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(16.dp, RoundedCornerShape(24.dp), spotColor = LiquidFrequency.ElectricCyan),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = "Tune Your Sound",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                        ),
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            Text(
                text = "No mandatory accounts · Pure lossless local listening",
                style = MaterialTheme.typography.labelSmall,
                color = LiquidFrequency.MutedLavender.copy(alpha = 0.65f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

/**
 * Stage 2: Identity & Profile Customization.
 * Refined frosted glass inputs with smooth keyboard interaction and live avatar preview.
 */
@Composable
private fun IdentityStageView(
    name: String,
    onNameChanged: (String) -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .adaptiveContentWidth(maxWidth = 580.dp)
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        // Navigation header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(LiquidFrequency.GlassSubstrate)
                    .border(1.dp, LiquidFrequency.GlassBorderBrush, CircleShape),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = LiquidFrequency.FrostedWhite,
                    modifier = Modifier.size(20.dp),
                )
            }

            Text(
                text = "STEP 1 OF 2",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
                color = LiquidFrequency.ElectricCyan,
            )

            Spacer(Modifier.size(44.dp))
        }

        Spacer(Modifier.height(36.dp))

        // Center Identity Card
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(),
        ) {
            // Live frequency avatar ring
            Box(
                modifier = Modifier
                    .size(108.dp)
                    .clip(CircleShape)
                    .background(LiquidFrequency.GlassSurface)
                    .border(2.dp, LiquidFrequency.GlassBorderActiveBrush, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                val initial = name.trim().firstOrNull()?.uppercase() ?: "T"
                Text(
                    text = initial,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 44.sp,
                    ),
                    color = LiquidFrequency.ElectricCyan,
                )
            }

            Spacer(Modifier.height(28.dp))

            Text(
                text = "What should we call you?",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.4).sp,
                ),
                color = LiquidFrequency.FrostedWhite,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Your identity is stored securely on your device and shapes your experience.",
                style = MaterialTheme.typography.bodyMedium,
                color = LiquidFrequency.MutedLavender,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 20.dp),
            )

            Spacer(Modifier.height(32.dp))

            // Glass input container
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlassPanel(shape = RoundedCornerShape(22.dp)),
                color = Color.Transparent,
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "LISTENER NAME",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.5.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = LiquidFrequency.MutedLavender,
                    )

                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = onNameChanged,
                        placeholder = {
                            Text(
                                "e.g. Gautam, Resonance, Echo",
                                color = LiquidFrequency.MutedLavender.copy(alpha = 0.5f),
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = LiquidFrequency.MidnightIndigo.copy(alpha = 0.7f),
                            unfocusedContainerColor = LiquidFrequency.MidnightIndigo.copy(alpha = 0.4f),
                            focusedBorderColor = LiquidFrequency.ElectricCyan,
                            unfocusedBorderColor = LiquidFrequency.FrostedWhite.copy(alpha = 0.15f),
                            focusedTextColor = LiquidFrequency.FrostedWhite,
                            unfocusedTextColor = LiquidFrequency.FrostedWhite,
                            cursorColor = LiquidFrequency.ElectricCyan,
                        ),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                onContinue()
                            }
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        Spacer(Modifier.height(48.dp))

        // Continue Button
        Button(
            onClick = onContinue,
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = LiquidFrequency.ElectricCyan,
                contentColor = LiquidFrequency.Obsidian,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .shadow(16.dp, RoundedCornerShape(24.dp), spotColor = LiquidFrequency.ElectricCyan),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "Explore Musical Frequencies",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

/**
 * Stage 3: Extraordinary Interactive Genre Selection Screen.
 *
 * “What does your world sound like?”
 * “Choose the sounds that feel like you.”
 *
 * Features reactive central Frequency Orb and distinct sculptured visual treatments
 * for each genre supported in the project.
 */
@Composable
private fun GenreSelectionStageView(
    state: OnboardingUiState,
    orbPrimaryColor: Color,
    orbSecondaryColor: Color,
    onToggleGenre: (String) -> Unit,
    onBack: () -> Unit,
    onFinish: () -> Unit,
    isSaving: Boolean,
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .adaptiveContentWidth(maxWidth = 640.dp),
    ) {
        // Top navigation bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(LiquidFrequency.GlassSubstrate)
                    .border(1.dp, LiquidFrequency.GlassBorderBrush, CircleShape),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = LiquidFrequency.FrostedWhite,
                    modifier = Modifier.size(20.dp),
                )
            }

            Surface(
                shape = CircleShape,
                color = LiquidFrequency.GlassSurface,
                border = BorderStroke(1.dp, LiquidFrequency.ElectricCyan.copy(alpha = 0.35f)),
            ) {
                Text(
                    text = "${state.selectedGenres.size} FREQUENCIES ACTIVE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = LiquidFrequency.ElectricCyan,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                )
            }
        }

        // Scrollable content area
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(8.dp))

            // Central living Frequency Orb reacting to selected genres
            FrequencyOrb(
                size = 140.dp,
                isPlaying = state.selectedGenres.isNotEmpty(),
                isAnimated = true,
                primaryColor = orbPrimaryColor,
                secondaryColor = orbSecondaryColor,
                accentColor = LiquidFrequency.SoftCoral,
            )

            Spacer(Modifier.height(18.dp))

            Text(
                text = "What does your world sound like?",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Light,
                    letterSpacing = (-0.5).sp,
                ),
                color = LiquidFrequency.FrostedWhite,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Choose the sounds that feel like you to tune your personal discovery engine.",
                style = MaterialTheme.typography.bodySmall,
                color = LiquidFrequency.MutedLavender,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp),
            )

            Spacer(Modifier.height(24.dp))

            // Rich interactive genre cards
            val pairs = state.availableGenres.chunked(2)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                pairs.forEach { rowGenres ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        rowGenres.forEach { genre ->
                            val isSelected = genre.id in state.selectedGenres
                            LiquidFrequencyGenreCard(
                                genre = genre,
                                isSelected = isSelected,
                                onClick = { onToggleGenre(genre.id) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        if (rowGenres.size == 1) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }

            Spacer(Modifier.height(28.dp))
        }

        // Bottom dock with Finish / Enter App action
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .liquidGlassPanel(shape = RoundedCornerShape(26.dp), isActive = true),
            color = Color.Transparent,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(
                        text = "READY TO LISTEN",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.2.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = LiquidFrequency.MutedLavender,
                    )
                    Text(
                        text = if (state.selectedGenres.isEmpty()) "Select at least 1" else "${state.selectedGenres.size} genres tuned",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = LiquidFrequency.FrostedWhite,
                    )
                }

                Button(
                    onClick = onFinish,
                    enabled = !isSaving && state.selectedGenres.isNotEmpty(),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LiquidFrequency.ElectricCyan,
                        contentColor = LiquidFrequency.Obsidian,
                        disabledContainerColor = LiquidFrequency.ElectricCyan.copy(alpha = 0.35f),
                    ),
                    modifier = Modifier.height(48.dp),
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            color = LiquidFrequency.Obsidian,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp),
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                text = "Enter TuneGlass",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Individual Distinctive Genre Card with custom visual treatments.
 *
 * Implements the prompt's visual directions:
 * - Electronic/EDM: luminous geometric pulse
 * - Ambient/Lo-Fi: atmospheric hazy gradient
 * - Rock: energetic angular accents
 * - Jazz/Indie: flowing acoustic contours
 * - Hip-hop/DHH: rhythmic bold patterns
 * - Pop/K-Pop: celestial sparkle
 */
@Composable
private fun LiquidFrequencyGenreCard(
    genre: GenreOption,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "GenreCardScale",
    )

    val visualSignature = remember(genre.id) { getGenreSignature(genre.id) }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent,
        modifier = modifier
            .scale(scale)
            .height(104.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    0f to if (isSelected) visualSignature.primaryColor.copy(alpha = 0.28f) else LiquidFrequency.GlassSurface,
                    1f to if (isSelected) visualSignature.secondaryColor.copy(alpha = 0.22f) else LiquidFrequency.GlassSubstrate,
                )
            )
            .border(
                width = if (isSelected) 1.8.dp else 1.dp,
                brush = if (isSelected) {
                    Brush.linearGradient(
                        listOf(visualSignature.primaryColor, visualSignature.secondaryColor)
                    )
                } else {
                    LiquidFrequency.GlassBorderBrush
                },
                shape = RoundedCornerShape(20.dp),
            ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
        ) {
            // Subtle ambient background geometry/accent
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .align(Alignment.TopEnd)
                    .clip(CircleShape)
                    .background(visualSignature.primaryColor.copy(alpha = if (isSelected) 0.22f else 0.08f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = genre.emoji,
                    fontSize = 20.sp,
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(end = 40.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                // Genre title & active status indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = genre.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                            fontSize = 15.sp,
                        ),
                        color = if (isSelected) LiquidFrequency.FrostedWhite else LiquidFrequency.FrostedWhite.copy(alpha = 0.85f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(visualSignature.primaryColor),
                        )
                    }
                }

                // Subtitle / artists snippet
                Text(
                    text = genre.subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = if (isSelected) LiquidFrequency.FrostedWhite.copy(alpha = 0.85f) else LiquidFrequency.MutedLavender,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 14.sp,
                )

                // Visual motif badge
                Text(
                    text = visualSignature.tag,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = visualSignature.primaryColor,
                )
            }
        }
    }
}

private data class GenreVisualSignature(
    val primaryColor: Color,
    val secondaryColor: Color,
    val tag: String,
)

private fun getGenreSignature(id: String): GenreVisualSignature = when (id) {
    "EDM" -> GenreVisualSignature(LiquidFrequency.ElectricCyan, Color(0xFF00E5FF), "GEOMETRIC PULSE")
    "Lo-Fi" -> GenreVisualSignature(LiquidFrequency.Ultraviolet, Color(0xFF7C4DFF), "ATMOSPHERIC HAZE")
    "Rock" -> GenreVisualSignature(LiquidFrequency.SoftCoral, Color(0xFFFF5252), "ANGULAR ENERGY")
    "Indie" -> GenreVisualSignature(Color(0xFF64FFDA), Color(0xFF1DE9B6), "ACOUSTIC RIBBONS")
    "DHH" -> GenreVisualSignature(Color(0xFFFFAB40), Color(0xFFFF6D00), "RHYTHMIC BARS")
    "Punjabi" -> GenreVisualSignature(Color(0xFFFF5252), Color(0xFFFF1744), "DYNAMIC SURGE")
    "Hindi" -> GenreVisualSignature(LiquidFrequency.Ultraviolet, Color(0xFFBA68C8), "HARMONIC CONTOUR")
    "Bollywood" -> GenreVisualSignature(Color(0xFFFFD54F), Color(0xFFFFB300), "CINEMATIC SPECTRA")
    "English" -> GenreVisualSignature(LiquidFrequency.ElectricCyan, LiquidFrequency.Ultraviolet, "ORBITAL WAVES")
    "R&B" -> GenreVisualSignature(Color(0xFFE040FB), Color(0xFF7C4DFF), "FLUID RESONANCE")
    "K-Pop" -> GenreVisualSignature(Color(0xFFFF4081), Color(0xFFF50057), "STELLAR SPARKLE")
    "South" -> GenreVisualSignature(Color(0xFF00E676), Color(0xFF00B0FF), "ACOUSTIC WAVE")
    "Bhojpuri" -> GenreVisualSignature(Color(0xFFFF9100), Color(0xFFFF3D00), "FOLK FREQUENCY")
    "Haryanvi" -> GenreVisualSignature(Color(0xFFFF5722), Color(0xFFE64A19), "PULSING BASS")
    else -> GenreVisualSignature(LiquidFrequency.ElectricCyan, LiquidFrequency.Ultraviolet, "FREQUENCY")
}
