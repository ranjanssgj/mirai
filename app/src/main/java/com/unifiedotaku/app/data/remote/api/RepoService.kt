package com.unifiedotaku.app.data.remote.api

import retrofit2.http.GET

interface RepoService {
    @GET("index.min.json")
    suspend fun getExtensions(): List<RepoExtension>
}
