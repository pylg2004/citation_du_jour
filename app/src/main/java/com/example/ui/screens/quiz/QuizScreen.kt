package com.example.ui.screens.quiz

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.QuizQuestion
import com.example.data.model.QuizResultEntity
import com.example.ui.viewmodel.QuizPlayState

@Composable
fun QuizScreen(
    quizState: QuizPlayState,
    quizHistory: List<QuizResultEntity>,
    onStartQuiz: (theme: String, count: Int, difficulty: String) -> Unit,
    onSubmitAnswer: (optionIndex: Int) -> Unit,
    onNextQuestion: () -> Unit,
    onResetQuiz: () -> Unit
) {
    when (quizState) {
        is QuizPlayState.Idle -> {
            QuizSetupView(onStartQuiz = onStartQuiz)
        }
        is QuizPlayState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Préparation du quizz sur les citations...",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Sélection de 10 questions de la base...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
        is QuizPlayState.InProgress -> {
            QuizPlayView(
                state = quizState,
                onSubmitAnswer = onSubmitAnswer,
                onNextQuestion = onNextQuestion
            )
        }
        is QuizPlayState.Completed -> {
            QuizResultView(
                state = quizState,
                onRestart = onResetQuiz
            )
        }
        is QuizPlayState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = quizState.message,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(onClick = onResetQuiz) {
                        Text("Réessayer")
                    }
                }
            }
        }
    }
}

@Composable
fun QuizSetupView(
    onStartQuiz: (theme: String, count: Int, difficulty: String) -> Unit
) {
    val themes = listOf(
        Pair("Tous (Mélangé)", Icons.Default.Casino),
        Pair("Inspiration", Icons.Default.AutoAwesome),
        Pair("Philosophie", Icons.Default.Psychology),
        Pair("Motivation", Icons.Default.FitnessCenter),
        Pair("Amour", Icons.Default.Favorite),
        Pair("Sagesse", Icons.Default.MenuBook),
        Pair("Science", Icons.Default.Science),
        Pair("Stoïcisme", Icons.Default.SelfImprovement)
    )

    var selectedTheme by remember { mutableStateOf("Tous (Mélangé)") }
    var questionCount by remember { mutableStateOf(10) }
    var selectedDifficulty by remember { mutableStateOf("Adaptatif") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Banner Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                Color(0xFFFF8F00)
                            )
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FormatQuote,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "QUIZZ DES CITATIONS",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "Testez vos connaissances sur plus de 10 000 citations",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        )
                    }
                }
            }
        }

        // Theme Selection
        item {
            Column {
                Text(
                    text = "1. SÉLECTIONNER UN THÈME",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    themes.chunked(2).forEach { rowThemes ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowThemes.forEach { (themeName, icon) ->
                                val isSelected = selectedTheme == themeName
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(56.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable { selectedTheme = themeName }
                                        .testTag("theme_${themeName.lowercase()}"),
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = themeName,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Question Count Selection
        item {
            Column {
                Text(
                    text = "2. NOMBRE DE QUESTIONS",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    listOf(5, 10, 15, 20).forEach { count ->
                        val isSelected = questionCount == count
                        Button(
                            onClick = { questionCount = count },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Text("$count Qs", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Difficulty Selection
        item {
            Column {
                Text(
                    text = "3. NIVEAU DE DIFFICULTÉ",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    listOf("Adaptatif", "Facile", "Moyen", "Difficile").forEach { diff ->
                        val isSelected = selectedDifficulty == diff
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedDifficulty = diff },
                            label = { Text(diff, modifier = Modifier.padding(vertical = 4.dp)) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }
        }

        // Start Quiz Button
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    val actualTheme = if (selectedTheme.contains("Mélangé") || selectedTheme == "Tous (Mélangé)") "Tous" else selectedTheme
                    onStartQuiz(actualTheme, questionCount, selectedDifficulty)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("start_quiz_btn"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Jouer le Quizz ($questionCount Questions)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun QuizPlayView(
    state: QuizPlayState.InProgress,
    onSubmitAnswer: (Int) -> Unit,
    onNextQuestion: () -> Unit
) {
    val currentQuestion = state.questions[state.currentIndex]
    val totalQuestions = state.questions.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            // Header Progress Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Question ${state.currentIndex + 1} / $totalQuestions",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${state.score} pts",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = (state.currentIndex + 1).toFloat() / totalQuestions,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Question Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Thème: ${state.theme}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = currentQuestion.question,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            lineHeight = 28.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 4 Option Cards
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                currentQuestion.options.forEachIndexed { index, option ->
                    val isSelected = state.selectedOptionIndex == index
                    val isCorrect = index == currentQuestion.correctAnswerIndex

                    val cardColor = when {
                        !state.isAnswerSubmitted -> {
                            if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        }
                        isCorrect -> Color(0xFF1B5E20)
                        isSelected && !isCorrect -> Color(0xFFB71C1C)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }

                    val textColor = when {
                        !state.isAnswerSubmitted -> MaterialTheme.colorScheme.onSurface
                        isCorrect -> Color.White
                        isSelected && !isCorrect -> Color.White
                        else -> MaterialTheme.colorScheme.onSurface
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable(enabled = !state.isAnswerSubmitted) {
                                onSubmitAnswer(index)
                            }
                            .testTag("quiz_option_$index"),
                        color = cardColor,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = ('A' + index).toString(),
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = textColor
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = option,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = textColor
                                ),
                                modifier = Modifier.weight(1f)
                            )

                            if (state.isAnswerSubmitted) {
                                if (isCorrect) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = "Correct", tint = Color.White)
                                } else if (isSelected) {
                                    Icon(Icons.Default.Cancel, contentDescription = "Faux", tint = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            // Explanation box after submit
            if (state.isAnswerSubmitted) {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = currentQuestion.explanation,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
            }
        }

        // Action Button at bottom
        if (state.isAnswerSubmitted) {
            Button(
                onClick = onNextQuestion,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("next_question_btn"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = if (state.currentIndex + 1 < totalQuestions) "Question Suivante" else "Voir les Résultats",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.ArrowForward, contentDescription = null)
            }
        } else {
            Spacer(modifier = Modifier.height(52.dp))
        }
    }
}

@Composable
fun QuizResultView(
    state: QuizPlayState.Completed,
    onRestart: () -> Unit
) {
    val percentage = ((state.correctAnswersCount.toDouble() / state.totalQuestions) * 100).toInt()

    val badgeTitle = when {
        percentage == 100 -> "🏆 Grand Maître de la Culture !"
        percentage >= 80 -> "🌟 Expert Érudit !"
        percentage >= 50 -> "👍 Bon Connaisseur !"
        else -> "📚 Apprenti Curieux"
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFF8F00),
                                MaterialTheme.colorScheme.primary
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(64.dp)
                )
            }
        }

        item {
            Text(
                text = badgeTitle,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            )
            Text(
                text = "Quizz: ${state.theme}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Score Total", style = MaterialTheme.typography.labelSmall)
                        Text(
                            text = "${state.finalScore} pts",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Réussite", style = MaterialTheme.typography.labelSmall)
                        Text(
                            text = "$percentage %",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }

        item {
            Button(
                onClick = onRestart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("restart_quiz_btn"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Lancer un autre Quizz", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
        }

        item {
            Text(
                text = "RÉCAPITULATIF DES QUESTIONS",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                ),
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        items(state.questions) { q ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = q.question,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Bonne réponse: ${q.options[q.correctAnswerIndex]}",
                        style = MaterialTheme.typography.labelMedium.copy(color = Color(0xFF4CAF50))
                    )
                    Text(
                        text = q.explanation,
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }
        }
    }
}
