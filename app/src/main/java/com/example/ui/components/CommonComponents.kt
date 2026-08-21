package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DisappearingFlameOrange
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.IncomingBubbleDark
import com.example.ui.theme.IncomingBubbleLight
import com.example.ui.theme.LightBg
import com.example.ui.theme.LightBorderSubtle
import com.example.ui.theme.LightSurface
import com.example.ui.theme.LightSurfaceBorder
import com.example.ui.theme.LightSurfaceElevated
import com.example.ui.theme.OnlineGreen
import com.example.ui.theme.OutgoingBubbleDark
import com.example.ui.theme.OutgoingBubbleLight
import com.example.ui.theme.SecurityShieldGold
import com.example.ui.theme.SecurityShieldGreen
import com.example.ui.theme.SecurityShieldRed
import com.example.ui.theme.SleekBlueBorderLight
import com.example.ui.theme.SleekBlueContainerDark
import com.example.ui.theme.SleekBlueContainerLight
import com.example.ui.theme.SleekBlueDark
import com.example.ui.theme.SleekBlueLight
import com.example.ui.theme.SleekBluePrimary
import com.example.ui.theme.SleekBlueSoft
import com.example.ui.theme.SleekBlueTintLight
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextMutedLight
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextPrimaryLight
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.TextSecondaryLight

@Composable
fun BitChatAvatar(
    initials: String,
    colorHex: String,
    size: Dp = 48.dp,
    isOnline: Boolean = false,
    isVerified: Boolean = false,
    modifier: Modifier = Modifier
) {
    val bgBrush = remember(colorHex) {
        val baseColor = try {
            Color(android.graphics.Color.parseColor(colorHex))
        } catch (e: Exception) {
            EmeraldPrimary
        }
        Brush.linearGradient(
            colors = listOf(baseColor, baseColor.copy(alpha = 0.7f))
        )
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(bgBrush)
                .border(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials.take(2).uppercase(),
                color = Color.White,
                fontSize = (size.value * 0.38f).sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (isOnline) {
            Box(
                modifier = Modifier
                    .size(size * 0.3f)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape)
                    .background(OnlineGreen)
                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
            )
        } else if (isVerified) {
            Box(
                modifier = Modifier
                    .size(size * 0.32f)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, SleekBluePrimary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.VerifiedUser,
                    contentDescription = "Terverifikasi",
                    tint = SleekBluePrimary,
                    modifier = Modifier.size(size * 0.22f)
                )
            }
        }
    }
}

@Composable
fun DateDividerPill(
    dateText: String,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background == Color(0xFF0B0F19)
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isDark) Color(0xFF1E293B) else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            Text(
                text = dateText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = if (isDark) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun DeviceSyncPillBanner(
    syncedDeviceCount: Int,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background == Color(0xFF0B0F19)
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isDark) Color(0xFF131D31) else SleekBlueTintLight,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isDark) Color(0xFF1E293B) else SleekBlueBorderLight
        ),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.Lock,
                contentDescription = null,
                tint = if (isDark) SleekBlueSoft else SleekBlueDark,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = "Tersinkronisasi di $syncedDeviceCount perangkat aktif",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isDark) SleekBlueSoft else SleekBlueDark
            )
        }
    }
}

@Composable
fun E2EEShieldBadge(
    isVerified: Boolean,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val tintColor = if (isVerified) SecurityShieldGreen else SecurityShieldGold
    val text = if (isVerified) "E2EE Terverifikasi" else "E2EE Aktif"

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = tintColor.copy(alpha = 0.15f),
        border = androidx.compose.foundation.BorderStroke(1.dp, tintColor.copy(alpha = 0.4f)),
        modifier = modifier
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = if (isVerified) Icons.Default.VerifiedUser else Icons.Default.Lock,
                contentDescription = "Shield",
                tint = tintColor,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = text,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = tintColor
            )
        }
    }
}

@Composable
fun DisappearingTimerChip(
    durationSeconds: Long,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    if (durationSeconds <= 0L) return

    val label = when (durationSeconds) {
        5L -> "5s"
        30L -> "30s"
        60L -> "1m"
        3600L -> "1j"
        86400L -> "24j"
        604800L -> "7h"
        else -> "${durationSeconds}s"
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = DisappearingFlameOrange.copy(alpha = 0.18f),
        border = androidx.compose.foundation.BorderStroke(1.dp, DisappearingFlameOrange.copy(alpha = 0.4f)),
        modifier = modifier.then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Timer,
                contentDescription = "Timer",
                tint = DisappearingFlameOrange,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = DisappearingFlameOrange
            )
        }
    }
}

@Composable
fun DisappearingCountdownBadge(
    expiresAtTimestamp: Long,
    modifier: Modifier = Modifier
) {
    if (expiresAtTimestamp <= 0L) return

    val now = remember { System.currentTimeMillis() }
    val diffSec = ((expiresAtTimestamp - now) / 1000).coerceAtLeast(1)

    val label = when {
        diffSec < 60 -> "${diffSec}d"
        diffSec < 3600 -> "${diffSec / 60}m"
        else -> "${diffSec / 3600}j"
    }

    Row(
        modifier = modifier
            .background(DisappearingFlameOrange.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(text = "🔥", fontSize = 9.sp)
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = DisappearingFlameOrange
        )
    }
}

@Composable
fun WaveformAudioPlayer(
    durationSeconds: Int,
    waveformLevels: String = "40,60,80,45,90,70,35,80,65,50,75,40,95,60,45,30",
    isOutgoing: Boolean,
    modifier: Modifier = Modifier
) {
    var isPlaying by remember { mutableStateOf(false) }
    var playbackProgress by remember { mutableFloatStateOf(0.35f) }

    val infiniteTransition = rememberInfiniteTransition(label = "audio_anim")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val levels = remember(waveformLevels) {
        waveformLevels.split(",").mapNotNull { it.trim().toIntOrNull() }
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isOutgoing) Color(0x22000000) else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        IconButton(
            onClick = { isPlaying = !isPlaying },
            modifier = Modifier
                .size(36.dp)
                .background(if (isOutgoing) EmeraldLight else EmeraldPrimary, CircleShape)
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = Color(0xFF022C22),
                modifier = Modifier.size(20.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            // Waveform visual bars
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                levels.forEachIndexed { index, level ->
                    val fraction = index.toFloat() / levels.size
                    val isPlayed = fraction <= playbackProgress
                    val barColor = when {
                        isPlayed && isOutgoing -> EmeraldLight
                        isPlayed && !isOutgoing -> EmeraldPrimary
                        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    }
                    val heightMultiplier = if (isPlaying && isPlayed) pulse else 1.0f
                    val barHeight = ((level / 100f) * 20f * heightMultiplier).coerceIn(4f, 22f)

                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(barHeight.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(barColor)
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isPlaying) "0:05" else "0:00",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "0:%02d".format(durationSeconds),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

/**
 * QR Code / Fingerprint Matrix Canvas representation for Safety Number Verification
 */
@Composable
fun SafetyMatrixCanvas(
    seed: String,
    size: Dp = 160.dp,
    modifier: Modifier = Modifier
) {
    val hash = remember(seed) {
        val md = java.security.MessageDigest.getInstance("SHA-256")
        md.digest(seed.toByteArray())
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0F172A))
            .border(2.dp, EmeraldPrimary.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val grid = 8
            val cellW = this.size.width / grid
            val cellH = this.size.height / grid

            for (r in 0 until grid) {
                for (c in 0 until grid) {
                    val byteIndex = (r * grid + c) % hash.size
                    val bitVal = (hash[byteIndex].toInt() shr (c % 8)) and 1
                    val isCorner = (r < 2 && c < 2) || (r < 2 && c >= grid - 2) || (r >= grid - 2 && c < 2)

                    val cellColor = when {
                        isCorner -> EmeraldLight
                        bitVal == 1 -> EmeraldPrimary
                        else -> Color(0xFF1E293B)
                    }

                    drawRect(
                        color = cellColor,
                        topLeft = Offset(c * cellW + cellW * 0.1f, r * cellH + cellH * 0.1f),
                        size = Size(cellW * 0.8f, cellH * 0.8f)
                    )
                }
            }
        }
    }
}
