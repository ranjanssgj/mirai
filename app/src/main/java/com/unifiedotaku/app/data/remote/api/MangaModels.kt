package com.unifiedotaku.app.data.remote.api

import com.google.gson.annotations.SerializedName

data class MangaDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("cover") val cover: String
)

data class MangaDetailsDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("cover") val cover: String,
    @SerializedName("description") val description: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("author") val author: String?,
    @SerializedName("genres") val genres: List<String>?
)

data class ExtensionContent(
    @SerializedName("name") val name: String,
    @SerializedName("icon") val icon: String,
    @SerializedName("latestUpdates") val latestUpdates: List<MangaDto>
)

data class ChapterDto(
    @SerializedName("id") val id: String,
    @SerializedName("number") val number: Float,
    @SerializedName("title") val title: String,
    @SerializedName("date") val date: String?
)

data class MangaHomeResponse(
    @SerializedName("extensions") val extensions: List<ExtensionContent>
)

// Extension Repository Models
data class RepoExtension(
    @SerializedName("name") val name: String,
    @SerializedName("pkg") val pkg: String,
    @SerializedName("apk") val apk: String,
    @SerializedName("lang") val lang: String,
    @SerializedName("code") val code: Int,
    @SerializedName("version") val version: String,
    @SerializedName("nsfw") val nsfw: Int,
    @SerializedName("sources") val sources: List<RepoSource>?
)

data class RepoSource(
    @SerializedName("name") val name: String,
    @SerializedName("lang") val lang: String,
    @SerializedName("id") val id: String,
    @SerializedName("baseUrl") val baseUrl: String
)
