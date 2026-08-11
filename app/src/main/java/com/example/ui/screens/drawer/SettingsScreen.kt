package com.example.ui.screens.drawer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.UserProfileEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    userProfile: UserProfileEntity?,
    onUpdateSettings: (themeMode: String, notificationsEnabled: Boolean) -> Unit,
    onBack: () -> Unit
) {
    val profile = userProfile ?: UserProfileEntity()

    var themeMode by remember(profile.themeMode) { mutableStateOf(profile.themeMode) }
    var notificationsEnabled by remember(profile.notificationsEnabled) { mutableStateOf(profile.notificationsEnabled) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Paramètres", fontWeight = FontWeight.Bold) },
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
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Appearance Theme Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Thème de l'application", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("DARK" to "Sombre", "LIGHT" to "Clair", "SYSTEM" to "Système").forEach { (mode, label) ->
                            val isSelected = themeMode == mode
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    themeMode = mode
                                    onUpdateSettings(mode, notificationsEnabled)
                                },
                                label = { Text(label) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("theme_chip_$mode"),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }
            }

            // Notifications Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Notifications quotidiennes", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Text("Rappel de la citation du jour", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { checked ->
                            notificationsEnabled = checked
                            onUpdateSettings(themeMode, checked)
                        },
                        modifier = Modifier.testTag("notifications_switch")
                    )
                }
            }

            // About Application Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("À propos de l'application", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Dev : Genie Coding Haiti", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("Email : geniecoding2004@gmail.com", style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.primary))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Version 1.0.0 - 10 000+ Citations & Quizz AI", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
