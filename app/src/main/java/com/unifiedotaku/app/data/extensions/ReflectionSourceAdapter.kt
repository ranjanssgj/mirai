package com.unifiedotaku.app.data.extensions

import android.util.Log
import com.unifiedotaku.app.data.remote.api.MangaDetailsDto
import com.unifiedotaku.app.data.remote.api.MangaDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.reflect.Method

/**
 * Wraps a Tachiyomi/Komikku extension source object into our [MangaSource]
 * interface using reflection. This avoids needing the full source-api
 * dependency in our classpath.
 *
 * The loaded extension object is expected to implement (by class hierarchy):
 *   - id: Long
 *   - name: String
 *   - baseUrl: String
 *   - lang: String
 *   - getSearchManga(page: Int, query: String, filters: FilterList): MangasPage
 *   - getMangaDetails(manga: SManga): SManga
 *   - getChapterList(manga: SManga): List<SChapter>
 *   - getPageList(chapter: SChapter): List<Page>
 *   - getPopularManga(page: Int): MangasPage
 */
class ReflectionSourceAdapter(
    private val sourceObj: Any,
    private val extensionPkg: String
) : MangaSource {

    companion object {
        private const val TAG = "ReflectionSource"
    }

    private val sourceClass = sourceObj.javaClass

    override val name: String = try {
        sourceClass.getMethod("getName").invoke(sourceObj) as? String ?: "Unknown"
    } catch (_: Exception) { "Unknown" }

    override val baseUrl: String = try {
        sourceClass.getMethod("getBaseUrl").invoke(sourceObj) as? String ?: ""
    } catch (_: Exception) { "" }

    val id: Long = try {
        sourceClass.getMethod("getId").invoke(sourceObj) as? Long ?: 0L
    } catch (_: Exception) { 0L }

    val lang: String = try {
        sourceClass.getMethod("getLang").invoke(sourceObj) as? String ?: "en"
    } catch (_: Exception) { "en" }

    // ──────────────────── reflection helpers ────────────────────

    /** Create an SManga instance from just a url + title */
    private fun createSManga(url: String, title: String = ""): Any? {
        return try {
            // Try to find SManga.create() static method
            val smangaClass = sourceClass.classLoader!!
                .loadClass("eu.kanade.tachiyomi.source.model.SManga")
            val companion = smangaClass.getField("Companion").get(null)
            val createMethod = companion.javaClass.getMethod("create")
            val manga = createMethod.invoke(companion)
            // Set url and title
            manga.javaClass.getMethod("setUrl", String::class.java).invoke(manga, url)
            manga.javaClass.getMethod("setTitle", String::class.java).invoke(manga, title)
            manga
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create SManga", e)
            null
        }
    }

    /** Create an SChapter instance from just a url */
    private fun createSChapter(url: String): Any? {
        return try {
            val chapterClass = sourceClass.classLoader!!
                .loadClass("eu.kanade.tachiyomi.source.model.SChapter")
            val companion = chapterClass.getField("Companion").get(null)
            val createMethod = companion.javaClass.getMethod("create")
            val chapter = createMethod.invoke(companion)
            chapter.javaClass.getMethod("setUrl", String::class.java).invoke(chapter, url)
            chapter.javaClass.getMethod("setName", String::class.java).invoke(chapter, "")
            chapter
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create SChapter", e)
            null
        }
    }

    /** Create an empty FilterList */
    private fun createFilterList(): Any? {
        return try {
            val filterListClass = sourceClass.classLoader!!
                .loadClass("eu.kanade.tachiyomi.source.model.FilterList")
            filterListClass.getDeclaredConstructor(Array<Any>::class.java)
                .newInstance(emptyArray<Any>())
        } catch (e: Exception) {
            // Try no-arg constructor or vararg constructor
            try {
                val filterListClass = sourceClass.classLoader!!
                    .loadClass("eu.kanade.tachiyomi.source.model.FilterList")
                // FilterList has a vararg constructor: FilterList(vararg fs: Filter<*>)
                // which at JVM level is FilterList([Leu.kanade...Filter;)
                val filterArray = java.lang.reflect.Array.newInstance(
                    sourceClass.classLoader!!.loadClass("eu.kanade.tachiyomi.source.model.Filter"),
                    0
                )
                filterListClass.constructors.first().newInstance(filterArray)
            } catch (e2: Exception) {
                Log.e(TAG, "Failed to create FilterList", e2)
                null
            }
        }
    }

    /** Extract mangas from a MangasPage object */
    private fun extractMangasFromPage(mangasPage: Any): List<MangaDto> {
        return try {
            val getMangasMethod = mangasPage.javaClass.getMethod("getMangas")
            @Suppress("UNCHECKED_CAST")
            val mangasList = getMangasMethod.invoke(mangasPage) as? List<Any> ?: emptyList()
            mangasList.mapNotNull { smanga -> smangaToDto(smanga) }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to extract mangas from page", e)
            emptyList()
        }
    }

    /** Convert an SManga object to our MangaDto */
    private fun smangaToDto(smanga: Any): MangaDto? {
        return try {
            val url = smanga.javaClass.getMethod("getUrl").invoke(smanga) as? String ?: return null
            val title = smanga.javaClass.getMethod("getTitle").invoke(smanga) as? String ?: ""
            val thumbnail = try {
                smanga.javaClass.getMethod("getThumbnail_url").invoke(smanga) as? String
            } catch (_: Exception) { null }

            MangaDto(
                id = url,  // use url as ID for extension-loaded manga
                title = title,
                cover = thumbnail ?: ""
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to convert SManga to DTO", e)
            null
        }
    }

    /** Convert an SManga to a full MangaDetailsDto */
    private fun smangaToDetailsDto(smanga: Any): MangaDetailsDto? {
        return try {
            val url = smanga.javaClass.getMethod("getUrl").invoke(smanga) as? String ?: return null
            val title = smanga.javaClass.getMethod("getTitle").invoke(smanga) as? String ?: ""
            val thumbnail = try {
                smanga.javaClass.getMethod("getThumbnail_url").invoke(smanga) as? String
            } catch (_: Exception) { null }
            val description = try {
                smanga.javaClass.getMethod("getDescription").invoke(smanga) as? String
            } catch (_: Exception) { null }
            val author = try {
                smanga.javaClass.getMethod("getAuthor").invoke(smanga) as? String
            } catch (_: Exception) { null }
            val artist = try {
                smanga.javaClass.getMethod("getArtist").invoke(smanga) as? String
            } catch (_: Exception) { null }
            val genre = try {
                smanga.javaClass.getMethod("getGenre").invoke(smanga) as? String
            } catch (_: Exception) { null }
            val status = try {
                smanga.javaClass.getMethod("getStatus").invoke(smanga) as? Int ?: 0
            } catch (_: Exception) { 0 }

            val statusStr = when (status) {
                1 -> "Ongoing"
                2 -> "Completed"
                3 -> "Licensed"
                4 -> "Publishing Finished"
                5 -> "Cancelled"
                6 -> "On Hiatus"
                else -> null
            }

            MangaDetailsDto(
                id = url,
                title = title,
                cover = thumbnail ?: "",
                description = description,
                status = statusStr,
                author = listOfNotNull(author, artist).joinToString(", ").ifEmpty { null },
                genres = genre?.split(",")?.map { it.trim() }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to convert SManga to DetailsDto", e)
            null
        }
    }

    // ──────────────────── MangaSource implementation ────────────────────

    override suspend fun searchManga(query: String): List<MangaDto> = withContext(Dispatchers.IO) {
        try {
            val filterList = createFilterList() ?: return@withContext emptyList()
            val filterListClass = filterList.javaClass

            // Try suspend getSearchManga first (extensions-lib 1.5+)
            val method: Method = sourceClass.getMethod(
                "getSearchManga",
                Int::class.javaPrimitiveType,
                String::class.java,
                filterListClass
            )

            // Suspend functions get a Continuation parameter appended.
            // If the method expects a continuation, we need a different approach.
            // Try invoking as a regular method first; if it fails, fall back to blocking.
            val result = try {
                // Try calling as regular (non-suspend) — works if extension overrides fetchSearchManga
                method.invoke(sourceObj, 1, query, filterList)
            } catch (_: Exception) {
                // Try the deprecated fetch* method via RxJava
                tryFetchSearchManga(query, filterList)
            }

            if (result != null) {
                extractMangasFromPage(result)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "searchManga failed for $name", e)
            emptyList()
        }
    }

    private fun tryFetchSearchManga(query: String, filterList: Any): Any? {
        return try {
            val filterListClass = filterList.javaClass
            val fetchMethod = sourceClass.getMethod(
                "fetchSearchManga",
                Int::class.javaPrimitiveType,
                String::class.java,
                filterListClass
            )
            val observable = fetchMethod.invoke(sourceObj, 1, query, filterList)
            // Call toBlocking().first()
            val toBlocking = observable!!.javaClass.getMethod("toBlocking")
            val blocking = toBlocking.invoke(observable)
            val first = blocking!!.javaClass.getMethod("first")
            first.invoke(blocking)
        } catch (e: Exception) {
            Log.e(TAG, "fetchSearchManga fallback failed", e)
            null
        }
    }

    override suspend fun getMangaDetails(id: String): MangaDetailsDto = withContext(Dispatchers.IO) {
        try {
            val smanga = createSManga(id) ?: return@withContext createEmptyDetails(id)
            val method = sourceClass.getMethod("getMangaDetails", smanga.javaClass.interfaces.firstOrNull() ?: smanga.javaClass)
            val result = try {
                method.invoke(sourceObj, smanga)
            } catch (_: Exception) {
                tryFetchMangaDetails(smanga)
            }

            if (result != null) {
                smangaToDetailsDto(result) ?: createEmptyDetails(id)
            } else {
                createEmptyDetails(id)
            }
        } catch (e: Exception) {
            Log.e(TAG, "getMangaDetails failed for $name", e)
            createEmptyDetails(id)
        }
    }

    private fun tryFetchMangaDetails(smanga: Any): Any? {
        return try {
            val smangaInterface = sourceClass.classLoader!!
                .loadClass("eu.kanade.tachiyomi.source.model.SManga")
            val method = sourceClass.getMethod("fetchMangaDetails", smangaInterface)
            val observable = method.invoke(sourceObj, smanga)
            val toBlocking = observable!!.javaClass.getMethod("toBlocking")
            val blocking = toBlocking.invoke(observable)
            val first = blocking!!.javaClass.getMethod("first")
            first.invoke(blocking)
        } catch (e: Exception) {
            Log.e(TAG, "fetchMangaDetails fallback failed", e)
            null
        }
    }

    override suspend fun getChapters(id: String): List<RawChapter> = withContext(Dispatchers.IO) {
        try {
            val smanga = createSManga(id) ?: return@withContext emptyList()
            val smangaInterface = sourceClass.classLoader!!
                .loadClass("eu.kanade.tachiyomi.source.model.SManga")
            val method = sourceClass.getMethod("getChapterList", smangaInterface)
            val result = try {
                method.invoke(sourceObj, smanga)
            } catch (_: Exception) {
                tryFetchChapterList(smanga)
            }

            @Suppress("UNCHECKED_CAST")
            val chapters = result as? List<Any> ?: return@withContext emptyList()
            chapters.mapNotNull { sChapterToRaw(it) }
        } catch (e: Exception) {
            Log.e(TAG, "getChapters failed for $name", e)
            emptyList()
        }
    }

    private fun tryFetchChapterList(smanga: Any): Any? {
        return try {
            val smangaInterface = sourceClass.classLoader!!
                .loadClass("eu.kanade.tachiyomi.source.model.SManga")
            val method = sourceClass.getMethod("fetchChapterList", smangaInterface)
            val observable = method.invoke(sourceObj, smanga)
            val toBlocking = observable!!.javaClass.getMethod("toBlocking")
            val blocking = toBlocking.invoke(observable)
            val first = blocking!!.javaClass.getMethod("first")
            first.invoke(blocking)
        } catch (e: Exception) {
            Log.e(TAG, "fetchChapterList fallback failed", e)
            null
        }
    }

    private fun sChapterToRaw(sChapter: Any): RawChapter? {
        return try {
            val url = sChapter.javaClass.getMethod("getUrl").invoke(sChapter) as? String ?: return null
            val name = sChapter.javaClass.getMethod("getName").invoke(sChapter) as? String ?: ""
            val dateUpload = try {
                sChapter.javaClass.getMethod("getDate_upload").invoke(sChapter) as? Long ?: 0L
            } catch (_: Exception) { 0L }
            val scanlator = try {
                sChapter.javaClass.getMethod("getScanlator").invoke(sChapter) as? String
            } catch (_: Exception) { null }

            RawChapter(
                url = url,
                name = name,
                uploadDate = if (dateUpload > 0) dateUpload.toString() else null,
                scanlator = scanlator
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to convert SChapter", e)
            null
        }
    }

    override suspend fun getPages(chapterId: String): List<String> = withContext(Dispatchers.IO) {
        try {
            val sChapter = createSChapter(chapterId) ?: return@withContext emptyList()
            val sChapterInterface = sourceClass.classLoader!!
                .loadClass("eu.kanade.tachiyomi.source.model.SChapter")
            val method = sourceClass.getMethod("getPageList", sChapterInterface)
            val result = try {
                method.invoke(sourceObj, sChapter)
            } catch (_: Exception) {
                tryFetchPageList(sChapter)
            }

            @Suppress("UNCHECKED_CAST")
            val pages = result as? List<Any> ?: return@withContext emptyList()
            pages.mapNotNull { page ->
                try {
                    // Try imageUrl first, then url
                    val imageUrl = page.javaClass.getMethod("getImageUrl").invoke(page) as? String
                    imageUrl ?: (page.javaClass.getMethod("getUrl").invoke(page) as? String)
                } catch (_: Exception) { null }
            }
        } catch (e: Exception) {
            Log.e(TAG, "getPages failed for $name", e)
            emptyList()
        }
    }

    private fun tryFetchPageList(sChapter: Any): Any? {
        return try {
            val sChapterInterface = sourceClass.classLoader!!
                .loadClass("eu.kanade.tachiyomi.source.model.SChapter")
            val method = sourceClass.getMethod("fetchPageList", sChapterInterface)
            val observable = method.invoke(sourceObj, sChapter)
            val toBlocking = observable!!.javaClass.getMethod("toBlocking")
            val blocking = toBlocking.invoke(observable)
            val first = blocking!!.javaClass.getMethod("first")
            first.invoke(blocking)
        } catch (e: Exception) {
            Log.e(TAG, "fetchPageList fallback failed", e)
            null
        }
    }

    override suspend fun getLatestUpdates(): List<MangaDto> = withContext(Dispatchers.IO) {
        try {
            val method = sourceClass.getMethod(
                "getPopularManga",
                Int::class.javaPrimitiveType
            )
            val result = try {
                method.invoke(sourceObj, 1)
            } catch (_: Exception) {
                tryFetchPopularManga()
            }

            if (result != null) {
                extractMangasFromPage(result)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "getLatestUpdates failed for $name", e)
            emptyList()
        }
    }

    private fun tryFetchPopularManga(): Any? {
        return try {
            val method = sourceClass.getMethod("fetchPopularManga", Int::class.javaPrimitiveType)
            val observable = method.invoke(sourceObj, 1)
            val toBlocking = observable!!.javaClass.getMethod("toBlocking")
            val blocking = toBlocking.invoke(observable)
            val first = blocking!!.javaClass.getMethod("first")
            first.invoke(blocking)
        } catch (e: Exception) {
            Log.e(TAG, "fetchPopularManga fallback failed", e)
            null
        }
    }

    private fun createEmptyDetails(id: String) = MangaDetailsDto(
        id = id,
        title = "",
        cover = "",
        description = null,
        status = null,
        author = null,
        genres = null
    )
}
