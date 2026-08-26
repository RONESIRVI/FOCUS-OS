package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = FocusPrimary,
    onPrimary = FocusOnPrimary,
    primaryContainer = FocusPrimaryDark,
    onPrimaryContainer = FocusTextPrimary,
    secondary = FocusWarning,
    onSecondary = FocusOnPrimary,
    tertiary = FocusDanger,
    background = FocusBackground,
    onBackground = FocusTextPrimary,
    surface = FocusSurface,
    onSurface = FocusTextPrimary,
    surfaceVariant = FocusSurfaceVariant,
    onSurfaceVariant = FocusTextSecondary,
    outline = FocusOutline,
    error = FocusDanger,
    onError = FocusDangerDark
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

