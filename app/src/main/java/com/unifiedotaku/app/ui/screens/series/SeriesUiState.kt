package com.unifiedotaku.app.ui.screens.series

import com.unifiedotaku.app.data.local.database.entities.LibraryStatus
import com.unifiedotaku.app.domain.model.Chapter
import com.unifiedotaku.app.domain.model.Episode
import com.unifiedotaku.app.domain.model.Series

/**
 * UI state for the Series Detail screen.
 */
/** One related season/adaptation for the series (e.g. Prequel, Sequel, Manga). */
data class RelatedEntry(
    val relation: String,
    val malId: Int,
    val type: String,
    val name: String,
    val url: String
)

data class SeriesUiState(
    val isLoading: Boolean = true,
    val series: Series? = null,
    val episodes: List<Episode> = emptyList(),
    val chapters: List<Chapter> = emptyList(),
    val note: String = "",
    val libraryStatus: LibraryStatus? = null,
    val selectedSeason: Int? = null,
    val availableSeasons: List<Int> = emptyList(),
    val error: String? = null,
    val isRefreshing: Boolean = false,
    val streamUrl: String? = null,
    val streamReferer: String? = null,
    val isStreamLoading: Boolean = false,
    val relatedSeasons: List<RelatedEntry> = emptyList(),
    val adaptationMangaId: String? = null
)

/**
 * Download dialog options for anime.
 */
sealed class AnimeDownloadOption {
    data object AllEpisodes : AnimeDownloadOption()
    data object UnwatchedEpisodes : AnimeDownloadOption()
    data class SelectedSeason(val season: Int) : AnimeDownloadOption()
    data class SelectedEpisodes(val episodeIds: List<String>) : AnimeDownloadOption()
}

/**
 * Download dialog options for manga.
 */
sealed class MangaDownloadOption {
    data object AllChapters : MangaDownloadOption()
    data object UnreadChapters : MangaDownloadOption()
    data object NextChapter : MangaDownloadOption()
    data object Next5Chapters : MangaDownloadOption()
    data object Next10Chapters : MangaDownloadOption()
    data object Next25Chapters : MangaDownloadOption()
    data class SelectedChapters(val chapterIds: List<String>) : MangaDownloadOption()
}
