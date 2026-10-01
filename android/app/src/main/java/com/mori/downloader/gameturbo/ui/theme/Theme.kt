package com.mori.downloader.gameturbo.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Dark color scheme (Game Turbo default)
private val DarkGameturboColorScheme = darkColorScheme(
    primary = Color(0xFF00D4AA),
    primaryContainer = Color(0xFF004D3A),
    secondary = Color(0xFF74B9FF),
    secondaryContainer = Color(0xFF1A3A5C),
    tertiary = Color(0xFFFFD93D),
    tertiaryContainer = Color(0xFF4D3E00),
    error = Color(0xFFFF6B6B),
    errorContainer = Color(0xFF5C1A1A),
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    surfaceVariant = Color(0xFF2A2A2A),
    outline = Color(0xFF3A3A3A),
    onPrimary = Color(0xFF000000),
    onPrimaryContainer = Color(0xFFB3FFEB),
    onSecondary = Color(0xFF000000),
    onSecondaryContainer = Color(0xFFCDE8FF),
    onTertiary = Color(0xFF000000),
    onTertiaryContainer = Color(0xFFFFF3B3),
    onError = Color(0xFFFFFFFF),
    onErrorContainer = Color(0xFFFFDADA),
    onBackground = Color(0xFFFFFFFF),
    onSurface = Color(0xFFFFFFFF),
    onSurfaceVariant = Color(0xFFCACACA),
    inversePrimary = Color(0xFF00D4AA),
    surfaceTint = Color(0xFF00D4AA)
)

// Light color scheme (optional)
private val LightGameturboColorScheme = lightColorScheme(
    primary = Color(0xFF008C70),
    primaryContainer = Color(0xFFB3FFEB),
    secondary = Color(0xFF2E86DE),
    secondaryContainer = Color(0xFFCDE8FF),
    tertiary = Color(0xFFB8860B),
    tertiaryContainer = Color(0xFFFFF3B3),
    error = Color(0xFFD32F2F),
    errorContainer = Color(0xFFFFDADA),
    background = Color(0xFFF5F5F5),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE0E0E0),
    outline = Color(0xFF757575),
    onPrimary = Color(0xFFFFFFFF),
    onPrimaryContainer = Color(0xFF00291D),
    onSecondary = Color(0xFFFFFFFF),
    onSecondaryContainer = Color(0xFF001E3A),
    onTertiary = Color(0xFFFFFFFF),
    onTertiaryContainer = Color(0xFF362900),
    onError = Color(0xFFFFFFFF),
    onErrorContainer = Color(0xFF4C0000),
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    onSurfaceVariant = Color(0xFF454545),
    inversePrimary = Color(0xFF00D4AA),
    surfaceTint = Color(0xFF008C70)
)

@Composable
fun GameTurboTheme(
    darkTheme: Boolean = true, // Force dark for gaming
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkGameturboColorScheme else LightGameturboColorScheme
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}

// Typography
val Typography = androidx.compose.material3.Typography(
    displayLarge = androidx.compose.material3.TextStyle(
        fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = -0.25.sp
    ),
    displayMedium = androidx.compose.material3.TextStyle(
        fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Normal,
        fontSize = 45.sp,
        lineHeight = 52.sp
    ),
    displaySmall = androidx.compose.material3.TextStyle(
        fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 44.sp
    ),
    headlineLarge = androidx.compose.material3.TextStyle(
        fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 40.sp
    ),
    headlineMedium = androidx.compose.material3.TextStyle(
        fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    headlineSmall = androidx.compose.material3.TextStyle(
        fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),
    titleLarge = androidx.compose.material3.TextStyle(
        fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleMedium = androidx.compose.material3.TextStyle(
        fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = androidx.compose.material3.TextStyle(
        fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = androidx.compose.material3.TextStyle(
        fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = androidx.compose.material3.TextStyle(
        fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = androidx.compose.material3.TextStyle(
        fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    labelLarge = androidx.compose.material3.TextStyle(
        fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = androidx.compose.material3.TextStyle(
        fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = androidx.compose.material3.TextStyle(
        fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

// Shapes
val Shapes = androidx.compose.material3.Shapes(
    extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
    small = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
)

// Color extensions
import androidx.compose.ui.graphics.Color

val androidx.compose.material3.ColorScheme.gameturboPrimary: Color
    get() = primary

val androidx.compose.material3.ColorScheme.gameturboSurface: Color
    get() = surface

val androidx.compose.material3.ColorScheme.gameturboSurfaceVariant: Color
    get() = surfaceVariant