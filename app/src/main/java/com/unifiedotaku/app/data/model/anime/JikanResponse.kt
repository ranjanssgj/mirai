package com.unifiedotaku.app.data.model.anime

import com.google.gson.annotations.SerializedName

data class JikanResponse(
    @SerializedName("data") val data: List<AnimeDto>,
    @SerializedName("pagination") val pagination: PaginationDto
)

data class JikanSingleResponse(
    @SerializedName("data") val data: AnimeDto
)

data class JikanEpisodesResponse(
    @SerializedName("data") val data: List<JikanEpisodeDto>,
    @SerializedName("pagination") val pagination: PaginationDto
)

data class JikanEpisodeDto(
    @SerializedName("mal_id") val malId: Int,
    @SerializedName("title") val title: String,
    @SerializedName("title_japanese") val titleJapanese: String?,
    @SerializedName("title_romanji") val titleRomanji: String?,
    @SerializedName("score") val score: Double?,
    @SerializedName("aired") val aired: String?,
    @SerializedName("filler") val filler: Boolean,
    @SerializedName("recap") val recap: Boolean,
    @SerializedName("forum_url") val forumUrl: String?
)

/** Jikan relation entry: one relation type with list of MAL entries (anime/manga). */
data class RelationEntryDto(
    @SerializedName("relation") val relation: String,
    @SerializedName("entry") val entry: List<RelationMalEntryDto> = emptyList()
)

data class RelationMalEntryDto(
    @SerializedName("mal_id") val malId: Int,
    @SerializedName("type") val type: String,
    @SerializedName("name") val name: String,
    @SerializedName("url") val url: String
)

data class AnimeDto(
    @SerializedName("mal_id") val malId: Int,
    @SerializedName("url") val url: String,
    @SerializedName("images") val images: AnimeImagesDto,
    @SerializedName("trailer") val trailer: TrailerDto,
    @SerializedName("title") val title: String,
    @SerializedName("title_english") val titleEnglish: String?,
    @SerializedName("title_japanese") val titleJapanese: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("source") val source: String?,
    @SerializedName("episodes") val episodes: Int?,
    @SerializedName("status") val status: String?,
    @SerializedName("airing") val airing: Boolean,
    @SerializedName("duration") val duration: String?,
    @SerializedName("rating") val rating: String?,
    @SerializedName("score") val score: Double?,
    @SerializedName("scored_by") val scoredBy: Int?,
    @SerializedName("rank") val rank: Int?,
    @SerializedName("popularity") val popularity: Int?,
    @SerializedName("members") val members: Int?,
    @SerializedName("favorites") val favorites: Int?,
    @SerializedName("synopsis") val synopsis: String?,
    @SerializedName("background") val background: String?,
    @SerializedName("season") val season: String?,
    @SerializedName("year") val year: Int?,
    @SerializedName("broadcast") val broadcast: BroadcastDto?,
    @SerializedName("producers") val producers: List<EntityDto> = emptyList(),
    @SerializedName("licensors") val licensors: List<EntityDto> = emptyList(),
    @SerializedName("studios") val studios: List<EntityDto> = emptyList(),
    @SerializedName("genres") val genres: List<EntityDto> = emptyList(),
    @SerializedName("relations") val relations: List<RelationEntryDto> = emptyList()
)

data class AnimeImagesDto(
    @SerializedName("jpg") val jpg: ImageTypeDto,
    @SerializedName("webp") val webp: ImageTypeDto
)

data class ImageTypeDto(
    @SerializedName("image_url") val imageUrl: String,
    @SerializedName("small_image_url") val smallImageUrl: String,
    @SerializedName("large_image_url") val largeImageUrl: String
)

data class TrailerDto(
    @SerializedName("youtube_id") val youtubeId: String?,
    @SerializedName("url") val url: String?,
    @SerializedName("embed_url") val embedUrl: String?
)

data class BroadcastDto(
    @SerializedName("day") val day: String?,
    @SerializedName("time") val time: String?,
    @SerializedName("timezone") val timezone: String?,
    @SerializedName("string") val string: String?
)

data class EntityDto(
    @SerializedName("mal_id") val malId: Int,
    @SerializedName("type") val type: String,
    @SerializedName("name") val name: String,
    @SerializedName("url") val url: String
)

data class PaginationDto(
    @SerializedName("last_visible_page") val lastVisiblePage: Int,
    @SerializedName("has_next_page") val hasNextPage: Boolean,
    @SerializedName("current_page") val currentPage: Int,
    @SerializedName("items") val items: ItemsDto
)

data class ItemsDto(
    @SerializedName("count") val count: Int,
    @SerializedName("total") val total: Int,
    @SerializedName("per_page") val perPage: Int
)
