package com.unifiedotaku.app.data.extensions.manga

import com.unifiedotaku.app.data.extensions.MangaSource
import com.unifiedotaku.app.data.extensions.RawChapter
import com.unifiedotaku.app.data.remote.api.MangaDetailsDto
import com.unifiedotaku.app.data.remote.api.MangaDto
import okhttp3.OkHttpClient
import okhttp3.Request
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonArray
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ComixSource @Inject constructor(
    private val client: OkHttpClient,
    private val gson: Gson
) : MangaSource {
    
    override val name = "Comix (Built-in)"
    override val id: Long = 1001L // Unique ID for built-in source

    // Using API v2 as per comix.py
    override val baseUrl = "https://comix.to/api/v2" 
    // Search still needs a fallback or different endpoint as comix.py didn't have search. 
    // Keeping comick.io for search for now as a fallback, or we can try comix.to if it has search.
    // For now, let's stick to the requested logic: use comix.py logic.
    // comix.py only has get_manga_info, get_all_chapters, get_chapter_images. 
    // I will try to use the same base URL for search if possible, or fallback.
    // Let's assume search might work on v2 or we use the old one for search only.
    private val searchUrl = "https://api.comick.io/v1.0/search" 

    override val headers = mapOf(
        "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
        "Referer" to "https://comix.to/"
    )

    override suspend fun searchManga(query: String): List<MangaDto> {
        // comix.py doesn't have search. Using previous logic for search but adapting to new data if needed.
        // If comix.to has a search, we should use it, but I don't have that info. 
        // I will use the old search URL but map the result to what we need (hid).
        
        val url = if (query.isEmpty()) {
            "$searchUrl?sort=view&page=1&limit=30" 
        } else {
            "$searchUrl?q=$query&limit=30&page=1"
        }
        
        val request = Request.Builder()
            .url(url)
            // Use old headers for this specific call if it's comick.io
             .header("User-Agent", headers["User-Agent"]!!)
             .header("Referer", "https://comick.io/") 
            .build()
        
        return withContext(Dispatchers.IO) {
            try {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@withContext emptyList<MangaDto>()
                    val body = response.body?.string() ?: return@withContext emptyList<MangaDto>()
                    val json = gson.fromJson(body, JsonArray::class.java)
                    
                    json.mapNotNull { 
                        try {
                            val item = it.asJsonObject
                            val hid = item.get("hid").asString
                            val title = item.get("title").asString
                            val mdCovers = item.getAsJsonArray("md_covers")
                            val coverFileName = if (mdCovers != null && mdCovers.size() > 0) {
                                mdCovers[0].asJsonObject.get("b2key").asString
                            } else ""
                            // Check if we need to map HID to something else for comix.to?
                            // comix.py uses "manga_code" which extract_manga_code gets from URL. 
                            // The HID from comick.io seems to be the ID used in API. 
                            // comix.py uses /manga/{code}/. 
                            // For now assuming HID works or we might need slug.
                            // comick.io returns slug too.
                            val slug = if (item.has("slug")) item.get("slug").asString else hid

                            val coverUrl = if (coverFileName.isNotEmpty()) "https://meo.comick.pictures/$coverFileName" else ""
        
                            // We use Slug or HID? comix.py says "manga_code" from URL. 
                            // Usually: comick.io/comic/00-title -> 00 is code? 
                            // comix.py: 93q1r-the-summoner -> 93q1r.
                            // The HID in comick.io is usually that code.
                            MangaDto(id = hid, title = title, cover = coverUrl)
                        } catch (e: Exception) { null }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }

    override suspend fun getMangaDetails(id: String): MangaDetailsDto {
        // Uses comix.py logic: GET /manga/{manga_code}/
        val url = "$baseUrl/manga/$id/"
        val request = Request.Builder()
            .url(url)
             .apply { headers.forEach { (k, v) -> header(k, v) } }
            .build()

        return withContext(Dispatchers.IO) {
            try {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) throw Exception("Failed to fetch details: ${response.code}")
                    val body = response.body?.string() ?: throw Exception("Empty response")
                    val json = gson.fromJson(body, JsonObject::class.java)
                    // comix.py: data = response.json()["result"]
                    val result = json.getAsJsonObject("result")
                    
                    val title = result.get("title").asString
                    val desc = if (result.has("synopsis") && !result.get("synopsis").isJsonNull) result.get("synopsis").asString.replace(Regex("<.*?>"), "") else "No description"
                    val statusInt = if(result.has("status")) result.get("status").asInt else 0
                    val status = when(statusInt) {
                        1 -> "Ongoing"
                        2 -> "Completed"
                        else -> "Unknown"
                    }

                    // comix.py: poster_url via poster.large/medium
                    var coverUrl = ""
                    if (result.has("poster") && !result.get("poster").isJsonNull) {
                        val poster = result.getAsJsonObject("poster")
                         if (poster.has("large") && !poster.get("large").isJsonNull) {
                             coverUrl = poster.get("large").asString
                         } else if (poster.has("medium") && !poster.get("medium").isJsonNull) {
                             coverUrl = poster.get("medium").asString
                         }
                    }

                    // Genres: term_ids (list of ids) - not names. 
                    // comix.py doesn't map them. We might skip genres or just show IDs if no map.
                    // For now empty or simple placeholder if existing.
                    val genres = emptyList<String>() // Mapping IDs to names requires extra call or map
                    
                    MangaDetailsDto(
                        id = id,
                        title = title,
                        description = desc,
                        author = "Unknown", // comix.py doesn't extract author
                        status = status,
                        cover = coverUrl,
                        genres = genres
                    )
                }
            } catch (e: Exception) {
                throw e
            }
        }
    }

    override suspend fun getChapters(id: String): List<RawChapter> {
        // comix.py: /manga/{code}/chapters?limit=100&page={page}&order[number]=asc
        // Needs pagination.
        return withContext(Dispatchers.IO) {
            val allChapters = mutableListOf<RawChapter>()
            var page = 1
            var hasMore = true

            while (hasMore) {
                val url = "$baseUrl/manga/$id/chapters?limit=100&page=$page&order[number]=desc" // Prefer desc for UI? standard is usually desc
                val request = Request.Builder()
                    .url(url)
                    .apply { headers.forEach { (k, v) -> header(k, v) } }
                    .build()

                try {
                    client.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) {
                            hasMore = false
                            return@use
                        }
                        val body = response.body?.string() ?: return@use
                        
                        val json = gson.fromJson(body, JsonObject::class.java)
                        val result = json.getAsJsonObject("result") // comix.py: response.json()["result"]
                        val items = if (result.has("items") && !result.get("items").isJsonNull) result.getAsJsonArray("items") else null

                        if (items == null || items.size() == 0) {
                            hasMore = false
                        } else {
                            items.forEach { 
                                val item = it.asJsonObject
                                val chapId = item.get("chapter_id").asString // comix.py: chapter_id
                                val number = if(item.has("number")) item.get("number").asString else ""
                                val title = if (item.has("name") && !item.get("name").isJsonNull) item.get("name").asString else 
                                            if (item.has("title") && !item.get("title").isJsonNull) item.get("title").asString else ""
                                val date = if (item.has("created_at")) item.get("created_at").asString else ""

                                // Group
                                var scanlator = ""
                                if (item.has("scanlation_group") && !item.get("scanlation_group").isJsonNull) {
                                    scanlator = item.getAsJsonObject("scanlation_group").get("name").asString
                                } else if (item.has("is_official") && item.get("is_official").asBoolean) {
                                    scanlator = "Official"
                                }

                                val name = "Chapter $number${if (title.isNotEmpty()) " - $title" else ""}"
                                
                                allChapters.add(RawChapter(
                                    url = chapId, // We use chapter_id for getPages
                                    name = name,
                                    uploadDate = date,
                                    scanlator = scanlator
                                ))
                            }
                            page++
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    hasMore = false
                }
            }
            allChapters
        }
    }

    override suspend fun getPages(chapterId: String): List<String> {
        // comix.py: /chapters/{chapter_id}/
        val url = "$baseUrl/chapters/$chapterId/"
        val request = Request.Builder()
            .url(url)
            .apply { headers.forEach { (k, v) -> header(k, v) } }
            .build()
            
        return withContext(Dispatchers.IO) {
            try {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@withContext emptyList<String>()
                    val body = response.body?.string() ?: return@withContext emptyList<String>()
                    val json = gson.fromJson(body, JsonObject::class.java)
                    
                    // comix.py: data.get("result", {}).get("images", [])
                    val result = json.getAsJsonObject("result")
                    val images = result.getAsJsonArray("images")
                    
                    images.map { 
                        val img = it.asJsonObject
                        img.get("url").asString
                    }
                }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    override suspend fun getLatestUpdates(): List<MangaDto> {
         return searchManga("")
    }
}
