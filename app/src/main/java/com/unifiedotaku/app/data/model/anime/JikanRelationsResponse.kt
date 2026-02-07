package com.unifiedotaku.app.data.model.anime

import com.google.gson.annotations.SerializedName

data class JikanRelationsResponse(
    @SerializedName("data") val data: List<RelationEntryDto>
)
