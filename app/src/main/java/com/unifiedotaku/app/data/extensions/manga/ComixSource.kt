package com.unifiedotaku.app.data.extensions.manga

import com.unifiedotaku.app.data.extensions.MangaSource
import com.unifiedotaku.app.data.remote.api.ChapterDto
import com.unifiedotaku.app.data.remote.api.MangaDetailsDto
import com.unifiedotaku.app.data.remote.api.MangaDto
import okhttp3.OkHttpClient
import okhttp3.Request
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonArray
import javax.inject.Inject

class ComixSource @Inject constructor(
    private val client: OkHttpClient,
    private val gson: Gson
) : MangaSource {
    
    override val name = "Comix"
    override val baseUrl = "https://api.comix.to"

    override suspend fun searchManga(query: String): List<MangaDto> {
        val url = "$baseUrl/v1.0/search?q=$query&limit=25&page=1"
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            .build()
        
        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    println("ComixSource: Search failed with code ${response.code}")
                    return emptyList()
                }
                val body = response.body?.string() ?: return emptyList()
                
                if (body.trim().startsWith("<")) {
                     println("ComixSource: Received HTML instead of JSON")
                     return emptyList()
                }

                try {
                    val json = gson.fromJson(body, JsonArray::class.java)
                    json.map { 
                        val item = it.asJsonObject
                        val hid = item.get("hid").asString
                        val title = item.get("title").asString
                        val mdCovers = item.getAsJsonArray("md_covers")
                        val coverFileName = if (mdCovers != null && mdCovers.size() > 0) {
                            mdCovers[0].asJsonObject.get("b2key").asString
                        } else ""
                        val coverUrl = if (coverFileName.isNotEmpty()) "https://meo.comix.pictures/$coverFileName" else ""
    
                        MangaDto(id = hid, title = title, cover = coverUrl)
                    }
                } catch (e: Exception) {
                    println("ComixSource: Failed to parse JSON: ${e.message}")
                    emptyList()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun getMangaDetails(id: String): MangaDetailsDto {
        val url = "$baseUrl/comic/$id"
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw Exception("Failed to fetch details")
                val body = response.body?.string() ?: throw Exception("Empty response")
                val json = gson.fromJson(body, JsonObject::class.java)
                val comic = json.getAsJsonObject("comic")
                
                val title = comic.get("title").asString
                val desc = comic.get("desc").asString
                val status = when(comic.get("status").asInt) {
                    1 -> "Ongoing"
                    2 -> "Completed"
                    else -> "Unknown"
                }

                val mdCovers = comic.getAsJsonArray("md_covers")
                val coverFileName = if (mdCovers != null && mdCovers.size() > 0) {
                     mdCovers[0].asJsonObject.get("b2key").asString
                } else ""
                val coverUrl = if (coverFileName.isNotEmpty()) "https://meo.comix.pictures/$coverFileName" else ""

                val authors = comic.getAsJsonArray("md_comic_md_genres")?.mapNotNull { 
                    val jsonObj = it.asJsonObject
                    if (jsonObj.has("md_genres")) jsonObj.getAsJsonObject("md_genres").get("name").asString else null
                } ?: emptyList()
                
                val author = "Unknown"

                MangaDetailsDto(
                    id = id,
                    title = title,
                    description = desc,
                    author = author,
                    status = status,
                    cover = coverUrl,
                    genres = authors
                )
            }
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun getChapters(id: String): List<ChapterDto> {
        val url = "$baseUrl/comic/$id/chapters?limit=10000&lang=en"
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return emptyList()
                val body = response.body?.string() ?: return emptyList()
                val json = gson.fromJson(body, JsonObject::class.java)
                val chapters = json.getAsJsonArray("chapters")
                
                chapters.map {
                    val item = it.asJsonObject
                    val hid = item.get("hid").asString
                    val chapName = if (item.has("chap")) item.get("chap").asString else "One Shot"
                    val title = if (item.has("title") && !item.get("title").isJsonNull) item.get("title").asString else ""
                    val created = item.get("created_at").asString
                    
                    ChapterDto(
                        id = hid,
                        number = chapName.toFloatOrNull() ?: 0f,
                        title = title.ifEmpty { "Chapter $chapName" },
                        date = created
                    )
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getPages(chapterId: String): List<String> {
        val url = "$baseUrl/chapter/$chapterId"
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return emptyList()
                val body = response.body?.string() ?: return emptyList()
                val json = gson.fromJson(body, JsonObject::class.java)
                val chapter = json.getAsJsonObject("chapter")
                val images = chapter.getAsJsonArray("images")
                
                images.map { 
                    val img = it.asJsonObject
                    val b2key = img.get("b2key").asString
                    "https://meo.comix.pictures/$b2key"
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
