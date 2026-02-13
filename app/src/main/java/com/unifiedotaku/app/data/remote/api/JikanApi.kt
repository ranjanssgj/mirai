package com.unifiedotaku.app.data.remote.api

import com.unifiedotaku.app.data.model.anime.JikanResponse
import com.unifiedotaku.app.data.model.anime.JikanSingleResponse
import com.unifiedotaku.app.data.model.anime.JikanEpisodesResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface JikanApi {
    @GET("top/anime")
    suspend fun getTopAnime(
        @Query("filter") filter: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 25
    ): JikanResponse

    @GET("anime/{id}")
    suspend fun getAnimeDetails(@Path("id") id: String): JikanSingleResponse

    /** Full anime response including relations (Prequel, Sequel, Adaptation, etc.). */
    @GET("anime/{id}/full")
    suspend fun getAnimeFull(@Path("id") id: String): JikanSingleResponse
    
    @GET("anime/{id}/episodes")
    suspend fun getAnimeEpisodes(@Path("id") id: String, @Query("page") page: Int = 1): JikanEpisodesResponse

    @GET("anime")
    suspend fun searchAnime(
        @Query("q") q: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 25
    ): JikanResponse
    @GET("anime/{id}/relations")
    suspend fun getAnimeRelations(@Path("id") id: String): com.unifiedotaku.app.data.model.anime.JikanRelationsResponse

    @GET("manga/{id}")
    suspend fun getMangaDetails(@Path("id") id: String): com.unifiedotaku.app.data.model.anime.JikanMangaSingleResponse
}
