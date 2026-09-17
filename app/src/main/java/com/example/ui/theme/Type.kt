package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.R

val SpaceGroteskFontFamily = FontFamily(
    Font(R.font.space_grotesk, weight = FontWeight.Normal)
)

val ManropeFontFamily = FontFamily(
    Font(R.font.manrope, weight = FontWeight.Normal)
)

private val defaultTypography = Typography()

// Set of Material typography styles to start with
val Typography = Typography(
    displayLarge = defaultTypography.displayLarge.copy(fontFamily = ManropeFontFamily),
    displayMedium = defaultTypography.displayMedium.copy(fontFamily = ManropeFontFamily),
    displaySmall = defaultTypography.displaySmall.copy(fontFamily = ManropeFontFamily),
    headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = ManropeFontFamily),
    headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = ManropeFontFamily),
    headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = ManropeFontFamily),
    titleLarge = defaultTypography.titleLarge.copy(fontFamily = ManropeFontFamily),
    titleMedium = defaultTypography.titleMedium.copy(fontFamily = ManropeFontFamily),
    titleSmall = defaultTypography.titleSmall.copy(fontFamily = ManropeFontFamily),
    bodyLarge = defaultTypography.bodyLarge.copy(fontFamily = ManropeFontFamily),
    bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = ManropeFontFamily),
    bodySmall = defaultTypography.bodySmall.copy(fontFamily = ManropeFontFamily),
    labelLarge = defaultTypography.labelLarge.copy(fontFamily = ManropeFontFamily),
    labelMedium = defaultTypography.labelMedium.copy(fontFamily = ManropeFontFamily),
    labelSmall = defaultTypography.labelSmall.copy(fontFamily = ManropeFontFamily)
)
