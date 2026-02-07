package com.unifiedotaku.app.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Tracks downloaded episodes/chapters.
 */
@Entity(tableName = "downloads")
data class Download(
    @PrimaryKey 
    val id: String,                        // Unique download ID
    val seriesId: String,                  // Associated series
    val seriesTitle: String,
    val seriesCoverUrl: String,
    val type: MediaType,                   // ANIME or MANGA
    val contentId: String,                 // Episode or chapter ID
    val number: Int,                       // Episode or chapter number
    val title: String? = null,
    val filePath: String,                  // Local file path
    val fileSize: Long = 0,                // Size in bytes
    val status: DownloadStatus = DownloadStatus.PENDING,
    val progress: Int = 0,                 // 0-100
    val error: String? = null,             // Error message if failed
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)

/**
 * Status of a download task.
 */
enum class DownloadStatus {
    PENDING,        // Queued, waiting to start
    DOWNLOADING,    // Currently downloading
    COMPLETED,      // Successfully downloaded
    FAILED,         // Download failed
    PAUSED          // User paused
}

/**
 * Type of media being downloaded.
 */
// MediaType is defined in LibraryItem.kt
