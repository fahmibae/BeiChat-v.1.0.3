package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.crypto.CryptoManager
import com.example.data.model.ContactEntity
import com.example.data.model.ConversationEntity
import com.example.data.model.ConversationType
import com.example.data.model.DeviceType
import com.example.data.model.LinkedDeviceEntity
import com.example.data.model.MediaType
import com.example.data.model.MessageEntity
import com.example.data.model.SecurityLogEntity
import com.example.data.repository.BitChatRepository
import com.example.mesh.MeshNetworkManager
import com.example.mesh.MeshPacketLog
import com.example.mesh.MeshPeerNode
import com.example.mesh.MeshSosBroadcast
import com.example.mesh.MeshStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

enum class AppThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

data class BitChatUiState(
    val selectedTab: Int = 0,
    val chatFilter: Int = 0,
    val searchQuery: String = "",
    val activeConversationId: String? = null,
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val isSafetyNumberDialogOpen: Boolean = false,
    val isCreateGroupDialogOpen: Boolean = false,
    val isLinkDeviceDialogOpen: Boolean = false,
    val isDisappearingTimerSheetOpen: Boolean = false,
    val isAttachmentSheetOpen: Boolean = false,
    val isGroupInfoSheetOpen: Boolean = false,
    val activeHighResMediaMessage: MessageEntity? = null,
    val replyingMessage: MessageEntity? = null,
    val editingMessage: MessageEntity? = null,
    val isAppLocked: Boolean = false,
    val isBiometricLockEnabled: Boolean = false,
    val toastMessage: String? = null
)

class BitChatViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val chatDao = com.example.data.local.AppDatabase.getDatabase(application).chatDao()
    private val repository = BitChatRepository(
        chatDao = chatDao,
        appScope = viewModelScope
    )
    private val meshManager = MeshNetworkManager.getInstance(application)

    private val _uiState = MutableStateFlow(BitChatUiState())
    val uiState: StateFlow<BitChatUiState> = _uiState.asStateFlow()

    val isMeshModeActive: StateFlow<Boolean> = meshManager.isMeshModeEnabled
    val isRadarScanning: StateFlow<Boolean> = meshManager.isRadarScanning
    val meshPeers: StateFlow<List<MeshPeerNode>> = meshManager.activePeers
    val meshPacketLogs: StateFlow<List<MeshPacketLog>> = meshManager.packetLogs
    val meshSosAlerts: StateFlow<List<MeshSosBroadcast>> = meshManager.sosAlerts
    val meshStats: StateFlow<MeshStats> = meshManager.meshStats

    val selectedTab: StateFlow<Int> = _uiState
        .map { it.selectedTab }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val chatFilter: StateFlow<Int> = _uiState
        .map { it.chatFilter }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val searchQuery: StateFlow<String> = _uiState
        .map { it.searchQuery }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val isAppLocked: StateFlow<Boolean> = _uiState
        .map { it.isAppLocked }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val themeMode: StateFlow<AppThemeMode> = _uiState
        .map { it.themeMode }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppThemeMode.SYSTEM)

    val isBiometricLockEnabled: StateFlow<Boolean> = _uiState
        .map { it.isBiometricLockEnabled }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isCreateGroupOpen: StateFlow<Boolean> = _uiState
        .map { it.isCreateGroupDialogOpen }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isLinkDeviceOpen: StateFlow<Boolean> = _uiState
        .map { it.isLinkDeviceDialogOpen }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isSafetyNumberDialogOpen: StateFlow<Boolean> = _uiState
        .map { it.isSafetyNumberDialogOpen }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isDisappearingTimerSheetOpen: StateFlow<Boolean> = _uiState
        .map { it.isDisappearingTimerSheetOpen }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isAttachmentSheetOpen: StateFlow<Boolean> = _uiState
        .map { it.isAttachmentSheetOpen }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isGroupInfoSheetOpen: StateFlow<Boolean> = _uiState
        .map { it.isGroupInfoSheetOpen }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val activeMediaViewerMessage: StateFlow<MessageEntity?> = _uiState
        .map { it.activeHighResMediaMessage }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val replyingMessage: StateFlow<MessageEntity?> = _uiState
        .map { it.replyingMessage }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val editingMessage: StateFlow<MessageEntity?> = _uiState
        .map { it.editingMessage }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val conversations: StateFlow<List<ConversationEntity>> = combine(
        repository.allConversations,
        _uiState
    ) { convs, state ->
        convs.filter { conv ->
            val matchesSearch = state.searchQuery.isEmpty() ||
                    conv.title.contains(state.searchQuery, ignoreCase = true) ||
                    conv.lastMessageText.contains(state.searchQuery, ignoreCase = true)

            val matchesFilter = when (state.chatFilter) {
                0 -> true
                1 -> conv.type == ConversationType.DIRECT
                2 -> conv.type == ConversationType.GROUP
                3 -> conv.unreadCount > 0
                else -> true
            }

            matchesSearch && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalUnreadCount: StateFlow<Int> = conversations.map { list ->
        list.sumOf { it.unreadCount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val activeConversation: StateFlow<ConversationEntity?> = _uiState
        .flatMapLatest { state ->
            if (state.activeConversationId != null) {
                repository.getConversationFlow(state.activeConversationId)
            } else {
                flowOf(null)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val activeMessages: StateFlow<List<MessageEntity>> = _uiState
        .flatMapLatest { state ->
            if (state.activeConversationId != null) {
                repository.getMessagesForConversation(state.activeConversationId)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val linkedDevices: StateFlow<List<LinkedDeviceEntity>> = repository.allLinkedDevices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val securityLogs: StateFlow<List<SecurityLogEntity>> = repository.securityLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val contacts: StateFlow<List<ContactEntity>> = repository.allContacts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        observeIncomingPhysicalMeshMessages()
    }

    private fun observeIncomingPhysicalMeshMessages() {
        viewModelScope.launch {
            meshManager.incomingMeshMessages.collect { payload ->
                val senderTitle = payload.senderName.ifBlank { "Node Sekitar (${payload.senderAddress.takeLast(4)})" }
                val allConvs = repository.allConversations.first()
                val existing = allConvs.find { it.title.equals(senderTitle, ignoreCase = true) || it.peerPublicKey == payload.senderAddress }

                val convId = if (existing != null) {
                    existing.id
                } else {
                    val newConv = ConversationEntity(
                        id = "mesh_${UUID.randomUUID().toString().take(8)}",
                        type = if (payload.isBroadcast) ConversationType.GROUP else ConversationType.DIRECT,
                        title = senderTitle,
                        peerPublicKey = payload.senderAddress.ifBlank { "PUB_BLE_${UUID.randomUUID().toString().take(6)}" },
                        avatarColorHex = "#3B82F6",
                        avatarInitials = senderTitle.take(2).uppercase(),
                        lastMessageText = payload.messageText,
                        lastMessageTimestamp = payload.timestamp,
                        isVerified = true
                    )
                    repository.insertConversation(newConv)
                    newConv.id
                }

                val key = CryptoManager.deriveKeyFromSeed(payload.senderAddress.ifBlank { "BITCHAT_SECRET" })
                val encrypted = CryptoManager.encrypt(payload.messageText, key)

                val incomingMsg = MessageEntity(
                    conversationId = convId,
                    senderId = payload.senderAddress.ifBlank { "peer_$convId" },
                    senderName = senderTitle,
                    content = payload.messageText,
                    encryptedCipherPayload = encrypted.cipherBase64,
                    ivHex = encrypted.ivHex,
                    timestamp = payload.timestamp,
                    status = com.example.data.model.MessageStatus.READ,
                    isOutgoing = false,
                    mediaType = MediaType.NONE
                )
                chatDao.insertMessage(incomingMsg)
            }
        }
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    fun setChatFilter(index: Int) {
        _uiState.update { it.copy(chatFilter = index) }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setSearchQuery(query: String) = updateSearchQuery(query)

    fun setThemeMode(mode: AppThemeMode) {
        _uiState.update { it.copy(themeMode = mode) }
    }

    fun toggleDarkLightMode() {
        _uiState.update {
            val nextMode = when (it.themeMode) {
                AppThemeMode.LIGHT -> AppThemeMode.DARK
                AppThemeMode.DARK -> AppThemeMode.LIGHT
                AppThemeMode.SYSTEM -> AppThemeMode.DARK
            }
            it.copy(themeMode = nextMode)
        }
    }

    fun selectConversation(id: String) {
        _uiState.update { it.copy(activeConversationId = id) }
        viewModelScope.launch {
            chatDao.markConversationRead(id)
        }
    }

    fun clearActiveConversation() {
        _uiState.update {
            it.copy(
                activeConversationId = null,
                isSafetyNumberDialogOpen = false,
                isDisappearingTimerSheetOpen = false,
                isAttachmentSheetOpen = false,
                isGroupInfoSheetOpen = false,
                activeHighResMediaMessage = null,
                replyingMessage = null,
                editingMessage = null
            )
        }
    }

    fun sendMessage(text: String) {
        val convId = _uiState.value.activeConversationId ?: return
        val currentEdit = _uiState.value.editingMessage
        val currentReply = _uiState.value.replyingMessage

        viewModelScope.launch {
            if (currentEdit != null) {
                repository.editMessage(currentEdit.id, text)
                _uiState.update { it.copy(editingMessage = null) }
                return@launch
            }

            repository.sendMessage(
                conversationId = convId,
                text = text,
                replyToId = currentReply?.id,
                replyToSender = currentReply?.senderName ?: "",
                replyToContent = currentReply?.content?.ifBlank { currentReply.mediaCaption } ?: ""
            )
            _uiState.update { it.copy(replyingMessage = null) }

            val activeConv = repository.getConversation(convId)
            val recipient = activeConv?.peerPublicKey?.takeIf { it.contains(":") } ?: activeConv?.title ?: "Peer Node"
            
            if (activeConv?.type == ConversationType.GROUP) {
                meshManager.broadcastGroupMessage(text)
            } else {
                meshManager.sendOffGridMessage(recipient, text)
            }
        }
    }

    fun startReplyingMessage(message: MessageEntity) {
        _uiState.update { it.copy(replyingMessage = message, editingMessage = null) }
    }

    fun cancelReplying() {
        _uiState.update { it.copy(replyingMessage = null) }
    }

    fun startEditingMessage(message: MessageEntity) {
        _uiState.update { it.copy(editingMessage = message, replyingMessage = null) }
    }

    fun cancelEditing() {
        _uiState.update { it.copy(editingMessage = null) }
    }

    fun deleteMessage(messageId: Long) {
        viewModelScope.launch {
            repository.deleteMessage(messageId)
        }
    }

    fun broadcastMeshGroupChat(text: String) {
        viewModelScope.launch {
            meshManager.broadcastGroupMessage(text)
        }
    }

    fun toggleMeshMode(enabled: Boolean) {
        meshManager.toggleMeshMode(enabled)
    }

    fun toggleRadarScan() {
        meshManager.toggleRadarScan()
    }

    fun refreshRadarScan() {
        meshManager.refreshScan()
    }

    fun startDirectChatWithMeshPeer(peer: MeshPeerNode) {
        viewModelScope.launch {
            val allConvs = repository.allConversations.first()
            val existing = allConvs.find { it.title.equals(peer.name, ignoreCase = true) || it.peerPublicKey == peer.id }
            val convId = if (existing != null) {
                existing.id
            } else {
                val newConv = ConversationEntity(
                    id = "mesh_${UUID.randomUUID().toString().take(8)}",
                    type = ConversationType.DIRECT,
                    title = peer.name,
                    peerPublicKey = peer.id,
                    avatarColorHex = "#3B82F6",
                    avatarInitials = peer.name.take(2).uppercase(),
                    lastMessageText = "Koneksi Radio Mesh P2P siap (${peer.protocol})",
                    lastMessageTimestamp = System.currentTimeMillis(),
                    isVerified = true
                )
                repository.insertConversation(newConv)
                newConv.id
            }
            _uiState.update {
                it.copy(
                    selectedTab = 0,
                    activeConversationId = convId
                )
            }
        }
    }

    fun startDirectChatWithContact(contact: ContactEntity) {
        viewModelScope.launch {
            val convId = repository.getOrCreateDirectConversationForContact(contact)
            _uiState.update {
                it.copy(
                    selectedTab = 0,
                    activeConversationId = convId,
                    isCreateGroupDialogOpen = false
                )
            }
        }
    }

    fun sendSosEmergency(message: String, coords: String) {
        viewModelScope.launch {
            meshManager.broadcastSosEmergency(message, coords)
        }
    }

    fun addCustomMeshNode(name: String, role: String, protocol: String) {
        meshManager.triggerNewPeerDiscovery(name, role, protocol)
    }

    fun sendHighResImage(caption: String, isHighRes: Boolean) {
        val convId = _uiState.value.activeConversationId ?: return
        viewModelScope.launch {
            repository.sendMessage(
                conversationId = convId,
                text = caption,
                mediaType = MediaType.IMAGE_HIGH_RES,
                mediaUri = "content://media/external/images/sample_${System.currentTimeMillis()}.jpg",
                mediaCaption = caption.ifEmpty { "Foto Resolusi Penuh Tanpa Kompresi" },
                mediaSizeFormatted = if (isHighRes) "14.8 MB (RAW)" else "4.2 MB (HD)",
                isHighRes = isHighRes
            )
            _uiState.update { it.copy(isAttachmentSheetOpen = false) }
        }
    }

    fun sendVoiceNote(durationSec: Int) {
        val convId = _uiState.value.activeConversationId ?: return
        viewModelScope.launch {
            repository.sendMessage(
                conversationId = convId,
                text = "",
                mediaType = MediaType.AUDIO_VOICE_NOTE,
                mediaUri = "content://media/audio/voice_${System.currentTimeMillis()}.m4a",
                mediaCaption = "Pesan Suara Terenkripsi",
                mediaSizeFormatted = "${(durationSec * 32)} KB",
                audioDuration = durationSec
            )
            _uiState.update { it.copy(isAttachmentSheetOpen = false) }
        }
    }

    fun sendDocument(fileName: String, fileSize: String) {
        val convId = _uiState.value.activeConversationId ?: return
        viewModelScope.launch {
            repository.sendMessage(
                conversationId = convId,
                text = fileName,
                mediaType = MediaType.DOCUMENT,
                mediaUri = "content://media/docs/$fileName",
                mediaCaption = fileName,
                mediaSizeFormatted = fileSize
            )
            _uiState.update { it.copy(isAttachmentSheetOpen = false) }
        }
    }

    fun sendLocation() {
        val convId = _uiState.value.activeConversationId ?: return
        viewModelScope.launch {
            repository.sendMessage(
                conversationId = convId,
                text = "📍 -6.2088, 106.8456 (Jakarta Pusat, ID)",
                mediaType = MediaType.SECURE_LOCATION,
                mediaCaption = "Koordinat Lokasi Aman GPS",
                mediaSizeFormatted = "GPS Fix ±3m"
            )
            _uiState.update { it.copy(isAttachmentSheetOpen = false) }
        }
    }

    fun createGroup(title: String, description: String, selectedContactIds: List<String>, autoDeleteDurationSeconds: Long) {
        viewModelScope.launch {
            val newId = repository.createGroup(
                title = title,
                description = description,
                selectedContactIds = selectedContactIds,
                autoDeleteDurationSeconds = autoDeleteDurationSeconds
            )
            _uiState.update {
                it.copy(
                    isCreateGroupDialogOpen = false,
                    activeConversationId = newId
                )
            }
        }
    }

    fun updateDisappearingTimer(durationSeconds: Long) {
        val convId = _uiState.value.activeConversationId ?: return
        viewModelScope.launch {
            repository.updateAutoDeleteTimer(convId, durationSeconds)
            _uiState.update { it.copy(isDisappearingTimerSheetOpen = false) }
        }
    }

    fun toggleVerification(conversationId: String, verified: Boolean) {
        viewModelScope.launch {
            repository.toggleConversationVerification(conversationId, verified)
            _uiState.update { it.copy(isSafetyNumberDialogOpen = false) }
        }
    }

    fun linkNewDevice(name: String, type: DeviceType, platform: String) {
        viewModelScope.launch {
            repository.linkNewDevice(name, type, platform)
            _uiState.update { it.copy(isLinkDeviceDialogOpen = false) }
        }
    }

    fun revokeDevice(deviceId: String) {
        viewModelScope.launch {
            repository.revokeDevice(deviceId)
        }
    }

    fun triggerEmergencyPanicWipe() {
        viewModelScope.launch {
            repository.triggerEmergencyLocalWipe()
            clearActiveConversation()
        }
    }

    fun toggleBiometricLock(enabled: Boolean) {
        _uiState.update { it.copy(isBiometricLockEnabled = enabled) }
    }

    fun lockApp() {
        _uiState.update { it.copy(isAppLocked = true) }
    }

    fun unlockApp() {
        _uiState.update { it.copy(isAppLocked = false) }
    }

    // Modal controls
    fun openSafetyNumberDialog() = _uiState.update { it.copy(isSafetyNumberDialogOpen = true) }
    fun closeSafetyNumberDialog() = _uiState.update { it.copy(isSafetyNumberDialogOpen = false) }

    fun openCreateGroupDialog() = _uiState.update { it.copy(isCreateGroupDialogOpen = true) }
    fun closeCreateGroupDialog() = _uiState.update { it.copy(isCreateGroupDialogOpen = false) }

    fun openLinkDeviceDialog() = _uiState.update { it.copy(isLinkDeviceDialogOpen = true) }
    fun closeLinkDeviceDialog() = _uiState.update { it.copy(isLinkDeviceDialogOpen = false) }

    fun openDisappearingTimerSheet() = _uiState.update { it.copy(isDisappearingTimerSheetOpen = true) }
    fun closeDisappearingTimerSheet() = _uiState.update { it.copy(isDisappearingTimerSheetOpen = false) }

    fun openAttachmentSheet() = _uiState.update { it.copy(isAttachmentSheetOpen = true) }
    fun closeAttachmentSheet() = _uiState.update { it.copy(isAttachmentSheetOpen = false) }

    fun openGroupInfoSheet() = _uiState.update { it.copy(isGroupInfoSheetOpen = true) }
    fun closeGroupInfoSheet() = _uiState.update { it.copy(isGroupInfoSheetOpen = false) }

    fun openMediaViewer(message: MessageEntity) = _uiState.update { it.copy(activeHighResMediaMessage = message) }
    fun closeMediaViewer() = _uiState.update { it.copy(activeHighResMediaMessage = null) }
}
