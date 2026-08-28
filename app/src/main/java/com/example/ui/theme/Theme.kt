package com.example.ui.theme

import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private fun getDynamicColorScheme(themeKey: String) = when (themeKey) {
    "CYBER_NEON" -> darkColorScheme(
        primary = Color(0xFF00E5FF),
        onPrimary = Color(0xFF000000),
        primaryContainer = Color(0xFF004B56),
        onPrimaryContainer = FocusTextPrimary,
        secondary = FocusWarning,
        onSecondary = FocusOnPrimary,
        tertiary = FocusDanger,
        background = Color(0xFF0A0E1A),
        onBackground = FocusTextPrimary,
        surface = Color(0xFF121829),
        onSurface = FocusTextPrimary,
        surfaceVariant = Color(0xFF1C253B),
        onSurfaceVariant = FocusTextSecondary,
        outline = Color(0xFF283654),
        error = FocusDanger,
        onError = FocusDangerDark
    )
    "WARM_SUNSET" -> darkColorScheme(
        primary = Color(0xFFFF9100),
        onPrimary = Color(0xFF000000),
        primaryContainer = Color(0xFF563000),
        onPrimaryContainer = FocusTextPrimary,
        secondary = FocusWarning,
        onSecondary = FocusOnPrimary,
        tertiary = FocusDanger,
        background = Color(0xFF140D0B),
        onBackground = FocusTextPrimary,
        surface = Color(0xFF221613),
        onSurface = FocusTextPrimary,
        surfaceVariant = Color(0xFF33221E),
        onSurfaceVariant = FocusTextSecondary,
        outline = Color(0xFF4A322C),
        error = FocusDanger,
        onError = FocusDangerDark
    )
    "ICE_BLUE" -> darkColorScheme(
        primary = Color(0xFF448AFF),
        onPrimary = Color(0xFF000000),
        primaryContainer = Color(0xFF163266),
        onPrimaryContainer = FocusTextPrimary,
        secondary = FocusWarning,
        onSecondary = FocusOnPrimary,
        tertiary = FocusDanger,
        background = Color(0xFF09111E),
        onBackground = FocusTextPrimary,
        surface = Color(0xFF101C30),
        onSurface = FocusTextPrimary,
        surfaceVariant = Color(0xFF1A2A47),
        onSurfaceVariant = FocusTextSecondary,
        outline = Color(0xFF273E66),
        error = FocusDanger,
        onError = FocusDangerDark
    )
    else -> darkColorScheme( // DEEP_DARK (Default Emerald Green)
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
}

@Composable
fun FocusOSTheme(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("FocusPrefs", Context.MODE_PRIVATE)
    val themeKey = prefs.getString("NOTIF_DESIGN_THEME", "DEEP_DARK") ?: "DEEP_DARK"
    val colorScheme = getDynamicColorScheme(themeKey)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

