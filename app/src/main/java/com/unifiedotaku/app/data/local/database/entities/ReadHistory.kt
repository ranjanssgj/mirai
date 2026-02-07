package com.unifiedotaku.app.data.local.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Tracks reading progress for manga chapters.
 */
@Entity(
    tableName = "read_history",
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
data class ReadHistory(
    @PrimaryKey(autoGenerate = true) 
    val id: Long = 0,
    val seriesId: String,                  // FK to LibraryItem
    val chapterId: String,                 // Unique chapter identifier
    val chapterNumber: Float,              // Float to support 10.5 chapters
    val chapterTitle: String? = null,
    val currentPage: Int = 0,              // For resume
    val totalPages: Int = 0,
    val isCompleted: Boolean = false,      // True if read all pages
    val completedAt: Long? = null,
    val lastReadAt: Long = System.currentTimeMillis()
)
