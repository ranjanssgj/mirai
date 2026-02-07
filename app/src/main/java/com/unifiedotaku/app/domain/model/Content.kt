package com.unifiedotaku.app.domain.model

/**
 * Domain model representing an anime episode.
 */
data class Episode(
    val id: String,
    val seriesId: String,
    val number: Int,
    val title: String? = null,
    val description: String? = null,
    val thumbnailUrl: String? = null,
    val duration: Long? = null,            // Duration in milliseconds
    val seasonNumber: Int? = null,
    val isFiller: Boolean = false,
    val airDate: Long? = null,
    val watchProgress: Long = 0,           // Watch position in ms
    val isWatched: Boolean = false,
    val isDownloaded: Boolean = false
)

/**
 * Domain model representing a manga chapter.
 */
data class Chapter(
    val id: String,
    val seriesId: String,
    val number: Float,                     // Float for 10.5 chapters
    val title: String? = null,
    val volume: Int? = null,
    val pageCount: Int = 0,
    val releaseDate: Long? = null,
    val scanlator: String? = null,         // Scanlation group
    val readProgress: Int = 0,             // Current page
    val isRead: Boolean = false,
    val isDownloaded: Boolean = false
)
