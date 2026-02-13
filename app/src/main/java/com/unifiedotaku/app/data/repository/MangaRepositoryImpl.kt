package com.unifiedotaku.app.data.repository

import com.unifiedotaku.app.data.extensions.ExtensionManager
import com.unifiedotaku.app.data.remote.api.ChapterDto
import com.unifiedotaku.app.utils.ChapterSanitizer
import javax.inject.Inject

class MangaRepositoryImpl @Inject constructor(
    private val jikanApi: com.unifiedotaku.app.data.remote.api.JikanApi,
    private val extensionManager: ExtensionManager
) : MangaRepository {

    override suspend fun getMangaDetailsFromJikan(id: String): Result<com.unifiedotaku.app.data.model.anime.MangaDto> {
        return try {
            val response = jikanApi.getMangaDetails(id)
            Result.success(response.data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun searchManga(query: String, extension: String): Result<List<com.unifiedotaku.app.data.remote.api.MangaDto>> {
        return try {
            val source = extensionManager.getMangaSource(extension)
            if (source != null) {
                Result.success(source.searchManga(query))
            } else {
                Result.failure(Exception("Manga source not found: $extension"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMangaDetails(id: String, extension: String): Result<com.unifiedotaku.app.data.remote.api.MangaDetailsDto> {
        return try {
            val source = extensionManager.getMangaSource(extension)
            if (source != null) {
                Result.success(source.getMangaDetails(id))
            } else {
                Result.failure(Exception("Manga source not found: $extension"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMangaChapters(id: String, extension: String): Result<List<com.unifiedotaku.app.data.remote.api.ChapterDto>> {
        return try {
            val source = extensionManager.getMangaSource(extension)
            if (source != null) {
                val raw = source.getChapters(id)
                val cleanResult = ChapterSanitizer.sanitize(raw, id).map { 
                    ChapterDto(id = it.id, number = it.number, title = it.title ?: "Chapter ${it.number}", date = it.releaseDate?.toString()) 
                }
                Result.success(cleanResult)
            } else {
                Result.failure(Exception("Manga source not found: $extension"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMangaPages(chapterId: String, extension: String): Result<List<String>> {
        return try {
            val source = extensionManager.getMangaSource(extension)
            if (source != null) {
                Result.success(source.getPages(chapterId))
            } else {
                Result.failure(Exception("Manga source not found: $extension"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMangaHome(): com.unifiedotaku.app.data.remote.api.MangaHomeResponse {
        val extensions = extensionManager.getAllMangaSources().mapIndexed { index, source ->
            val extensionId = extensionManager.getAllExtensionIds().getOrElse(index) { source.name }
            val updates = try {
                source.getLatestUpdates()
            } catch (e: Exception) {
                emptyList()
            }
            
            com.unifiedotaku.app.data.remote.api.ExtensionContent(
                name = extensionId,
                icon = "",
                latestUpdates = updates
            )
        }
        return com.unifiedotaku.app.data.remote.api.MangaHomeResponse(extensions)
    }
}
