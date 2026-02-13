package com.unifiedotaku.app.utils

import com.unifiedotaku.app.data.extensions.RawChapter
import com.unifiedotaku.app.domain.model.Chapter
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

object ChapterSanitizer {

    private val chapterRegex = Pattern.compile("(?:vol\\.?\\s*\\d+\\s+)?(?:ch\\.?|chapter)\\s*(\\d+(?:\\.\\d+)?)", Pattern.CASE_INSENSITIVE)

    fun sanitize(rawList: List<RawChapter>, seriesId: String): List<Chapter> {
        return rawList
            .map { raw ->
                val (number, title) = parseTitle(raw.name)
                val timestamp = parseDate(raw.uploadDate)
                
                Chapter(
                    id = raw.url,
                    seriesId = seriesId,
                    number = number,
                    title = title,
                    volume = null,
                    pageCount = 0,
                    releaseDate = timestamp,
                    scanlator = raw.scanlator
                )
            }
            .filter { it.number >= 0 }
            .groupBy { it.number }
            .map { (_, versions) ->
                // Deduping: Pick the first one (or could be improved to pick largest scanlator)
                versions.first()
            }
            .sortedByDescending { it.number }
    }

    private fun parseTitle(name: String): Pair<Float, String> {
        val matcher = chapterRegex.matcher(name)
        val number = if (matcher.find()) {
            matcher.group(1)?.toFloatOrNull() ?: 0f
        } else {
            // Fallback: search for first number
            val firstNum = Pattern.compile("(\\d+(?:\\.\\d+)?)").matcher(name)
            if (firstNum.find()) firstNum.group(1)?.toFloatOrNull() ?: 0f else 0f
        }

        // Clean title: "Vol.1 Ch.12 - The End" -> "The End"
        // If "Chapter 12", title becomes "Chapter 12" if no other info
        var cleanTitle = name
        if (name.contains("-")) {
            cleanTitle = name.substringAfter("-").trim()
        } else if (matcher.find()) {
             // If matches "Chapter 12" and no dash, title might just be the name
        }

        return Pair(number, if (cleanTitle.isEmpty()) name else cleanTitle)
    }

    private fun parseDate(dateStr: String?): Long {
        if (dateStr == null) return System.currentTimeMillis()

        // 1. Relative: "2 hours ago", "5 mins ago", "1 day ago"
        if (dateStr.contains("ago", ignoreCase = true)) {
            val now = System.currentTimeMillis()
            val parts = dateStr.trim().split(" ")
            if (parts.size >= 2) {
                val value = parts[0].toLongOrNull() ?: 0L
                val unit = parts[1].lowercase()
                return when {
                    unit.contains("sec") -> now - TimeUnit.SECONDS.toMillis(value)
                    unit.contains("min") -> now - TimeUnit.MINUTES.toMillis(value)
                    unit.contains("hour") -> now - TimeUnit.HOURS.toMillis(value)
                    unit.contains("day") -> now - TimeUnit.DAYS.toMillis(value)
                    unit.contains("week") -> now - TimeUnit.DAYS.toMillis(value * 7)
                    unit.contains("month") -> now - TimeUnit.DAYS.toMillis(value * 30)
                    unit.contains("year") -> now - TimeUnit.DAYS.toMillis(value * 365)
                    else -> now
                }
            }
        }

        // 2. ISO/Standard formats
        val formats = listOf(
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd",
            "dd/MM/yyyy",
            "MM/dd/yyyy"
        )

        for (format in formats) {
            try {
                val sdf = SimpleDateFormat(format, Locale.US)
                return sdf.parse(dateStr)?.time ?: continue
            } catch (e: Exception) {
                // Try next
            }
        }

        return System.currentTimeMillis()
    }
}
