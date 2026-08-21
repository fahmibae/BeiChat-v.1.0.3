package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.ContactEntity
import com.example.data.model.ConversationEntity
import com.example.data.model.LinkedDeviceEntity
import com.example.data.model.MessageEntity
import com.example.data.model.SecurityLogEntity

@Database(
    entities = [
        ConversationEntity::class,
        MessageEntity::class,
        LinkedDeviceEntity::class,
        ContactEntity::class,
        SecurityLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bitchat_secure.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
