package com.unifiedotaku.app.data.extensions

import com.unifiedotaku.app.data.remote.api.MangaDetailsDto
import com.unifiedotaku.app.data.remote.api.MangaDto

interface MangaSource {
    val name: String
    val baseUrl: String
    suspend fun searchManga(query: String): List<MangaDto>
    suspend fun getMangaDetails(id: String): MangaDetailsDto
    suspend fun getChapters(id: String): List<RawChapter>
    suspend fun getPages(chapterId: String): List<String>
    suspend fun getLatestUpdates(): List<MangaDto>
    val headers: Map<String, String>
    val id: Long
}
