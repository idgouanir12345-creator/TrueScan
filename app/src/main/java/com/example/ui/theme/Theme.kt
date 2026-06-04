package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF33B4B4), // Lighter teal for high-contrast dark readability
    secondary = PurpleGrey80,
    tertiary = HealthyGreen,
    background = Slate900, // #1A1C1E as defined in Color.kt
    surface = Slate800,    // #252729 as defined in Color.kt
    onPrimary = Color(0xFF003737),
    onSecondary = Color.White,
    onBackground = Color(0xFFE2E2E6),
    onSurface = Color(0xFFE2E2E6)
)

private val LightColorScheme = lightColorScheme(
    primary = TrueGreen,   // #006A6A Professional Teal
    secondary = Color(0xFF404949), // OnSurfaceSecondary
    tertiary = HealthyGreen, // #2D6A4F Forest Green
    background = Slate50,    // #FDFBFF light backplate
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF1A1C1E),
    onSurface = Color(0xFF1A1C1E),
    outline = TealLightBorder
)

@Composable
fun TrueScanTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Let's disable dynamic color by default to preserve TrueScan brand forest greens
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
