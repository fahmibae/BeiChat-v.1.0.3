package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ConversationType {
    DIRECT,
    GROUP
}

enum class MessageStatus {
    SENDING,
    SENT,
    DELIVERED,
    READ
}

enum class MediaType {
    NONE,
    IMAGE_HIGH_RES,
    AUDIO_VOICE_NOTE,
    DOCUMENT,
    SECURE_LOCATION
}

enum class DeviceType {
    DESKTOP,
    TABLET,
    WEB,
    MOBILE
}

enum class SyncStatus {
    SYNCED,
    SYNCING,
    PENDING_KEY_EXCHANGE
}

enum class SecuritySeverity {
    INFO,
    WARNING,
    CRITICAL
}

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: String,
    val type: ConversationType,
    val title: String,
    val peerPublicKey: String,
    val avatarUrl: String = "",
    val avatarColorHex: String = "#10B981",
    val avatarInitials: String = "",
    val lastMessageText: String = "",
    val lastMessageTimestamp: Long = System.currentTimeMillis(),
    val unreadCount: Int = 0,
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val isMuted: Boolean = false,
    val isVerified: Boolean = false,
    val autoDeleteDurationSeconds: Long = 0L, // 0 = Off, 5s, 30s, 60s, 3600s, 86400s
    val encryptionAlgorithm: String = "AES-256-GCM / X25519",
    val groupMembersCount: Int = 1,
    val groupDescription: String = "",
    val groupAdmins: String = "", // Comma-separated admin names/IDs
    val isOnline: Boolean = false
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val conversationId: String,
    val senderId: String,
    val senderName: String,
    val content: String, // Plaintext preview (decrypted on local device)
    val encryptedCipherPayload: String, // Base64 encrypted cipher payload
    val ivHex: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: MessageStatus = MessageStatus.SENT,
    val isOutgoing: Boolean = true,
    val mediaType: MediaType = MediaType.NONE,
    val mediaUri: String = "",
    val mediaCaption: String = "",
    val mediaSizeFormatted: String = "",
    val mediaChecksumSha256: String = "",
    val isHighResOriginal: Boolean = true,
    val audioDurationSeconds: Int = 0,
    val audioWaveformLevels: String = "40,60,80,45,90,70,35,80,65,50,75,40,95,60,45,30",
    val autoDeleteDurationSeconds: Long = 0L,
    val expiresAtTimestamp: Long = 0L, // 0 if no auto-delete, otherwise expiration epoch ms
    val isExpiredAndDeleted: Boolean = false,
    val replyToMessageId: Long? = null,
    val replyToSender: String = "",
    val replyToContent: String = "",
    val reactions: String = "", // e.g. "❤️,👍,🔒"
    val isEdited: Boolean = false,
    val editedTimestamp: Long = 0L
)

@Entity(tableName = "linked_devices")
data class LinkedDeviceEntity(
    @PrimaryKey val id: String,
    val deviceName: String,
    val deviceType: DeviceType,
    val platform: String,
    val location: String,
    val ipAddress: String,
    val linkedTimestamp: Long,
    val lastActiveTimestamp: Long,
    val isCurrentDevice: Boolean = false,
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
    val sessionKeyFingerprint: String
)

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey val id: String,
    val name: String,
    val username: String,
    val publicKey: String,
    val fingerprint: String,
    val avatarColorHex: String = "#06B6D4",
    val avatarInitials: String = "",
    val isVerified: Boolean = false,
    val statusMessage: String = "BitChat End-to-End Encrypted",
    val isOnline: Boolean = false,
    val lastSeenTime: String = "Online"
)

@Entity(tableName = "security_logs")
data class SecurityLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val eventType: String,
    val description: String,
    val severity: SecuritySeverity = SecuritySeverity.INFO
)
