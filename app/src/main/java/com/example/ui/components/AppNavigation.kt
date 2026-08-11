package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfileEntity

enum class BottomTab(val route: String, val title: String, val icon: ImageVector) {
    HOME("home", "Accueil", Icons.Default.Home),
    QUIZ("quiz", "Quizz", Icons.Default.Psychology),
    AI("ai", "cdj_ia", Icons.Default.AutoAwesome),
    MESSAGES("messages", "Messages", Icons.Default.Forum)
}

enum class DrawerDestination(val title: String, val icon: ImageVector) {
    PROFILE("Profil", Icons.Default.Person),
    FAVORITES("Favoris", Icons.Default.Favorite),
    SETTINGS("Paramètres", Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopHeaderBar(
    title: String,
    onOpenDrawer: () -> Unit,
    userProfile: UserProfileEntity?,
    onProfileClick: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FormatQuote,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                )
            }
        },
        navigationIcon = {
            IconButton(
                onClick = onOpenDrawer,
                modifier = Modifier.testTag("drawer_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu Drawer",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        actions = {
            Box(
                modifier = Modifier
                    .padding(end = 12.dp)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable { onProfileClick() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = userProfile?.displayName?.take(1)?.uppercase() ?: "A",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
fun AppBottomNavigationBar(
    currentTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        BottomTab.values().forEach { tab ->
            val isSelected = currentTab == tab
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.title
                    )
                },
                label = {
                    Text(
                        text = tab.title,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                modifier = Modifier.testTag("tab_${tab.route}")
            )
        }
    }
}

@Composable
fun AppDrawerContent(
    userProfile: UserProfileEntity?,
    isLoggedIn: Boolean = false,
    onDestinationSelected: (DrawerDestination) -> Unit,
    onLoginRequested: () -> Unit = {},
    onLogoutRequested: () -> Unit = {},
    onCloseDrawer: () -> Unit
) {
    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.surface
    ) {
        // Drawer Header with User Info
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(24.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userProfile?.displayName?.take(1)?.uppercase() ?: "A",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isLoggedIn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    ) {
                        Text(
                            text = if (isLoggedIn) "Connecté" else "Invité",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isLoggedIn) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = userProfile?.displayName ?: "Alexandre Martin",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
                Text(
                    text = "@${userProfile?.username ?: "citations_master"}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${userProfile?.totalPoints ?: 0} points de culture",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Navigation Items
        DrawerDestination.values().forEach { destination ->
            NavigationDrawerItem(
                label = { Text(destination.title, fontWeight = FontWeight.Medium) },
                icon = { Icon(destination.icon, contentDescription = destination.title) },
                selected = false,
                onClick = {
                    onDestinationSelected(destination)
                    onCloseDrawer()
                },
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .testTag("drawer_item_${destination.name.lowercase()}")
            )
        }

        Divider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

        if (isLoggedIn) {
            NavigationDrawerItem(
                label = { Text("Se déconnecter", fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.error) },
                icon = { Icon(Icons.Default.Logout, contentDescription = "Déconnexion", tint = MaterialTheme.colorScheme.error) },
                selected = false,
                onClick = {
                    onLogoutRequested()
                    onCloseDrawer()
                },
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .testTag("drawer_logout_item")
            )
        } else {
            NavigationDrawerItem(
                label = { Text("Se connecter / S'inscrire", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                icon = { Icon(Icons.Default.Login, contentDescription = "Connexion", tint = MaterialTheme.colorScheme.primary) },
                selected = false,
                onClick = {
                    onLoginRequested()
                    onCloseDrawer()
                },
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .testTag("drawer_login_item")
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Divider(modifier = Modifier.padding(horizontal = 16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = "Citations & Quizz AI v1.0\nProjet Android Kotlin",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            )
        }
    }
}
