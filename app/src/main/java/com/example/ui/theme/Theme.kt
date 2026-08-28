package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.data.ReaderThemeMode

private val LightColorScheme = lightColorScheme(
    primary = IslamicEmerald,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD1E7DD),
    onPrimaryContainer = EmeraldDark,
    secondary = IslamicGold,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFF3CD),
    onSecondaryContainer = GoldDark,
    tertiary = EmeraldLight,
    background = Color(0xFFFBFBFB),
    onBackground = Color(0xFF1E2124),
    surface = Color.White,
    onSurface = Color(0xFF1E2124),
    surfaceVariant = Color(0xFFF1F3F5),
    onSurfaceVariant = Color(0xFF495057),
    outline = Color(0xFFCED4DA)
)

private val DarkColorScheme = darkColorScheme(
    primary = GoldLight,
    onPrimary = Color(0xFF221A00),
    primaryContainer = Color(0xFF332900),
    onPrimaryContainer = GoldLight,
    secondary = Color(0xFF75B798),
    onSecondary = Color(0xFF003822),
    secondaryContainer = Color(0xFF0F5132),
    onSecondaryContainer = Color(0xFFD1E7DD),
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder
)

private val ParchmentColorScheme = lightColorScheme(
    primary = Color(0xFF6B4226),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE8D7C3),
    onPrimaryContainer = Color(0xFF3B2312),
    secondary = IslamicEmerald,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDCEFE6),
    onSecondaryContainer = EmeraldDark,
    background = ParchmentBg,
    onBackground = ParchmentText,
    surface = ParchmentSurface,
    onSurface = ParchmentText,
    surfaceVariant = Color(0xFFE8E0D2),
    onSurfaceVariant = ParchmentSubtext,
    outline = ParchmentBorder
)

private val EmeraldColorScheme = darkColorScheme(
    primary = IslamicGold,
    onPrimary = Color(0xFF1F1600),
    primaryContainer = Color(0xFF3A2B00),
    onPrimaryContainer = GoldLight,
    secondary = EmeraldLight,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF0A3622),
    onSecondaryContainer = Color(0xFFD1E7DD),
    background = Color(0xFF0A1B14),
    onBackground = Color(0xFFE8F5E9),
    surface = Color(0xFF10281E),
    onSurface = Color(0xFFE8F5E9),
    surfaceVariant = Color(0xFF17382A),
    onSurfaceVariant = Color(0xFFA5D6A7),
    outline = Color(0xFF23533E)
)

@Composable
fun MyApplicationTheme(
    themeMode: ReaderThemeMode = ReaderThemeMode.PARCHMENT,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = when (themeMode) {
        ReaderThemeMode.PARCHMENT -> ParchmentColorScheme
        ReaderThemeMode.LIGHT -> LightColorScheme
        ReaderThemeMode.DARK -> DarkColorScheme
        ReaderThemeMode.EMERALD -> EmeraldColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
