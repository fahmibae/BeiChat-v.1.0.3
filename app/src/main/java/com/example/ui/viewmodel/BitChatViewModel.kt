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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AppThemeMode {
    SYSTEM, // Mengikuti Tema Sistem
    LIGHT,  // Mode Terang
    DARK    // Mode Gelap
}

data class BitChatUiState(
    val selectedTab: Int = 0, // 0 = Chat, 1 = Mesh P2P Off-Grid, 2 = Perangkat, 3 = Keamanan, 4 = Akun
    val chatFilter: Int = 0, // 0 = Semua, 1 = Pribadi, 2 = Grup, 3 = Belum Dibaca
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
    val isAppLocked: Boolean = false,
    val isBiometricLockEnabled: Boolean = false,
    val toastMessage: String? = null
)

class BitChatViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = BitChatRepository.getInstance(application)
    private val meshManager = MeshNetworkManager.getInstance(application)

    private val _uiState = MutableStateFlow(BitChatUiState())
    val uiState: StateFlow<BitChatUiState> = _uiState.asStateFlow()

    // Mesh Network StateFlows
    val isMeshModeActive: StateFlow<Boolean> = meshManager.isMeshModeEnabled
    val isRadarScanning: StateFlow<Boolean> = meshManager.isRadarScanning
    val meshPeers: StateFlow<List<MeshPeerNode>> = meshManager.activePeers
    val meshPacketLogs: StateFlow<List<MeshPacketLog>> = meshManager.packetLogs
    val meshSosAlerts: StateFlow<List<MeshSosBroadcast>> = meshManager.sosAlerts
    val meshStats: StateFlow<MeshStats> = meshManager.meshStats

    // Screen StateFlows mapped for clean Compose observation
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

    val conversations: StateFlow<List<ConversationEntity>> = combine(
        repository.allConversations,
        _uiState
    ) { convs, state ->
        convs.filter { conv ->
            val matchesSearch = state.searchQuery.isEmpty() ||
                    conv.title.contains(state.searchQuery, ignoreCase = true) ||
                    conv.lastMessageText.contains(state.searchQuery, ignoreCase = true)

            val matchesFilter = when (state.chatFilter) {
                0 -> true // Semua
                1 -> conv.type == ConversationType.DIRECT // Pribadi
                2 -> conv.type == ConversationType.GROUP // Grup
                3 -> conv.unreadCount > 0 // Belum Dibaca
                else -> true
            }

            matchesSearch && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalUnreadCount: StateFlow<Int> = repository.allConversations
        .map { convs -> convs.sumOf { it.unreadCount } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

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

    val contacts: StateFlow<List<ContactEntity>> = repository.allContacts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val securityLogs: StateFlow<List<SecurityLogEntity>> = repository.securityLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    fun setChatFilter(filter: Int) {
        _uiState.update { it.copy(chatFilter = filter) }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

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
    }

    fun clearActiveConversation() {
        _uiState.update {
            it.copy(
                activeConversationId = null,
                isSafetyNumberDialogOpen = false,
                isDisappearingTimerSheetOpen = false,
                isAttachmentSheetOpen = false,
                isGroupInfoSheetOpen = false,
                activeHighResMediaMessage = null
            )
        }
    }

    fun sendMessage(text: String) {
        val convId = _uiState.value.activeConversationId ?: return
        viewModelScope.launch {
            repository.sendMessage(
                conversationId = convId,
                text = text
            )
            // Log to Off-Grid Mesh packet monitor
            val activeConv = repository.getConversation(convId)
            val recipientTitle = activeConv?.title ?: "Peer Node"
            meshManager.sendOffGridMessage(recipientTitle, text)
        }
    }

    // Mesh Network Operations
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
            // Find or create conversation with this peer node
            val existing = conversations.value.find { it.title.contains(peer.name.take(6), ignoreCase = true) }
            if (existing != null) {
                _uiState.update { it.copy(activeConversationId = existing.id) }
            } else {
                // Create dedicated offline mesh conversation
                val newConvId = "conv_mesh_${peer.id}"
                val newConv = ConversationEntity(
                    id = newConvId,
                    type = ConversationType.DIRECT,
                    title = "${peer.name} [Off-Grid Mesh]",
                    peerPublicKey = peer.publicKey,
                    avatarColorHex = "#2563EB",
                    avatarInitials = peer.name.take(2).uppercase(),
                    lastMessageText = "Jalur direct P2P mesh aktif via ${peer.protocol} (0 KB Internet).",
                    lastMessageTimestamp = System.currentTimeMillis(),
                    isVerified = true,
                    isOnline = true
                )
                repository.insertConversation(newConv)
                _uiState.update { it.copy(activeConversationId = newConvId) }
            }
        }
    }

    fun sendSosEmergency(message: String, coords: String) {
        meshManager.broadcastSosEmergency(message, coords)
    }

    fun addCustomMeshNode(name: String, role: String, protocol: String) {
        meshManager.triggerNewPeerDiscovery(name, role, protocol)
    }

    fun sendHighResImage(caption: String, isHighRes: Boolean) {
        val convId = _uiState.value.activeConversationId ?: return
        val sampleImages = listOf(
            "https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?q=80&w=1200&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1550751827-4bd374c3f58b?q=80&w=1200&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1518770660439-4636190af475?q=80&w=1200&auto=format&fit=crop"
        )
        val selectedImage = sampleImages.random()
        val sizeFormatted = if (isHighRes) "6.4 MB (HD Lossless Original)" else "420 KB (Standar)"

        viewModelScope.launch {
            repository.sendMessage(
                conversationId = convId,
                text = "",
                mediaType = MediaType.IMAGE_HIGH_RES,
                mediaUri = selectedImage,
                mediaCaption = caption.ifEmpty { "Security_HD_Render_Original.png" },
                mediaSizeFormatted = sizeFormatted,
                isHighRes = isHighRes
            )
        }
        _uiState.update { it.copy(isAttachmentSheetOpen = false) }
    }

    fun sendVoiceNote(durationSeconds: Int) {
        val convId = _uiState.value.activeConversationId ?: return
        viewModelScope.launch {
            repository.sendMessage(
                conversationId = convId,
                text = "",
                mediaType = MediaType.AUDIO_VOICE_NOTE,
                mediaCaption = "Pesan Suara Terenkripsi",
                mediaSizeFormatted = "${durationSeconds * 32} KB",
                audioDuration = durationSeconds
            )
        }
        _uiState.update { it.copy(isAttachmentSheetOpen = false) }
    }

    fun sendDocument(fileName: String, fileSize: String) {
        val convId = _uiState.value.activeConversationId ?: return
        viewModelScope.launch {
            repository.sendMessage(
                conversationId = convId,
                text = "",
                mediaType = MediaType.DOCUMENT,
                mediaCaption = fileName,
                mediaSizeFormatted = fileSize
            )
        }
        _uiState.update { it.copy(isAttachmentSheetOpen = false) }
    }

    fun sendLocation() {
        val convId = _uiState.value.activeConversationId ?: return
        viewModelScope.launch {
            repository.sendMessage(
                conversationId = convId,
                text = "",
                mediaType = MediaType.SECURE_LOCATION,
                mediaCaption = "Koordinat Lokasi Aman (-6.2088, 106.8456)",
                mediaSizeFormatted = "GPS Encrypted"
            )
        }
        _uiState.update { it.copy(isAttachmentSheetOpen = false) }
    }

    fun updateDisappearingTimer(seconds: Long) {
        val convId = _uiState.value.activeConversationId ?: return
        viewModelScope.launch {
            repository.updateAutoDeleteTimer(convId, seconds)
        }
        _uiState.update { it.copy(isDisappearingTimerSheetOpen = false) }
    }

    fun toggleVerification(conversationId: String, isVerified: Boolean) {
        viewModelScope.launch {
            repository.toggleConversationVerification(conversationId, isVerified)
        }
    }

    fun createGroup(title: String, description: String, selectedContactIds: List<String>, timerSeconds: Long) {
        viewModelScope.launch {
            val groupId = repository.createGroup(title, description, selectedContactIds, timerSeconds)
            _uiState.update {
                it.copy(
                    isCreateGroupDialogOpen = false,
                    activeConversationId = groupId
                )
            }
        }
    }

    fun startDirectChatWithContact(contact: ContactEntity) {
        viewModelScope.launch {
            val convId = repository.getOrCreateDirectConversationForContact(contact)
            _uiState.update {
                it.copy(
                    isCreateGroupDialogOpen = false,
                    activeConversationId = convId
                )
            }
        }
    }

    fun addNewContactAndStartChat(name: String, username: String) {
        viewModelScope.launch {
            val newContact = repository.addNewContact(name, username)
            startDirectChatWithContact(newContact)
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
