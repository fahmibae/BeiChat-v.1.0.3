package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.data.model.MessageEntity
import com.example.ui.dialogs.AttachmentSheet
import com.example.ui.dialogs.CreateGroupDialog
import com.example.ui.dialogs.DisappearingTimerSheet
import com.example.ui.dialogs.GroupInfoSheet
import com.example.ui.dialogs.HighResMediaViewerDialog
import com.example.ui.dialogs.LinkDeviceDialog
import com.example.ui.dialogs.NewConversationDialog
import com.example.ui.dialogs.SafetyNumberDialog
import com.example.ui.screens.AppLockOverlay
import com.example.ui.screens.ChatDetailScreen
import androidx.compose.foundation.isSystemInDarkTheme
import com.example.ui.screens.ConversationListScreen
import com.example.ui.theme.BitChatTheme
import com.example.ui.viewmodel.AppThemeMode
import com.example.ui.viewmodel.BitChatViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: BitChatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            BitChatTheme {
                BitChatApp(
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
fun BitChatApp(
    viewModel: BitChatViewModel
) {
    val conversations by viewModel.conversations.collectAsState()
    val activeConversation by viewModel.activeConversation.collectAsState()
    val activeMessages by viewModel.activeMessages.collectAsState()
    val contacts by viewModel.contacts.collectAsState()
    val linkedDevices by viewModel.linkedDevices.collectAsState()
    val securityLogs by viewModel.securityLogs.collectAsState()

    val selectedTab by viewModel.selectedTab.collectAsState()
    val chatFilter by viewModel.chatFilter.collectAsState()
    val totalUnreadCount by viewModel.totalUnreadCount.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isAppLocked by viewModel.isAppLocked.collectAsState()
    val isBiometricLockEnabled by viewModel.isBiometricLockEnabled.collectAsState()

    // Mesh Off-Grid State
    val isMeshActive by viewModel.isMeshModeActive.collectAsState()
    val isRadarScanning by viewModel.isRadarScanning.collectAsState()
    val meshPeers by viewModel.meshPeers.collectAsState()
    val meshPacketLogs by viewModel.meshPacketLogs.collectAsState()
    val meshSosAlerts by viewModel.meshSosAlerts.collectAsState()
    val meshStats by viewModel.meshStats.collectAsState()

    // Dialog & sheet states
    val isCreateGroupOpen by viewModel.isCreateGroupOpen.collectAsState()
    val isLinkDeviceOpen by viewModel.isLinkDeviceOpen.collectAsState()
    val isSafetyNumberDialogOpen by viewModel.isSafetyNumberDialogOpen.collectAsState()
    val isDisappearingTimerSheetOpen by viewModel.isDisappearingTimerSheetOpen.collectAsState()
    val isAttachmentSheetOpen by viewModel.isAttachmentSheetOpen.collectAsState()
    val isGroupInfoSheetOpen by viewModel.isGroupInfoSheetOpen.collectAsState()
    val activeMediaViewerMessage by viewModel.activeMediaViewerMessage.collectAsState()

    Surface(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedContent(
                targetState = activeConversation,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "main_navigation"
            ) { targetConv ->
                if (targetConv != null) {
                    BackHandler {
                        viewModel.clearActiveConversation()
                    }

                    ChatDetailScreen(
                        conversation = targetConv,
                        messages = activeMessages,
                        onBack = { viewModel.clearActiveConversation() },
                        onSendMessage = { text -> viewModel.sendMessage(text) },
                        onOpenAttachmentSheet = { viewModel.openAttachmentSheet() },
                        onOpenDisappearingTimerSheet = { viewModel.openDisappearingTimerSheet() },
                        onOpenSafetyNumbersDialog = { viewModel.openSafetyNumberDialog() },
                        onOpenGroupInfoSheet = { viewModel.openGroupInfoSheet() },
                        onOpenMediaViewer = { msg -> viewModel.openMediaViewer(msg) }
                    )
                } else {
                    ConversationListScreen(
                        conversations = conversations,
                        selectedTab = selectedTab,
                        chatFilter = chatFilter,
                        searchQuery = searchQuery,
                        totalUnreadCount = totalUnreadCount,
                        linkedDevices = linkedDevices,
                        securityLogs = securityLogs,
                        isBiometricLockEnabled = isBiometricLockEnabled,
                        isMeshActive = isMeshActive,
                        isRadarScanning = isRadarScanning,
                        meshPeers = meshPeers,
                        meshPacketLogs = meshPacketLogs,
                        meshSosAlerts = meshSosAlerts,
                        meshStats = meshStats,
                        onTabSelected = { tab -> viewModel.selectTab(tab) },
                        onChatFilterSelected = { filter -> viewModel.setChatFilter(filter) },
                        onSearchQueryChanged = { q -> viewModel.updateSearchQuery(q) },
                        onSelectConversation = { id -> viewModel.selectConversation(id) },
                        onOpenCreateGroup = { viewModel.openCreateGroupDialog() },
                        onOpenLinkDevice = { viewModel.openLinkDeviceDialog() },
                        onRevokeDevice = { deviceId -> viewModel.revokeDevice(deviceId) },
                        onToggleBiometricLock = { enabled -> viewModel.toggleBiometricLock(enabled) },
                        onLockAppNow = { viewModel.lockApp() },
                        onEmergencyPanicWipe = { viewModel.triggerEmergencyPanicWipe() },
                        onToggleMeshActive = { enabled -> viewModel.toggleMeshMode(enabled) },
                        onToggleRadarScan = { viewModel.toggleRadarScan() },
                        onDirectChatWithPeer = { peer -> viewModel.startDirectChatWithMeshPeer(peer) },
                        onSendSosEmergency = { msg, coords -> viewModel.sendSosEmergency(msg, coords) },
                        onAddCustomNode = { name, role, proto -> viewModel.addCustomMeshNode(name, role, proto) }
                    )
                }
            }

            // Dialogs
            if (isCreateGroupOpen) {
                NewConversationDialog(
                    contacts = contacts,
                    onDismiss = { viewModel.closeCreateGroupDialog() },
                    onStartDirectChatWithContact = { contact ->
                        viewModel.startDirectChatWithContact(contact)
                    },
                    onCreateGroup = { title, desc, members, timerSec ->
                        viewModel.createGroup(title, desc, members, timerSec)
                    }
                )
            }

            if (isLinkDeviceOpen) {
                LinkDeviceDialog(
                    onDismiss = { viewModel.closeLinkDeviceDialog() },
                    onLinkDevice = { name, type, platform ->
                        viewModel.linkNewDevice(name, type, platform)
                    }
                )
            }

            if (isSafetyNumberDialogOpen && activeConversation != null) {
                SafetyNumberDialog(
                    conversation = activeConversation!!,
                    onDismiss = { viewModel.closeSafetyNumberDialog() },
                    onToggleVerification = { verified ->
                        viewModel.toggleVerification(activeConversation!!.id, verified)
                    }
                )
            }

            if (isDisappearingTimerSheetOpen && activeConversation != null) {
                DisappearingTimerSheet(
                    currentDurationSeconds = activeConversation!!.autoDeleteDurationSeconds,
                    onDismiss = { viewModel.closeDisappearingTimerSheet() },
                    onSelectDuration = { durationSec ->
                        viewModel.updateDisappearingTimer(durationSec)
                    }
                )
            }

            if (isAttachmentSheetOpen) {
                AttachmentSheet(
                    onDismiss = { viewModel.closeAttachmentSheet() },
                    onSendImage = { caption, isHighRes ->
                        viewModel.sendHighResImage(caption, isHighRes)
                    },
                    onSendVoiceNote = { durationSec ->
                        viewModel.sendVoiceNote(durationSec)
                    },
                    onSendDocument = { fileName, fileSize ->
                        viewModel.sendDocument(fileName, fileSize)
                    },
                    onSendLocation = {
                        viewModel.sendLocation()
                    }
                )
            }

            if (isGroupInfoSheetOpen && activeConversation != null) {
                GroupInfoSheet(
                    conversation = activeConversation!!,
                    contacts = contacts,
                    onDismiss = { viewModel.closeGroupInfoSheet() },
                    onOpenDisappearingTimer = {
                        viewModel.closeGroupInfoSheet()
                        viewModel.openDisappearingTimerSheet()
                    }
                )
            }

            activeMediaViewerMessage?.let { mediaMsg ->
                HighResMediaViewerDialog(
                    message = mediaMsg,
                    onDismiss = { viewModel.closeMediaViewer() }
                )
            }

            // App Lock Overlay
            if (isAppLocked) {
                AppLockOverlay(
                    onUnlock = { viewModel.unlockApp() }
                )
            }
        }
    }
}
