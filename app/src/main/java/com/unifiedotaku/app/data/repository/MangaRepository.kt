package com.unifiedotaku.app.data.repository

import com.unifiedotaku.app.data.remote.api.ChapterDto
import com.unifiedotaku.app.data.remote.api.MangaDetailsDto
import com.unifiedotaku.app.data.remote.api.MangaDto

interface MangaRepository {
    suspend fun searchManga(query: String, extension: String = "comix.to"): Result<List<MangaDto>>
    suspend fun getMangaDetails(id: String, extension: String = "comix.to"): Result<MangaDetailsDto>
    suspend fun getMangaChapters(id: String, extension: String = "comix.to"): Result<List<ChapterDto>>
    suspend fun getMangaPages(chapterId: String, extension: String = "comix.to"): Result<List<String>>
    suspend fun getMangaHome(): com.unifiedotaku.app.data.remote.api.MangaHomeResponse
}
