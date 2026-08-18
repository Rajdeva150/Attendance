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

private val DarkColorScheme = darkColorScheme(
    primary = AmberLight,
    onPrimary = Color(0xFF2E1900),
    primaryContainer = Color(0xFF472D00),
    onPrimaryContainer = AmberContainerLight,
    secondary = DutyBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF1D3B5C),
    onSecondaryContainer = Color(0xFFD6E4FF),
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceSecondary,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder,
    error = LeaveRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = AmberPrimary,
    onPrimary = Color.White,
    primaryContainer = AmberContainerLight,
    onPrimaryContainer = AmberOnContainerLight,
    secondary = DutyBlue,
    onSecondary = Color.White,
    secondaryContainer = DutyBlueBg,
    onSecondaryContainer = Color(0xFF0F3661),
    background = SlateBackground,
    onBackground = SlateTextPrimary,
    surface = SlateCard,
    onSurface = SlateTextPrimary,
    surfaceVariant = SlateCardSecondary,
    onSurfaceVariant = SlateTextSecondary,
    outline = SlateBorder,
    error = LeaveRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent custom branding
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
