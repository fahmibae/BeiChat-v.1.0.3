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

private val BitChatDarkColorScheme = darkColorScheme(
    primary = SleekBlueLight,
    onPrimary = Color.White,
    primaryContainer = SleekBlueContainerDark,
    onPrimaryContainer = SleekBlueContainerLight,
    secondary = SleekBlueSoft,
    onSecondary = Color(0xFF0F172A),
    secondaryContainer = Color(0xFF1E293B),
    onSecondaryContainer = Color(0xFFE2E8F0),
    tertiary = SleekBluePrimary,
    background = DarkBg,
    onBackground = TextPrimaryDark,
    surface = DarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = TextSecondaryDark,
    outline = DarkSurfaceBorder,
    error = SecurityShieldRed,
    onError = Color.White
)

private val BitChatLightColorScheme = lightColorScheme(
    primary = SleekBluePrimary,
    onPrimary = Color.White,
    primaryContainer = SleekBlueContainerLight,
    onPrimaryContainer = SleekBlueDark,
    secondary = SleekBlueDark,
    onSecondary = Color.White,
    secondaryContainer = SleekBlueTintLight,
    onSecondaryContainer = SleekBlueDark,
    tertiary = SleekBlueLight,
    background = LightBg,
    onBackground = TextPrimaryLight,
    surface = LightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = LightSurfaceElevated,
    onSurfaceVariant = TextSecondaryLight,
    outline = LightSurfaceBorder,
    error = SecurityShieldRed,
    onError = Color.White
)

@Composable
fun BitChatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set false by default to showcase custom BitChat cyber-security aesthetic
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> BitChatDarkColorScheme
        else -> BitChatLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
