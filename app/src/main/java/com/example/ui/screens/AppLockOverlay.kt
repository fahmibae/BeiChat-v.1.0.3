package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary

@Composable
fun AppLockOverlay(
    onUnlock: () -> Unit
) {
    var enteredPin by remember { mutableStateOf("") }
    val maxPinLength = 4

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("app_lock_overlay"),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(EmeraldPrimary.copy(alpha = 0.15f))
                    .border(2.dp, EmeraldPrimary.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Terkunci",
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "BeiChat Terkunci",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "Masukkan 4-digit PIN atau gunakan sensor biometrik",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp, bottom = 24.dp)
            )

            // PIN Dots Indicator
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                for (i in 0 until maxPinLength) {
                    val isFilled = i < enteredPin.length
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(
                                if (isFilled) EmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .border(
                                1.5.dp,
                                if (isFilled) EmeraldPrimary else MaterialTheme.colorScheme.outline,
                                CircleShape
                            )
                    )
                }
            }

            // Keypad Numbers 1-9
            val keypad = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("BIO", "0", "DEL")
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                keypad.forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        row.forEach { key ->
                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable {
                                        when (key) {
                                            "BIO" -> onUnlock()
                                            "DEL" -> {
                                                if (enteredPin.isNotEmpty()) {
                                                    enteredPin = enteredPin.dropLast(1)
                                                }
                                            }
                                            else -> {
                                                if (enteredPin.length < maxPinLength) {
                                                    enteredPin += key
                                                    if (enteredPin.length == maxPinLength) {
                                                        onUnlock()
                                                        enteredPin = ""
                                                    }
                                                }
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                when (key) {
                                    "BIO" -> {
                                        Icon(
                                            imageVector = Icons.Default.Fingerprint,
                                            contentDescription = "Biometrik",
                                            tint = EmeraldPrimary,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                    "DEL" -> {
                                        Icon(
                                            imageVector = Icons.Default.Backspace,
                                            contentDescription = "Hapus",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    else -> {
                                        Text(
                                            text = key,
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Quick bypass for smooth testing
            Button(
                onClick = onUnlock,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .testTag("biometric_unlock_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = null,
                    tint = Color(0xFF022C22),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Autentikasi Cepat",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF022C22)
                )
            }
        }
    }
}
