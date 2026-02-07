package com.unifiedotaku.app.data.remote.api

import retrofit2.http.*

/**
 * MyAnimeList API service for forum posts and tracking.
 * Uses OAuth2 authorization.
 */
interface MalApiService {
    
    companion object {
        const val BASE_URL = "https://api.myanimelist.net/v2/"
        const val AUTH_URL = "https://myanimelist.net/v1/oauth2/authorize"
        const val TOKEN_URL = "https://myanimelist.net/v1/oauth2/token"
    }

    // ==================== User Info ====================
    
    @GET("users/@me")
    suspend fun getCurrentUser(
        @Header("Authorization") auth: String
    ): MalUser

    // ==================== Anime List ====================
    
    @GET("users/@me/animelist")
    suspend fun getAnimeList(
        @Header("Authorization") auth: String,
        @Query("status") status: String? = null,
        @Query("sort") sort: String = "list_updated_at",
        @Query("limit") limit: Int = 100,
        @Query("offset") offset: Int = 0,
        @Query("fields") fields: String = "list_status,num_episodes,mean"
    ): MalAnimeListResponse

    @PATCH("anime/{anime_id}/my_list_status")
    @FormUrlEncoded
    suspend fun updateAnimeStatus(
        @Header("Authorization") auth: String,
        @Path("anime_id") animeId: Int,
        @Field("status") status: String? = null,
        @Field("score") score: Int? = null,
        @Field("num_watched_episodes") watchedEpisodes: Int? = null
    ): MalAnimeListStatus

    @DELETE("anime/{anime_id}/my_list_status")
    suspend fun deleteAnimeFromList(
        @Header("Authorization") auth: String,
        @Path("anime_id") animeId: Int
    )

    // ==================== Manga List ====================
    
    @GET("users/@me/mangalist")
    suspend fun getMangaList(
        @Header("Authorization") auth: String,
        @Query("status") status: String? = null,
        @Query("sort") sort: String = "list_updated_at",
        @Query("limit") limit: Int = 100,
        @Query("offset") offset: Int = 0,
        @Query("fields") fields: String = "list_status,num_chapters,mean"
    ): MalMangaListResponse

    @PATCH("manga/{manga_id}/my_list_status")
    @FormUrlEncoded
    suspend fun updateMangaStatus(
        @Header("Authorization") auth: String,
        @Path("manga_id") mangaId: Int,
        @Field("status") status: String? = null,
        @Field("score") score: Int? = null,
        @Field("num_chapters_read") chaptersRead: Int? = null
    ): MalMangaListStatus

    // ==================== Search ====================
    
    @GET("anime")
    suspend fun searchAnime(
        @Header("Authorization") auth: String,
        @Query("q") query: String,
        @Query("limit") limit: Int = 20,
        @Query("fields") fields: String = "id,title,main_picture,mean,num_episodes,status"
    ): MalSearchResponse

    @GET("manga")
    suspend fun searchManga(
        @Header("Authorization") auth: String,
        @Query("q") query: String,
        @Query("limit") limit: Int = 20,
        @Query("fields") fields: String = "id,title,main_picture,mean,num_chapters,status"
    ): MalSearchResponse

    // ==================== Forum ====================
    
    @GET("forum/boards")
    suspend fun getForumBoards(
        @Header("Authorization") auth: String
    ): MalForumBoardsResponse

    @GET("forum/topics")
    suspend fun getForumTopics(
        @Header("Authorization") auth: String,
        @Query("board_id") boardId: Int? = null,
        @Query("subboard_id") subboardId: Int? = null,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0,
        @Query("sort") sort: String = "recent",
        @Query("q") query: String? = null
    ): MalForumTopicsResponse

    @GET("forum/topic/{topic_id}")
    suspend fun getForumTopic(
        @Header("Authorization") auth: String,
        @Path("topic_id") topicId: Int,
        @Query("limit") limit: Int = 100,
        @Query("offset") offset: Int = 0
    ): MalForumTopicDetailResponse
}

// ==================== Response Models ====================

data class MalUser(
    val id: Int,
    val name: String,
    val picture: String?,
    val gender: String?,
    val location: String?,
    val joined_at: String,
    val anime_statistics: MalAnimeStatistics?
)

data class MalAnimeStatistics(
    val num_items_watching: Int,
    val num_items_completed: Int,
    val num_items_on_hold: Int,
    val num_items_dropped: Int,
    val num_items_plan_to_watch: Int,
    val num_items: Int,
    val num_days_watched: Float,
    val mean_score: Float
)

data class MalAnimeListResponse(
    val data: List<MalAnimeListEntry>,
    val paging: MalPaging?
)

data class MalAnimeListEntry(
    val node: MalAnimeNode,
    val list_status: MalAnimeListStatus?
)

data class MalAnimeNode(
    val id: Int,
    val title: String,
    val main_picture: MalPicture?,
    val mean: Float?,
    val num_episodes: Int?,
    val status: String?
)

data class MalAnimeListStatus(
    val status: String?,
    val score: Int,
    val num_episodes_watched: Int,
    val is_rewatching: Boolean,
    val updated_at: String?
)

data class MalMangaListResponse(
    val data: List<MalMangaListEntry>,
    val paging: MalPaging?
)

data class MalMangaListEntry(
    val node: MalMangaNode,
    val list_status: MalMangaListStatus?
)

data class MalMangaNode(
    val id: Int,
    val title: String,
    val main_picture: MalPicture?,
    val mean: Float?,
    val num_chapters: Int?,
    val status: String?
)

data class MalMangaListStatus(
    val status: String?,
    val score: Int,
    val num_chapters_read: Int,
    val is_rereading: Boolean,
    val updated_at: String?
)

data class MalPicture(
    val medium: String?,
    val large: String?
)

data class MalPaging(
    val next: String?
)

data class MalSearchResponse(
    val data: List<MalSearchEntry>,
    val paging: MalPaging?
)

data class MalSearchEntry(
    val node: MalAnimeNode
)

// ==================== Forum Models ====================

data class MalForumBoardsResponse(
    val categories: List<MalForumCategory>
)

data class MalForumCategory(
    val title: String,
    val boards: List<MalForumBoard>
)

data class MalForumBoard(
    val id: Int,
    val title: String,
    val description: String,
    val subboards: List<MalForumSubboard>?
)

data class MalForumSubboard(
    val id: Int,
    val title: String
)

data class MalForumTopicsResponse(
    val data: List<MalForumTopic>,
    val paging: MalPaging?
)

data class MalForumTopic(
    val id: Int,
    val title: String,
    val created_at: String,
    val created_by: MalForumUser,
    val number_of_posts: Int,
    val last_post_created_at: String?,
    val last_post_created_by: MalForumUser?
)

data class MalForumUser(
    val id: Int,
    val name: String,
    val forum_avator: String?
)

data class MalForumTopicDetailResponse(
    val data: MalForumTopicDetail,
    val paging: MalPaging?
)

data class MalForumTopicDetail(
    val title: String,
    val posts: List<MalForumPost>
)

data class MalForumPost(
    val id: Int,
    val number: Int,
    val created_at: String,
    val created_by: MalForumUser,
    val body: String,
    val signature: String?
)
