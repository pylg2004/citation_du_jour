package com.example.ui.screens.messages

import android.content.Context
import android.widget.Toast
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ContactEntity
import com.example.data.model.DirectMessageEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(
    contacts: List<ContactEntity>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onGetMessages: (contactId: Long) -> Flow<List<DirectMessageEntity>>,
    onSendMessage: (contactId: Long, text: String) -> Unit,
    onEditMessage: (messageId: Long, newContent: String) -> Unit = { _, _ -> }
) {
    var selectedContact by remember { mutableStateOf<ContactEntity?>(null) }

    if (selectedContact != null) {
        val activeContact = selectedContact!!
        val messagesFlow = remember(activeContact.id) { onGetMessages(activeContact.id) }
        val messagesList by messagesFlow.collectAsState(initial = emptyList())

        ChatDetailScreen(
            contact = activeContact,
            messages = messagesList,
            onBack = { selectedContact = null },
            onSendMessage = { text -> onSendMessage(activeContact.id, text) },
            onEditMessage = onEditMessage
        )
    } else {
        Scaffold { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("messages_search_input"),
                    placeholder = { Text("Chercher par nom d'utilisateur (@sophie)...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Effacer")
                            }
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (contacts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Forum,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Aucune conversation trouvée.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                } else {
                    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(contacts, key = { it.id }) { contact ->
                            val color = try {
                                Color(android.graphics.Color.parseColor(contact.avatarColorHex))
                            } catch (e: Exception) {
                                MaterialTheme.colorScheme.primary
                            }

                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedContact = contact }
                                    .testTag("contact_item_${contact.username}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box {
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(CircleShape)
                                                .background(color),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = contact.displayName.take(1).uppercase(),
                                                style = MaterialTheme.typography.titleLarge.copy(
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                        }

                                        if (contact.statusOnline) {
                                            Box(
                                                modifier = Modifier
                                                    .size(12.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF4CAF50))
                                                    .align(Alignment.BottomEnd)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = contact.displayName,
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )

                                            Text(
                                                text = dateFormat.format(Date(contact.lastMessageTime)),
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(2.dp))

                                        Text(
                                            text = "@${contact.username}",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = contact.lastMessage,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                            ),
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    contact: ContactEntity,
    messages: List<DirectMessageEntity>,
    onBack: () -> Unit,
    onSendMessage: (String) -> Unit,
    onEditMessage: (messageId: Long, newContent: String) -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val context = LocalContext.current

    var editingMessage by remember { mutableStateOf<DirectMessageEntity?>(null) }
    var editInputText by remember { mutableStateOf("") }

    val avatarColor = try {
        Color(android.graphics.Color.parseColor(contact.avatarColorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    if (editingMessage != null) {
        AlertDialog(
            onDismissRequest = { editingMessage = null },
            title = { Text("Modifier le message") },
            text = {
                OutlinedTextField(
                    value = editInputText,
                    onValueChange = { editInputText = it },
                    label = { Text("Nouveau message") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editInputText.isNotBlank()) {
                            onEditMessage(editingMessage!!.id, editInputText)
                            Toast.makeText(context, "Message modifié !", Toast.LENGTH_SHORT).show()
                            editingMessage = null
                        }
                    }
                ) {
                    Text("Enregistrer")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingMessage = null }) {
                    Text("Annuler")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(avatarColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = contact.displayName.take(1).uppercase(),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = contact.displayName,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "@${contact.username} • En ligne",
                                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF4CAF50))
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
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
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }

                items(messages, key = { it.id }) { msg ->
                    val isMe = msg.isFromMe
                    val fifteenMinutesMs = 15 * 60 * 1000L
                    val canEdit = isMe && (System.currentTimeMillis() - msg.timestamp <= fifteenMinutesMs)

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
                    ) {
                        Surface(
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (isMe) 16.dp else 4.dp,
                                bottomEnd = if (isMe) 4.dp else 16.dp
                            ),
                            color = if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.widthIn(max = 290.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = msg.content,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        color = if (isMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (canEdit) {
                                        Text(
                                            text = "Modifier (15mn)",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                                                fontWeight = FontWeight.Bold
                                            ),
                                            modifier = Modifier.clickable {
                                                editingMessage = msg
                                                editInputText = msg.content
                                            }
                                        )
                                    } else {
                                        Spacer(modifier = Modifier.width(1.dp))
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (msg.isEdited) {
                                            Text(
                                                text = "(modifié) ",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontSize = 10.sp,
                                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                                    color = (if (isMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant).copy(alpha = 0.7f)
                                                )
                                            )
                                        }
                                        Text(
                                            text = dateFormat.format(Date(msg.timestamp)),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 10.sp,
                                                color = (if (isMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant).copy(alpha = 0.7f)
                                            )
                                        )
                                        if (isMe) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Default.DoneAll,
                                                contentDescription = "Vu",
                                                tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }
            }

            // Text Field
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
                            .testTag("dm_input"),
                        placeholder = { Text("Écrivez un message...") },
                        shape = RoundedCornerShape(20.dp),
                        singleLine = true
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
                        enabled = inputText.isNotBlank(),
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                if (inputText.isNotBlank()) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .testTag("dm_send_btn")
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
