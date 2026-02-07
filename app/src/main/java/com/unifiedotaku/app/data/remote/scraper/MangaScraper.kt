package com.unifiedotaku.app.data.remote.scraper

import com.unifiedotaku.app.data.local.database.entities.MediaType
import com.unifiedotaku.app.domain.model.SourceChapter
import com.unifiedotaku.app.domain.model.MangaPage
import com.unifiedotaku.app.domain.model.Series
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Scraper for manga sources using comix.to.
 * Uses a combination of API calls and HTML scraping.
 */
@Singleton
class MangaScraper @Inject constructor(
    client: OkHttpClient
) : BaseScraper(client) {

    companion object {
        private const val BASE_URL = "https://comix.to"
        private const val API_URL = "$BASE_URL/api/v2"
    }

    /**
     * Search for manga by query using the comix.to API.
     */
    suspend fun search(query: String): List<Series> = withContext(Dispatchers.IO) {
        try {
            val json = fetchString("$API_URL/manga?keyword=${query.replace(" ", "+")}&limit=20")
            val root = JSONObject(json)
            val results = root.optJSONArray("data") ?: return@withContext emptyList()
            
            (0 until results.length()).mapNotNull { i ->
                val obj = results.getJSONObject(i)
                parseComixManga(obj)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback: scrape search page
            searchFromPage(query)
        }
    }

    /**
     * Fallback search by scraping the search page.
     */
    private suspend fun searchFromPage(query: String): List<Series> {
        return try {
            val doc = fetchDocument("$BASE_URL/browser?keyword=${query.replace(" ", "+")}")
            parseComixGrid(doc)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Get most popular manga from home page.
     */
    suspend fun getPopular(): List<Series> = withContext(Dispatchers.IO) {
        try {
            val doc = fetchDocument("$BASE_URL/home")
            // Target "Most Recent Popular" section
            val popularSection = doc.selectFirst(".section:has(.title:contains(Popular))")
            popularSection?.let { parseComixGrid(it) } ?: parseComixGrid(doc)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Get latest updated manga.
     */
    suspend fun getLatestUpdates(): List<Series> = withContext(Dispatchers.IO) {
        try {
            val doc = fetchDocument("$BASE_URL/home")
            // Target "Latest Updates" section
            parseComixGrid(doc)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Get newly released manga.
     */
    suspend fun getNewReleases(): List<Series> = withContext(Dispatchers.IO) {
        try {
            val doc = fetchDocument("$BASE_URL/browser?sort=created_at")
            parseComixGrid(doc)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Get manga details by slug.
     */
    suspend fun getMangaDetails(slug: String): Series? = withContext(Dispatchers.IO) {
        try {
            val doc = fetchDocument("$BASE_URL/title/$slug")
            
            // Extract from Next.js __NEXT_DATA__
            val nextData = doc.selectFirst("script#__NEXT_DATA__")?.data()
            if (nextData != null) {
                val json = JSONObject(nextData)
                val props = json.getJSONObject("props").getJSONObject("pageProps")
                val manga = props.optJSONObject("manga") ?: props.optJSONObject("comic")
                
                if (manga != null) {
                    return@withContext Series(
                        id = slug,
                        title = manga.optString("title", ""),
                        coverUrl = manga.optString("cover", manga.optString("poster", "")),
                        synopsis = manga.optString("description", manga.optString("synopsis", "")),
                        type = MediaType.MANGA,
                        format = detectFormat(manga.optString("country", "jp")),
                        status = manga.optString("status", "Unknown"),
                        rating = manga.optDouble("rating", 0.0).toFloat().takeIf { it > 0 },
                        chapterCount = manga.optInt("total_chapters").takeIf { it > 0 },
                        genres = manga.optJSONArray("genres")?.let { arr ->
                            (0 until arr.length()).map { arr.optString(it) }
                        } ?: emptyList(),
                        authors = manga.optJSONArray("authors")?.let { arr ->
                            (0 until arr.length()).map { arr.optString(it) }
                        } ?: emptyList(),
                        isAnime = false
                    )
                }
            }
            
            // Fallback: parse HTML directly
            val title = doc.selectFirst("h1.title, .manga-title, .comic-title")?.text() ?: ""
            val cover = doc.selectFirst(".poster img, .cover img")?.attr("src") ?: ""
            val synopsis = doc.selectFirst(".description, .synopsis, .summary")?.text()
            val genres = doc.select(".genres a, .tags a").map { it.text() }
            
            Series(
                id = slug,
                title = title,
                coverUrl = cover,
                synopsis = synopsis,
                type = MediaType.MANGA,
                format = "Manga",
                status = doc.selectFirst(".status")?.text() ?: "Unknown",
                genres = genres,
                isAnime = false
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Get chapters for a manga.
     */
    suspend fun getChapters(mangaSlug: String): List<SourceChapter> = withContext(Dispatchers.IO) {
        try {
            val doc = fetchDocument("$BASE_URL/title/$mangaSlug")
            
            // Try to extract from __NEXT_DATA__ first
            val nextData = doc.selectFirst("script#__NEXT_DATA__")?.data()
            if (nextData != null) {
                val json = JSONObject(nextData)
                val props = json.getJSONObject("props").getJSONObject("pageProps")
                val chapters = props.optJSONArray("chapters")
                
                if (chapters != null && chapters.length() > 0) {
                    return@withContext (0 until chapters.length()).map { i ->
                        val ch = chapters.getJSONObject(i)
                        SourceChapter(
                            id = ch.optString("slug", ch.optString("id")),
                            number = ch.optDouble("number", (i + 1).toDouble()).toFloat(),
                            title = ch.optString("title", "").takeIf { it.isNotEmpty() },
                            volume = ch.optInt("volume").takeIf { it > 0 },
                            releaseDate = ch.optString("created_at", ch.optString("date")),
                            scanlator = ch.optString("group"),
                            pageCount = ch.optInt("pages").takeIf { it > 0 }
                        )
                    }.sortedByDescending { it.number }
                }
            }
            
            // Fallback: parse chapter list from HTML
            doc.select("a.title[href*='-chapter-'], .chapter-item a, .chapter-list a").mapIndexed { index, el ->
                val href = el.attr("href")
                val chapterSlug = href.substringAfterLast("/")
                val chapterNum = Regex("""chapter-?(\d+(?:\.\d+)?)""", RegexOption.IGNORE_CASE)
                    .find(href)?.groupValues?.get(1)?.toFloatOrNull() ?: (index + 1).toFloat()
                
                SourceChapter(
                    id = chapterSlug,
                    number = chapterNum,
                    title = el.selectFirst("b, .title-text")?.text()?.takeIf { it.isNotEmpty() },
                    volume = null,
                    releaseDate = el.parent()?.selectFirst(".date, .time")?.text(),
                    scanlator = el.parent()?.selectFirst(".group, .scanlator")?.text(),
                    pageCount = null
                )
            }.sortedByDescending { it.number }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Get pages for a chapter using the API.
     * The chapterSlug format is typically: {numericId}-chapter-{number}
     */
    suspend fun getChapterPages(mangaSlug: String, chapterSlug: String): List<MangaPage> = withContext(Dispatchers.IO) {
        android.util.Log.d("MangaScraper", "getChapterPages: mangaSlug=$mangaSlug, chapterSlug=$chapterSlug")
        
        // Extract numeric chapter ID from slug (e.g., "6998364-chapter-652" -> "6998364")
        val chapterNumericId = extractChapterId(chapterSlug)
        
        if (chapterNumericId != null) {
            // Try the API endpoint first - this is the most reliable method
            try {
                val apiResponse = fetchString("$API_URL/chapters/$chapterNumericId")
                val json = JSONObject(apiResponse)
                val result = json.optJSONObject("result")
                val images = result?.optJSONArray("images")
                
                if (images != null && images.length() > 0) {
                    android.util.Log.d("MangaScraper", "API returned ${images.length()} images")
                    return@withContext (0 until images.length()).map { i ->
                        val imgObj = images.optJSONObject(i)
                        if (imgObj != null) {
                            MangaPage(
                                index = i,
                                imageUrl = imgObj.optString("url", imgObj.optString("src", "")),
                                width = imgObj.optInt("w", imgObj.optInt("width", 0)),
                                height = imgObj.optInt("h", imgObj.optInt("height", 0))
                            )
                        } else {
                            // Simple string array
                            MangaPage(
                                index = i,
                                imageUrl = images.optString(i, ""),
                                width = 0,
                                height = 0
                            )
                        }
                    }.filter { it.imageUrl.isNotEmpty() }
                }
            } catch (e: Exception) {
                android.util.Log.e("MangaScraper", "API call failed: ${e.message}")
            }
        }
        
        // Fallback: Try HTML scraping with __NEXT_DATA__
        try {
            val doc = fetchDocument("$BASE_URL/title/$mangaSlug/$chapterSlug")
            
            val nextData = doc.selectFirst("script#__NEXT_DATA__")?.data()
            if (nextData != null) {
                val json = JSONObject(nextData)
                val props = json.getJSONObject("props").getJSONObject("pageProps")
                val pages = props.optJSONArray("pages") ?: props.optJSONArray("images")
                
                if (pages != null && pages.length() > 0) {
                    return@withContext (0 until pages.length()).map { i ->
                        val page = pages.optJSONObject(i)
                        if (page != null) {
                            MangaPage(
                                index = i,
                                imageUrl = page.optString("url", page.optString("src")),
                                width = page.optInt("width", 0),
                                height = page.optInt("height", 0)
                            )
                        } else {
                            MangaPage(
                                index = i,
                                imageUrl = pages.optString(i),
                                width = 0,
                                height = 0
                            )
                        }
                    }
                }
            }
            
            // Final fallback: DOM images
            doc.select("img.page-image, .read-viewer .page img, .reader-content img").mapIndexed { index, img ->
                val src = img.attr("src").takeIf { it.isNotEmpty() && !it.startsWith("data:") }
                    ?: img.attr("data-src")
                    ?: ""
                
                MangaPage(
                    index = index,
                    imageUrl = src,
                    width = img.attr("width").toIntOrNull() ?: 0,
                    height = img.attr("height").toIntOrNull() ?: 0
                )
            }.filter { it.imageUrl.isNotEmpty() }
        } catch (e: Exception) {
            android.util.Log.e("MangaScraper", "HTML scraping failed: ${e.message}")
            emptyList()
        }
    }

    /**
     * Get pages using chapter ID (for backward compatibility).
     * Supports multiple formats:
     * - "mangaSlug/chapterSlug" 
     * - "numericId-chapter-number"
     * - raw numeric ID
     */
    suspend fun getChapterPages(chapterId: String): List<MangaPage> = withContext(Dispatchers.IO) {
        android.util.Log.d("MangaScraper", "getChapterPages(single): chapterId=$chapterId")
        
        // Format 1: "mangaSlug/chapterSlug"
        if (chapterId.contains("/")) {
            val parts = chapterId.split("/")
            if (parts.size >= 2) {
                return@withContext getChapterPages(parts[0], parts[1])
            }
        }
        
        // Format 2: Extract numeric ID and use API
        val numericId = extractChapterId(chapterId)
        if (numericId != null) {
            try {
                val apiResponse = fetchString("$API_URL/chapters/$numericId")
                val json = JSONObject(apiResponse)
                val result = json.optJSONObject("result")
                val images = result?.optJSONArray("images")
                
                if (images != null && images.length() > 0) {
                    android.util.Log.d("MangaScraper", "API returned ${images.length()} images for ID $numericId")
                    return@withContext (0 until images.length()).map { i ->
                        val imgObj = images.optJSONObject(i)
                        if (imgObj != null) {
                            MangaPage(
                                index = i,
                                imageUrl = imgObj.optString("url", imgObj.optString("src", "")),
                                width = imgObj.optInt("w", imgObj.optInt("width", 0)),
                                height = imgObj.optInt("h", imgObj.optInt("height", 0))
                            )
                        } else {
                            MangaPage(
                                index = i,
                                imageUrl = images.optString(i, ""),
                                width = 0,
                                height = 0
                            )
                        }
                    }.filter { it.imageUrl.isNotEmpty() }
                }
            } catch (e: Exception) {
                android.util.Log.e("MangaScraper", "API call failed for ID $numericId: ${e.message}")
            }
        }
        
        // Fallback: Try direct URL scraping
        try {
            val doc = fetchDocument("$BASE_URL/title/$chapterId")
            doc.select("img.page-image, .read-viewer .page img, .reader-content img").mapIndexed { index, img ->
                MangaPage(
                    index = index,
                    imageUrl = img.attr("src").takeIf { it.isNotEmpty() && !it.startsWith("data:") } 
                        ?: img.attr("data-src") ?: "",
                    width = 0,
                    height = 0
                )
            }.filter { it.imageUrl.isNotEmpty() }
        } catch (e: Exception) {
            android.util.Log.e("MangaScraper", "HTML scraping failed: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Extract numeric chapter ID from various slug formats.
     * Examples: "6998364-chapter-652" -> "6998364", "6998364" -> "6998364"
     */
    private fun extractChapterId(slug: String): String? {
        // Try to extract leading numeric ID
        val match = Regex("""^(\d+)""").find(slug)
        if (match != null) {
            return match.groupValues[1]
        }
        
        // If entire slug is numeric
        if (slug.all { it.isDigit() }) {
            return slug
        }
        
        return null
    }

    /**
     * Get weekly release schedule (not available on comix.to).
     */
    suspend fun getSchedule(): Map<String, List<Series>> = withContext(Dispatchers.IO) {
        // Comix.to doesn't have a schedule endpoint
        emptyMap()
    }

    /**
     * Get manga by genre.
     */
    suspend fun getByGenre(genre: String, page: Int = 1): List<Series> = withContext(Dispatchers.IO) {
        try {
            val doc = fetchDocument("$BASE_URL/browser?genres=$genre&page=$page")
            parseComixGrid(doc)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Get manga by type (manga, manhwa, manhua).
     */
    suspend fun getByType(type: String, page: Int = 1): List<Series> = withContext(Dispatchers.IO) {
        try {
            val doc = fetchDocument("$BASE_URL/browser?type=${type.lowercase()}&page=$page")
            parseComixGrid(doc)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // ==================== Helper Functions ====================

    /**
     * Parse manga from comix.to API JSON object.
     */
    private fun parseComixManga(obj: JSONObject): Series? {
        return try {
            Series(
                id = obj.optString("slug", obj.optString("id")),
                title = obj.optString("title", ""),
                coverUrl = obj.optString("cover", obj.optString("poster", "")),
                type = MediaType.MANGA,
                format = detectFormat(obj.optString("country", "jp")),
                status = obj.optString("status", "Unknown"),
                rating = obj.optDouble("rating", 0.0).toFloat().takeIf { it > 0 },
                chapterCount = obj.optInt("total_chapters").takeIf { it > 0 },
                isAnime = false
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Parse manga grid from comix.to HTML.
     */
    private fun parseComixGrid(element: org.jsoup.nodes.Element): List<Series> {
        // Try multiple selectors for different page layouts
        val cards = element.select("a.poster[href*='/title/'], .manga-card a, .comic-card a")
            .takeIf { it.isNotEmpty() } 
            ?: element.select("a[href*='/title/']")
        
        return cards.mapNotNull { card ->
            try {
                val href = card.attr("href")
                val slug = href.substringAfter("/title/").substringBefore("/")
                
                if (slug.isEmpty()) return@mapNotNull null
                
                val img = card.selectFirst("img")
                val titleEl = card.selectFirst(".title") 
                    ?: card.parent()?.selectFirst("a.title")
                    ?: card.parent()?.selectFirst(".title")
                
                Series(
                    id = slug,
                    title = titleEl?.text() ?: img?.attr("alt") ?: slug.replace("-", " ").capitalizeWords(),
                    coverUrl = img?.attr("src")
                        ?: img?.attr("data-src")
                        ?: "",
                    type = MediaType.MANGA,
                    format = "Manga",
                    status = "Unknown",
                    isAnime = false
                )
            } catch (e: Exception) {
                null
            }
        }.distinctBy { it.id }
    }

    /**
     * Parse manga grid from Document.
     */
    private fun parseComixGrid(doc: org.jsoup.nodes.Document): List<Series> {
        return parseComixGrid(doc.body())
    }

    /**
     * Detect format based on country code.
     */
    private fun detectFormat(country: String): String {
        return when (country.lowercase()) {
            "jp", "japan" -> "Manga"
            "kr", "korea" -> "Manhwa"
            "cn", "china" -> "Manhua"
            else -> "Manga"
        }
    }

    /**
     * Extension function to capitalize words.
     */
    private fun String.capitalizeWords(): String {
        return split(" ", "-").joinToString(" ") { word ->
            word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
    }
}
