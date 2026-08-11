package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserAccountDao {
    @Query("SELECT * FROM user_accounts WHERE isLoggedIn = 1 LIMIT 1")
    fun getLoggedInUser(): Flow<UserAccountEntity?>

    @Query("SELECT * FROM user_accounts WHERE isLoggedIn = 1 LIMIT 1")
    suspend fun getLoggedInUserOnce(): UserAccountEntity?

    @Query("SELECT * FROM user_accounts WHERE email = :identifier OR username = :identifier LIMIT 1")
    suspend fun findUserByIdentifier(identifier: String): UserAccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserAccountEntity): Long

    @Query("UPDATE user_accounts SET isLoggedIn = 0")
    suspend fun logoutAllUsers()

    @Query("UPDATE user_accounts SET isLoggedIn = 1 WHERE id = :userId")
    suspend fun setLoggedIn(userId: Long)

    @Query("UPDATE user_accounts SET level = :level, xp = :xp WHERE id = :userId")
    suspend fun updateUserLevel(userId: Long, level: Int, xp: Int)
}

@Dao
interface QuoteDao {
    @Query("SELECT * FROM quotes ORDER BY id DESC")
    fun getAllQuotes(): Flow<List<QuoteEntity>>

    @Query("SELECT * FROM quotes WHERE isLiked = 1 ORDER BY id DESC")
    fun getFavoriteQuotes(): Flow<List<QuoteEntity>>

    @Query("SELECT * FROM quotes ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomQuote(): QuoteEntity?

    @Query("SELECT * FROM quotes ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomQuotes(limit: Int): List<QuoteEntity>

    @Query("SELECT * FROM quotes WHERE category = :category ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomQuotesByCategory(category: String, limit: Int): List<QuoteEntity>

    @Query("SELECT DISTINCT author FROM quotes WHERE author != :excludeAuthor ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomAuthorsExcept(excludeAuthor: String, limit: Int): List<String>

    @Query("SELECT DISTINCT category FROM quotes WHERE category != :excludeCategory ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomCategoriesExcept(excludeCategory: String, limit: Int): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuote(quote: QuoteEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuotes(quotes: List<QuoteEntity>)

    @Query("UPDATE quotes SET isLiked = :isLiked, likesCount = likesCount + :likeDelta WHERE id = :quoteId")
    suspend fun updateLikeStatus(quoteId: Long, isLiked: Boolean, likeDelta: Int)

    @Query("DELETE FROM quotes WHERE id = :quoteId")
    suspend fun deleteQuote(quoteId: Long)

    @Query("SELECT COUNT(*) FROM quotes")
    suspend fun getQuoteCount(): Int
}

@Dao
interface QuizDao {
    @Query("SELECT * FROM quiz_results ORDER BY dateTimestamp DESC")
    fun getAllResults(): Flow<List<QuizResultEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResult(result: QuizResultEntity): Long

    @Query("SELECT SUM(score) FROM quiz_results")
    suspend fun getTotalScore(): Int?
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_sessions ORDER BY createdAt DESC")
    fun getChatSessions(): Flow<List<ChatSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ChatSessionEntity): Long

    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY id ASC")
    fun getMessagesForSession(sessionId: Long): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages ORDER BY id ASC")
    fun getAllChatMessages(): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity): Long

    @Query("DELETE FROM chat_messages WHERE sessionId = :sessionId")
    suspend fun clearChatHistoryForSession(sessionId: Long)

    @Query("DELETE FROM chat_messages")
    suspend fun clearChatHistory()
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM contacts ORDER BY lastMessageTime DESC")
    fun getAllContacts(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE username LIKE '%' || :query || '%' OR displayName LIKE '%' || :query || '%'")
    fun searchContacts(query: String): Flow<List<ContactEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: ContactEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContacts(contacts: List<ContactEntity>)

    @Query("SELECT * FROM direct_messages WHERE contactId = :contactId ORDER BY timestamp ASC")
    fun getMessagesForContact(contactId: Long): Flow<List<DirectMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDirectMessage(message: DirectMessageEntity): Long

    @Query("UPDATE direct_messages SET content = :newContent, isEdited = 1, editedTimestamp = :editedTime WHERE id = :messageId")
    suspend fun updateDirectMessage(messageId: Long, newContent: String, editedTime: Long)

    @Query("UPDATE contacts SET lastMessage = :lastMessage, lastMessageTime = :timestamp WHERE id = :contactId")
    suspend fun updateContactLastMessage(contactId: Long, lastMessage: String, timestamp: Long)

    @Query("SELECT COUNT(*) FROM contacts")
    suspend fun getContactCount(): Int
}

@Dao
interface UserDao {
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 1")
    suspend fun getUserProfileOnce(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfileEntity)

    @Query("UPDATE user_profile SET totalPoints = totalPoints + :points WHERE id = 1")
    suspend fun addPoints(points: Int)
}

