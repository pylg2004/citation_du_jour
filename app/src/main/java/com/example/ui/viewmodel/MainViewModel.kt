package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.repository.AppRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AppRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = AppRepository(database)
        viewModelScope.launch {
            repository.seedDatabaseIfNeeded()
        }
    }

    // Auth & Account State
    val loggedInUser: StateFlow<UserAccountEntity?> = repository.loggedInUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val isLoggedIn: StateFlow<Boolean> = loggedInUser
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _showAuthDialog = MutableStateFlow(false)
    val showAuthDialog: StateFlow<Boolean> = _showAuthDialog.asStateFlow()

    private val _authPromptReason = MutableStateFlow("")
    val authPromptReason: StateFlow<String> = _authPromptReason.asStateFlow()

    private var pendingAuthAction: (() -> Unit)? = null

    fun openAuthDialog(reason: String) {
        _authPromptReason.value = reason
        _showAuthDialog.value = true
    }

    fun closeAuthDialog() {
        _showAuthDialog.value = false
    }

    fun requireAuth(reason: String, action: () -> Unit): Boolean {
        if (isLoggedIn.value) {
            action()
            return true
        } else {
            pendingAuthAction = action
            openAuthDialog(reason)
            return false
        }
    }

    fun registerUser(fullName: String, username: String, email: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.registerUser(fullName, username, email, password)
            if (success) {
                closeAuthDialog()
                pendingAuthAction?.invoke()
                pendingAuthAction = null
            }
            onResult(success)
        }
    }

    fun loginUser(emailOrUsername: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.loginUser(emailOrUsername, password)
            if (success) {
                closeAuthDialog()
                pendingAuthAction?.invoke()
                pendingAuthAction = null
            }
            onResult(success)
        }
    }

    fun logoutUser() {
        viewModelScope.launch {
            repository.logoutUser()
        }
    }

    // Quotes State
    val allQuotes: StateFlow<List<QuoteEntity>> = repository.allQuotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteQuotes: StateFlow<List<QuoteEntity>> = repository.favoriteQuotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val quoteOfTheDay: StateFlow<QuoteEntity?> = allQuotes
        .map { quotes -> if (quotes.isNotEmpty()) quotes.first() else null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun toggleQuoteLike(quote: QuoteEntity) {
        requireAuth("pour ajouter une citation à vos favoris") {
            viewModelScope.launch {
                repository.toggleQuoteLike(quote)
            }
        }
    }

    fun addQuote(text: String, author: String, category: String) {
        requireAuth("pour ajouter une citation") {
            viewModelScope.launch {
                repository.addQuote(text, author, category)
            }
        }
    }

    fun deleteQuote(quoteId: Long) {
        viewModelScope.launch {
            repository.deleteQuote(quoteId)
        }
    }

    // Quiz State
    private val _currentQuizState = MutableStateFlow<QuizPlayState>(QuizPlayState.Idle)
    val currentQuizState: StateFlow<QuizPlayState> = _currentQuizState.asStateFlow()

    val quizResults: StateFlow<List<QuizResultEntity>> = repository.quizResults
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun startNewQuiz(theme: String = "Tous", count: Int = 10, difficulty: String = "Adaptatif") {
        requireAuth("pour jouer aux quizz") {
            viewModelScope.launch {
                _currentQuizState.value = QuizPlayState.Loading
                try {
                    val userLevel = loggedInUser.value?.level ?: 1
                    val userDiff = if (difficulty == "Adaptatif") {
                        when (userLevel) {
                            1 -> "Facile"
                            2 -> "Moyen"
                            else -> "Difficile"
                        }
                    } else difficulty

                    val questions = repository.generateQuiz(theme, count, userDiff)
                    _currentQuizState.value = QuizPlayState.InProgress(
                        theme = theme,
                        questions = questions,
                        currentIndex = 0,
                        score = 0,
                        correctAnswersCount = 0,
                        selectedOptionIndex = null,
                        isAnswerSubmitted = false
                    )
                } catch (e: Exception) {
                    _currentQuizState.value = QuizPlayState.Error("Échec du chargement du quizz. Réessayez.")
                }
            }
        }
    }

    fun submitAnswer(optionIndex: Int) {
        val state = _currentQuizState.value
        if (state is QuizPlayState.InProgress && !state.isAnswerSubmitted) {
            val currentQuestion = state.questions[state.currentIndex]
            val isCorrect = optionIndex == currentQuestion.correctAnswerIndex
            val addedPoints = if (isCorrect) 10 else 0
            val newScore = state.score + addedPoints
            val newCorrectCount = if (isCorrect) state.correctAnswersCount + 1 else state.correctAnswersCount

            _currentQuizState.value = state.copy(
                selectedOptionIndex = optionIndex,
                isAnswerSubmitted = true,
                score = newScore,
                correctAnswersCount = newCorrectCount
            )
        }
    }

    fun nextQuestion() {
        val state = _currentQuizState.value
        if (state is QuizPlayState.InProgress && state.isAnswerSubmitted) {
            if (state.currentIndex + 1 < state.questions.size) {
                _currentQuizState.value = state.copy(
                    currentIndex = state.currentIndex + 1,
                    selectedOptionIndex = null,
                    isAnswerSubmitted = false
                )
            } else {
                val totalQuestions = state.questions.size
                viewModelScope.launch {
                    repository.saveQuizResult(
                        theme = state.theme,
                        score = state.score,
                        totalQuestions = totalQuestions,
                        correctAnswers = state.correctAnswersCount
                    )
                }
                _currentQuizState.value = QuizPlayState.Completed(
                    theme = state.theme,
                    questions = state.questions,
                    finalScore = state.score,
                    totalQuestions = totalQuestions,
                    correctAnswersCount = state.correctAnswersCount
                )
            }
        }
    }

    fun resetQuizState() {
        _currentQuizState.value = QuizPlayState.Idle
    }

    // AI Chat & Sessions State
    val chatSessions: StateFlow<List<ChatSessionEntity>> = repository.chatSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeSessionId = MutableStateFlow<Long>(1)
    val activeSessionId: StateFlow<Long> = _activeSessionId.asStateFlow()

    val chatMessages: StateFlow<List<ChatMessageEntity>> = _activeSessionId.flatMapLatest { sessionId ->
        repository.getMessagesForSession(sessionId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    fun selectChatSession(sessionId: Long) {
        _activeSessionId.value = sessionId
    }

    fun createNewChatSession(title: String = "Nouvelle discussion") {
        requireAuth("pour démarrer une nouvelle discussion avec l'IA") {
            viewModelScope.launch {
                val newId = repository.createNewChatSession(title)
                _activeSessionId.value = newId
            }
        }
    }

    fun sendChatMessage(userText: String) {
        if (userText.isBlank()) return
        requireAuth("pour écrire à l'assistant cdj_ia") {
            viewModelScope.launch {
                _isAiThinking.value = true
                try {
                    repository.sendChatMessage(userText, _activeSessionId.value)
                } finally {
                    _isAiThinking.value = false
                }
            }
        }
    }

    fun discussQuoteWithAi(quoteText: String, author: String, onNavigateToAi: () -> Unit) {
        requireAuth("pour discuter d'une citation avec cdj_ia") {
            val prompt = "Peux-tu m'expliquer et analyser en profondeur cette citation de $author : « $quoteText » ?"
            viewModelScope.launch {
                onNavigateToAi()
                sendChatMessage(prompt)
            }
        }
    }

    fun clearChatHistory() {
        viewModelScope.launch {
            repository.clearChatHistory()
        }
    }

    // Direct Messages & Contacts State
    val allContacts: StateFlow<List<ContactEntity>> = repository.allContacts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val filteredContacts: StateFlow<List<ContactEntity>> = combine(allContacts, searchQuery) { contacts, query ->
        if (query.isBlank()) contacts
        else contacts.filter {
            it.username.contains(query, ignoreCase = true) ||
            it.displayName.contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun getDirectMessages(contactId: Long): Flow<List<DirectMessageEntity>> {
        return repository.getDirectMessages(contactId)
    }

    fun sendDirectMessage(contactId: Long, senderName: String, text: String) {
        if (text.isBlank()) return
        requireAuth("pour envoyer des messages") {
            viewModelScope.launch {
                val currentUserName = loggedInUser.value?.fullName ?: senderName
                repository.sendDirectMessage(contactId, currentUserName, text)
            }
        }
    }

    fun editDirectMessage(messageId: Long, newContent: String) {
        if (newContent.isBlank()) return
        viewModelScope.launch {
            repository.editDirectMessage(messageId, newContent)
        }
    }

    fun addNewContact(username: String, displayName: String, bio: String) {
        viewModelScope.launch {
            repository.addNewContact(username, displayName, bio)
        }
    }

    // User Profile & Settings
    val userProfile: StateFlow<UserProfileEntity?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun updateProfile(username: String, displayName: String, bio: String, themeMode: String, notificationsEnabled: Boolean) {
        viewModelScope.launch {
            repository.updateProfile(username, displayName, bio, themeMode, notificationsEnabled)
        }
    }
}

sealed class QuizPlayState {
    object Idle : QuizPlayState()
    object Loading : QuizPlayState()
    data class InProgress(
        val theme: String,
        val questions: List<QuizQuestion>,
        val currentIndex: Int,
        val score: Int,
        val correctAnswersCount: Int,
        val selectedOptionIndex: Int?,
        val isAnswerSubmitted: Boolean
    ) : QuizPlayState()
    data class Completed(
        val theme: String,
        val questions: List<QuizQuestion>,
        val finalScore: Int,
        val totalQuestions: Int,
        val correctAnswersCount: Int
    ) : QuizPlayState()
    data class Error(val message: String) : QuizPlayState()
}

