package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthDialog(
    promptReason: String = "",
    onDismiss: () -> Unit,
    onLogin: (emailOrUser: String, pass: String, callback: (Boolean) -> Unit) -> Unit,
    onRegister: (fullName: String, user: String, email: String, pass: String, callback: (Boolean) -> Unit) -> Unit
) {
    val context = LocalContext.current
    var isRegisterTab by remember { mutableStateOf(false) }

    // Login Fields
    var loginIdentifier by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }

    // Register Fields
    var regFullName by remember { mutableStateOf("") }
    var regUsername by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isRegisterTab) "Créer un compte" else "Connexion requise",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (promptReason.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Connectez-vous $promptReason.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Tab selector
                TabRow(
                    selectedTabIndex = if (isRegisterTab) 1 else 0,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Tab(
                        selected = !isRegisterTab,
                        onClick = { isRegisterTab = false },
                        text = { Text("Connexion", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = isRegisterTab,
                        onClick = { isRegisterTab = true },
                        text = { Text("Inscription", fontWeight = FontWeight.Bold) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (!isRegisterTab) {
                    // LOGIN FORM
                    OutlinedTextField(
                        value = loginIdentifier,
                        onValueChange = { loginIdentifier = it },
                        label = { Text("Email ou nom d'utilisateur") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_identifier_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = loginPassword,
                        onValueChange = { loginPassword = it },
                        label = { Text("Mot de passe") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_password_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                } else {
                    // REGISTER FORM
                    OutlinedTextField(
                        value = regFullName,
                        onValueChange = { regFullName = it },
                        label = { Text("Nom complet") },
                        leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_fullname_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = regUsername,
                        onValueChange = { regUsername = it },
                        label = { Text("Nom d'utilisateur (@pseudo)") },
                        leadingIcon = { Icon(Icons.Default.AlternateEmail, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_username_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = regEmail,
                        onValueChange = { regEmail = it },
                        label = { Text("Adresse email") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_email_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = regPassword,
                        onValueChange = { regPassword = it },
                        label = { Text("Mot de passe") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_password_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isLoading = true
                    if (!isRegisterTab) {
                        if (loginIdentifier.isBlank() || loginPassword.isBlank()) {
                            Toast.makeText(context, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
                            isLoading = false
                            return@Button
                        }
                        onLogin(loginIdentifier, loginPassword) { success ->
                            isLoading = false
                            if (!success) {
                                Toast.makeText(context, "Identifiants incorrects", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Connexion réussie !", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        if (regFullName.isBlank() || regUsername.isBlank() || regEmail.isBlank() || regPassword.isBlank()) {
                            Toast.makeText(context, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
                            isLoading = false
                            return@Button
                        }
                        onRegister(regFullName, regUsername, regEmail, regPassword) { success ->
                            isLoading = false
                            if (success) {
                                Toast.makeText(context, "Compte créé avec succès !", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Erreur lors de la création du compte", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("auth_confirm_button")
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                } else {
                    Text(if (isRegisterTab) "S'inscrire" else "Se connecter", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Plus tard", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}
