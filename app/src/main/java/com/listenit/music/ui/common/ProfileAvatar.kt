package com.listenit.music.ui.common

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.listenit.music.ui.theme.LiquidFrequency

data class MiniDoodle(
    val id: String,
    val label: String,
    val emoji: String,
    val bgColors: List<Color>,
)

val MINI_DOODLES = listOf(
    MiniDoodle("doodle_headphones", "Groove", "🎧", listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))),
    MiniDoodle("doodle_note", "Melody", "🎵", listOf(Color(0xFFEC4899), Color(0xFFF43F5E))),
    MiniDoodle("doodle_cat", "Cool Cat", "🐱", listOf(Color(0xFFF59E0B), Color(0xFFD97706))),
    MiniDoodle("doodle_fire", "Anthem", "🔥", listOf(Color(0xFFEF4444), Color(0xFFEA580C))),
    MiniDoodle("doodle_sparkles", "Cosmic", "✨", listOf(Color(0xFF10B981), Color(0xFF059669))),
    MiniDoodle("doodle_planet", "Astral", "🪐", listOf(Color(0xFF8B5CF6), Color(0xFF3B82F6))),
    MiniDoodle("doodle_coffee", "Lo-Fi", "☕", listOf(Color(0xFF92400E), Color(0xFF78350F))),
    MiniDoodle("doodle_vinyl", "Retro", "💿", listOf(Color(0xFF4B5563), Color(0xFF1F2937))),
    MiniDoodle("doodle_sunglasses", "Chill", "🕶️", listOf(Color(0xFF06B6D4), Color(0xFF0284C7))),
    MiniDoodle("doodle_robot", "Synth", "🤖", listOf(Color(0xFF14B8A6), Color(0xFF0F766E))),
)

/**
 * Universal User Avatar display.
 * Renders gallery/network image if set, matching mini doodle if selected,
 * or user initial / person icon as graceful fallback.
 */
@Composable
fun UserAvatarDisplay(
    avatar: String?,
    displayName: String? = null,
    size: Dp = 36.dp,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val doodle = remember(avatar) {
        if (!avatar.isNullOrBlank() && avatar.startsWith("doodle_")) {
            MINI_DOODLES.firstOrNull { it.id == avatar }
        } else null
    }

    val clickableModifier = if (onClick != null) {
        modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick,
        )
    } else modifier

    Box(
        modifier = clickableModifier
            .size(size)
            .clip(CircleShape)
            .background(
                if (doodle != null) {
                    Brush.linearGradient(doodle.bgColors)
                } else {
                    Brush.linearGradient(
                        listOf(
                            LiquidFrequency.GlassSurface,
                            LiquidFrequency.GlassSubstrate,
                        )
                    )
                }
            )
            .border(
                1.dp,
                if (doodle != null) Color.White.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.12f),
                CircleShape
            ),
        contentAlignment = Alignment.Center,
    ) {
        when {
            doodle != null -> {
                Text(
                    text = doodle.emoji,
                    fontSize = (size.value * 0.48f).sp,
                )
            }
            !avatar.isNullOrBlank() -> {
                AsyncImage(
                    model = avatar,
                    contentDescription = displayName ?: "User avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            !displayName.isNullOrBlank() -> {
                Text(
                    text = displayName.trim().first().uppercase(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = (size.value * 0.45f).sp,
                    ),
                    color = LiquidFrequency.FrostedWhite,
                )
            }
            else -> {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "User profile",
                    tint = LiquidFrequency.FrostedWhite.copy(alpha = 0.85f),
                    modifier = Modifier.size((size.value * 0.55f).dp),
                )
            }
        }
    }
}

/**
 * Bottom Sheet for picking a profile photo from device gallery
 * or choosing from vibrant mini doodles.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileAvatarPickerSheet(
    currentAvatar: String?,
    onSelectAvatar: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        if (uri != null) {
            onSelectAvatar(uri.toString())
            onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = LiquidFrequency.GlassSurfaceElevated,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Choose Profile Photo",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = LiquidFrequency.FrostedWhite,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Upload from gallery or pick a mini doodle avatar",
                style = MaterialTheme.typography.bodyMedium,
                color = LiquidFrequency.MutedLavender,
            )

            Spacer(Modifier.height(24.dp))

            // Current Preview
            UserAvatarDisplay(
                avatar = currentAvatar,
                size = 80.dp,
            )

            Spacer(Modifier.height(24.dp))

            // Option 1: Gallery Photo
            Button(
                onClick = { galleryLauncher.launch("image/*") },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LiquidFrequency.FrostedWhite,
                    contentColor = LiquidFrequency.Obsidian,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
            ) {
                Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Choose from Gallery", fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(20.dp))

            // Option 2: Mini Doodles Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(Icons.Filled.Palette, contentDescription = null, tint = LiquidFrequency.ElectricCyan, modifier = Modifier.size(18.dp))
                Text(
                    text = "Or Pick a Mini Doodle",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = LiquidFrequency.FrostedWhite,
                )
            }

            Spacer(Modifier.height(12.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                items(MINI_DOODLES, key = { it.id }) { doodle ->
                    val isSelected = currentAvatar == doodle.id
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable {
                                onSelectAvatar(doodle.id)
                                onDismiss()
                            }
                            .padding(vertical = 4.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(doodle.bgColors))
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) LiquidFrequency.ElectricCyan else Color.White.copy(alpha = 0.3f),
                                    shape = CircleShape,
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(doodle.emoji, fontSize = 26.sp)
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .align(Alignment.BottomEnd)
                                        .background(LiquidFrequency.ElectricCyan, CircleShape),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        Icons.Filled.Check,
                                        contentDescription = null,
                                        tint = LiquidFrequency.Obsidian,
                                        modifier = Modifier.size(12.dp),
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = doodle.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) LiquidFrequency.ElectricCyan else LiquidFrequency.MutedLavender,
                        )
                    }
                }
            }

            if (!currentAvatar.isNullOrBlank()) {
                Spacer(Modifier.height(20.dp))
                OutlinedButton(
                    onClick = {
                        onSelectAvatar("")
                        onDismiss()
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Remove Avatar", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
