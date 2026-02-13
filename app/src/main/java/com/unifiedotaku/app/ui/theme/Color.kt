package com.unifiedotaku.app.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================================
// STITCH DESIGN SYSTEM - Color Tokens
// Extracted from Stitch design reference files (PNG + HTML)
// ============================================================================

// Legacy Material colors (kept for compatibility)
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)
val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

/**
 * Stitch Design System Colors
 * 
 * Primary: Vibrant pink/magenta (#EE2B8C)
 * Background: Warm dark with maroon undertone (#221019)
 * Font: Spline Sans
 */
object AppColors {
    // ========================================================================
    // MONOCHROME CLAYMORPHISM PALETTE
    // ========================================================================

    // Backgrounds
    val DarkBackground = Color(0xFF121212) // Deepest Black
    val DarkSurface = Color(0xFF1E1E1E)    // Dark Grey - Slightly lighter than bg

    // Clay Colors (Base colors for objects)
    val ClayCard = Color(0xFF1E1E1E)       // Surface (Cards)
    val ClayContainer = Color(0xFF252525)  // Slightly lighter for floating containers

    // Highlights & Shadows (Crucial for Claymorphism)
    // Top-Left: Light/Soft reflection (White with 5% opacity)
    val ClayShadowLight = Color(0x0DFFFFFF) // 5% White
    // Bottom-Right: Deep Shadow (Black with 40% opacity)
    val ClayShadowDark = Color(0x66000000)  // 40% Black

    // Text Colors
    val TextPrimary = Color(0xFFFFFFFF)       // Pure White
    val TextSecondary = Color(0xFFB0B0B0)     // Light Grey
    val TextTertiary = Color(0xFF999999)      // Medium Grey
    val TextMuted = Color(0xFF666666)         // Dark Grey

    // Accents (Strictly Black & White)
    val Primary = Color(0xFFFFFFFF)           // Pure White
    val PrimaryDark = Color(0xFFB0B0B0)       // Light grey
    val OnPrimary = Color(0xFF000000)         // Black text on primary

    val Secondary = Color(0xFFB0B0B0)         // Light grey
    val OnSecondary = Color(0xFF121212)       // Black text on secondary

    // Semantic Colors (Monochrome adaptation)
    val Error = Color(0xFFFFFFFF)             // White
    val Success = Color(0xFFFFFFFF)           // White
    val Warning = Color(0xFFB0B0B0)           // Light Grey
    val Info = Color(0xFF999999)              // Medium Grey

    // Compatibility Fields
    val DarkSurfaceVariant = Color(0xFF252525)
    val DarkCard = ClayCard
    val Border = Color(0x1AFFFFFF)
    val PrimaryContainer = ClayContainer
    val DividerLight = Color(0x0DFFFFFF)
    val SettingsSurface = DarkSurface
    val TextOnPrimary = OnPrimary
    val GlassBg = Color(0x0DFFFFFF)
    val GlassDarkBg = Color(0x66000000)
    val GlassTagBg = Color(0x1AFFFFFF)
    val DarkBackgroundAlt = Color(0xFF0A0A0A)
}
