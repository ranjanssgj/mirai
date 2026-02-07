package com.unifiedotaku.app.data.local.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Tracks watching progress for anime episodes.
 */
@Entity(
    tableName = "watch_history",
    foreignKeys = [
        ForeignKey(
            entity = LibraryItem::class,
            parentColumns = ["id"],
            childColumns = ["seriesId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("seriesId")]
)
data class WatchHistory(
    @PrimaryKey(autoGenerate = true) 
    val id: Long = 0,
    val seriesId: String,                  // FK to LibraryItem
    val episodeId: String,                 // Unique episode identifier
    val episodeNumber: Int,
    val episodeTitle: String? = null,
    val seasonNumber: Int? = null,
    val watchedDurationMs: Long = 0,       // Position in video (for resume)
    val totalDurationMs: Long = 0,         // Total video duration
    val isCompleted: Boolean = false,      // True if watched > 90%
    val completedAt: Long? = null,         // Timestamp when completed
    val lastWatchedAt: Long = System.currentTimeMillis()
)
