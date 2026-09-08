package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.example.R

val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.custom_com_google_android_gms_fonts_certs
)

val spaceGroteskName = GoogleFont("Space Grotesk")

val SpaceGroteskFontFamily = FontFamily(
    Font(googleFont = spaceGroteskName, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = spaceGroteskName, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = spaceGroteskName, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = spaceGroteskName, fontProvider = provider, weight = FontWeight.Bold),
    Font(googleFont = spaceGroteskName, fontProvider = provider, weight = FontWeight.ExtraBold)
)

val manropeName = GoogleFont("Manrope")

val ManropeFontFamily = FontFamily(
    Font(googleFont = manropeName, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = manropeName, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = manropeName, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = manropeName, fontProvider = provider, weight = FontWeight.Bold),
    Font(googleFont = manropeName, fontProvider = provider, weight = FontWeight.ExtraBold)
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
