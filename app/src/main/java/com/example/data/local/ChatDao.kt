package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ContactEntity
import com.example.data.model.ConversationEntity
import com.example.data.model.LinkedDeviceEntity
import com.example.data.model.MessageEntity
import com.example.data.model.SecurityLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    // Conversations
    @Query("SELECT * FROM conversations WHERE isArchived = 0 ORDER BY isPinned DESC, lastMessageTimestamp DESC")
    fun getAllActiveConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE id = :id")
    fun getConversationFlow(id: String): Flow<ConversationEntity?>

    @Query("SELECT * FROM conversations WHERE id = :id")
    suspend fun getConversationById(id: String): ConversationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ConversationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversations(conversations: List<ConversationEntity>)

    @Update
    suspend fun updateConversation(conversation: ConversationEntity)

    @Query("DELETE FROM conversations WHERE id = :id")
    suspend fun deleteConversation(id: String)

    @Query("UPDATE conversations SET unreadCount = 0 WHERE id = :id")
    suspend fun markConversationRead(id: String)

    @Query("UPDATE conversations SET autoDeleteDurationSeconds = :durationSeconds WHERE id = :id")
    suspend fun updateAutoDeleteDuration(id: String, durationSeconds: Long)

    @Query("UPDATE conversations SET isVerified = :isVerified WHERE id = :id")
    suspend fun updateConversationVerification(id: String, isVerified: Boolean)

    // Messages
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getMessagesForConversation(conversationId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE id = :id")
    suspend fun getMessageById(id: Long): MessageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)

    @Update
    suspend fun updateMessage(message: MessageEntity)

    @Query("DELETE FROM messages WHERE id = :id")
    suspend fun deleteMessage(id: Long)

    @Query("DELETE FROM messages WHERE expiresAtTimestamp > 0 AND expiresAtTimestamp <= :now")
    suspend fun purgeExpiredMessages(now: Long): Int

    @Query("DELETE FROM messages WHERE conversationId = :conversationId")
    suspend fun clearMessagesForConversation(conversationId: String)

    // Linked Devices (Multi-device sync)
    @Query("SELECT * FROM linked_devices ORDER BY isCurrentDevice DESC, lastActiveTimestamp DESC")
    fun getAllLinkedDevices(): Flow<List<LinkedDeviceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLinkedDevice(device: LinkedDeviceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLinkedDevices(devices: List<LinkedDeviceEntity>)

    @Update
    suspend fun updateLinkedDevice(device: LinkedDeviceEntity)

    @Query("DELETE FROM linked_devices WHERE id = :id")
    suspend fun deleteLinkedDevice(id: String)

    // Contacts
    @Query("SELECT * FROM contacts ORDER BY isOnline DESC, name ASC")
    fun getAllContacts(): Flow<List<ContactEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContacts(contacts: List<ContactEntity>)

    @Query("UPDATE contacts SET isVerified = :isVerified WHERE id = :id")
    suspend fun updateContactVerification(id: String, isVerified: Boolean)

    // Security Audit Logs
    @Query("SELECT * FROM security_logs ORDER BY timestamp DESC LIMIT 50")
    fun getSecurityLogs(): Flow<List<SecurityLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSecurityLog(log: SecurityLogEntity)

    // Emergency Local Panic Wipe
    @Query("DELETE FROM messages")
    suspend fun wipeAllMessages()

    @Query("DELETE FROM conversations")
    suspend fun wipeAllConversations()

    @Query("DELETE FROM linked_devices WHERE isCurrentDevice = 0")
    suspend fun wipeRemoteDevices()
}
