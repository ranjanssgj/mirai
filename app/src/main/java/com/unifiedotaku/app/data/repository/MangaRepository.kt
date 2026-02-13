package com.unifiedotaku.app.data.repository

import com.unifiedotaku.app.data.remote.api.ChapterDto
import com.unifiedotaku.app.data.remote.api.MangaDetailsDto
import com.unifiedotaku.app.data.remote.api.MangaDto

interface MangaRepository {
    suspend fun searchManga(query: String, extension: String): Result<List<MangaDto>>
    suspend fun getMangaDetails(id: String, extension: String): Result<MangaDetailsDto>
    suspend fun getMangaDetailsFromJikan(id: String): Result<com.unifiedotaku.app.data.model.anime.MangaDto>
    suspend fun getMangaChapters(id: String, extension: String): Result<List<ChapterDto>>
    suspend fun getMangaPages(chapterId: String, extension: String): Result<List<String>>
    suspend fun getMangaHome(): com.unifiedotaku.app.data.remote.api.MangaHomeResponse
}
