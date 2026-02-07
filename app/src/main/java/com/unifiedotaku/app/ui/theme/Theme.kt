package com.unifiedotaku.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Color.White,
    onPrimary = Color.Black,
    primaryContainer = Color.White.copy(alpha = 0.1f),
    onPrimaryContainer = Color.White,
    
    secondary = Color.White.copy(alpha = 0.7f),
    onSecondary = Color.Black,
    secondaryContainer = Color.White.copy(alpha = 0.1f),
    onSecondaryContainer = Color.White,
    
    background = Color.Black,
    onBackground = Color.White,
    
    surface = Color(0xFF121212),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF1E1E1E),
    onSurfaceVariant = Color.White.copy(alpha = 0.7f),
    
    outline = Color(0xFF2A2A2A),
    
    error = Color(0xFFCF6679),
    onError = Color.Black
)

private val LightColorScheme = lightColorScheme(
    primary = Color.Black,
    onPrimary = Color.White,
    background = Color.White,
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black
)

@Composable
fun UnifiedOtakuTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    accentColor: Color = Color.White, // Default to White per request
    pureBlack: Boolean = true, // Default to true per request
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        val baseBackground = if (pureBlack) Color.Black else AppColors.DarkBackground
        val baseSurface = if (pureBlack) Color(0xFF121212) else AppColors.DarkSurface
        
        darkColorScheme(
            primary = accentColor,
            onPrimary = if (accentColor == Color.White) Color.Black else Color.White,
            primaryContainer = accentColor.copy(alpha = 0.1f),
            onPrimaryContainer = Color.White,
            
            secondary = Color.White.copy(alpha = 0.7f),
            onSecondary = Color.Black,
            
            background = baseBackground,
            onBackground = Color.White,
            
            surface = baseSurface,
            onSurface = Color.White,
            surfaceVariant = if (pureBlack) Color(0xFF1E1E1E) else AppColors.DarkSurfaceVariant,
            onSurfaceVariant = Color.White.copy(alpha = 0.7f),
            
            outline = Color(0xFF2A2A2A),
            error = Color(0xFFCF6679),
            onError = Color.Black
        )
    } else {
        LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
