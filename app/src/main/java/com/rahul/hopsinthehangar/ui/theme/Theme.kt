package com.rahul.hopsinthehangar.ui.theme

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
    primary = PrimaryGold,
    secondary = SkyBlue,
    tertiary = SecondaryAmber,
    background = DarkBlue,
    surface = DarkBlue.copy(alpha = 0.8f),
    onPrimary = DarkBlue,
    onSecondary = White,
    onTertiary = White,
    onBackground = White,
    onSurface = White
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryGold,
    secondary = DarkBlue,
    tertiary = SecondaryAmber,
    background = White,
    surface = White.copy(alpha = 0.9f),
    onPrimary = DarkBlue,
    onSecondary = White,
    onTertiary = White,
    onBackground = DarkBlue,
    onSurface = DarkBlue
)

@Composable
fun HopsInTheHangarTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
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
