package com.unifiedotaku.app.data.local.database.dao

import androidx.room.*
import com.unifiedotaku.app.data.local.database.entities.Download
import com.unifiedotaku.app.data.local.database.entities.DownloadStatus
import com.unifiedotaku.app.data.local.database.entities.MediaType
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for download management.
 */
@Dao
interface DownloadDao {

    // ===== Query Operations =====

    @Query("SELECT * FROM downloads ORDER BY createdAt DESC")
    fun getAll(): Flow<List<Download>>

    @Query("SELECT * FROM downloads WHERE type = :type ORDER BY createdAt DESC")
    fun getByType(type: MediaType): Flow<List<Download>>

    @Query("SELECT * FROM downloads WHERE seriesId = :seriesId ORDER BY number ASC")
    fun getForSeries(seriesId: String): Flow<List<Download>>

    @Query("SELECT * FROM downloads WHERE status = :status ORDER BY createdAt ASC")
    fun getByStatus(status: DownloadStatus): Flow<List<Download>>

    @Query("""
        SELECT * FROM downloads 
        WHERE status IN ('PENDING', 'DOWNLOADING') 
        ORDER BY createdAt ASC
    """)
    fun getActiveDownloads(): Flow<List<Download>>

    @Query("SELECT * FROM downloads WHERE id = :id")
    suspend fun getById(id: String): Download?

    @Query("""
        SELECT * FROM downloads 
        WHERE seriesId = :seriesId AND number = :number 
        LIMIT 1
    """)
    suspend fun getBySeriesAndNumber(seriesId: String, number: Int): Download?

    @Query("SELECT COUNT(*) FROM downloads WHERE status = :status")
    suspend fun getCountByStatus(status: DownloadStatus): Int

    @Query("SELECT SUM(fileSize) FROM downloads WHERE status = 'COMPLETED'")
    suspend fun getTotalDownloadedSize(): Long?

    // ===== Insert/Update Operations =====

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(download: Download)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(downloads: List<Download>)

    @Query("""
        UPDATE downloads 
        SET status = :status, progress = :progress 
        WHERE id = :id
    """)
    suspend fun updateProgress(id: String, status: DownloadStatus, progress: Int)

    @Query("""
        UPDATE downloads 
        SET status = 'COMPLETED', progress = 100, completedAt = :timestamp 
        WHERE id = :id
    """)
    suspend fun markCompleted(id: String, timestamp: Long = System.currentTimeMillis())

    @Query("""
        UPDATE downloads 
        SET status = 'FAILED', error = :error 
        WHERE id = :id
    """)
    suspend fun markFailed(id: String, error: String?)

    @Query("UPDATE downloads SET status = 'PAUSED' WHERE id = :id")
    suspend fun pause(id: String)

    @Query("UPDATE downloads SET status = 'PENDING' WHERE id = :id")
    suspend fun resume(id: String)

    @Query("""
        UPDATE downloads 
        SET status = 'PENDING' 
        WHERE status = 'DOWNLOADING'
    """)
    suspend fun resetInterruptedDownloads()

    // ===== Delete Operations =====

    @Delete
    suspend fun delete(download: Download)

    @Query("DELETE FROM downloads WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM downloads WHERE seriesId = :seriesId")
    suspend fun deleteForSeries(seriesId: String)

    @Query("DELETE FROM downloads WHERE status = 'COMPLETED'")
    suspend fun deleteCompleted()

    @Query("DELETE FROM downloads")
    suspend fun deleteAll()
}
