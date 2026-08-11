package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.UserProfileEntity
import com.example.ui.components.*
import com.example.ui.screens.ai.AiChatScreen
import com.example.ui.screens.drawer.FavoritesScreen
import com.example.ui.screens.drawer.ProfileScreen
import com.example.ui.screens.drawer.SettingsScreen
import com.example.ui.screens.home.AllQuotesScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.messages.MessagesScreen
import com.example.ui.screens.quiz.QuizScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()

            val isDarkTheme = when (userProfile?.themeMode) {
                "DARK" -> true
                "LIGHT" -> false
                else -> false // Default to white background
            }

            MyApplicationTheme(darkTheme = isDarkTheme) {
                MainAppContent(
                    viewModel = viewModel,
                    userProfile = userProfile
                )
            }
        }
    }
}

enum class ActiveScreen {
    MAIN_TABS,
    ALL_QUOTES,
    DRAWER_PROFILE,
    DRAWER_FAVORITES,
    DRAWER_SETTINGS
}

@Composable
fun MainAppContent(
    viewModel: MainViewModel,
    userProfile: UserProfileEntity?
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    var currentTab by remember { mutableStateOf(BottomTab.HOME) }
    var activeScreen by remember { mutableStateOf(ActiveScreen.MAIN_TABS) }

    // State collections
    val allQuotes by viewModel.allQuotes.collectAsStateWithLifecycle()
    val favoriteQuotes by viewModel.favoriteQuotes.collectAsStateWithLifecycle()
    val quoteOfTheDay by viewModel.quoteOfTheDay.collectAsStateWithLifecycle()

    val quizState by viewModel.currentQuizState.collectAsStateWithLifecycle()
    val quizHistory by viewModel.quizResults.collectAsStateWithLifecycle()

    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val chatSessions by viewModel.chatSessions.collectAsStateWithLifecycle()
    val activeSessionId by viewModel.activeSessionId.collectAsStateWithLifecycle()
    val isAiThinking by viewModel.isAiThinking.collectAsStateWithLifecycle()

    val filteredContacts by viewModel.filteredContacts.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    val showAuthDialog by viewModel.showAuthDialog.collectAsStateWithLifecycle()
    val authPromptReason by viewModel.authPromptReason.collectAsStateWithLifecycle()
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()

    if (showAuthDialog) {
        AuthDialog(
            promptReason = authPromptReason,
            onDismiss = { viewModel.closeAuthDialog() },
            onLogin = { email, pass, onResult -> viewModel.loginUser(email, pass, onResult) },
            onRegister = { fullName, username, email, pass, onResult -> viewModel.registerUser(fullName, username, email, pass, onResult) }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = activeScreen == ActiveScreen.MAIN_TABS,
        drawerContent = {
            AppDrawerContent(
                userProfile = userProfile,
                isLoggedIn = isLoggedIn,
                onDestinationSelected = { dest ->
                    activeScreen = when (dest) {
                        DrawerDestination.PROFILE -> ActiveScreen.DRAWER_PROFILE
                        DrawerDestination.FAVORITES -> ActiveScreen.DRAWER_FAVORITES
                        DrawerDestination.SETTINGS -> ActiveScreen.DRAWER_SETTINGS
                    }
                },
                onLoginRequested = { viewModel.openAuthDialog("pour accéder à toutes les fonctionnalités") },
                onLogoutRequested = { viewModel.logoutUser() },
                onCloseDrawer = {
                    coroutineScope.launch { drawerState.close() }
                }
            )
        }
    ) {
        when (activeScreen) {
            ActiveScreen.ALL_QUOTES -> {
                AllQuotesScreen(
                    quotes = allQuotes,
                    onToggleLike = { viewModel.toggleQuoteLike(it) },
                    onDeleteQuote = { viewModel.deleteQuote(it) },
                    onDiscussWithAi = { quote ->
                        currentTab = BottomTab.AI
                        activeScreen = ActiveScreen.MAIN_TABS
                        viewModel.sendChatMessage("Explique cette citation de ${quote.author} : « ${quote.text} »")
                    },
                    onBackClick = { activeScreen = ActiveScreen.MAIN_TABS }
                )
            }

            ActiveScreen.DRAWER_PROFILE -> {
                ProfileScreen(
                    userProfile = userProfile,
                    isLoggedIn = isLoggedIn,
                    favoriteCount = favoriteQuotes.size,
                    quizCount = quizHistory.size,
                    onUpdateProfile = { username, displayName, bio, themeMode, notificationsEnabled ->
                        viewModel.updateProfile(username, displayName, bio, themeMode, notificationsEnabled)
                    },
                    onLogout = { viewModel.logoutUser() },
                    onLoginRequested = { viewModel.openAuthDialog("pour accéder à toutes les fonctionnalités") },
                    onBack = { activeScreen = ActiveScreen.MAIN_TABS }
                )
            }

            ActiveScreen.DRAWER_FAVORITES -> {
                FavoritesScreen(
                    favoriteQuotes = favoriteQuotes,
                    onToggleLike = { viewModel.toggleQuoteLike(it) },
                    onBack = { activeScreen = ActiveScreen.MAIN_TABS }
                )
            }

            ActiveScreen.DRAWER_SETTINGS -> {
                SettingsScreen(
                    userProfile = userProfile,
                    onUpdateSettings = { themeMode, notificationsEnabled ->
                        userProfile?.let {
                            viewModel.updateProfile(
                                it.username,
                                it.displayName,
                                it.bio,
                                themeMode,
                                notificationsEnabled
                            )
                        }
                    },
                    onBack = { activeScreen = ActiveScreen.MAIN_TABS }
                )
            }

            ActiveScreen.MAIN_TABS -> {
                Scaffold(
                    topBar = {
                        TopHeaderBar(
                            title = when (currentTab) {
                                BottomTab.HOME -> "Citations"
                                BottomTab.QUIZ -> "Quizz Citations"
                                BottomTab.AI -> "cdj_ia"
                                BottomTab.MESSAGES -> "Messagerie"
                            },
                            onOpenDrawer = {
                                coroutineScope.launch { drawerState.open() }
                            },
                            userProfile = userProfile,
                            onProfileClick = { activeScreen = ActiveScreen.DRAWER_PROFILE }
                        )
                    },
                    bottomBar = {
                        AppBottomNavigationBar(
                            currentTab = currentTab,
                            onTabSelected = { tab -> currentTab = tab }
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentTab) {
                            BottomTab.HOME -> {
                                HomeScreen(
                                    quoteOfTheDay = quoteOfTheDay,
                                    allQuotes = allQuotes,
                                    onToggleLike = { viewModel.toggleQuoteLike(it) },
                                    onNavigateToAllQuotes = { activeScreen = ActiveScreen.ALL_QUOTES },
                                    onNavigateToFavorites = { activeScreen = ActiveScreen.DRAWER_FAVORITES },
                                    onDiscussWithAi = { quote ->
                                        currentTab = BottomTab.AI
                                        viewModel.sendChatMessage("Analyse et explique cette citation de ${quote.author} : « ${quote.text} »")
                                    }
                                )
                            }

                            BottomTab.QUIZ -> {
                                QuizScreen(
                                    quizState = quizState,
                                    quizHistory = quizHistory,
                                    onStartQuiz = { theme, count, diff ->
                                        viewModel.startNewQuiz(theme, count, diff)
                                    },
                                    onSubmitAnswer = { optionIndex ->
                                        viewModel.submitAnswer(optionIndex)
                                    },
                                    onNextQuestion = {
                                        viewModel.nextQuestion()
                                    },
                                    onResetQuiz = {
                                        viewModel.resetQuizState()
                                    }
                                )
                            }

                            BottomTab.AI -> {
                                AiChatScreen(
                                    chatMessages = chatMessages,
                                    chatSessions = chatSessions,
                                    activeSessionId = activeSessionId,
                                    isThinking = isAiThinking,
                                    onSendMessage = { text ->
                                        viewModel.sendChatMessage(text)
                                    },
                                    onSelectSession = { sessionId ->
                                        viewModel.selectChatSession(sessionId)
                                    },
                                    onCreateNewSession = {
                                        viewModel.createNewChatSession()
                                    },
                                    onClearHistory = {
                                        viewModel.clearChatHistory()
                                    }
                                )
                            }

                            BottomTab.MESSAGES -> {
                                MessagesScreen(
                                    contacts = filteredContacts,
                                    searchQuery = searchQuery,
                                    onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                                    onGetMessages = { contactId -> viewModel.getDirectMessages(contactId) },
                                    onSendMessage = { contactId, text ->
                                        userProfile?.let { prof ->
                                            viewModel.sendDirectMessage(contactId, prof.displayName, text)
                                        }
                                    },
                                    onEditMessage = { messageId, newContent ->
                                        viewModel.editDirectMessage(messageId, newContent)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
