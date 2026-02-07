package com.unifiedotaku.app.data.repository

import com.unifiedotaku.app.data.remote.api.BackendApi
import com.unifiedotaku.app.data.remote.api.ChapterDto
import com.unifiedotaku.app.data.remote.api.MangaDetailsDto
import com.unifiedotaku.app.data.remote.api.MangaDto
import javax.inject.Inject

class MangaRepositoryImpl @Inject constructor(
    private val backendApi: BackendApi
) : MangaRepository {

    override suspend fun searchManga(query: String, extension: String): Result<List<MangaDto>> {
        return try {
            val results = backendApi.searchManga(query, extension)
            Result.success(results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMangaDetails(id: String, extension: String): Result<MangaDetailsDto> {
        return try {
             val details = backendApi.getMangaDetails(id, extension)
             Result.success(details)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMangaChapters(id: String, extension: String): Result<List<ChapterDto>> {
        return try {
             val chapters = backendApi.getMangaChapters(id, extension)
             Result.success(chapters)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMangaPages(chapterId: String, extension: String): Result<List<String>> {
        return try {
             val pages = backendApi.getMangaPages(chapterId, extension)
             Result.success(pages)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMangaHome(): com.unifiedotaku.app.data.remote.api.MangaHomeResponse {
        return backendApi.getMangaHome()
    }
}
