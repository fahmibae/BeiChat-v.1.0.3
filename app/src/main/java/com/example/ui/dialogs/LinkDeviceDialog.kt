package com.example.ui.dialogs

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tablet
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.DeviceType
import com.example.ui.components.SafetyMatrixCanvas
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary

@Composable
fun LinkDeviceDialog(
    onDismiss: () -> Unit,
    onLinkDevice: (name: String, type: DeviceType, platform: String) -> Unit
) {
    var deviceName by remember { mutableStateOf("") }
    var selectedDeviceType by remember { mutableStateOf(DeviceType.DESKTOP) }
    var pairingCode by remember {
        mutableStateOf(
            "${(1000..9999).random()}-${(1000..9999).random()}-${(1000..9999).random()}"
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "scanner_line")
    val scanProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scanProgress"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 20.dp)
                .testTag("link_device_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(CyanAccent.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Devices,
                                contentDescription = "Devices",
                                tint = CyanAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Tautkan Perangkat Baru",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Sinkronisasi Enkripsi Multi-Device",
                                fontSize = 11.sp,
                                color = CyanAccent
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = "Buka BitChat di Desktop / Browser / Tablet Anda lalu pindai kode QR berikut untuk menyinkronkan kunci sesi secara end-to-end tanpa perantara cloud.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp, bottom = 14.dp)
                )

                // Animated QR Scanner Canvas
                Box(contentAlignment = Alignment.Center) {
                    SafetyMatrixCanvas(
                        seed = pairingCode,
                        size = 160.dp
                    )
                    // Scanner glow overlay line
                    Box(
                        modifier = Modifier
                            .width(140.dp)
                            .height(2.dp)
                            .background(CyanAccent)
                            .padding(top = (scanProgress * 140).dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Pairing Token Box
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "KODE PAIRING AMAN",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = pairingCode,
                                fontSize = 14.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = CyanAccent
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = null,
                            tint = CyanAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Device Type Selector Row
                Text(
                    text = "TIPE PERANGKAT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val types = listOf(
                        Triple(DeviceType.DESKTOP, "Desktop", Icons.Default.Computer),
                        Triple(DeviceType.TABLET, "Tablet", Icons.Default.Tablet),
                        Triple(DeviceType.WEB, "Web", Icons.Default.Language)
                    )

                    types.forEach { (type, label, icon) ->
                        val isSelected = selectedDeviceType == type
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) EmeraldPrimary.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) EmeraldPrimary else Color.Transparent
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedDeviceType = type }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    tint = if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = deviceName,
                    onValueChange = { deviceName = it },
                    label = { Text("Nama Perangkat") },
                    placeholder = {
                        Text(
                            when (selectedDeviceType) {
                                DeviceType.DESKTOP -> "cth. MacBook Pro / ThinkPad"
                                DeviceType.TABLET -> "cth. iPad Pro 12.9"
                                DeviceType.WEB -> "cth. Chrome di Linux Ubuntu"
                                else -> "cth. Laptop Kantor"
                            }
                        )
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("device_name_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val name = deviceName.ifBlank {
                            when (selectedDeviceType) {
                                DeviceType.DESKTOP -> "BitChat Desktop Client"
                                DeviceType.TABLET -> "BitChat Tablet Client"
                                DeviceType.WEB -> "BitChat Web Session"
                                else -> "BitChat Linked Client"
                            }
                        }
                        val platform = when (selectedDeviceType) {
                            DeviceType.DESKTOP -> "Desktop OS (E2EE Synchronized)"
                            DeviceType.TABLET -> "Tablet OS (E2EE Synchronized)"
                            DeviceType.WEB -> "Web Cryptography API (Chrome)"
                            else -> "Multi-Platform Client"
                        }
                        onLinkDevice(name, selectedDeviceType, platform)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("submit_link_device_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = null,
                        tint = Color(0xFF022C22),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Konfirmasi Sinkronisasi Sesi",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF022C22)
                    )
                }
            }
        }
    }
}
