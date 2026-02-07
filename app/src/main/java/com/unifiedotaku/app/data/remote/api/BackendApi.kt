package com.unifiedotaku.app.data.remote.api


import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface BackendApi {
    // Streaming
    @POST("anime/stream")
    suspend fun getStreamUrl(@Body request: StreamRequest): StreamResponse

    // Manga Extension
    @GET("manga/search")
    suspend fun searchManga(
        @Query("query") query: String,
        @Query("extension") extension: String = "comix.to"
    ): List<MangaDto>

    @GET("manga/details")
    suspend fun getMangaDetails(
        @Query("id") id: String,
        @Query("extension") extension: String = "comix.to"
    ): MangaDetailsDto

    @GET("manga/chapters")
    suspend fun getMangaChapters(
        @Query("id") id: String,
        @Query("extension") extension: String = "comix.to"
    ): List<ChapterDto>
    
    @GET("manga/pages")
    suspend fun getMangaPages(
        @Query("chapterId") chapterId: String,
        @Query("extension") extension: String = "comix.to"
    ): List<String>

    @GET("manga-home")
    suspend fun getMangaHome(): MangaHomeResponse
}

data class MangaHomeResponse(val extensions: List<ExtensionContent>)
data class ExtensionContent(
    val id: String,
    val name: String,
    val icon: String,
    val latestUpdates: List<MangaDto>
)

data class StreamRequest(val animeName: String, val episode: Int)
data class StreamResponse(
    val url: String,
    val referer: String? = null,
    val userAgent: String? = null,
    val subtitles: List<com.unifiedotaku.app.domain.model.Subtitle> = emptyList(),
    val isM3u8: Boolean = true
)


// Placeholder DTOs - Will be moved to model package
data class MangaDto(val id: String, val title: String, val cover: String)
data class MangaDetailsDto(val id: String, val title: String, val description: String, val author: String, val status: String, val cover: String, val genres: List<String> = emptyList())
data class ChapterDto(val id: String, val number: Float, val title: String, val date: String? = null)
