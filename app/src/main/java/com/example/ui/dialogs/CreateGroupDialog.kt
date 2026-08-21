package com.example.ui.dialogs

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
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
import com.example.data.model.ContactEntity
import com.example.ui.components.BitChatAvatar
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupDialog(
    contacts: List<ContactEntity>,
    onDismiss: () -> Unit,
    onCreateGroup: (title: String, description: String, selectedContactIds: List<String>, timerSeconds: Long) -> Unit
) {
    var groupTitle by remember { mutableStateOf("") }
    var groupDescription by remember { mutableStateOf("") }
    var selectedTimerSeconds by remember { mutableLongStateOf(0L) }
    var isTimerDropdownExpanded by remember { mutableStateOf(false) }

    val selectedContactIds = remember { mutableStateListOf<String>() }

    val timerOptions = listOf(
        Pair(0L, "Nonaktif (Simpan Permanen)"),
        Pair(30L, "30 Detik (Disappearing)"),
        Pair(60L, "1 Menit (Disappearing)"),
        Pair(3600L, "1 Jam (Disappearing)"),
        Pair(86400L, "24 Jam (Disappearing)"),
        Pair(604800L, "7 Hari (Disappearing)")
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
                .testTag("create_group_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
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
                                .background(EmeraldPrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = "Group",
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Grup Terenkripsi Baru",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Kunci Sesi Grup E2EE Otomatis",
                                fontSize = 11.sp,
                                color = EmeraldPrimary
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

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = groupTitle,
                    onValueChange = { groupTitle = it },
                    label = { Text("Nama Grup") },
                    placeholder = { Text("cth. Tim Riset Kriptografi") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("group_title_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = groupDescription,
                    onValueChange = { groupDescription = it },
                    label = { Text("Deskripsi / Topik Grup") },
                    placeholder = { Text("Ruang koordinasi aman peer-to-peer") },
                    maxLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Disappearing message dropdown
                ExposedDropdownMenuBox(
                    expanded = isTimerDropdownExpanded,
                    onExpandedChange = { isTimerDropdownExpanded = !isTimerDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = timerOptions.firstOrNull { it.first == selectedTimerSeconds }?.second ?: "Nonaktif",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Pesan Otomatis Hapus") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isTimerDropdownExpanded) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = EmeraldPrimary
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = isTimerDropdownExpanded,
                        onDismissRequest = { isTimerDropdownExpanded = false }
                    ) {
                        timerOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.second) },
                                onClick = {
                                    selectedTimerSeconds = option.first
                                    isTimerDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "PILIH ANGGOTA (${selectedContactIds.size} dipilih)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .height(160.dp)
                ) {
                    items(contacts, key = { it.id }) { contact ->
                        val isSelected = selectedContactIds.contains(contact.id)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    if (isSelected) {
                                        selectedContactIds.remove(contact.id)
                                    } else {
                                        selectedContactIds.add(contact.id)
                                    }
                                }
                                .padding(vertical = 6.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BitChatAvatar(
                                initials = contact.avatarInitials,
                                colorHex = contact.avatarColorHex,
                                size = 38.dp,
                                isOnline = contact.isOnline,
                                isVerified = contact.isVerified
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = contact.name,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = contact.username,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        selectedContactIds.add(contact.id)
                                    } else {
                                        selectedContactIds.remove(contact.id)
                                    }
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = EmeraldPrimary
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        if (groupTitle.isNotBlank()) {
                            onCreateGroup(
                                groupTitle.trim(),
                                groupDescription.trim(),
                                selectedContactIds.toList(),
                                selectedTimerSeconds
                            )
                        }
                    },
                    enabled = groupTitle.isNotBlank(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmeraldPrimary,
                        disabledContainerColor = EmeraldPrimary.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("submit_create_group_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color(0xFF022C22),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Buat Grup Terenkripsi E2EE",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF022C22)
                    )
                }
            }
        }
    }
}
