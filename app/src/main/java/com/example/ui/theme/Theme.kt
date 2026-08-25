package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = FocusCyan,
    onPrimary = FocusPurple,
    primaryContainer = FocusCyanDark,
    onPrimaryContainer = FocusGold,
    secondary = FocusAccentOrange,
    onSecondary = FocusPurple,
    tertiary = FocusGold,
    background = FocusSlateBg,
    onBackground = FocusTextPrimary,
    surface = FocusSurface,
    onSurface = FocusTextPrimary,
    surfaceVariant = FocusSurfaceVariant,
    onSurfaceVariant = FocusTextSecondary,
    outline = FocusOutline,
    error = FocusCoralRed,
    onError = FocusCoralRedDark
)

@Composable
fun FocusOSTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}

