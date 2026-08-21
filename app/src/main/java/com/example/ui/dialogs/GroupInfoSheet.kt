package com.example.ui.dialogs

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.ContactEntity
import com.example.data.model.ConversationEntity
import com.example.ui.components.BitChatAvatar
import com.example.ui.components.DisappearingTimerChip
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary

@Composable
fun GroupInfoSheet(
    conversation: ConversationEntity,
    contacts: List<ContactEntity>,
    onDismiss: () -> Unit,
    onOpenDisappearingTimer: () -> Unit
) {
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
                .testTag("group_info_dialog")
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
                    Text(
                        text = "Info Grup",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                BitChatAvatar(
                    initials = conversation.avatarInitials,
                    colorHex = conversation.avatarColorHex,
                    size = 64.dp,
                    isVerified = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = conversation.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Grup Terenkripsi • ${conversation.groupMembersCount} Anggota",
                    fontSize = 12.sp,
                    color = EmeraldPrimary
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Description Box
                if (conversation.groupDescription.isNotEmpty()) {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "DESKRIPSI GRUP",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = conversation.groupDescription,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Security & Ephemeral Timer Row
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
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
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = "Pesan Otomatis Dihapus",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (conversation.autoDeleteDurationSeconds > 0)
                                            "Aktif (${conversation.autoDeleteDurationSeconds} detik)"
                                        else "Nonaktif",
                                        fontSize = 11.sp,
                                        color = EmeraldPrimary
                                    )
                                }
                            }

                            OutlinedButton(
                                onClick = onOpenDisappearingTimer,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Ubah", fontSize = 11.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Kunci Sesi Grup: ${conversation.peerPublicKey.take(12)}...",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = CyanAccent
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Member List
                Text(
                    text = "DAFTAR ANGGOTA (${conversation.groupMembersCount})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Current User (Admin)
                MemberRowItem(
                    name = "Anda (Pembuat)",
                    subtitle = "Admin Grup • Kunci Terverifikasi",
                    initials = "ME",
                    colorHex = "#10B981",
                    isAdmin = true,
                    isVerified = true
                )

                contacts.take(conversation.groupMembersCount - 1).forEach { contact ->
                    MemberRowItem(
                        name = contact.name,
                        subtitle = contact.username,
                        initials = contact.avatarInitials,
                        colorHex = contact.avatarColorHex,
                        isAdmin = conversation.groupAdmins.contains(contact.name),
                        isVerified = contact.isVerified
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Tutup",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF022C22)
                    )
                }
            }
        }
    }
}

@Composable
private fun MemberRowItem(
    name: String,
    subtitle: String,
    initials: String,
    colorHex: String,
    isAdmin: Boolean,
    isVerified: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BitChatAvatar(
            initials = initials,
            colorHex = colorHex,
            size = 38.dp,
            isVerified = isVerified
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (isAdmin) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = EmeraldPrimary.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "ADMIN",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
