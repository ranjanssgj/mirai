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
    // PRIMARY BRAND COLORS
    // ========================================================================
    val Primary = Color(0xFFEE2B8C)           // Main accent - pink/magenta
    val PrimaryDark = Color(0xFFC2186D)       // Pressed/hover states
    val PrimaryLight = Color(0xFFF85DA8)      // Lighter variant
    val PrimaryContainer = Color(0x33EE2B8C) // 20% primary for backgrounds
    val Secondary = Color(0xFFF0562E)         // Orange secondary accent
    
    // ========================================================================
    // BACKGROUND COLORS - Warm Dark Theme
    // ========================================================================
    /** Main app background - warm maroon undertone */
    val DarkBackground = Color(0xFF221019)
    
    /** Pure black for settings/OLED screens */
    val DarkBackgroundPure = Color(0xFF000000)
    
    /** Slightly lighter background for contrast */
    val DarkBackgroundAlt = Color(0xFF1A0D12)
    
    // ========================================================================
    // SURFACE COLORS - Cards & Elevated Elements
    // ========================================================================
    /** Primary surface for cards, modals */
    val DarkSurface = Color(0xFF351A28)
    
    /** Elevated surface variant */
    val DarkSurfaceVariant = Color(0xFF482336)
    
    /** Lighter surface for nested elements */
    val DarkSurfaceLighter = Color(0xFF5D2F46)
    
    /** Card backgrounds */
    val DarkCard = Color(0xFF331926)
    
    /** Settings surface (iOS-style dark) */
    val SettingsSurface = Color(0xFF1C1C1E)
    
    /** Panel/input field backgrounds */
    val PanelBg = Color(0xFF1A1C26)
    
    /** Input field background */
    val InputBg = Color(0xFF361B29)
    
    /** Dropdown/select background */
    val SelectBg = Color(0xFF1E1E24)
    
    // ========================================================================
    // GLASS & OVERLAY EFFECTS
    // ========================================================================
    /** Glass morphism background */
    val GlassBg = Color(0x33FFFFFF)           // 20% white
    
    /** Glass with blur for tags */
    val GlassTagBg = Color(0x33FFFFFF)        // 20% white
    
    /** Dark glass overlay */
    val GlassDarkBg = Color(0x4D000000)       // 30% black
    
    /** Overlay for images */
    val Overlay = Color(0x66000000)           // 40% black
    
    /** Strong overlay */
    val OverlayStrong = Color(0x99000000)     // 60% black
    
    // ========================================================================
    // TEXT COLORS
    // ========================================================================
    val TextPrimary = Color(0xFFFFFFFF)       // White
    val TextSecondary = Color(0xFFB3B3B3)     // 70% white
    val TextTertiary = Color(0xFF808080)      // 50% white
    val TextMuted = Color(0xFF666666)         // 40% white
    val TextPlaceholder = Color(0xFFC992AD)   // Muted pink
    val TextOnPrimary = Color(0xFFFFFFFF)     // Text on primary buttons
    
    // ========================================================================
    // ACCENT COLORS
    // ========================================================================
    val AccentBlue = Color(0xFF64B5F6)
    val AccentTeal = Color(0xFF4DB6AC)
    val AccentOrange = Color(0xFFFFB74D)
    val AccentGreen = Color(0xFF81C784)
    val AccentPurple = Color(0xFF9C7CF4)
    val AccentCyan = Color(0xFF67E8F9)
    val AccentYellow = Color(0xFFFFC107)
    
    // ========================================================================
    // STATUS COLORS - Library States
    // ========================================================================
    val Watching = Color(0xFF64B5F6)          // Blue - currently watching
    val Reading = Color(0xFF64B5F6)           // Blue - currently reading
    val Completed = Color(0xFF81C784)         // Green - completed
    val Planned = Color(0xFFFFB74D)           // Orange - plan to watch
    val OnHold = Color(0xFFE0E0E0)            // Grey - on hold
    val Dropped = Color(0xFFE57373)           // Red - dropped
    
    // ========================================================================
    // SEMANTIC COLORS
    // ========================================================================
    val Error = Color(0xFFCF6679)
    val Success = Color(0xFF81C784)
    val Warning = Color(0xFFFFB74D)
    val Info = Color(0xFF64B5F6)
    val Rating = Color(0xFFFFD700)            // Gold for stars
    
    // ========================================================================
    // BORDER & DIVIDER COLORS
    // ========================================================================
    val Divider = Color(0xFF2A2A2A)           // Dark divider
    val DividerLight = Color(0x0DFFFFFF)      // 5% white
    val DividerSubtle = Color(0x1AFFFFFF)     // 10% white
    val Border = Color(0x1AFFFFFF)            // 10% white border
    val BorderFocused = Primary               // Primary color when focused
    
    // ========================================================================
    // SHIMMER / LOADING
    // ========================================================================
    val Shimmer = Color(0xFF2A2A2A)
    val ShimmerHighlight = Color(0xFF3A3A3A)
    
    // ========================================================================
    // LEGACY ALIASES (for backwards compatibility)
    // ========================================================================
    val PrimaryPurple = Primary
    val PrimaryPink = Primary
}
