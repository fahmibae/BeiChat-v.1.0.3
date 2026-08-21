package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.example.crypto.CryptoManager
import com.example.data.model.ConversationEntity
import com.example.ui.components.SafetyMatrixCanvas
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.SecurityShieldGreen

@Composable
fun SafetyNumberDialog(
    conversation: ConversationEntity,
    onDismiss: () -> Unit,
    onToggleVerification: (Boolean) -> Unit
) {
    val safetyInfo = remember(conversation.peerPublicKey) {
        CryptoManager.generateSafetyNumbers(
            CryptoManager.localUserPublicKey,
            conversation.peerPublicKey
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 24.dp)
                .testTag("safety_number_dialog")
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
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Shield",
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Verifikasi Keamanan",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
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
                    text = "Bandingkan 60 digit nomor keamanan ini dengan kontak ${conversation.title} untuk memverifikasi bahwa tidak ada pihak ketiga yang dapat menyadap obrolan end-to-end ini.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
                )

                // QR Matrix Canvas
                SafetyMatrixCanvas(
                    seed = safetyInfo.rawHashHex,
                    size = 170.dp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Safety Number Digits Box
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "NOMOR KEAMANAN (60 DIGIT)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        val blocks = safetyInfo.safetyNumberFormatted.split(" ")
                        // Display in 4 rows of 3 blocks
                        for (r in 0 until 4) {
                            val rowBlocks = blocks.drop(r * 3).take(3).joinToString("   ")
                            Text(
                                text = rowBlocks,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 10.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Fingerprint SHA-256:",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = safetyInfo.shortFingerprint,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = CyanAccent
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Verified Toggle Switch Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (conversation.isVerified) SecurityShieldGreen.copy(alpha = 0.12f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (conversation.isVerified) SecurityShieldGreen.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VerifiedUser,
                                    contentDescription = "Verified",
                                    tint = if (conversation.isVerified) SecurityShieldGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Tandai sebagai Terverifikasi",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = if (conversation.isVerified) "Identitas kontak telah teruji & diotentikasi."
                                else "Aktifkan setelah membandingkan nomor keamanan.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Switch(
                            checked = conversation.isVerified,
                            onCheckedChange = { onToggleVerification(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = EmeraldPrimary
                            ),
                            modifier = Modifier.testTag("verify_switch")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Selesai",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF022C22)
                    )
                }
            }
        }
    }
}
