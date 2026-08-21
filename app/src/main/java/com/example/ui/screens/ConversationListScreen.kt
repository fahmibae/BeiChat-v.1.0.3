package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.WifiTethering
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import com.example.ui.viewmodel.AppThemeMode
import com.example.data.model.ConversationEntity
import com.example.data.model.ConversationType
import com.example.data.model.LinkedDeviceEntity
import com.example.data.model.SecurityLogEntity
import com.example.mesh.MeshPacketLog
import com.example.mesh.MeshPeerNode
import com.example.mesh.MeshSosBroadcast
import com.example.mesh.MeshStats
import com.example.ui.components.BitChatAvatar
import com.example.ui.components.DisappearingTimerChip
import com.example.ui.components.E2EEShieldBadge
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DisappearingFlameOrange
import com.example.ui.theme.OnlineGreen
import com.example.ui.theme.SecurityShieldRed
import com.example.ui.theme.SleekBlueBorderLight
import com.example.ui.theme.SleekBlueDark
import com.example.ui.theme.SleekBlueLight
import com.example.ui.theme.SleekBluePrimary
import com.example.ui.theme.SleekBlueSoft
import com.example.ui.theme.SleekBlueTintLight
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ConversationListScreen(
    conversations: List<ConversationEntity>,
    selectedTab: Int,
    chatFilter: Int,
    searchQuery: String,
    totalUnreadCount: Int,
    linkedDevices: List<LinkedDeviceEntity>,
    securityLogs: List<SecurityLogEntity>,
    isBiometricLockEnabled: Boolean,
    isMeshActive: Boolean,
    isRadarScanning: Boolean,
    meshPeers: List<MeshPeerNode>,
    meshPacketLogs: List<MeshPacketLog>,
    meshSosAlerts: List<MeshSosBroadcast>,
    meshStats: MeshStats,
    onTabSelected: (Int) -> Unit,
    onChatFilterSelected: (Int) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onSelectConversation: (String) -> Unit,
    onOpenCreateGroup: () -> Unit,
    onOpenLinkDevice: () -> Unit,
    onRevokeDevice: (String) -> Unit,
    onToggleBiometricLock: (Boolean) -> Unit,
    onLockAppNow: () -> Unit,
    onEmergencyPanicWipe: () -> Unit,
    onToggleMeshActive: (Boolean) -> Unit,
    onToggleRadarScan: () -> Unit,
    onDirectChatWithPeer: (MeshPeerNode) -> Unit,
    onSendSosEmergency: (String, String) -> Unit,
    onAddCustomNode: (String, String, String) -> Unit
) {
    var isSearchActive by remember { mutableStateOf(false) }

    val filterOptions = listOf("Semua", "Pribadi", "Grup", "Belum Dibaca")

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("conversation_list_screen"),
        topBar = {
            val isDarkTop = MaterialTheme.colorScheme.background == Color(0xFF0B0F19)
            Surface(
                color = if (isDarkTop) Color(0xFF0B0F19) else MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Title / Branding
                        Column {
                            Text(
                                text = when (selectedTab) {
                                    0 -> "BeiChat"
                                    1 -> "Mesh P2P (Off-Grid)"
                                    2 -> "Perangkat Tertaut"
                                    3 -> "Pusat Keamanan"
                                    4 -> "Akun Saya"
                                    else -> "BeiChat"
                                },
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface,
                                letterSpacing = 0.4.sp
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (isMeshActive) OnlineGreen else MaterialTheme.colorScheme.outline)
                                )
                                Text(
                                    text = if (isMeshActive) "📡 OFF-GRID MESH (0 KB DATA)" else "🔒 E2EE Active",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp,
                                    color = if (isMeshActive) OnlineGreen else SleekBluePrimary
                                )
                            }
                        }

                        // Action buttons in TopBar
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    isSearchActive = !isSearchActive
                                    if (!isSearchActive) {
                                        onSearchQueryChanged("")
                                    }
                                },
                                modifier = Modifier.testTag("search_icon_button")
                            ) {
                                Icon(
                                    imageVector = if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
                                    contentDescription = "Cari",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            IconButton(
                                onClick = onEmergencyPanicWipe,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(SecurityShieldRed.copy(alpha = 0.12f))
                                    .testTag("quick_panic_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Panic Wipe",
                                    tint = SecurityShieldRed,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Search input bar when activated
                    AnimatedVisibility(visible = isSearchActive) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChanged,
                            placeholder = { Text("Cari pesan, kontak, atau grup...") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Cari",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(
                                        onClick = { onSearchQueryChanged("") },
                                        modifier = Modifier.testTag("clear_search_input_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Reset pencarian",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SleekBluePrimary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                .testTag("search_text_input")
                        )
                    }

                    // Quick Filter Pills (Authentic WhatsApp-style)
                    if (selectedTab == 0) {
                        val isDark = MaterialTheme.colorScheme.background == Color(0xFF0B0F19)
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filterOptions.size) { index ->
                                val label = filterOptions[index]
                                val isSelected = chatFilter == index
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = if (isSelected) {
                                        if (isDark) Color(0xFF132B45) else SleekBlueTintLight
                                    } else {
                                        if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9)
                                    },
                                    border = if (isSelected) {
                                        androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (isDark) SleekBluePrimary.copy(alpha = 0.5f) else SleekBlueBorderLight
                                        )
                                    } else null,
                                    modifier = Modifier
                                        .clickable { onChatFilterSelected(index) }
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) {
                                            if (isDark) SleekBlueSoft else SleekBlueDark
                                        } else {
                                            if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                                        },
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            // Modern WhatsApp-style Bottom Navigation Bar
            val isDarkNav = MaterialTheme.colorScheme.background == Color(0xFF0B0F19)
            val navItemColors = NavigationBarItemDefaults.colors(
                selectedIconColor = if (isDarkNav) SleekBlueSoft else SleekBluePrimary,
                selectedTextColor = if (isDarkNav) Color.White else Color(0xFF0F172A),
                indicatorColor = if (isDarkNav) Color(0xFF132B45) else SleekBlueTintLight,
                unselectedIconColor = if (isDarkNav) Color(0xFF8696A0) else Color(0xFF54656F),
                unselectedTextColor = if (isDarkNav) Color(0xFF8696A0) else Color(0xFF54656F)
            )

            NavigationBar(
                containerColor = if (isDarkNav) Color(0xFF0B0F19) else Color.White,
                tonalElevation = 4.dp,
                modifier = Modifier
                    .testTag("whatsapp_bottom_navigation")
                    .border(
                        androidx.compose.foundation.BorderStroke(
                            0.5.dp,
                            if (isDarkNav) Color(0xFF1E293B) else Color(0xFFE2E8F0)
                        )
                    )
            ) {
                // 0: Chat
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { onTabSelected(0) },
                    icon = {
                        if (totalUnreadCount > 0) {
                            BadgedBox(
                                badge = {
                                    Badge(
                                        containerColor = SleekBluePrimary,
                                        contentColor = Color.White
                                    ) {
                                        Text(totalUnreadCount.toString())
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (selectedTab == 0) Icons.Filled.ChatBubble else Icons.Outlined.ChatBubbleOutline,
                                    contentDescription = "Chat"
                                )
                            }
                        } else {
                            Icon(
                                imageVector = if (selectedTab == 0) Icons.Filled.ChatBubble else Icons.Outlined.ChatBubbleOutline,
                                contentDescription = "Chat"
                            )
                        }
                    },
                    label = { 
                        Text(
                            text = "Chat", 
                            fontSize = 12.sp, 
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium 
                        ) 
                    },
                    colors = navItemColors,
                    modifier = Modifier.testTag("nav_chat")
                )

                // 1: Off-Grid Mesh
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { onTabSelected(1) },
                    icon = {
                        if (isMeshActive) {
                            BadgedBox(
                                badge = {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(OnlineGreen)
                                    )
                                }
                            ) {
                                Icon(
                                    imageVector = if (selectedTab == 1) Icons.Filled.WifiTethering else Icons.Outlined.WifiTethering,
                                    contentDescription = "Off-Grid"
                                )
                            }
                        } else {
                            Icon(
                                imageVector = if (selectedTab == 1) Icons.Filled.WifiTethering else Icons.Outlined.WifiTethering,
                                contentDescription = "Off-Grid"
                            )
                        }
                    },
                    label = { 
                        Text(
                            text = "Off-Grid", 
                            fontSize = 12.sp, 
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium 
                        ) 
                    },
                    colors = navItemColors,
                    modifier = Modifier.testTag("nav_offgrid")
                )

                // 2: Perangkat
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { onTabSelected(2) },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 2) Icons.Filled.Devices else Icons.Outlined.Devices,
                            contentDescription = "Perangkat"
                        )
                    },
                    label = { 
                        Text(
                            text = "Perangkat", 
                            fontSize = 12.sp, 
                            fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Medium 
                        ) 
                    },
                    colors = navItemColors,
                    modifier = Modifier.testTag("nav_devices")
                )

                // 3: Keamanan
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { onTabSelected(3) },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 3) Icons.Filled.Shield else Icons.Outlined.Shield,
                            contentDescription = "Keamanan"
                        )
                    },
                    label = { 
                        Text(
                            text = "Keamanan", 
                            fontSize = 12.sp, 
                            fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Medium 
                        ) 
                    },
                    colors = navItemColors,
                    modifier = Modifier.testTag("nav_security")
                )

                // 4: Akun
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { onTabSelected(4) },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 4) Icons.Filled.AccountCircle else Icons.Outlined.AccountCircle,
                            contentDescription = "Akun"
                        )
                    },
                    label = { 
                        Text(
                            text = "Akun", 
                            fontSize = 12.sp, 
                            fontWeight = if (selectedTab == 4) FontWeight.Bold else FontWeight.Medium 
                        ) 
                    },
                    colors = navItemColors,
                    modifier = Modifier.testTag("nav_account")
                )
            }
        },
        floatingActionButton = {
            // Floating Action Button on Chat screen
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = onOpenCreateGroup,
                    containerColor = SleekBluePrimary,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .padding(bottom = 8.dp, end = 8.dp)
                        .testTag("create_group_fab")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Buat Percakapan",
                        tint = Color.White
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Main Content Area according to selectedTab with WhatsApp-style sliding animation
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally(
                            animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
                            initialOffsetX = { fullWidth -> fullWidth }
                        ) + fadeIn(animationSpec = tween(280))) togetherWith
                        (slideOutHorizontally(
                            animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
                            targetOffsetX = { fullWidth -> -fullWidth }
                        ) + fadeOut(animationSpec = tween(280)))
                    } else {
                        (slideInHorizontally(
                            animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
                            initialOffsetX = { fullWidth -> -fullWidth }
                        ) + fadeIn(animationSpec = tween(280))) togetherWith
                        (slideOutHorizontally(
                            animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
                            targetOffsetX = { fullWidth -> fullWidth }
                        ) + fadeOut(animationSpec = tween(280)))
                    }.using(SizeTransform(clip = false))
                },
                label = "main_tabs_slide_anim"
            ) { targetTab ->
                when (targetTab) {
                    0 -> {
                        // Chat Screen (Conversations List)
                        if (conversations.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Tidak Ada Percakapan",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Mulai obrolan pribadi atau buat grup terenkripsi baru.",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 12.dp),
                                contentPadding = PaddingValues(vertical = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(conversations, key = { it.id }) { conv ->
                                    ConversationCardItem(
                                        conversation = conv,
                                        onClick = { onSelectConversation(conv.id) }
                                    )
                                }
                            }
                        }
                    }

                    1 -> {
                        // Mesh P2P (Off-Grid)
                        MeshOffGridScreen(
                            isMeshActive = isMeshActive,
                            isRadarScanning = isRadarScanning,
                            activePeers = meshPeers,
                            packetLogs = meshPacketLogs,
                            sosAlerts = meshSosAlerts,
                            meshStats = meshStats,
                            onToggleMeshActive = onToggleMeshActive,
                            onToggleRadarScan = onToggleRadarScan,
                            onDirectChatWithPeer = onDirectChatWithPeer,
                            onSendSosEmergency = onSendSosEmergency,
                            onAddCustomNode = onAddCustomNode
                        )
                    }

                    2 -> {
                        // Linked Devices
                        LinkedDevicesScreen(
                            linkedDevices = linkedDevices,
                            onOpenLinkDeviceDialog = onOpenLinkDevice,
                            onRevokeDevice = onRevokeDevice
                        )
                    }

                    3 -> {
                        // Security Center
                        SecurityCenterScreen(
                            isBiometricLockEnabled = isBiometricLockEnabled,
                            securityLogs = securityLogs,
                            onToggleBiometricLock = onToggleBiometricLock,
                            onLockAppNow = onLockAppNow,
                            onEmergencyPanicWipe = onEmergencyPanicWipe
                        )
                    }

                    4 -> {
                        // Account Screen (Profil & Pengaturan Akun)
                        AccountScreen(
                            isBiometricLockEnabled = isBiometricLockEnabled,
                            isMeshModeActive = isMeshActive,
                            onToggleBiometricLock = onToggleBiometricLock,
                            onToggleMeshMode = onToggleMeshActive,
                            onOpenLinkedDevices = { onTabSelected(2) },
                            onOpenSecurityLogs = { onTabSelected(3) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ConversationCardItem(
    conversation: ConversationEntity,
    onClick: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background == Color(0xFF0B0F19)
    val dateFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val formattedTime = remember(conversation.lastMessageTimestamp) {
        dateFormatter.format(Date(conversation.lastMessageTimestamp))
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF111827) else MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("conversation_card_${conversation.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            BitChatAvatar(
                initials = conversation.avatarInitials,
                colorHex = conversation.avatarColorHex,
                size = 46.dp,
                isOnline = conversation.isOnline,
                isVerified = conversation.isVerified
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Body
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Text(
                            text = conversation.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (conversation.isVerified) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Terverifikasi",
                                tint = if (isDark) SleekBlueSoft else SleekBluePrimary,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }

                    Text(
                        text = formattedTime,
                        fontSize = 11.sp,
                        color = if (conversation.unreadCount > 0) (if (isDark) SleekBlueSoft else SleekBluePrimary)
                        else (if (isDark) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        if (conversation.isPinned) {
                            Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = "Disematkan",
                                tint = SleekBluePrimary,
                                modifier = Modifier.size(13.dp)
                            )
                        }

                        Text(
                            text = conversation.lastMessageText.ifEmpty { "Kunci enkripsi terinisialisasi" },
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = if (conversation.unreadCount > 0) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Disappearing timer icon badge
                        if (conversation.autoDeleteDurationSeconds > 0) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = "Timer",
                                tint = DisappearingFlameOrange,
                                modifier = Modifier.size(13.dp)
                            )
                        }

                        // Unread badge
                        if (conversation.unreadCount > 0) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(SleekBluePrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = conversation.unreadCount.toString(),
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
