package com.unifiedotaku.app.domain.model

/**
 * Represents a video stream source for anime playback.
 */
data class StreamSource(
    val name: String = "",                  // Server name (e.g., VidStream, StreamSB)
    val url: String,                        // .m3u8 or .mp4 URL
    val quality: String = "auto",           // 1080p, 720p, 480p, auto
    val type: StreamType = StreamType.HLS,
    val isM3U8: Boolean = false,
    val subtitles: List<Subtitle> = emptyList(),
    val headers: Map<String, String> = emptyMap()  // Required headers for playback
)

enum class StreamType {
    HLS,    // .m3u8 streams
    MP4,    // Direct MP4 files
    DASH    // DASH streams
}

/**
 * Subtitle track for video playback.
 */
data class Subtitle(
    val url: String,
    val language: String,
    val label: String,
    val isDefault: Boolean = false
)

/**
 * Represents an image page in a manga chapter.
 */
data class MangaPage(
    val index: Int,
    val imageUrl: String,
    val width: Int = 0,
    val height: Int = 0,
    val headers: Map<String, String> = emptyMap()
)

/**
 * Domain model for an anime episode.
 */
/**
 * Domain model for an anime episode from a source.
 */
data class SourceEpisode(
    val id: String,
    val number: Float,
    val title: String = "",
    val thumbnail: String? = null,
    val synopsis: String? = null,
    val duration: Int? = null,           // Duration in minutes
    val isFiller: Boolean = false,
    val isRecap: Boolean = false,
    val airDate: String? = null
)

/**
 * Domain model for a manga chapter from a source.
 */
data class SourceChapter(
    val id: String,
    val number: Float,
    val title: String? = null,
    val volume: Int? = null,
    val releaseDate: String? = null,
    val scanlator: String? = null,
    val pageCount: Int? = null
)
