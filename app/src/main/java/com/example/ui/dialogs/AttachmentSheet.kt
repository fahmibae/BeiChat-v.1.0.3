package com.example.ui.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary

enum class AttachmentTab {
    MENU,
    IMAGE_SENDER,
    AUDIO_RECORDER,
    DOCUMENT_PICKER
}

@Composable
fun AttachmentSheet(
    onDismiss: () -> Unit,
    onSendImage: (caption: String, isHighRes: Boolean) -> Unit,
    onSendVoiceNote: (durationSeconds: Int) -> Unit,
    onSendDocument: (fileName: String, fileSize: String) -> Unit,
    onSendLocation: () -> Unit
) {
    var activeTab by remember { mutableStateOf(AttachmentTab.MENU) }
    var imageCaption by remember { mutableStateOf("") }
    var isHighResOriginal by remember { mutableStateOf(true) }
    var recordingDuration by remember { mutableIntStateOf(6) }

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
                .testTag("attachment_dialog")
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = when (activeTab) {
                            AttachmentTab.MENU -> "Kirim Media Terenkripsi"
                            AttachmentTab.IMAGE_SENDER -> "Kirim Foto (Resolusi Tinggi)"
                            AttachmentTab.AUDIO_RECORDER -> "Rekam Pesan Suara"
                            AttachmentTab.DOCUMENT_PICKER -> "Pilih Dokumen Aman"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    IconButton(onClick = {
                        if (activeTab != AttachmentTab.MENU) activeTab = AttachmentTab.MENU
                        else onDismiss()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                when (activeTab) {
                    AttachmentTab.MENU -> {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            // 1. High-Res Photo
                            AttachmentOptionCard(
                                icon = Icons.Default.Image,
                                iconBg = EmeraldPrimary,
                                title = "Foto Resolusi Tinggi (HD Lossless)",
                                subtitle = "Enkripsi AES-256 tanpa kompresi / penurunan kualitas",
                                onClick = { activeTab = AttachmentTab.IMAGE_SENDER }
                            )

                            // 2. Audio Voice Note
                            AttachmentOptionCard(
                                icon = Icons.Default.Mic,
                                iconBg = CyanAccent,
                                title = "Pesan Suara Terenkripsi",
                                subtitle = "Klip audio terenkripsi lokal dengan visualizer gelombang",
                                onClick = { activeTab = AttachmentTab.AUDIO_RECORDER }
                            )

                            // 3. Secure Document
                            AttachmentOptionCard(
                                icon = Icons.Default.Description,
                                iconBg = Color(0xFF8B5CF6),
                                title = "Dokumen / File Aman",
                                subtitle = "PDF, Arsip ZIP, Dokumen dengan SHA-256 hash",
                                onClick = { activeTab = AttachmentTab.DOCUMENT_PICKER }
                            )

                            // 4. Secure Location
                            AttachmentOptionCard(
                                icon = Icons.Default.LocationOn,
                                iconBg = Color(0xFFF59E0B),
                                title = "Koordinat Lokasi Aman",
                                subtitle = "Kirim koordinat GPS terenkripsi end-to-end",
                                onClick = {
                                    onSendLocation()
                                    onDismiss()
                                }
                            )
                        }
                    }

                    AttachmentTab.IMAGE_SENDER -> {
                        Column {
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.HighQuality,
                                                contentDescription = null,
                                                tint = EmeraldPrimary
                                            )
                                            Column {
                                                Text(
                                                    text = "Resolusi Tinggi Asli (HD Lossless)",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = if (isHighResOriginal) "Kualitas penuh 100% (Bit-by-Bit E2EE)"
                                                    else "Kompresi standar",
                                                    fontSize = 11.sp,
                                                    color = EmeraldPrimary
                                                )
                                            }
                                        }

                                        Switch(
                                            checked = isHighResOriginal,
                                            onCheckedChange = { isHighResOriginal = it },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = Color.White,
                                                checkedTrackColor = EmeraldPrimary
                                            )
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = imageCaption,
                                onValueChange = { imageCaption = it },
                                label = { Text("Keterangan Gambar (Opsional)") },
                                placeholder = { Text("Tulis deskripsi aman...") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = EmeraldPrimary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = { onSendImage(imageCaption, isHighResOriginal) },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = null,
                                    tint = Color(0xFF022C22),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isHighResOriginal) "Kirim Foto Resolusi Tinggi Asli" else "Kirim Foto Standar",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF022C22)
                                )
                            }
                        }
                    }

                    AttachmentTab.AUDIO_RECORDER -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(CyanAccent.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = null,
                                    tint = CyanAccent,
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "0:0%02d".format(recordingDuration),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                text = "Audio terenkripsi instan sebelum transmisi",
                                fontSize = 11.sp,
                                color = EmeraldPrimary,
                                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                            )

                            Button(
                                onClick = { onSendVoiceNote(recordingDuration) },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = null,
                                    tint = Color(0xFF083344),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Kirim Pesan Suara",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF083344)
                                )
                            }
                        }
                    }

                    AttachmentTab.DOCUMENT_PICKER -> {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            val docs = listOf(
                                Pair("Audit_Keamanan_E2EE_2026.pdf", "3.4 MB"),
                                Pair("Protokol_Kriptografi_BitChat.docx", "1.2 MB"),
                                Pair("Source_Code_X25519_Archive.zip", "8.9 MB")
                            )

                            docs.forEach { (name, size) ->
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { onSendDocument(name, size) }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Description,
                                            contentDescription = null,
                                            tint = Color(0xFF8B5CF6),
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = name,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "$size • Enkripsi AES-GCM",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.Default.Security,
                                            contentDescription = null,
                                            tint = EmeraldPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AttachmentOptionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBg: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconBg.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconBg,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
