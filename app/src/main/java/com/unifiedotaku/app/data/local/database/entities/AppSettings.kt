package com.unifiedotaku.app.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Key-value store for app settings.
 * Flexible schema for various configuration options.
 */
@Entity(tableName = "app_settings")
data class AppSettings(
    @PrimaryKey 
    val key: String,
    val value: String,
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        // Theme Settings
        const val KEY_THEME = "theme"                           // light, dark, system
        const val KEY_ACCENT_COLOR = "accent_color"             // hex color
        
        // Player Settings
        const val KEY_PLAYER_QUALITY = "player_quality"         // auto, 1080p, 720p, 480p
        const val KEY_PLAYER_BUFFER_SIZE = "player_buffer_size" // small, normal, large
        const val KEY_PLAYER_SKIP_INTRO = "player_skip_intro"   // true/false
        const val KEY_PLAYER_SKIP_OUTRO = "player_skip_outro"   // true/false
        const val KEY_PLAYER_AUTO_NEXT = "player_auto_next"     // true/false
        const val KEY_PLAYER_SPEED = "player_speed"             // 1.0, 1.25, 1.5, 2.0
        const val KEY_SUBTITLE_SIZE = "subtitle_size"           // 0 (small), 1 (medium), 2 (large)
        const val KEY_SUBTITLE_COLOR = "subtitle_color"         // int color
        const val KEY_SUBTITLE_BACKGROUND = "subtitle_bg"       // none, outline, shadow, dim, solid
        
        // Reader Settings
        const val KEY_READER_MODE = "reader_mode"               // vertical, horizontal, webtoon
        const val KEY_READER_DIRECTION = "reader_direction"     // ltr, rtl
        const val KEY_READER_BRIGHTNESS = "reader_brightness"   // 0-100 float
        const val KEY_READER_ZOOM = "reader_zoom"               // fit_width, fit_height, fit_screen
        const val KEY_READER_TRANSITION = "reader_transition"   // none, slide, curl, fade
        const val KEY_READER_KEEP_SCREEN = "reader_keep_screen" // true/false
        const val KEY_VOLUME_KEYS_SCROLL = "volume_keys_scroll" // true/false
        
        // Download Settings
        const val KEY_DOWNLOAD_PATH = "download_path"           // custom path
        const val KEY_DOWNLOAD_WIFI_ONLY = "download_wifi_only" // true/false
        const val KEY_DOWNLOAD_QUALITY = "download_quality"     // anime quality
        
        // Security
        const val KEY_APP_LOCK = "app_lock"                     // true/false
        const val KEY_APP_LOCK_PIN = "app_lock_pin"             // encrypted PIN
        const val KEY_INCOGNITO_MODE = "incognito_mode"         // true/false
        const val KEY_NOTIFICATION_PRIVACY = "notif_privacy"    // true/false
        
        // Accounts
        const val KEY_MAL_TOKEN = "mal_token"
        const val KEY_ANILIST_TOKEN = "anilist_token"
        const val KEY_KITSU_TOKEN = "kitsu_token"
        
        // Misc
        const val KEY_LAST_SYNC = "last_sync"                   // timestamp
        const val KEY_UPDATE_CHANNEL = "update_channel"         // stable, beta
    }
}
