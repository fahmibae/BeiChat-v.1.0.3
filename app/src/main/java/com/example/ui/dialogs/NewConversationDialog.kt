package com.example.ui.dialogs

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.ContactEntity
import com.example.ui.components.BitChatAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewConversationDialog(
    contacts: List<ContactEntity>,
    onDismiss: () -> Unit,
    onStartDirectChatWithContact: (ContactEntity) -> Unit,
    onCreateGroup: (title: String, description: String, selectedContactIds: List<String>, timerSeconds: Long) -> Unit
) {
    // Tab 0 = Pilih Akun / Kontak (Chat Pribadi), Tab 1 = Tambah Grup Baru
    var selectedTab by remember { mutableIntStateOf(0) }

    // State for Direct Chat Tab
    var contactSearchQuery by remember { mutableStateOf("") }

    // State for Group Chat Tab
    var groupTitle by remember { mutableStateOf("") }
    var groupDescription by remember { mutableStateOf("") }
    var selectedTimerSeconds by remember { mutableLongStateOf(0L) }
    var isTimerDropdownExpanded by remember { mutableStateOf(false) }
    val selectedGroupMemberIds = remember { mutableStateListOf<String>() }

    val timerOptions = listOf(
        Pair(0L, "Nonaktif (Simpan Permanen)"),
        Pair(30L, "30 Detik (Disappearing)"),
        Pair(60L, "1 Menit (Disappearing)"),
        Pair(3600L, "1 Jam (Disappearing)"),
        Pair(86400L, "24 Jam (Disappearing)"),
        Pair(604800L, "7 Hari (Disappearing)")
    )

    val filteredContacts = remember(contacts, contactSearchQuery) {
        if (contactSearchQuery.isBlank()) {
            contacts
        } else {
            contacts.filter {
                it.name.contains(contactSearchQuery, ignoreCase = true) ||
                        it.username.contains(contactSearchQuery, ignoreCase = true)
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 20.dp)
                .testTag("new_conversation_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
            ) {
                // Header Dialog
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (selectedTab == 0) Icons.Default.PersonAdd else Icons.Default.Group,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = if (selectedTab == 0) "Mulai Pesan Baru" else "Buat Grup Baru",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (selectedTab == 0) "Pilih akun kontak tujuan" else "Grup terenkripsi end-to-end",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_new_conversation_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tab Switcher: Pilih Akun vs Tambah Grup
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp)),
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = MaterialTheme.colorScheme.primary,
                            height = 3.dp
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        selectedContentColor = MaterialTheme.colorScheme.primary,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Pilih Akun",
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                            }
                        },
                        modifier = Modifier.testTag("tab_select_account")
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        selectedContentColor = MaterialTheme.colorScheme.primary,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Group,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Tambah Grup",
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                            }
                        },
                        modifier = Modifier.testTag("tab_create_group")
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (selectedTab == 0) {
                    // TAB 0: PILIH AKUN / KONTAK
                    OutlinedTextField(
                        value = contactSearchQuery,
                        onValueChange = { contactSearchQuery = it },
                        placeholder = { 
                            Text(
                                "Cari nama atau username (@)...", 
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            ) 
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            if (contactSearchQuery.isNotEmpty()) {
                                IconButton(onClick = { contactSearchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Hapus",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_contact_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "AKUN TERHUBUNG (${filteredContacts.size})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (filteredContacts.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 30.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Tidak ada akun kontak ditemukan",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            items(filteredContacts, key = { it.id }) { contact ->
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    ),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            onStartDirectChatWithContact(contact)
                                        }
                                        .testTag("contact_item_${contact.id}")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        BitChatAvatar(
                                            initials = contact.avatarInitials,
                                            colorHex = contact.avatarColorHex,
                                            size = 42.dp,
                                            isOnline = contact.isOnline,
                                            isVerified = contact.isVerified
                                        )

                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text(
                                                    text = contact.name,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                if (contact.isVerified) {
                                                    Icon(
                                                        imageVector = Icons.Default.Shield,
                                                        contentDescription = "Terverifikasi",
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = contact.username,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                text = contact.statusMessage,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.primary
                                        ) {
                                            Text(
                                                text = "Kirim Pesan",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onPrimary,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // TAB 1: TAMBAH GRUP BARU
                    OutlinedTextField(
                        value = groupTitle,
                        onValueChange = { groupTitle = it },
                        label = { Text("Nama Grup") },
                        placeholder = { 
                            Text(
                                "cth. Tim Riset Lapangan",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            ) 
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("group_title_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = groupDescription,
                        onValueChange = { groupDescription = it },
                        label = { Text("Deskripsi / Topik Grup") },
                        placeholder = { 
                            Text(
                                "Ruang koordinasi aman peer-to-peer",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            ) 
                        },
                        maxLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

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
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = isTimerDropdownExpanded,
                            onDismissRequest = { isTimerDropdownExpanded = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            timerOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { 
                                        Text(
                                            text = option.second,
                                            color = MaterialTheme.colorScheme.onSurface
                                        ) 
                                    },
                                    onClick = {
                                        selectedTimerSeconds = option.first
                                        isTimerDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "PILIH ANGGOTA GRUP (${selectedGroupMemberIds.size} dipilih)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(contacts, key = { it.id }) { contact ->
                            val isSelected = selectedGroupMemberIds.contains(contact.id)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                        else Color.Transparent
                                    )
                                    .clickable {
                                        if (isSelected) {
                                            selectedGroupMemberIds.remove(contact.id)
                                        } else {
                                            selectedGroupMemberIds.add(contact.id)
                                        }
                                    }
                                    .padding(vertical = 4.dp, horizontal = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                BitChatAvatar(
                                    initials = contact.avatarInitials,
                                    colorHex = contact.avatarColorHex,
                                    size = 32.dp,
                                    isOnline = contact.isOnline,
                                    isVerified = contact.isVerified
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = contact.name,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = contact.username,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { checked ->
                                        if (checked) {
                                            selectedGroupMemberIds.add(contact.id)
                                        } else {
                                            selectedGroupMemberIds.remove(contact.id)
                                        }
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = MaterialTheme.colorScheme.primary,
                                        uncheckedColor = MaterialTheme.colorScheme.outline
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            if (groupTitle.isNotBlank()) {
                                onCreateGroup(
                                    groupTitle.trim(),
                                    groupDescription.trim(),
                                    selectedGroupMemberIds.toList(),
                                    selectedTimerSeconds
                                )
                            }
                        },
                        enabled = groupTitle.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("submit_create_group_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Buat Grup Terenkripsi E2EE",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}
