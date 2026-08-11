package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryOrange,
    onPrimary = OnPrimaryOrange,
    primaryContainer = PrimaryOrangeContainer,
    onPrimaryContainer = OnPrimaryOrangeContainer,
    secondary = SecondaryOrange,
    onSecondary = OnSecondaryOrange,
    secondaryContainer = SecondaryOrangeContainer,
    onSecondaryContainer = OnSecondaryOrangeContainer,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onBackground = OnDarkSurface,
    onSurface = OnDarkSurface
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryOrange,
    onPrimary = OnPrimaryOrange,
    primaryContainer = PrimaryOrangeContainer,
    onPrimaryContainer = OnPrimaryOrangeContainer,
    secondary = SecondaryOrange,
    onSecondary = OnSecondaryOrange,
    secondaryContainer = SecondaryOrangeContainer,
    onSecondaryContainer = OnSecondaryOrangeContainer,
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    onBackground = OnLightSurface,
    onSurface = OnLightSurface,
    onSurfaceVariant = OnLightSurface.copy(alpha = 0.7f)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Default to light white background theme
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}


