package com.unifiedotaku.app.data.extensions

data class RawChapter(
    val url: String,
    val name: String,
    val uploadDate: String?,
    val scanlator: String? = null
)
