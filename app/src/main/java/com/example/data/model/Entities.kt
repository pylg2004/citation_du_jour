package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_accounts")
data class UserAccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val username: String,
    val email: String,
    val passwordHash: String,
    val level: Int = 1,
    val xp: Int = 0,
    val isLoggedIn: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "quotes")
data class QuoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val author: String,
    val category: String,
    val likesCount: Int = 0,
    val isLiked: Boolean = false,
    val isCustom: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "quiz_results")
data class QuizResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val theme: String,
    val score: Int,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val percentage: Int,
    val dateTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_sessions")
data class ChatSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long = 1,
    val sender: String, // "USER" or "AI"
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "direct_messages")
data class DirectMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val contactId: Long,
    val senderName: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isFromMe: Boolean,
    val status: String = "DELIVERED",
    val isEdited: Boolean = false,
    val editedTimestamp: Long? = null
)

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    val displayName: String,
    val bio: String,
    val avatarColorHex: String = "#FF6D00",
    val statusOnline: Boolean = true,
    val lastMessage: String = "",
    val lastMessageTime: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Long = 1,
    val username: String = "guest_user",
    val displayName: String = "Invité",
    val bio: String = "Bienvenue sur Citations & Quizz.",
    val avatarUri: String? = null,
    val themeMode: String = "LIGHT",
    val notificationsEnabled: Boolean = true,
    val totalPoints: Int = 0
)

data class QuizQuestion(
    val id: Int = 0,
    val question: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val explanation: String
)


