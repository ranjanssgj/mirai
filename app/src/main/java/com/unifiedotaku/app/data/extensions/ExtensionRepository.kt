package com.unifiedotaku.app.data.extensions

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.unifiedotaku.app.data.remote.api.RepoExtension
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExtensionRepository @Inject constructor(
    private val client: OkHttpClient,
    private val gson: Gson,
    private val repoService: com.unifiedotaku.app.data.remote.api.RepoService
) {
    private val repoUrl = "https://raw.githubusercontent.com/yuzono/manga-repo/repo/index.min.json"
    private val baseUrl = "https://raw.githubusercontent.com/yuzono/manga-repo/repo"

    suspend fun getAvailableExtensions(): List<RepoExtension> {
        return try {
            repoService.getExtensions()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun getApkUrl(extension: RepoExtension): String {
        return "$baseUrl/apk/${extension.apk}"
    }
    
    fun getIconUrl(extension: RepoExtension): String {
        return "$baseUrl/icon/${extension.pkg}.png"
    }
}
