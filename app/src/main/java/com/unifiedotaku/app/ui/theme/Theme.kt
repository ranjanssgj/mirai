package com.unifiedotaku.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = AppColors.Primary,
    onPrimary = AppColors.OnPrimary,
    primaryContainer = AppColors.ClayContainer,
    onPrimaryContainer = AppColors.TextPrimary,
    
    secondary = AppColors.Secondary,
    onSecondary = AppColors.OnSecondary,
    secondaryContainer = AppColors.ClayContainer,
    onSecondaryContainer = AppColors.TextPrimary,
    
    background = AppColors.DarkBackground,
    onBackground = AppColors.TextPrimary,
    
    surface = AppColors.DarkSurface,
    onSurface = AppColors.TextPrimary,
    surfaceVariant = AppColors.ClayCard,
    onSurfaceVariant = AppColors.TextSecondary,
    
    outline = AppColors.Secondary,
    
    error = AppColors.Error,
    onError = AppColors.DarkBackground
)

private val LightColorScheme = lightColorScheme(
    primary = Color.Black,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF0F0F0),
    onPrimaryContainer = Color.Black,

    secondary = Color(0xFF666666),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0E0E0),
    onSecondaryContainer = Color.Black,

    background = Color(0xFFFAFAFA),
    onBackground = Color.Black,

    surface = Color.White,
    onSurface = Color.Black,
    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = Color(0xFF666666),

    outline = Color(0xFFDDDDDD),
    
    error = Color(0xFFB00020),
    onError = Color.White
)

val MaterialTypography = Typography(
    displayLarge = AppTypography.DisplayLarge,
    displayMedium = AppTypography.DisplayMedium,
    headlineLarge = AppTypography.HeadlineLarge,
    headlineMedium = AppTypography.HeadlineMedium,
    headlineSmall = AppTypography.HeadlineSmall,
    titleLarge = AppTypography.TitleLarge,
    titleMedium = AppTypography.TitleMedium,
    titleSmall = AppTypography.TitleSmall,
    bodyLarge = AppTypography.BodyLarge,
    bodyMedium = AppTypography.BodyMedium,
    bodySmall = AppTypography.BodySmall,
    labelLarge = AppTypography.LabelLarge,
    labelMedium = AppTypography.LabelMedium,
    labelSmall = AppTypography.LabelSmall
)

@Composable
fun UnifiedOtakuTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, 
    accentColor: Color = Color.White,
    pureBlack: Boolean = false,
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

    // Apply Overrides
    var finalScheme = colorScheme

    // 1. Accent Color Override (If not dynamic or if user wants to force accent on top)
    if (!dynamicColor) {
        finalScheme = finalScheme.copy(
            primary = accentColor,
            onPrimary = if (darkTheme) Color.Black else Color.White, // Simplified contrast
            tertiary = accentColor,
        )
    }

    // 2. Pure Black / OLED override (Only for Dark Theme)
    if (darkTheme && pureBlack) {
        finalScheme = finalScheme.copy(
            background = Color.Black,
            surface = Color.Black,
            surfaceVariant = Color(0xFF121212) 
        )
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
        colorScheme = finalScheme,
        typography = MaterialTypography,
        content = content
    )
}
