package com.unifiedotaku.app.data.local.database.dao

import androidx.room.*
import com.unifiedotaku.app.data.local.database.entities.WatchHistory
import com.unifiedotaku.app.data.local.database.entities.ReadHistory
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for watch and read history tracking.
 */
@Dao
interface HistoryDao {

    // ===== Watch History (Anime) =====

    @Query("""
        SELECT * FROM watch_history 
        WHERE seriesId = :seriesId 
        ORDER BY lastWatchedAt DESC
    """)
    fun getWatchHistory(seriesId: String): Flow<List<WatchHistory>>

    @Query("""
        SELECT * FROM watch_history 
        WHERE seriesId = :seriesId AND episodeNumber = :episodeNumber
        LIMIT 1
    """)
    suspend fun getEpisodeProgress(seriesId: String, episodeNumber: Int): WatchHistory?

    @Query("""
        SELECT * FROM watch_history 
        WHERE episodeId = :episodeId
        LIMIT 1
    """)
    suspend fun getWatchHistoryByEpisodeId(episodeId: String): WatchHistory?

    @Query("""
        SELECT * FROM watch_history 
        WHERE seriesId = :seriesId AND isCompleted = 0 
        ORDER BY episodeNumber DESC 
        LIMIT 1
    """)
    suspend fun getLastWatchedEpisode(seriesId: String): WatchHistory?

    @Query("""
        SELECT * FROM watch_history 
        ORDER BY lastWatchedAt DESC 
        LIMIT :limit
    """)
    fun getRecentWatched(limit: Int = 20): Flow<List<WatchHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWatch(history: WatchHistory)

    @Query("""
        UPDATE watch_history 
        SET watchedDurationMs = :position, lastWatchedAt = :timestamp 
        WHERE seriesId = :seriesId AND episodeNumber = :episodeNumber
    """)
    suspend fun updateWatchPosition(
        seriesId: String, 
        episodeNumber: Int, 
        position: Long, 
        timestamp: Long = System.currentTimeMillis()
    )

    @Query("""
        UPDATE watch_history 
        SET isCompleted = 1, completedAt = :timestamp 
        WHERE seriesId = :seriesId AND episodeNumber = :episodeNumber
    """)
    suspend fun markEpisodeCompleted(
        seriesId: String, 
        episodeNumber: Int, 
        timestamp: Long = System.currentTimeMillis()
    )

    @Query("DELETE FROM watch_history WHERE seriesId = :seriesId")
    suspend fun deleteWatchHistoryForSeries(seriesId: String)

    // ===== Read History (Manga) =====

    @Query("""
        SELECT * FROM read_history 
        WHERE seriesId = :seriesId 
        ORDER BY lastReadAt DESC
    """)
    fun getReadHistory(seriesId: String): Flow<List<ReadHistory>>

    @Query("""
        SELECT * FROM read_history 
        WHERE seriesId = :seriesId AND chapterNumber = :chapterNumber
        LIMIT 1
    """)
    suspend fun getChapterProgress(seriesId: String, chapterNumber: Float): ReadHistory?

    @Query("""
        SELECT * FROM read_history 
        WHERE chapterId = :chapterId
        LIMIT 1
    """)
    suspend fun getReadHistoryByChapterId(chapterId: String): ReadHistory?

    @Query("""
        SELECT * FROM read_history 
        WHERE seriesId = :seriesId AND isCompleted = 0 
        ORDER BY chapterNumber DESC 
        LIMIT 1
    """)
    suspend fun getLastReadChapter(seriesId: String): ReadHistory?

    @Query("""
        SELECT * FROM read_history 
        ORDER BY lastReadAt DESC 
        LIMIT :limit
    """)
    fun getRecentRead(limit: Int = 20): Flow<List<ReadHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRead(history: ReadHistory)

    @Query("""
        UPDATE read_history 
        SET currentPage = :page, lastReadAt = :timestamp 
        WHERE seriesId = :seriesId AND chapterNumber = :chapterNumber
    """)
    suspend fun updateReadPosition(
        seriesId: String, 
        chapterNumber: Float, 
        page: Int, 
        timestamp: Long = System.currentTimeMillis()
    )

    @Query("""
        UPDATE read_history 
        SET isCompleted = 1, completedAt = :timestamp 
        WHERE seriesId = :seriesId AND chapterNumber = :chapterNumber
    """)
    suspend fun markChapterCompleted(
        seriesId: String, 
        chapterNumber: Float, 
        timestamp: Long = System.currentTimeMillis()
    )

    @Query("DELETE FROM read_history WHERE seriesId = :seriesId")
    suspend fun deleteReadHistoryForSeries(seriesId: String)

    // ===== Stats queries =====

    @Query("SELECT SUM(watchedDurationMs) FROM watch_history")
    suspend fun getTotalWatchTimeMs(): Long?

    @Query("SELECT COUNT(*) FROM watch_history WHERE isCompleted = 1")
    suspend fun getTotalEpisodesCompleted(): Int

    @Query("SELECT COUNT(*) FROM read_history WHERE isCompleted = 1")
    suspend fun getTotalChaptersCompleted(): Int

    @Query("SELECT * FROM watch_history")
    fun getAllWatchHistory(): Flow<List<WatchHistory>>

    @Query("SELECT * FROM read_history")
    fun getAllReadHistory(): Flow<List<ReadHistory>>

    // ===== Cleanup =====

    @Query("DELETE FROM watch_history")
    suspend fun clearAllWatchHistory()

    @Query("DELETE FROM read_history")
    suspend fun clearAllReadHistory()
    @Transaction
    suspend fun deleteAll() {
        clearAllWatchHistory()
        clearAllReadHistory()
    }
}
