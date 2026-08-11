package com.example.ui.screens.home

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.QuoteEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    quoteOfTheDay: QuoteEntity?,
    allQuotes: List<QuoteEntity>,
    onToggleLike: (QuoteEntity) -> Unit,
    onNavigateToAllQuotes: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onDiscussWithAi: (QuoteEntity) -> Unit = {}
) {
    val context = LocalContext.current

    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Banner Hero Header
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Transparent,
                    modifier = Modifier.fillMaxWidth()
                ) {
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
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.FormatQuote,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "CITATION DU JOUR",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = Color.White.copy(alpha = 0.9f),
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.2.sp
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))

                            if (quoteOfTheDay != null) {
                                Text(
                                    text = "« ${quoteOfTheDay.text} »",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = Color.White,
                                        fontStyle = FontStyle.Italic,
                                        fontSize = 17.sp,
                                        lineHeight = 24.sp
                                    )
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "— ${quoteOfTheDay.author}",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = Color.White.copy(alpha = 0.95f),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = { onToggleLike(quoteOfTheDay) },
                                            modifier = Modifier.testTag("like_quote_day")
                                        ) {
                                            Icon(
                                                imageVector = if (quoteOfTheDay.isLiked) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                                contentDescription = "J'aime",
                                                tint = if (quoteOfTheDay.isLiked) Color(0xFFFF5252) else Color.White
                                            )
                                        }

                                        IconButton(onClick = { onDiscussWithAi(quoteOfTheDay) }) {
                                            Icon(
                                                imageVector = Icons.Default.AutoAwesome,
                                                contentDescription = "Discuter avec l'IA",
                                                tint = Color.White
                                            )
                                        }

                                        IconButton(onClick = { shareQuote(context, quoteOfTheDay) }) {
                                            Icon(
                                                imageVector = Icons.Default.Share,
                                                contentDescription = "Partager",
                                                tint = Color.White
                                            )
                                        }
                                    }
                                }
                            } else {
                                CircularProgressIndicator(color = Color.White)
                            }
                        }
                    }
                }
            }

            // Action Buttons Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onNavigateToAllQuotes,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("btn_all_quotes"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.FormatListBulleted,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Toutes les citations", fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = onNavigateToFavorites,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("btn_favorites"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Mes Favoris", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Quick Category Filters
            item {
                Column {
                    Text(
                        text = "EXPLORER PAR THÈME",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    val categories = listOf("Inspiration", "Philosophie", "Motivation", "Amour", "Sagesse", "Science")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(categories) { category ->
                            FilterChip(
                                selected = false,
                                onClick = onNavigateToAllQuotes,
                                label = { Text(category) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Tag,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }
            }

            // Recent Feed Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CITATIONS RÉCENTES",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                    )
                    TextButton(onClick = onNavigateToAllQuotes) {
                        Text("Voir tout (${allQuotes.size})")
                    }
                }
            }

            // Quotes Cards List
            items(allQuotes.take(6)) { quote ->
                QuoteCardItem(
                    quote = quote,
                    onToggleLike = { onToggleLike(quote) },
                    onShare = { shareQuote(context, quote) },
                    onDiscussWithAi = { onDiscussWithAi(quote) }
                )
            }

            item { Spacer(modifier = Modifier.height(72.dp)) }
        }
    }
}

@Composable
fun QuoteCardItem(
    quote: QuoteEntity,
    onToggleLike: () -> Unit,
    onShare: () -> Unit,
    onDiscussWithAi: () -> Unit = {}
) {
    val context = LocalContext.current

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("quote_card_${quote.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = quote.category,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Copy button
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            val clip = android.content.ClipData.newPlainText("Citation", "« ${quote.text} » — ${quote.author}")
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Citation copiée !", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copier",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // IA Button
                    Button(
                        onClick = onDiscussWithAi,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("IA", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "« ${quote.text} »",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontStyle = FontStyle.Italic,
                    lineHeight = 22.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "— ${quote.author}",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { onToggleLike() }
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = if (quote.isLiked) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "J'aime",
                        tint = if (quote.isLiked) Color(0xFFFF5252) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${quote.likesCount}",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }

                IconButton(onClick = onShare) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Partager",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AddQuoteDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var text by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Inspiration") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajouter une citation", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Texte de la citation") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 4
                )
                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it },
                    label = { Text("Auteur (ex: Victor Hugo)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Thème / Catégorie") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (text.isNotBlank()) {
                        onConfirm(text, author, category)
                    }
                },
                enabled = text.isNotBlank()
            ) {
                Text("Ajouter")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

fun shareQuote(context: Context, quote: QuoteEntity) {
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, "« ${quote.text} » — ${quote.author} (via Citations & Quizz AI)")
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Partager la citation")
    context.startActivity(shareIntent)
}
