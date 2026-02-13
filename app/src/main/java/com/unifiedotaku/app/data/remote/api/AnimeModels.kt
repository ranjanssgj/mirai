package com.unifiedotaku.app.data.remote.api

import com.google.gson.annotations.SerializedName

data class StreamRequest(
    @SerializedName("title") val title: String,
    @SerializedName("episode") val episode: Int,
    @SerializedName("server") val server: String? = null,
    @SerializedName("category") val category: String? = null
)

data class StreamResponse(
    @SerializedName("url") val url: String,
    @SerializedName("referer") val referer: String? = null,
    @SerializedName("userAgent") val userAgent: String? = null,
    @SerializedName("isM3u8") val isM3u8: Boolean = true,
    @SerializedName("quality") val quality: String? = null,
    @SerializedName("subtitles") val subtitles: List<SubtitleDto> = emptyList()
)

data class SubtitleDto(
    @SerializedName("url") val url: String,
    @SerializedName("lang") val lang: String,
    @SerializedName("label") val label: String? = null
)

data class ServerDto(
    @SerializedName("name") val name: String,
    @SerializedName("serverParam") val serverParam: String,
    @SerializedName("category") val category: String? = null
)
