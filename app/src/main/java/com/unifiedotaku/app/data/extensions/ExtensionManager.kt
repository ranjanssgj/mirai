package com.unifiedotaku.app.data.extensions

import com.unifiedotaku.app.data.remote.api.ChapterDto
import com.unifiedotaku.app.data.remote.api.MangaDetailsDto
import com.unifiedotaku.app.data.remote.api.MangaDto
import com.unifiedotaku.app.data.remote.api.StreamResponse
import com.unifiedotaku.app.data.model.anime.AnimeDto

interface AnimeSource {
    val name: String
    val baseUrl: String
    suspend fun searchAnime(query: String): List<AnimeDto>
    suspend fun getStreamUrl(animeName: String, episodeNumber: Int): StreamResponse?
}

interface MangaSource {
    val name: String
    val baseUrl: String
    suspend fun searchManga(query: String): List<MangaDto>
    suspend fun getMangaDetails(id: String): MangaDetailsDto
    suspend fun getChapters(id: String): List<ChapterDto>
    suspend fun getPages(chapterId: String): List<String>
}

class ExtensionManager {
    private val animeSources = mutableMapOf<String, AnimeSource>()
    private val mangaSources = mutableMapOf<String, MangaSource>()

    fun registerAnimeSource(source: AnimeSource) {
        animeSources[source.name] = source
    }

    fun registerMangaSource(source: MangaSource) {
        mangaSources[source.name] = source
    }

    fun getAnimeSource(name: String): AnimeSource? = animeSources[name]
    fun getMangaSource(name: String): MangaSource? = mangaSources[name]
    
    // Default sources

    fun getComixSource(): MangaSource? = getMangaSource("Comix")

    fun getAllAnimeSources(): List<AnimeSource> = animeSources.values.toList()
    fun getAllMangaSources(): List<MangaSource> = mangaSources.values.toList()
}
