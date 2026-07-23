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
    primary = AestheticGold,
    secondary = SecondarySlate,
    tertiary = LightNavy,
    background = DeepNavy,
    surface = PrimaryNavy,
    onPrimary = DeepNavy,
    onSecondary = White,
    onTertiary = White,
    onBackground = White,
    onSurface = White
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryNavy,
    secondary = SecondarySlate,
    tertiary = AestheticGold,
    background = Color(0xFFF4F7FB), // Very light bluish white
    surface = White,
    onPrimary = White,
    onSecondary = White,
    onTertiary = PrimaryNavy,
    onBackground = PrimaryNavy,
    onSurface = PrimaryNavy
)

@Composable
fun HopsInTheHangarTheme(
    darkTheme: Boolean = true, // Force dark theme for aesthetic navy look by default
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
