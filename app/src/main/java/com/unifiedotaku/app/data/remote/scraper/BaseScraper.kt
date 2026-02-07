package com.unifiedotaku.app.data.remote.scraper

import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Base scraper class with common HTTP and parsing utilities.
 */
abstract class BaseScraper(
    protected val client: OkHttpClient
) {
    protected val defaultHeaders = mapOf(
        "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
        "Accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8",
        "Accept-Language" to "en-US,en;q=0.5"
    )

    /**
     * Fetch HTML document from URL.
     */
    protected suspend fun fetchDocument(url: String, headers: Map<String, String> = emptyMap()): Document {
        val request = Request.Builder()
            .url(url)
            .apply {
                defaultHeaders.forEach { (key, value) -> addHeader(key, value) }
                headers.forEach { (key, value) -> addHeader(key, value) }
            }
            .build()

        val response = client.newCall(request).execute()
        val body = response.body?.string() ?: throw Exception("Empty response body")
        return Jsoup.parse(body, url)
    }

    /**
     * Fetch raw string content from URL.
     */
    protected suspend fun fetchString(url: String, headers: Map<String, String> = emptyMap()): String {
        val request = Request.Builder()
            .url(url)
            .apply {
                defaultHeaders.forEach { (key, value) -> addHeader(key, value) }
                headers.forEach { (key, value) -> addHeader(key, value) }
            }
            .build()

        val response = client.newCall(request).execute()
        return response.body?.string() ?: throw Exception("Empty response body")
    }
}
