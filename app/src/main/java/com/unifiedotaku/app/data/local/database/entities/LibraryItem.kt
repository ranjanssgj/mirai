package com.unifiedotaku.app.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a series (Anime or Manga) in the user's library.
 */
@Entity(tableName = "library_items")
data class LibraryItem(
    @PrimaryKey 
    val id: String,                        // Unique series ID from source
    val title: String,
    val titleAlternate: String? = null,    // Japanese/Romaji title
    val coverUrl: String,
    val bannerUrl: String? = null,
    val type: MediaType,                   // ANIME or MANGA
    val status: LibraryStatus,             // User's watch/read status
    val seriesStatus: SeriesStatus,        // Ongoing, Completed, etc.
    val progress: Int = 0,                 // Current episode/chapter
    val totalCount: Int? = null,           // Total episodes/chapters (null if unknown)
    val score: Float? = null,              // User rating 1-10
    val synopsis: String? = null,
    val genres: String? = null,            // Comma-separated genre list
    val year: Int? = null,
    val season: String? = null,            // For anime: Winter, Spring, Summer, Fall
    val studio: String? = null,            // For anime
    val author: String? = null,            // For manga
    val source: String,                    // Source website identifier
    val sourceUrl: String,                 // Original URL
    val startDate: Long? = null,           // When user started watching/reading
    val finishDate: Long? = null,          // When user finished
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Type of media content.
 */
enum class MediaType {
    ANIME,
    MANGA
}

/**
 * User's personal status for tracking progress.
 */
enum class LibraryStatus {
    WATCHING,       // Currently watching (Anime)
    READING,        // Currently reading (Manga)
    PLANNED,        // Plan to watch/read
    COMPLETED,      // Finished
    ON_HOLD,        // Paused
    DROPPED         // Abandoned
}

/**
 * Publishing/Airing status of the series itself.
 */
enum class SeriesStatus {
    ONGOING,
    COMPLETED,
    UPCOMING,
    HIATUS,
    CANCELLED,
    UNKNOWN
}
