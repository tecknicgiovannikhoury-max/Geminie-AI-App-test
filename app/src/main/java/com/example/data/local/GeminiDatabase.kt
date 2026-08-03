package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ChatSessionEntity::class, ChatMessageEntity::class, DocumentEntity::class],
    version = 2,
    exportSchema = false
)
abstract class GeminiDatabase : RoomDatabase() {
    abstract fun geminiDao(): GeminiDao

    companion object {
        @Volatile
        private var INSTANCE: GeminiDatabase? = null

        fun getDatabase(context: Context): GeminiDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GeminiDatabase::class.java,
                    "gemini_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
