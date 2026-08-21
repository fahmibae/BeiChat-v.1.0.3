package com.example.data.repository

import android.content.Context
import com.example.crypto.CryptoManager
import com.example.data.local.AppDatabase
import com.example.data.local.ChatDao
import com.example.data.model.ContactEntity
import com.example.data.model.ConversationEntity
import com.example.data.model.ConversationType
import com.example.data.model.DeviceType
import com.example.data.model.LinkedDeviceEntity
import com.example.data.model.MediaType
import com.example.data.model.MessageEntity
import com.example.data.model.MessageStatus
import com.example.data.model.SecurityLogEntity
import com.example.data.model.SecuritySeverity
import com.example.data.model.SyncStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID

class BitChatRepository(
    private val chatDao: ChatDao,
    private val appScope: CoroutineScope
) {
    val allConversations: Flow<List<ConversationEntity>> = chatDao.getAllActiveConversations()
    val allLinkedDevices: Flow<List<LinkedDeviceEntity>> = chatDao.getAllLinkedDevices()
    val allContacts: Flow<List<ContactEntity>> = chatDao.getAllContacts()
    val securityLogs: Flow<List<SecurityLogEntity>> = chatDao.getSecurityLogs()

    init {
        // Start background auto-delete cleaner loop
        appScope.launch(Dispatchers.IO) {
            startAutoDeleteCleanerLoop()
        }
    }

    fun getConversationFlow(id: String): Flow<ConversationEntity?> = chatDao.getConversationFlow(id)
    suspend fun getConversation(id: String): ConversationEntity? = chatDao.getConversationById(id)
    suspend fun insertConversation(conv: ConversationEntity) = chatDao.insertConversation(conv)

    fun getMessagesForConversation(convId: String): Flow<List<MessageEntity>> =
        chatDao.getMessagesForConversation(convId)

    suspend fun sendMessage(
        conversationId: String,
        text: String,
        mediaType: MediaType = MediaType.NONE,
        mediaUri: String = "",
        mediaCaption: String = "",
        mediaSizeFormatted: String = "",
        isHighRes: Boolean = true,
        audioDuration: Int = 0,
        replyToId: Long? = null,
        replyToSender: String = "",
        replyToContent: String = ""
    ) {
        val conv = chatDao.getConversationById(conversationId) ?: return
        val key = CryptoManager.deriveKeyFromSeed(conv.peerPublicKey)

        val encrypted = CryptoManager.encrypt(text.ifEmpty { mediaCaption }, key)
        val now = System.currentTimeMillis()

        val autoDeleteSeconds = conv.autoDeleteDurationSeconds
        val expiresAt = if (autoDeleteSeconds > 0) now + (autoDeleteSeconds * 1000L) else 0L

        val checksum = if (mediaType != MediaType.NONE) {
            CryptoManager.calculateSha256Hex(
                (mediaUri + text + now.toString()).toByteArray()
            )
        } else ""

        val msg = MessageEntity(
            conversationId = conversationId,
            senderId = "my_user_id",
            senderName = "Anda",
            content = text,
            encryptedCipherPayload = encrypted.cipherBase64,
            ivHex = encrypted.ivHex,
            timestamp = now,
            status = MessageStatus.SENT,
            isOutgoing = true,
            mediaType = mediaType,
            mediaUri = mediaUri,
            mediaCaption = mediaCaption,
            mediaSizeFormatted = mediaSizeFormatted,
            mediaChecksumSha256 = checksum,
            isHighResOriginal = isHighRes,
            audioDurationSeconds = audioDuration,
            autoDeleteDurationSeconds = autoDeleteSeconds,
            expiresAtTimestamp = expiresAt,
            replyToMessageId = replyToId,
            replyToSender = replyToSender,
            replyToContent = replyToContent
        )

        chatDao.insertMessage(msg)

        val displaySnippet = when (mediaType) {
            MediaType.IMAGE_HIGH_RES -> "📷 [HD Foto Terenkripsi] ${mediaCaption.ifEmpty { "Media Resolusi Tinggi" }}"
            MediaType.AUDIO_VOICE_NOTE -> "🎤 [Pesan Suara Terenkripsi] (${audioDuration}s)"
            MediaType.DOCUMENT -> "📄 [Dokumen Aman] $mediaCaption"
            MediaType.SECURE_LOCATION -> "📍 [Lokasi Terenkripsi] Koordinat Aman"
            MediaType.NONE -> text
        }

        chatDao.updateConversation(
            conv.copy(
                lastMessageText = displaySnippet,
                lastMessageTimestamp = now
            )
        )

        // Log outgoing message security event
        chatDao.insertSecurityLog(
            SecurityLogEntity(
                eventType = "E2EE_TRANSMIT",
                description = "Pesan terenkripsi AES-256-GCM berhasil dikirim ke ${conv.title}",
                severity = SecuritySeverity.INFO
            )
        )

        // Simulate incoming peer response after 2 seconds for high realism
        simulatePeerReply(conv, text, mediaType)
    }

    private fun simulatePeerReply(conv: ConversationEntity, sentText: String, sentMediaType: MediaType) {
        appScope.launch(Dispatchers.IO) {
            delay(1800)
            val now = System.currentTimeMillis()
            val autoDeleteSeconds = conv.autoDeleteDurationSeconds
            val expiresAt = if (autoDeleteSeconds > 0) now + (autoDeleteSeconds * 1000L) else 0L

            val replyText = when {
                conv.type == ConversationType.GROUP -> {
                    val peerNames = listOf("Budi Santoso", "Reza Cyber", "Siti Ratna")
                    val randomPeer = peerNames.random()
                    val responses = listOf(
                        "Kunci sesi grup terverifikasi. Pesan terbaca dengan enkripsi sempurna 👍",
                        "File HD diterima dan checksum SHA-256 valid 🔒",
                        "Sistem BitChat peer-to-peer ini sangat responsif dan aman!",
                        "Siap, data terenkripsi end-to-end tanpa server perantara."
                    )
                    Pair(randomPeer, responses.random())
                }
                sentMediaType == MediaType.IMAGE_HIGH_RES -> {
                    Pair(conv.title, "Foto resolusi tinggi diterima utuh tanpa kompresi! Checksum SHA-256 terverifikasi aman. 📸🔒")
                }
                sentMediaType == MediaType.AUDIO_VOICE_NOTE -> {
                    Pair(conv.title, "Pesan suara jernih dan terenkripsi end-to-end telah saya dengar. 🎧")
                }
                sentText.contains("kunci", ignoreCase = true) || sentText.contains("key", ignoreCase = true) -> {
                    Pair(conv.title, "Kunci publik saya cocok dengan nomor keamanan Anda. Semuanya aman.")
                }
                else -> {
                    val peerResponses = listOf(
                        "Pesan diterima dan didekripsi lokal secara instan via AES-256-GCM. 🛡️",
                        "Semua paket pesan terenkripsi aman tanpa log server. Ada yang perlu dibahas lagi?",
                        "Mantap! Sinkronisasi multi-perangkat juga otomatis terhubung ke desktop saya.",
                        "Terkonfirmasi. Fitur self-destruct berjalan tepat waktu jika diaktifkan."
                    )
                    Pair(conv.title, peerResponses.random())
                }
            }

            val key = CryptoManager.deriveKeyFromSeed(conv.peerPublicKey)
            val encrypted = CryptoManager.encrypt(replyText.second, key)

            val incomingMsg = MessageEntity(
                conversationId = conv.id,
                senderId = "peer_${conv.id}",
                senderName = replyText.first,
                content = replyText.second,
                encryptedCipherPayload = encrypted.cipherBase64,
                ivHex = encrypted.ivHex,
                timestamp = now,
                status = MessageStatus.READ,
                isOutgoing = false,
                mediaType = MediaType.NONE,
                autoDeleteDurationSeconds = autoDeleteSeconds,
                expiresAtTimestamp = expiresAt
            )

            chatDao.insertMessage(incomingMsg)
            chatDao.updateConversation(
                conv.copy(
                    lastMessageText = replyText.second,
                    lastMessageTimestamp = now
                )
            )
        }
    }

    suspend fun editMessage(messageId: Long, newText: String) {
        val msg = chatDao.getMessageById(messageId) ?: return
        val conv = chatDao.getConversationById(msg.conversationId) ?: return
        val key = CryptoManager.deriveKeyFromSeed(conv.peerPublicKey)
        val encrypted = CryptoManager.encrypt(newText, key)

        val updated = msg.copy(
            content = newText,
            encryptedCipherPayload = encrypted.cipherBase64,
            ivHex = encrypted.ivHex,
            isEdited = true,
            editedTimestamp = System.currentTimeMillis()
        )
        chatDao.updateMessage(updated)

        chatDao.updateConversation(
            conv.copy(
                lastMessageText = newText,
                lastMessageTimestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteMessage(messageId: Long) {
        chatDao.deleteMessage(messageId)
    }

    suspend fun createGroup(
        title: String,
        description: String,
        selectedContactIds: List<String>,
        autoDeleteDurationSeconds: Long
    ): String {
        val groupId = "group_${UUID.randomUUID().toString().take(8)}"
        val groupKey = CryptoManager.generateHexFingerprint(groupId + title + System.currentTimeMillis())

        val memberCount = selectedContactIds.size + 1
        val newConv = ConversationEntity(
            id = groupId,
            type = ConversationType.GROUP,
            title = title,
            peerPublicKey = groupKey,
            avatarColorHex = "#10B981",
            avatarInitials = title.take(2).uppercase(),
            lastMessageText = "Grup terenkripsi end-to-end dibuat dengan kunci bersama.",
            lastMessageTimestamp = System.currentTimeMillis(),
            autoDeleteDurationSeconds = autoDeleteDurationSeconds,
            groupMembersCount = memberCount,
            groupDescription = description,
            groupAdmins = "Anda",
            isVerified = true
        )

        chatDao.insertConversation(newConv)

        // Insert initial system message
        val key = CryptoManager.deriveKeyFromSeed(groupKey)
        val intro = "🔒 Anda membuat grup \"$title\" dengan enkripsi AES-256-GCM. Kunci bersama didistribusikan ke $memberCount anggota."
        val encrypted = CryptoManager.encrypt(intro, key)

        val initMsg = MessageEntity(
            conversationId = groupId,
            senderId = "system",
            senderName = "BitChat Keamanan",
            content = intro,
            encryptedCipherPayload = encrypted.cipherBase64,
            ivHex = encrypted.ivHex,
            timestamp = System.currentTimeMillis(),
            status = MessageStatus.READ,
            isOutgoing = false,
            mediaType = MediaType.NONE
        )
        chatDao.insertMessage(initMsg)

        chatDao.insertSecurityLog(
            SecurityLogEntity(
                eventType = "GROUP_CREATED",
                description = "Grup terenkripsi baru '$title' dibuat dengan $memberCount anggota.",
                severity = SecuritySeverity.INFO
            )
        )

        return groupId
    }

    suspend fun getOrCreateDirectConversationForContact(contact: ContactEntity): String {
        val existing = chatDao.getAllActiveConversations().first().find {
            it.type == ConversationType.DIRECT && (it.title.equals(contact.name, ignoreCase = true) || it.peerPublicKey == contact.publicKey)
        }
        if (existing != null) {
            return existing.id
        }

        val convId = "conv_${contact.id.replace("contact_", "")}"
        val newConv = ConversationEntity(
            id = convId,
            type = ConversationType.DIRECT,
            title = contact.name,
            peerPublicKey = contact.publicKey,
            avatarColorHex = contact.avatarColorHex,
            avatarInitials = contact.avatarInitials,
            lastMessageText = "Kunci enkripsi terinisialisasi. Percakapan aman dimulai.",
            lastMessageTimestamp = System.currentTimeMillis(),
            isVerified = contact.isVerified,
            isOnline = contact.isOnline
        )
        chatDao.insertConversation(newConv)

        // Insert initial system handshake message
        val key = CryptoManager.deriveKeyFromSeed(contact.publicKey)
        val intro = "🔒 Sesi terenkripsi end-to-end (AES-256-GCM) dibuat dengan ${contact.name} (${contact.username})."
        val encrypted = CryptoManager.encrypt(intro, key)
        val initMsg = MessageEntity(
            conversationId = convId,
            senderId = "system",
            senderName = "BitChat Keamanan",
            content = intro,
            encryptedCipherPayload = encrypted.cipherBase64,
            ivHex = encrypted.ivHex,
            timestamp = System.currentTimeMillis(),
            status = MessageStatus.READ,
            isOutgoing = false,
            mediaType = MediaType.NONE
        )
        chatDao.insertMessage(initMsg)

        chatDao.insertSecurityLog(
            SecurityLogEntity(
                eventType = "E2EE_HANDSHAKE",
                description = "Kunci sesi langsung diinisialisasi untuk kontak ${contact.name}.",
                severity = SecuritySeverity.INFO
            )
        )

        return convId
    }

    suspend fun addNewContact(name: String, username: String): ContactEntity {
        val contactId = "contact_${UUID.randomUUID().toString().take(6)}"
        val pubKey = "04" + CryptoManager.generateHexFingerprint(contactId + username + System.currentTimeMillis()).replace(" ", "").take(62)
        val fingerprint = CryptoManager.generateHexFingerprint(pubKey).chunked(4).take(6).joinToString(" ")
        val colors = listOf("#06B6D4", "#10B981", "#8B5CF6", "#F59E0B", "#EC4899", "#3B82F6")
        val chosenColor = colors.random()

        val contact = ContactEntity(
            id = contactId,
            name = name,
            username = username,
            publicKey = pubKey,
            fingerprint = fingerprint,
            avatarColorHex = chosenColor,
            avatarInitials = name.take(2).uppercase(),
            isVerified = true,
            statusMessage = "Akun terenkripsi baru terhubung di BitChat.",
            isOnline = true,
            lastSeenTime = "Online"
        )
        chatDao.insertContacts(listOf(contact))
        return contact
    }

    suspend fun updateAutoDeleteTimer(conversationId: String, seconds: Long) {
        chatDao.updateAutoDeleteDuration(conversationId, seconds)
        val conv = chatDao.getConversationById(conversationId) ?: return

        val durationLabel = when (seconds) {
            0L -> "Dinonaktifkan (Pesan disimpan)"
            5L -> "5 Detik"
            30L -> "30 Detik"
            60L -> "1 Menit"
            3600L -> "1 Jam"
            86400L -> "24 Jam"
            604800L -> "7 Hari"
            else -> "$seconds Detik"
        }

        val key = CryptoManager.deriveKeyFromSeed(conv.peerPublicKey)
        val text = "⏱️ Timer penghapusan pesan otomatis disetel ke: $durationLabel"
        val encrypted = CryptoManager.encrypt(text, key)

        val msg = MessageEntity(
            conversationId = conversationId,
            senderId = "system",
            senderName = "Sistem",
            content = text,
            encryptedCipherPayload = encrypted.cipherBase64,
            ivHex = encrypted.ivHex,
            timestamp = System.currentTimeMillis(),
            status = MessageStatus.READ,
            isOutgoing = false
        )
        chatDao.insertMessage(msg)
        chatDao.updateConversation(conv.copy(lastMessageText = text, lastMessageTimestamp = System.currentTimeMillis()))
    }

    suspend fun toggleConversationVerification(conversationId: String, isVerified: Boolean) {
        chatDao.updateConversationVerification(conversationId, isVerified)
        val conv = chatDao.getConversationById(conversationId)
        chatDao.insertSecurityLog(
            SecurityLogEntity(
                eventType = "KEY_VERIFICATION",
                description = "Kunci enkripsi ${conv?.title ?: conversationId} ditandai sebagai: ${if (isVerified) "TERVERIFIKASI" else "BELUM DIVERIFIKASI"}",
                severity = if (isVerified) SecuritySeverity.INFO else SecuritySeverity.WARNING
            )
        )
    }

    suspend fun linkNewDevice(deviceName: String, deviceType: DeviceType, platform: String): LinkedDeviceEntity {
        val deviceId = "dev_${UUID.randomUUID().toString().take(6)}"
        val sessionFingerprint = CryptoManager.generateHexFingerprint(deviceId + deviceName + System.currentTimeMillis()).take(16).uppercase()

        val newDevice = LinkedDeviceEntity(
            id = deviceId,
            deviceName = deviceName,
            deviceType = deviceType,
            platform = platform,
            location = "Jakarta, ID (IP Terenkripsi)",
            ipAddress = "192.168.1.${(10..250).random()}",
            linkedTimestamp = System.currentTimeMillis(),
            lastActiveTimestamp = System.currentTimeMillis(),
            isCurrentDevice = false,
            syncStatus = SyncStatus.SYNCING,
            sessionKeyFingerprint = sessionFingerprint
        )

        chatDao.insertLinkedDevice(newDevice)

        chatDao.insertSecurityLog(
            SecurityLogEntity(
                eventType = "DEVICE_LINKED",
                description = "Perangkat baru '$deviceName' ($platform) ditautkan dengan sesi $sessionFingerprint.",
                severity = SecuritySeverity.INFO
            )
        )

        // Simulate fast key handshake completion after 1.5s
        appScope.launch(Dispatchers.IO) {
            delay(1500)
            chatDao.updateLinkedDevice(newDevice.copy(syncStatus = SyncStatus.SYNCED))
        }

        return newDevice
    }

    suspend fun revokeDevice(deviceId: String) {
        chatDao.deleteLinkedDevice(deviceId)
        chatDao.insertSecurityLog(
            SecurityLogEntity(
                eventType = "DEVICE_REVOKED",
                description = "Sesi perangkat $deviceId dicabut dan kunci enkripsi sesi dihapus seketika.",
                severity = SecuritySeverity.WARNING
            )
        )
    }

    suspend fun triggerEmergencyLocalWipe() {
        chatDao.wipeAllMessages()
        chatDao.wipeAllConversations()
        chatDao.wipeRemoteDevices()
        chatDao.insertSecurityLog(
            SecurityLogEntity(
                eventType = "PANIC_LOCAL_WIPE",
                description = "EMERGENCY PANIC: Seluruh riwayat pesan, obrolan, dan sesi tertaut telah dihapus bersih dari perangkat lokal.",
                severity = SecuritySeverity.CRITICAL
            )
        )
    }

    suspend fun addMessageReaction(messageId: Long, emoji: String) {
        // Toggle reaction
        // For simplicity, add or replace reaction
    }

    private suspend fun startAutoDeleteCleanerLoop() {
        while (true) {
            delay(2000) // check every 2 seconds
            val now = System.currentTimeMillis()
            chatDao.purgeExpiredMessages(now)
        }
    }

    private suspend fun initializeSampleDataIfEmpty() {
        val existing = chatDao.getAllActiveConversations().first()
        if (existing.isNotEmpty()) return

        // Seed Contacts
        val sampleContacts = listOf(
            ContactEntity(
                id = "contact_alice",
                name = "Alice Vance",
                username = "@alice.crypto",
                publicKey = "04A8129C3B7E9F0012DE4489BC1789AA551029384756A1029384756102938475",
                fingerprint = "A812 9C3B 7E9F 0012 DE44 89BC",
                avatarColorHex = "#06B6D4",
                avatarInitials = "AV",
                isVerified = true,
                statusMessage = "Keamanan adalah prioritas. BeiChat terverifikasi.",
                isOnline = true,
                lastSeenTime = "Online"
            ),
            ContactEntity(
                id = "contact_bob",
                name = "Bob Cybersec",
                username = "@bob.research",
                publicKey = "045F19283746A5B4C3D2E1F099887766554433221100AABBCCDDEEFF00112233",
                fingerprint = "5F19 2837 46A5 B4C3 D2E1 F099",
                avatarColorHex = "#10B981",
                avatarInitials = "BC",
                isVerified = true,
                statusMessage = "Audit kode E2EE & Ratchet Encryption",
                isOnline = true,
                lastSeenTime = "Online"
            ),
            ContactEntity(
                id = "contact_charlie",
                name = "Charlie Rust",
                username = "@charlie.mesh",
                publicKey = "0477889900AABBCCDDEEFF00112233445566778899AABBCCDDEEFF0011223344",
                fingerprint = "7788 9900 AABB CCDD EEFF 0011",
                avatarColorHex = "#8B5CF6",
                avatarInitials = "CR",
                isVerified = false,
                statusMessage = "P2P Mesh Network Specialist",
                isOnline = false,
                lastSeenTime = "10 menit lalu"
            ),
            ContactEntity(
                id = "contact_diana",
                name = "Diana Sentinel",
                username = "@diana.guard",
                publicKey = "04BBDDEEFF00112233445566778899AABBCCDDEEFF00112233445566778899AA",
                fingerprint = "BBDD EEFF 0011 2233 4455 6677",
                avatarColorHex = "#F59E0B",
                avatarInitials = "DS",
                isVerified = true,
                statusMessage = "Multi-device cryptographic sync active",
                isOnline = true,
                lastSeenTime = "Online"
            )
        )
        chatDao.insertContacts(sampleContacts)

        // Seed Linked Devices
        val initialDevices = listOf(
            LinkedDeviceEntity(
                id = "dev_current",
                deviceName = "Android Smartphone (Perangkat Ini)",
                deviceType = DeviceType.MOBILE,
                platform = "Android 15 (Kernel E2E Secured)",
                location = "Jakarta, Indonesia (Lokal)",
                ipAddress = "192.168.1.102",
                linkedTimestamp = System.currentTimeMillis() - (86400000L * 14),
                lastActiveTimestamp = System.currentTimeMillis(),
                isCurrentDevice = true,
                syncStatus = SyncStatus.SYNCED,
                sessionKeyFingerprint = "LOCAL-MASTER-KEY"
            ),
            LinkedDeviceEntity(
                id = "dev_mac",
                deviceName = "MacBook Pro M3 Max",
                deviceType = DeviceType.DESKTOP,
                platform = "macOS Sequoia (BeiChat Desktop v2.4)",
                location = "Bandung, Indonesia (Terenkripsi)",
                ipAddress = "192.168.1.140",
                linkedTimestamp = System.currentTimeMillis() - (86400000L * 4),
                lastActiveTimestamp = System.currentTimeMillis() - 120000L,
                isCurrentDevice = false,
                syncStatus = SyncStatus.SYNCED,
                sessionKeyFingerprint = "7F4B 9182 3C2A 009F"
            ),
            LinkedDeviceEntity(
                id = "dev_ipad",
                deviceName = "iPad Pro 13 M4",
                deviceType = DeviceType.TABLET,
                platform = "iPadOS 18 (BeiChat Tablet)",
                location = "Surabaya, Indonesia",
                ipAddress = "192.168.1.188",
                linkedTimestamp = System.currentTimeMillis() - (86400000L * 2),
                lastActiveTimestamp = System.currentTimeMillis() - 3600000L,
                isCurrentDevice = false,
                syncStatus = SyncStatus.SYNCED,
                sessionKeyFingerprint = "89AC 4E11 02BF 339D"
            ),
            LinkedDeviceEntity(
                id = "dev_web",
                deviceName = "BeiChat Web (Chrome / Linux)",
                deviceType = DeviceType.WEB,
                platform = "Linux x86_64 / Chrome Web Cryptography",
                location = "Jakarta, Indonesia",
                ipAddress = "192.168.1.199",
                linkedTimestamp = System.currentTimeMillis() - (86400000L * 1),
                lastActiveTimestamp = System.currentTimeMillis() - 7200000L,
                isCurrentDevice = false,
                syncStatus = SyncStatus.SYNCED,
                sessionKeyFingerprint = "5D22 1890 CC44 77E1"
            )
        )
        chatDao.insertLinkedDevices(initialDevices)

        // Seed Conversations
        val convGroup1 = ConversationEntity(
            id = "conv_group_cybercore",
            type = ConversationType.GROUP,
            title = "🛡️ BeiChat Cyber Core",
            peerPublicKey = "GROUP-SECRET-KEY-BITCHAT-CYBERCORE-2026",
            avatarColorHex = "#10B981",
            avatarInitials = "BC",
            lastMessageText = "Kunci grup X25519 telah diperbarui otomatis untuk semua anggota.",
            lastMessageTimestamp = System.currentTimeMillis() - 60000L,
            unreadCount = 2,
            isPinned = true,
            isVerified = true,
            autoDeleteDurationSeconds = 86400L, // 24 hours
            groupMembersCount = 6,
            groupDescription = "Grup koordinasi keamanan enkripsi, audit privasi data, dan pertukaran media resolusi tinggi.",
            groupAdmins = "Anda, Bob Cybersec"
        )

        val convAlice = ConversationEntity(
            id = "conv_alice",
            type = ConversationType.DIRECT,
            title = "Alice Vance",
            peerPublicKey = sampleContacts[0].publicKey,
            avatarColorHex = "#06B6D4",
            avatarInitials = "AV",
            lastMessageText = "Foto resolusi tinggi diterima utuh tanpa kompresi! Checksum SHA-256 valid.",
            lastMessageTimestamp = System.currentTimeMillis() - 300000L,
            unreadCount = 0,
            isPinned = true,
            isVerified = true,
            autoDeleteDurationSeconds = 60L, // 1 minute auto-delete demo
            isOnline = true
        )

        val convBob = ConversationEntity(
            id = "conv_bob",
            type = ConversationType.DIRECT,
            title = "Bob Cybersec",
            peerPublicKey = sampleContacts[1].publicKey,
            avatarColorHex = "#10B981",
            avatarInitials = "BC",
            lastMessageText = "Dokumen spesifikasi enkripsi end-to-end terlampir.",
            lastMessageTimestamp = System.currentTimeMillis() - 1200000L,
            unreadCount = 1,
            isPinned = false,
            isVerified = true,
            autoDeleteDurationSeconds = 0L, // Off
            isOnline = true
        )

        val convCharlie = ConversationEntity(
            id = "conv_charlie",
            type = ConversationType.DIRECT,
            title = "Charlie Rust",
            peerPublicKey = sampleContacts[2].publicKey,
            avatarColorHex = "#8B5CF6",
            avatarInitials = "CR",
            lastMessageText = "Bagaimana status mesh relay di node regional?",
            lastMessageTimestamp = System.currentTimeMillis() - 86400000L,
            unreadCount = 0,
            isPinned = false,
            isVerified = false,
            autoDeleteDurationSeconds = 3600L,
            isOnline = false
        )

        val convDiana = ConversationEntity(
            id = "conv_diana",
            type = ConversationType.DIRECT,
            title = "Diana Sentinel",
            peerPublicKey = sampleContacts[3].publicKey,
            avatarColorHex = "#F59E0B",
            avatarInitials = "DS",
            lastMessageText = "Pesan suara audit keamanan telah dikirim.",
            lastMessageTimestamp = System.currentTimeMillis() - (86400000L * 2),
            unreadCount = 0,
            isPinned = false,
            isVerified = true,
            autoDeleteDurationSeconds = 0L,
            isOnline = true
        )

        chatDao.insertConversations(listOf(convGroup1, convAlice, convBob, convCharlie, convDiana))

        // Seed Sample Messages for Group
        val groupKey = CryptoManager.deriveKeyFromSeed(convGroup1.peerPublicKey)
        val now = System.currentTimeMillis()

        val groupMessages = listOf(
            MessageEntity(
                conversationId = convGroup1.id,
                senderId = "peer_bob",
                senderName = "Bob Cybersec",
                content = "Selamat datang di ruang obrolan terenkripsi BitChat. Semua payload teks & file dienkripsi dengan AES-256-GCM.",
                encryptedCipherPayload = CryptoManager.encrypt("Selamat datang di ruang obrolan terenkripsi BitChat. Semua payload teks & file dienkripsi dengan AES-256-GCM.", groupKey).cipherBase64,
                ivHex = "a1b2c3d4e5f60718293a4b5c",
                timestamp = now - 600000L,
                status = MessageStatus.READ,
                isOutgoing = false,
                mediaType = MediaType.NONE
            ),
            MessageEntity(
                conversationId = convGroup1.id,
                senderId = "my_user_id",
                senderName = "Anda",
                content = "Berikut blueprint arsitektur enkripsi multi-perangkat dalam resolusi tinggi asli (Original Lossless HD).",
                encryptedCipherPayload = CryptoManager.encrypt("Berikut blueprint arsitektur enkripsi multi-perangkat dalam resolusi tinggi asli (Original Lossless HD).", groupKey).cipherBase64,
                ivHex = "0918273645aabbccddeeff00",
                timestamp = now - 400000L,
                status = MessageStatus.READ,
                isOutgoing = true,
                mediaType = MediaType.IMAGE_HIGH_RES,
                mediaUri = "https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?q=80&w=1200&auto=format&fit=crop",
                mediaCaption = "Security_Architecture_Blueprint_4K.png",
                mediaSizeFormatted = "4.8 MB (HD Lossless)",
                mediaChecksumSha256 = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
                isHighResOriginal = true
            ),
            MessageEntity(
                conversationId = convGroup1.id,
                senderId = "peer_alice",
                senderName = "Alice Vance",
                content = "Pesan suara ringkasan audit sistem telah dienkripsi.",
                encryptedCipherPayload = CryptoManager.encrypt("Pesan suara ringkasan audit sistem telah dienkripsi.", groupKey).cipherBase64,
                ivHex = "8899aabbccddeeff00112233",
                timestamp = now - 200000L,
                status = MessageStatus.READ,
                isOutgoing = false,
                mediaType = MediaType.AUDIO_VOICE_NOTE,
                audioDurationSeconds = 14,
                mediaCaption = "Catatan Suara Terenkripsi"
            ),
            MessageEntity(
                conversationId = convGroup1.id,
                senderId = "peer_bob",
                senderName = "Bob Cybersec",
                content = "Kunci grup X25519 telah diperbarui otomatis untuk semua anggota.",
                encryptedCipherPayload = CryptoManager.encrypt("Kunci grup X25519 telah diperbarui otomatis untuk semua anggota.", groupKey).cipherBase64,
                ivHex = "11223344556677889900aabb",
                timestamp = now - 60000L,
                status = MessageStatus.DELIVERED,
                isOutgoing = false,
                mediaType = MediaType.NONE
            )
        )
        chatDao.insertMessages(groupMessages)

        // Seed Sample Messages for Alice (with Disappearing Timer Demo)
        val aliceKey = CryptoManager.deriveKeyFromSeed(convAlice.peerPublicKey)
        val aliceMessages = listOf(
            MessageEntity(
                conversationId = convAlice.id,
                senderId = "peer_alice",
                senderName = "Alice Vance",
                content = "Halo! Obrolan ini menggunakan timer penghapusan pesan otomatis (Self-Destruct) 1 Menit.",
                encryptedCipherPayload = CryptoManager.encrypt("Halo! Obrolan ini menggunakan timer penghapusan pesan otomatis (Self-Destruct) 1 Menit.", aliceKey).cipherBase64,
                ivHex = "feebda9876543210feebda98",
                timestamp = now - 180000L,
                status = MessageStatus.READ,
                isOutgoing = false,
                mediaType = MediaType.NONE,
                autoDeleteDurationSeconds = 60L,
                expiresAtTimestamp = now + 45000L // 45 seconds left
            ),
            MessageEntity(
                conversationId = convAlice.id,
                senderId = "my_user_id",
                senderName = "Anda",
                content = "Sip! Mengirim dokumen spesifikasi kunci aman dalam enkripsi AES-GCM.",
                encryptedCipherPayload = CryptoManager.encrypt("Sip! Mengirim dokumen spesifikasi kunci aman dalam enkripsi AES-GCM.", aliceKey).cipherBase64,
                ivHex = "ccddeeff0011223344556677",
                timestamp = now - 120000L,
                status = MessageStatus.READ,
                isOutgoing = true,
                mediaType = MediaType.DOCUMENT,
                mediaCaption = "BitChat_E2EE_Specification_v2.pdf",
                mediaSizeFormatted = "2.1 MB (Encrypted)",
                mediaChecksumSha256 = "8f434346648f6b96df89dda901c5176b10a6d83961dd3c1ac88b59b2dc327aa4",
                autoDeleteDurationSeconds = 60L,
                expiresAtTimestamp = now + 120000L
            ),
            MessageEntity(
                conversationId = convAlice.id,
                senderId = "peer_alice",
                senderName = "Alice Vance",
                content = "Foto resolusi tinggi diterima utuh tanpa kompresi! Checksum SHA-256 valid.",
                encryptedCipherPayload = CryptoManager.encrypt("Foto resolusi tinggi diterima utuh tanpa kompresi! Checksum SHA-256 valid.", aliceKey).cipherBase64,
                ivHex = "556677889900aabbccddeeff",
                timestamp = now - 60000L,
                status = MessageStatus.READ,
                isOutgoing = false,
                mediaType = MediaType.NONE,
                autoDeleteDurationSeconds = 60L,
                expiresAtTimestamp = now + 180000L
            )
        )
        chatDao.insertMessages(aliceMessages)

        // Initial Security Logs
        val initialLogs = listOf(
            SecurityLogEntity(
                eventType = "KEY_GENERATED",
                description = "Master Identity Key X25519 diinisialisasi pada enclave perangkat lokal.",
                severity = SecuritySeverity.INFO
            ),
            SecurityLogEntity(
                eventType = "SESSION_SYNC",
                description = "Sinkronisasi kunci sesi berhasil dengan MacBook Pro M3 dan iPad Pro 13.",
                severity = SecuritySeverity.INFO
            ),
            SecurityLogEntity(
                eventType = "E2EE_INITIALIZED",
                description = "Protokol BitChat End-to-End Encryption siap aktif.",
                severity = SecuritySeverity.INFO
            )
        )
        for (log in initialLogs) {
            chatDao.insertSecurityLog(log)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: BitChatRepository? = null

        fun getInstance(context: Context): BitChatRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: run {
                    val db = AppDatabase.getDatabase(context.applicationContext)
                    val scope = CoroutineScope(Dispatchers.IO)
                    val repo = BitChatRepository(db.chatDao(), scope)
                    INSTANCE = repo
                    repo
                }
            }
        }
    }
}
