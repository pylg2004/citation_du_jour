package com.example.data.repository

import com.example.data.local.*
import com.example.data.model.*
import com.example.data.remote.GeminiApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class AppRepository(
    private val database: AppDatabase,
    private val geminiApiService: GeminiApiService = GeminiApiService()
) {
    private val userAccountDao = database.userAccountDao()
    private val quoteDao = database.quoteDao()
    private val quizDao = database.quizDao()
    private val chatDao = database.chatDao()
    private val messageDao = database.messageDao()
    private val userDao = database.userDao()

    // Authentication & Account State
    val loggedInUser: Flow<UserAccountEntity?> = userAccountDao.getLoggedInUser()

    suspend fun registerUser(fullName: String, username: String, email: String, password: String): Boolean {
        userAccountDao.logoutAllUsers()
        val newUser = UserAccountEntity(
            fullName = fullName.trim(),
            username = username.trim().removePrefix("@"),
            email = email.trim(),
            passwordHash = password,
            isLoggedIn = true,
            level = 1,
            xp = 0
        )
        val id = userAccountDao.insertUser(newUser)
        userDao.insertOrUpdateProfile(
            UserProfileEntity(
                username = newUser.username,
                displayName = newUser.fullName,
                bio = "Membre passionné de Citations & Quizz"
            )
        )
        return id > 0
    }

    suspend fun loginUser(emailOrUsername: String, password: String): Boolean {
        val user = userAccountDao.findUserByIdentifier(emailOrUsername.trim())
        return if (user != null && user.passwordHash == password) {
            userAccountDao.logoutAllUsers()
            userAccountDao.setLoggedIn(user.id)
            userDao.insertOrUpdateProfile(
                UserProfileEntity(
                    username = user.username,
                    displayName = user.fullName,
                    bio = "Membre passionné de Citations & Quizz"
                )
            )
            true
        } else {
            false
        }
    }

    suspend fun logoutUser() {
        userAccountDao.logoutAllUsers()
        userDao.insertOrUpdateProfile(
            UserProfileEntity(
                username = "guest_user",
                displayName = "Invité",
                bio = "Bienvenue sur Citations & Quizz."
            )
        )
    }

    // Quotes
    val allQuotes: Flow<List<QuoteEntity>> = quoteDao.getAllQuotes()
    val favoriteQuotes: Flow<List<QuoteEntity>> = quoteDao.getFavoriteQuotes()

    suspend fun toggleQuoteLike(quote: QuoteEntity) {
        val newStatus = !quote.isLiked
        val delta = if (newStatus) 1 else -1
        quoteDao.updateLikeStatus(quote.id, newStatus, delta)
    }

    suspend fun addQuote(text: String, author: String, category: String) {
        val newQuote = QuoteEntity(
            text = text,
            author = author.ifBlank { "Anonyme" },
            category = category.ifBlank { "Inspiration" },
            likesCount = 0,
            isLiked = false,
            isCustom = true
        )
        quoteDao.insertQuote(newQuote)
    }

    suspend fun deleteQuote(quoteId: Long) {
        quoteDao.deleteQuote(quoteId)
    }

    // Quiz
    val quizResults: Flow<List<QuizResultEntity>> = quizDao.getAllResults()

    suspend fun generateQuiz(theme: String = "Tous", count: Int = 10, difficulty: String = "Adaptatif"): List<QuizQuestion> {
        val questionCount = if (count <= 0) 10 else count
        val selectedQuotes = if (theme == "Tous" || theme == "Toutes les citations" || theme.contains("Mélangé", ignoreCase = true)) {
            quoteDao.getRandomQuotes(questionCount.coerceAtLeast(5))
        } else {
            val catQuotes = quoteDao.getRandomQuotesByCategory(theme, questionCount)
            if (catQuotes.size < questionCount) {
                val extra = quoteDao.getRandomQuotes(questionCount - catQuotes.size)
                (catQuotes + extra).distinctBy { it.id }
            } else {
                catQuotes
            }
        }

        val questions = mutableListOf<QuizQuestion>()
        val targetQuotes = selectedQuotes.take(questionCount)

        for ((index, quote) in targetQuotes.withIndex()) {
            val qType = index % 3
            when (qType) {
                0 -> {
                    // Author quiz: "Qui a écrit la citation suivante ?"
                    val wrongAuthors = quoteDao.getRandomAuthorsExcept(quote.author, 3)
                    val options = (wrongAuthors + quote.author).shuffled()
                    val correctIdx = options.indexOf(quote.author).coerceAtLeast(0)
                    questions.add(
                        QuizQuestion(
                            id = index + 1,
                            question = "Qui est l'auteur de cette citation : « ${quote.text} » ?",
                            options = options,
                            correctAnswerIndex = correctIdx,
                            explanation = "Cette pensée célèbre sur le thème '${quote.category}' a été rédigée par ${quote.author}."
                        )
                    )
                }
                1 -> {
                    // Category quiz: "À quel thème appartient cette citation de [Author] ?"
                    val wrongCats = quoteDao.getRandomCategoriesExcept(quote.category, 3)
                    val options = (wrongCats + quote.category).shuffled()
                    val correctIdx = options.indexOf(quote.category).coerceAtLeast(0)
                    questions.add(
                        QuizQuestion(
                            id = index + 1,
                            question = "Dans quel thème s'inscrit cette pensée de ${quote.author} : « ${quote.text} » ?",
                            options = options,
                            correctAnswerIndex = correctIdx,
                            explanation = "${quote.author} exprime cette idée dans la catégorie '${quote.category}'."
                        )
                    )
                }
                else -> {
                    // Quote completion
                    val words = quote.text.split(" ")
                    val half = (words.size / 2).coerceAtLeast(1)
                    val startPart = words.take(half).joinToString(" ")
                    val correctEnding = words.drop(half).joinToString(" ")

                    val otherQuotes = quoteDao.getRandomQuotes(3).filter { it.id != quote.id }
                    val wrongEndings = otherQuotes.map { oq ->
                        val oqWords = oq.text.split(" ")
                        val oqHalf = (oqWords.size / 2).coerceAtLeast(1)
                        oqWords.drop(oqHalf).joinToString(" ")
                    }

                    val allEndings = (wrongEndings + correctEnding).shuffled()
                    val correctIdx = allEndings.indexOf(correctEnding).coerceAtLeast(0)

                    questions.add(
                        QuizQuestion(
                            id = index + 1,
                            question = "Complétez la citation de ${quote.author} : « $startPart ... »",
                            options = allEndings,
                            correctAnswerIndex = correctIdx,
                            explanation = "La citation exacte de ${quote.author} est : « ${quote.text} »."
                        )
                    )
                }
            }
        }
        return questions
    }

    suspend fun saveQuizResult(theme: String, score: Int, totalQuestions: Int, correctAnswers: Int) {
        val percentage = ((correctAnswers.toDouble() / totalQuestions) * 100).toInt()
        val result = QuizResultEntity(
            theme = theme,
            score = score,
            totalQuestions = totalQuestions,
            correctAnswers = correctAnswers,
            percentage = percentage
        )
        quizDao.insertResult(result)
        userDao.addPoints(score)

        // Update user XP & Level
        val user = userAccountDao.getLoggedInUserOnce()
        if (user != null) {
            val newXp = user.xp + score
            val newLevel = (newXp / 100) + 1
            userAccountDao.updateUserLevel(user.id, newLevel, newXp)
        }
    }

    // AI Chat & Sessions
    val chatSessions: Flow<List<ChatSessionEntity>> = chatDao.getChatSessions()
    val chatMessages: Flow<List<ChatMessageEntity>> = chatDao.getAllChatMessages()

    fun getMessagesForSession(sessionId: Long): Flow<List<ChatMessageEntity>> {
        return chatDao.getMessagesForSession(sessionId)
    }

    suspend fun createNewChatSession(title: String = "Nouvelle discussion"): Long {
        val session = ChatSessionEntity(title = title)
        return chatDao.insertSession(session)
    }

    suspend fun sendChatMessage(userText: String, sessionId: Long = 1) {
        val userMsg = ChatMessageEntity(sessionId = sessionId, sender = "USER", content = userText)
        chatDao.insertMessage(userMsg)

        val history = chatDao.getMessagesForSession(sessionId).first().map { Pair(it.sender, it.content) }
        val aiResponseText = geminiApiService.sendChatMessage(userText, history)

        val aiMsg = ChatMessageEntity(sessionId = sessionId, sender = "AI", content = aiResponseText)
        chatDao.insertMessage(aiMsg)
    }

    suspend fun clearChatHistory() {
        chatDao.clearChatHistory()
    }

    // Messages / Contacts
    val allContacts: Flow<List<ContactEntity>> = messageDao.getAllContacts()

    fun searchContacts(query: String): Flow<List<ContactEntity>> {
        return messageDao.searchContacts(query)
    }

    fun getDirectMessages(contactId: Long): Flow<List<DirectMessageEntity>> {
        return messageDao.getMessagesForContact(contactId)
    }

    suspend fun sendDirectMessage(contactId: Long, senderName: String, content: String) {
        val msg = DirectMessageEntity(
            contactId = contactId,
            senderName = senderName,
            content = content,
            isFromMe = true,
            status = "DELIVERED"
        )
        messageDao.insertDirectMessage(msg)
        messageDao.updateContactLastMessage(contactId, content, System.currentTimeMillis())
    }

    suspend fun editDirectMessage(messageId: Long, newContent: String) {
        messageDao.updateDirectMessage(messageId, newContent, System.currentTimeMillis())
    }

    suspend fun addNewContact(username: String, displayName: String, bio: String) {
        val contact = ContactEntity(
            username = username.removePrefix("@"),
            displayName = displayName,
            bio = bio.ifBlank { "Utilisateur passionné de citations et quizz." },
            avatarColorHex = listOf("#FF6D00", "#FF8F00", "#E91E63", "#3F51B5", "#009688", "#795548").random(),
            lastMessage = "Conversation démarrée",
            lastMessageTime = System.currentTimeMillis()
        )
        messageDao.insertContact(contact)
    }

    // User Profile
    val userProfile: Flow<UserProfileEntity?> = userDao.getUserProfile()

    suspend fun updateProfile(username: String, displayName: String, bio: String, themeMode: String, notificationsEnabled: Boolean) {
        val current = userDao.getUserProfileOnce() ?: UserProfileEntity()
        val updated = current.copy(
            username = username,
            displayName = displayName,
            bio = bio,
            themeMode = themeMode,
            notificationsEnabled = notificationsEnabled
        )
        userDao.insertOrUpdateProfile(updated)
    }

    // Pre-populate Database
    suspend fun seedDatabaseIfNeeded() {
        if (quoteDao.getQuoteCount() < 10000) {
            val generated10k = QuoteSeeder.generate10kQuotes()
            generated10k.chunked(1000).forEach { chunk ->
                quoteDao.insertQuotes(chunk)
            }
        }

        if (chatDao.getChatSessions().first().isEmpty()) {
            chatDao.insertSession(ChatSessionEntity(id = 1, title = "Discussion Principale"))
        }

        if (messageDao.getContactCount() == 0) {
            val initialContacts = listOf(
                ContactEntity(
                    username = "sophie_martin",
                    displayName = "Sophie Martin",
                    bio = "Amatrice de philosophie stoïcienne et de grands auteurs.",
                    avatarColorHex = "#FF6D00",
                    statusOnline = true,
                    lastMessage = "Tu as vu la dernière citation d'Albert Einstein ?",
                    lastMessageTime = System.currentTimeMillis() - 1000 * 60 * 15
                ),
                ContactEntity(
                    username = "lucas_bernard",
                    displayName = "Lucas Bernard",
                    bio = "Passionné de quizz sur les citations.",
                    avatarColorHex = "#FF8F00",
                    statusOnline = true,
                    lastMessage = "J'ai fait un score parfait de 100 points au Quizz Citations !",
                    lastMessageTime = System.currentTimeMillis() - 1000 * 60 * 60 * 2
                ),
                ContactEntity(
                    username = "marie_curie",
                    displayName = "Marie Curie",
                    bio = "Chercheuse & passionnée de pensées scientifiques.",
                    avatarColorHex = "#9C27B0",
                    statusOnline = true,
                    lastMessage = "La curiosité est le moteur de toute grande découverte.",
                    lastMessageTime = System.currentTimeMillis() - 1000 * 60 * 60 * 5
                ),
                ContactEntity(
                    username = "victor_hugo",
                    displayName = "Victor Hugo",
                    bio = "Écrivain & poète passionné sur l'application.",
                    avatarColorHex = "#795548",
                    statusOnline = true,
                    lastMessage = "La liberté commence où l'ignorance finit.",
                    lastMessageTime = System.currentTimeMillis() - 1000 * 60 * 60 * 12
                ),
                ContactEntity(
                    username = "claire_bernard",
                    displayName = "Claire Bernard",
                    bio = "Passionnée d'art, de philosophie et de cinéma.",
                    avatarColorHex = "#4CAF50",
                    statusOnline = false,
                    lastMessage = "On se fait un Quizz Citations ce soir ?",
                    lastMessageTime = System.currentTimeMillis() - 1000 * 60 * 60 * 28
                )
            )
            messageDao.insertContacts(initialContacts)

            messageDao.insertDirectMessage(
                DirectMessageEntity(
                    contactId = 1,
                    senderName = "Sophie Martin",
                    content = "Salut ! Bienvenue sur Citations & Quizz !",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 30,
                    isFromMe = false
                )
            )
            messageDao.insertDirectMessage(
                DirectMessageEntity(
                    contactId = 1,
                    senderName = "Moi",
                    content = "Merci Sophie ! L'application est vraiment élégante.",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 25,
                    isFromMe = true
                )
            )
        }

        if (userDao.getUserProfileOnce() == null) {
            userDao.insertOrUpdateProfile(UserProfileEntity())
        }
    }
}

