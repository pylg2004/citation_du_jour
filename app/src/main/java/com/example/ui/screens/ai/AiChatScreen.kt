package com.example.ui.screens.ai

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessageEntity
import com.example.data.model.ChatSessionEntity
import com.example.ui.components.MarkdownText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiChatScreen(
    chatMessages: List<ChatMessageEntity>,
    chatSessions: List<ChatSessionEntity> = emptyList(),
    activeSessionId: Long = 1,
    isThinking: Boolean,
    onSendMessage: (String) -> Unit,
    onSelectSession: (Long) -> Unit = {},
    onCreateNewSession: () -> Unit = {},
    onClearHistory: () -> Unit = {}
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val context = LocalContext.current
    var showSessionMenu by remember { mutableStateOf(false) }

    val quickPrompts = listOf(
        "Donne-moi une citation motivante",
        "Explique la philosophie du Stoïcisme",
        "Pose-moi un mini-quizz de 3 questions",
        "Écris un poème sur la persévérance"
    )

    LaunchedEffect(chatMessages.size, isThinking) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { showSessionMenu = !showSessionMenu }
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Assistant cdj_ia", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                            val currentTitle = chatSessions.find { it.id == activeSessionId }?.title ?: "Discussion"
                            Text(
                                text = currentTitle,
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary)
                            )
                        }

                        DropdownMenu(
                            expanded = showSessionMenu,
                            onDismissRequest = { showSessionMenu = false }
                        ) {
                            Text(
                                text = "HISTORIQUE DES DISCUSSIONS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                            DropdownMenuItem(
                                text = { Text("+ Nouvelle discussion", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                                onClick = {
                                    showSessionMenu = false
                                    onCreateNewSession()
                                },
                                leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) }
                            )
                            Divider()
                            chatSessions.forEach { session ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = session.title,
                                            fontWeight = if (session.id == activeSessionId) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        showSessionMenu = false
                                        onSelectSession(session.id)
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.ChatBubbleOutline,
                                            contentDescription = null,
                                            tint = if (session.id == activeSessionId) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = onCreateNewSession,
                        modifier = Modifier.testTag("btn_new_chat")
                    ) {
                        Icon(Icons.Default.AddComment, contentDescription = "Nouveau chat", tint = MaterialTheme.colorScheme.primary)
                    }
                    if (chatMessages.isNotEmpty()) {
                        IconButton(onClick = onClearHistory) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "Effacer l'historique")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Chat Messages List
            if (chatMessages.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = "cdj_ia",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Bonjour ! Je suis cdj_ia, votre assistant IA.",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Posez-moi des questions sur les citations, la philosophie, les langues ou le développement !",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }

                    items(chatMessages, key = { it.id }) { msg ->
                        val isUser = msg.sender == "USER"
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
                        ) {
                            Row(
                                horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(
                                        topStart = 18.dp,
                                        topEnd = 18.dp,
                                        bottomStart = if (isUser) 18.dp else 4.dp,
                                        bottomEnd = if (isUser) 4.dp else 18.dp
                                    ),
                                    color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.widthIn(max = 300.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        if (!isUser) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "cdj_ia",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                )
                                                IconButton(
                                                    onClick = {
                                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                                        val clip = android.content.ClipData.newPlainText("IA Message", msg.content)
                                                        clipboard.setPrimaryClip(clip)
                                                        Toast.makeText(context, "Texte copié !", Toast.LENGTH_SHORT).show()
                                                    },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.ContentCopy,
                                                        contentDescription = "Copier",
                                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            MarkdownText(text = msg.content)
                                        } else {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Spacer(modifier = Modifier.weight(1f))
                                                IconButton(
                                                    onClick = {
                                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                                        val clip = android.content.ClipData.newPlainText("Message", msg.content)
                                                        clipboard.setPrimaryClip(clip)
                                                        Toast.makeText(context, "Texte copié !", Toast.LENGTH_SHORT).show()
                                                    },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.ContentCopy,
                                                        contentDescription = "Copier",
                                                        tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = msg.content,
                                                style = MaterialTheme.typography.bodyLarge.copy(
                                                    color = MaterialTheme.colorScheme.onPrimary,
                                                    fontSize = 15.sp,
                                                    lineHeight = 22.sp
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (isThinking) {
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "cdj_ia réfléchit...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }
            }

            // Prompt Suggestions Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(quickPrompts) { prompt ->
                    SuggestionChip(
                        onClick = { onSendMessage(prompt) },
                        label = { Text(prompt, style = MaterialTheme.typography.labelMedium) },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Input TextField Bar
            Surface(
                tonalElevation = 6.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("ai_chat_input"),
                        placeholder = { Text("Posez votre question à cdj_ia...") },
                        maxLines = 4,
                        shape = RoundedCornerShape(20.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                val text = inputText
                                inputText = ""
                                onSendMessage(text)
                            }
                        },
                        enabled = inputText.isNotBlank() && !isThinking,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                if (inputText.isNotBlank()) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .testTag("ai_send_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Envoyer",
                            tint = if (inputText.isNotBlank()) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
