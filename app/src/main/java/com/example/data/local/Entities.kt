package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_sessions")
data class ChatSessionEntity(
    @PrimaryKey val id: String,
    val title: String,
    val lastUpdated: Long = System.currentTimeMillis(),
    val modelUsed: String = "Gemini 3.1 Pro",
    val isEncryptedSynced: Boolean = true
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: String,
    val sender: String, // "USER" or "GEMINI"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val attachmentUri: String? = null,
    val attachmentName: String? = null,
    val isVoiceMessage: Boolean = false
)

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey val id: String,
    val name: String,
    val fileType: String,
    val sizeText: String,
    val summary: String,
    val fullText: String,
    val keywords: String = "AI Analysis, Key Findings, Optimization, Executive Summary",
    val sentiment: String = "POSITIVE",
    val sentimentScore: Float = 0.88f,
    val keyPhrases: String = "Operational efficiency improved | Financial trajectory positive | AI integration on schedule",
    val addedTimestamp: Long = System.currentTimeMillis()
)
