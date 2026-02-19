package com.unifiedotaku.app.util

import java.util.Locale

/**
 * Utility for normalizing titles to improve fuzzy matching.
 */
object TitleNormalizer {

    private val REMOVABLE_PATTERNS = listOf(
        "\\(TV\\)".toRegex(RegexOption.IGNORE_CASE),
        "\\(Movie\\)".toRegex(RegexOption.IGNORE_CASE),
        "\\(OVA\\)".toRegex(RegexOption.IGNORE_CASE),
        "\\(ONA\\)".toRegex(RegexOption.IGNORE_CASE),
        "\\(Special\\)".toRegex(RegexOption.IGNORE_CASE),
        "\\(One Shot\\)".toRegex(RegexOption.IGNORE_CASE),
        "Season \\d+".toRegex(RegexOption.IGNORE_CASE),
        "Part \\d+".toRegex(RegexOption.IGNORE_CASE),
        "Cour \\d+".toRegex(RegexOption.IGNORE_CASE),
        "\\d+(st|nd|rd|th) Season".toRegex(RegexOption.IGNORE_CASE)
    )

    fun normalize(title: String): String {
        var normalized = title.lowercase(Locale.US)
        
        // Remove known patterns
        REMOVABLE_PATTERNS.forEach { pattern ->
            normalized = normalized.replace(pattern, "")
        }
        
        // Remove symbols and extra whitespace
        normalized = normalized.replace(Regex("[^a-z0-9]"), " ")
        normalized = normalized.trim().replace(Regex("\\s+"), " ")
        
        return normalized
    }

    /**
     * Calculate Levenshtein distance similarity (0.0 to 1.0).
     */
    fun calculateSimilarity(s1: String, s2: String): Double {
        val n1 = normalize(s1)
        val n2 = normalize(s2)
        
        if (n1 == n2) return 1.0
        if (n1.isEmpty() || n2.isEmpty()) return 0.0
        
        // Check for containment (e.g. "Naruto" in "Naruto Shippuden")
        if (n1.contains(n2) || n2.contains(n1)) {
            // Penalize very short matches being contained in long strings if proportional length is low?
            // For now, give it a high score but not perfect
            return 0.9
        }

        val distance = levenshtein(n1, n2)
        val maxLen = maxOf(n1.length, n2.length)
        return 1.0 - (distance.toDouble() / maxLen)
    }

    private fun levenshtein(lhs: CharSequence, rhs: CharSequence): Int {
        val lhsLen = lhs.length
        val rhsLen = rhs.length

        var cost = IntArray(lhsLen + 1) { it }
        var newCost = IntArray(lhsLen + 1) { 0 }

        for (i in 1..rhsLen) {
            newCost[0] = i

            for (j in 1..lhsLen) {
                val match = if (lhs[j - 1] == rhs[i - 1]) 0 else 1
                val costReplace = cost[j - 1] + match
                val costInsert = cost[j] + 1
                val costDelete = newCost[j - 1] + 1

                newCost[j] = minOf(costInsert, costDelete, costReplace)
            }

            val swap = cost
            cost = newCost
            newCost = swap
        }

        return cost[lhsLen]
    }
}
