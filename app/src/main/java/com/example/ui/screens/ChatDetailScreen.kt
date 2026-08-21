package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.ConversationEntity
import com.example.data.model.ConversationType
import com.example.data.model.MediaType
import com.example.data.model.MessageEntity
import com.example.data.model.MessageStatus
import com.example.ui.components.BitChatAvatar
import com.example.ui.components.DisappearingCountdownBadge
import com.example.ui.components.DisappearingTimerChip
import com.example.ui.components.E2EEShieldBadge
import com.example.ui.components.EmojiPickerSheet
import com.example.ui.components.WaveformAudioPlayer
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DisappearingFlameOrange
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.IncomingBubbleDark
import com.example.ui.theme.IncomingBubbleLight
import com.example.ui.theme.OutgoingBubbleDark
import com.example.ui.theme.OutgoingBubbleLight
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ChatDetailScreen(
    conversation: ConversationEntity,
    messages: List<MessageEntity>,
    totalUnreadCount: Int = 0,
    replyingMessage: MessageEntity? = null,
    editingMessage: MessageEntity? = null,
    onBack: () -> Unit,
    onSendMessage: (String) -> Unit,
    onStartReply: (MessageEntity) -> Unit = {},
    onCancelReply: () -> Unit = {},
    onStartEdit: (MessageEntity) -> Unit = {},
    onCancelEdit: () -> Unit = {},
    onDeleteMessage: (Long) -> Unit = {},
    onOpenAttachmentSheet: () -> Unit,
    onOpenDisappearingTimerSheet: () -> Unit,
    onOpenSafetyNumbersDialog: () -> Unit,
    onOpenGroupInfoSheet: () -> Unit,
    onOpenMediaViewer: (MessageEntity) -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    var isMenuOpen by remember { mutableStateOf(false) }
    var isEmojiPickerOpen by remember { mutableStateOf(false) }
    var selectedActionMessage by remember { mutableStateOf<MessageEntity?>(null) }
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current

    // Sync input text when editing message changes
    LaunchedEffect(editingMessage) {
        if (editingMessage != null) {
            inputText = editingMessage.content
        }
    }

    val isDark = MaterialTheme.colorScheme.background == Color(0xFF0B0F19)
    val listState = rememberLazyListState()

    // Auto-scroll when messages update
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .testTag("chat_detail_screen")
    ) {
        // Custom Modern Top Bar
        Surface(
            color = if (isDark) Color(0xFF0B0F19) else MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // WhatsApp-Style Back Button with unread counter badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onBack() }
                        .padding(horizontal = 4.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                    if (totalUnreadCount > 0) {
                        Surface(
                            shape = CircleShape,
                            color = com.example.ui.theme.SleekBluePrimary,
                            modifier = Modifier
                                .padding(start = 2.dp, end = 4.dp)
                                .height(20.dp)
                        ) {
                            Text(
                                text = if (totalUnreadCount > 99) "99+" else totalUnreadCount.toString(),
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            if (conversation.type == ConversationType.GROUP) {
                                onOpenGroupInfoSheet()
                            } else {
                                onOpenSafetyNumbersDialog()
                            }
                        }
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    BitChatAvatar(
                        initials = conversation.avatarInitials,
                        colorHex = conversation.avatarColorHex,
                        size = 40.dp,
                        isOnline = conversation.isOnline,
                        isVerified = conversation.isVerified
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = conversation.title,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (isDark) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = if (conversation.type == ConversationType.GROUP)
                                    "${conversation.groupMembersCount} ANGGOTA • END-TO-END ENCRYPTED"
                                else if (conversation.isOnline) "ONLINE • END-TO-END ENCRYPTED"
                                else "END-TO-END ENCRYPTED",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = if (isDark) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Video call & Phone call sleek icons
                IconButton(onClick = onOpenGroupInfoSheet) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = "Video Call",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }

                IconButton(onClick = onOpenSafetyNumbersDialog) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Voice Call",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }

                // Disappearing Timer Badge
                if (conversation.autoDeleteDurationSeconds > 0) {
                    DisappearingTimerChip(
                        durationSeconds = conversation.autoDeleteDurationSeconds,
                        onClick = onOpenDisappearingTimerSheet,
                        modifier = Modifier.padding(end = 2.dp)
                    )
                }

                // Action Menu
                Box {
                    IconButton(onClick = { isMenuOpen = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Menu",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    DropdownMenu(
                        expanded = isMenuOpen,
                        onDismissRequest = { isMenuOpen = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Verifikasi Kunci Keamanan") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = EmeraldPrimary
                                )
                            },
                            onClick = {
                                isMenuOpen = false
                                onOpenSafetyNumbersDialog()
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Timer Hapus Otomatis") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = DisappearingFlameOrange
                                )
                            },
                            onClick = {
                                isMenuOpen = false
                                onOpenDisappearingTimerSheet()
                            }
                        )

                        if (conversation.type == ConversationType.GROUP) {
                            DropdownMenuItem(
                                text = { Text("Info Grup") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = CyanAccent
                                    )
                                },
                                onClick = {
                                    isMenuOpen = false
                                    onOpenGroupInfoSheet()
                                }
                            )
                        }
                    }
                }
            }
        }

        // E2EE & Off-Grid Mesh Info Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (isDark) Color(0xFF131D31) else com.example.ui.theme.SleekBlueTintLight)
                .padding(horizontal = 16.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = if (isDark) com.example.ui.theme.SleekBlueSoft else com.example.ui.theme.SleekBluePrimary,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = "🔒 P2P Mesh Off-Grid Aktif • AES-256-GCM + X25519 (0 KB Kuota)",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) com.example.ui.theme.SleekBlueSoft else com.example.ui.theme.SleekBlueDark
                )
            }
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                com.example.ui.components.DateDividerPill(dateText = "Hari Ini, 20 Agt")
                Spacer(modifier = Modifier.height(4.dp))
            }

            items(messages, key = { it.id }) { message ->
                MessageBubbleItem(
                    message = message,
                    isGroup = conversation.type == ConversationType.GROUP,
                    onOpenMediaViewer = onOpenMediaViewer,
                    onLongClick = {
                        selectedActionMessage = message
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    com.example.ui.components.DeviceSyncPillBanner(syncedDeviceCount = 3)
                }
            }
        }

        // WhatsApp-Style Reply Preview Bar
        AnimatedVisibility(
            visible = replyingMessage != null,
            enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut()
        ) {
            replyingMessage?.let { replyMsg ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 8.dp, bottomEnd = 8.dp),
                    color = if (isDark) Color(0xFF1E293B) else MaterialTheme.colorScheme.surfaceVariant,
                    tonalElevation = 2.dp,
                    border = androidx.compose.foundation.BorderStroke(
                        0.5.dp,
                        com.example.ui.theme.SleekBluePrimary.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(36.dp)
                                .background(com.example.ui.theme.SleekBluePrimary, RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Membalas ${replyMsg.senderName}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = com.example.ui.theme.SleekBluePrimary
                            )
                            Text(
                                text = replyMsg.content.ifBlank { replyMsg.mediaCaption ?: "Lampiran Media" },
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = onCancelReply,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Batal Balas",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // WhatsApp-Style Edit Message Bar
        AnimatedVisibility(
            visible = editingMessage != null,
            enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut()
        ) {
            editingMessage?.let { editMsg ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 8.dp, bottomEnd = 8.dp),
                    color = if (isDark) Color(0xFF1E293B) else Color(0xFFFEF3C7),
                    tonalElevation = 2.dp,
                    border = androidx.compose.foundation.BorderStroke(
                        0.5.dp,
                        Color(0xFFF59E0B)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(36.dp)
                                .background(Color(0xFFF59E0B), RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Edit Pesan",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD97706)
                            )
                            Text(
                                text = editMsg.content,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = {
                                onCancelEdit()
                                inputText = ""
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Batal Edit",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // WhatsApp-Style Floating Bottom Input Bar (Floating Rounded Pill + Independent Circular Action Button)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 6.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Main WhatsApp Floating Pill Card
            Surface(
                shape = RoundedCornerShape(26.dp),
                color = if (isDark) Color(0xFF1E293B) else MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp,
                tonalElevation = 2.dp,
                border = androidx.compose.foundation.BorderStroke(
                    0.5.dp,
                    if (isDark) Color(0xFF334155) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                ),
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Emoji Button
                    IconButton(
                        onClick = { isEmojiPickerOpen = true },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEmotions,
                            contentDescription = "Emoji",
                            tint = if (isDark) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Flexible Multi-line Auto-Expanding Text Input
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (inputText.isEmpty()) {
                            Text(
                                text = if (editingMessage != null) "Edit pesan Anda..." else "Ketik pesan terenkripsi...",
                                fontSize = 15.sp,
                                color = if (isDark) Color(0xFF64748B) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                        BasicTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            textStyle = TextStyle(
                                fontSize = 15.sp,
                                color = if (isDark) Color(0xFFF1F5F9) else MaterialTheme.colorScheme.onSurface
                            ),
                            cursorBrush = SolidColor(com.example.ui.theme.SleekBluePrimary),
                            maxLines = 5,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("chat_input_field")
                        )
                    }

                    // Attachment Clip Button
                    IconButton(
                        onClick = onOpenAttachmentSheet,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AttachFile,
                            contentDescription = "Lampiran Dokumen/Gambar",
                            tint = if (isDark) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(21.dp)
                        )
                    }

                    // Disappearing Timer / Quick Action Button
                    IconButton(
                        onClick = onOpenDisappearingTimerSheet,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Timer Otomatis Hapus",
                            tint = if (conversation.autoDeleteDurationSeconds > 0) DisappearingFlameOrange
                            else (if (isDark) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // WhatsApp Style Floating Round Action Button (Mic / Send FAB / Checkmark for Edit)
            Surface(
                onClick = {
                    if (inputText.isNotBlank()) {
                        onSendMessage(inputText.trim())
                        inputText = ""
                    } else {
                        onOpenAttachmentSheet()
                    }
                },
                shape = CircleShape,
                color = if (editingMessage != null) Color(0xFF10B981) else com.example.ui.theme.SleekBluePrimary,
                shadowElevation = 3.dp,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("chat_send_button")
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = when {
                            editingMessage != null -> Icons.Default.Check
                            inputText.isNotBlank() -> Icons.AutoMirrored.Filled.Send
                            else -> Icons.Default.Mic
                        },
                        contentDescription = if (editingMessage != null) "Simpan Edit" else if (inputText.isNotBlank()) "Kirim" else "Rekam Suara",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // WhatsApp-Style Long Press Context Menu BottomSheet / Dialog
        selectedActionMessage?.let { selectedMsg ->
            val isMyMessage = selectedMsg.isOutgoing
            AlertDialog(
                onDismissRequest = { selectedActionMessage = null },
                containerColor = if (isDark) Color(0xFF1E293B) else MaterialTheme.colorScheme.surface,
                title = {
                    Text(
                        text = "Opsi Pesan",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Action 1: Balas (Reply)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    onStartReply(selectedMsg)
                                    selectedActionMessage = null
                                }
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Reply,
                                contentDescription = "Balas",
                                tint = com.example.ui.theme.SleekBluePrimary
                            )
                            Text(
                                text = "Balas (Reply)",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Action 2: Salin (Copy)
                        if (selectedMsg.content.isNotBlank()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(selectedMsg.content))
                                        selectedActionMessage = null
                                    }
                                    .padding(horizontal = 12.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Salin",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Salin Teks",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Action 3: Edit Pesan (Hanya untuk pesan sendiri yang bertipe teks)
                        if (isMyMessage && selectedMsg.mediaType == MediaType.NONE) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        onStartEdit(selectedMsg)
                                        selectedActionMessage = null
                                    }
                                    .padding(horizontal = 12.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Pesan",
                                    tint = Color(0xFFF59E0B)
                                )
                                Text(
                                    text = "Edit Pesan",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Action 4: Hapus Pesan (Delete)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    onDeleteMessage(selectedMsg.id)
                                    selectedActionMessage = null
                                }
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Hapus Pesan",
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = "Hapus Pesan",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { selectedActionMessage = null }) {
                        Text("Tutup")
                    }
                }
            )
        }

        if (isEmojiPickerOpen) {
            EmojiPickerSheet(
                onDismiss = { isEmojiPickerOpen = false },
                onEmojiSelected = { emoji ->
                    inputText += emoji
                },
                onBackspace = {
                    if (inputText.isNotEmpty()) {
                        inputText = inputText.dropLast(1)
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubbleItem(
    message: MessageEntity,
    isGroup: Boolean,
    onOpenMediaViewer: (MessageEntity) -> Unit,
    onLongClick: () -> Unit = {}
) {
    val isOutgoing = message.isOutgoing
    val alignment = if (isOutgoing) Alignment.End else Alignment.Start

    val isDarkTheme = MaterialTheme.colorScheme.background == Color(0xFF0B0F19)
    val bubbleBg = if (isOutgoing) {
        if (isDarkTheme) OutgoingBubbleDark else OutgoingBubbleLight
    } else {
        if (isDarkTheme) IncomingBubbleDark else IncomingBubbleLight
    }

    // Sleek Interface Asymmetric Corner Radii (rounded-2xl rounded-tl-none / rounded-tr-none)
    val bubbleShape = if (isOutgoing) {
        RoundedCornerShape(topStart = 18.dp, topEnd = 2.dp, bottomStart = 18.dp, bottomEnd = 18.dp)
    } else {
        RoundedCornerShape(topStart = 2.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 18.dp)
    }

    val timeFormatter = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val formattedTime = remember(message.timestamp) { timeFormatter.format(Date(message.timestamp)) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        if (isGroup && !isOutgoing && message.senderId != "system") {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(start = 4.dp, bottom = 3.dp)
            ) {
                Text(
                    text = message.senderName,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (isDarkTheme) com.example.ui.theme.SleekBlueSoft else com.example.ui.theme.SleekBlueDark
                )
                Text(
                    text = formattedTime,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }

        Surface(
            shape = bubbleShape,
            color = bubbleBg,
            modifier = Modifier
                .widthIn(max = 310.dp)
                .combinedClickable(
                    onClick = { /* normal click */ },
                    onLongClick = onLongClick
                )
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                // Quoted / Reply Preview Box (WhatsApp Style inside Bubble)
                if (message.replyToSender.isNotBlank() && message.replyToContent.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isDarkTheme) Color(0xFF0F172A).copy(alpha = 0.7f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(28.dp)
                                    .background(com.example.ui.theme.SleekBluePrimary, RoundedCornerShape(2.dp))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = message.replyToSender,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = com.example.ui.theme.SleekBluePrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = message.replyToContent,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Media Type Renderers
                when (message.mediaType) {
                    MediaType.IMAGE_HIGH_RES -> {
                        Column(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onOpenMediaViewer(message) }
                        ) {
                            Box {
                                AsyncImage(
                                    model = message.mediaUri,
                                    contentDescription = message.mediaCaption,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                )

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color.Black.copy(alpha = 0.6f),
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.HighQuality,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Text(
                                            text = "4K RES",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }

                            if (message.mediaCaption.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            if (isDarkTheme) Color(0xFF1E293B) else MaterialTheme.colorScheme.surfaceVariant,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = message.mediaCaption,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Icon(
                                        imageVector = Icons.Default.AttachFile,
                                        contentDescription = "Download",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    MediaType.AUDIO_VOICE_NOTE -> {
                        WaveformAudioPlayer(
                            durationSeconds = message.audioDurationSeconds,
                            waveformLevels = message.audioWaveformLevels,
                            isOutgoing = isOutgoing,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    MediaType.DOCUMENT -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (isDarkTheme) Color(0xFF1E293B) else MaterialTheme.colorScheme.surfaceVariant,
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = if (isDarkTheme) com.example.ui.theme.SleekBlueSoft else com.example.ui.theme.SleekBluePrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = message.mediaCaption,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${message.mediaSizeFormatted} • AES-256 Encrypted",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (isDarkTheme) com.example.ui.theme.SleekBlueSoft else com.example.ui.theme.SleekBluePrimary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    MediaType.SECURE_LOCATION -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (isDarkTheme) Color(0xFF1E293B) else MaterialTheme.colorScheme.surfaceVariant,
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = "📍", fontSize = 18.sp)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Koordinat Lokasi Aman",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Lat: -6.2088, Long: 106.8456 (E2EE)",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    MediaType.NONE -> {
                        Text(
                            text = message.content,
                            fontSize = 14.sp,
                            color = if (isOutgoing) {
                                if (isDarkTheme) Color(0xFFF8FAFC) else Color(0xFF0F172A)
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            lineHeight = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Footer with Disappearing Countdown, Time & Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (message.expiresAtTimestamp > 0) {
                        Text(
                            text = "Auto-deletes",
                            fontSize = 9.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        DisappearingCountdownBadge(
                            expiresAtTimestamp = message.expiresAtTimestamp,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                    }

                    if (message.isEdited) {
                        Text(
                            text = "diedit",
                            fontSize = 10.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }

                    Text(
                        text = formattedTime,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.padding(end = 4.dp)
                    )

                    if (isOutgoing) {
                        Icon(
                            imageVector = when (message.status) {
                                MessageStatus.READ -> Icons.Default.DoneAll
                                MessageStatus.DELIVERED -> Icons.Default.DoneAll
                                else -> Icons.Default.Check
                            },
                            contentDescription = null,
                            tint = if (message.status == MessageStatus.READ) com.example.ui.theme.SleekBluePrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}
