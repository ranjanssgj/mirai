package com.unifiedotaku.app.utils

import com.unifiedotaku.app.data.model.anime.RelationEntryDto

/**
 * Utility to sort anime relations logically.
 * Sorting Logic:
 * 1. Group by Type: Prequel -> Parent (Current) -> Sequel -> Side Story -> Other.
 * 2. Sort by Year: Within same group, sort by year (if available, though DTO might lack it).
 * Note: Jikan RelationEntryDto entry doesn't have year, but we can sort by relation type priority.
 */
object RelationSorter {
    private val priorityMap = mapOf(
        "Prequel" to 1,
        "Parent story" to 2,
        "Full story" to 3,
        "Sequel" to 4,
        "Side story" to 5,
        "Spin-off" to 6,
        "Adaptation" to 7,
        "Summary" to 8,
        "Other" to 9
    )

    fun sortRelations(relations: List<RelationEntryDto>): List<RelationEntryDto> {
        return relations.sortedBy { priorityMap[it.relation] ?: 100 }
    }
}
