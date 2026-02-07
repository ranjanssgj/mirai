package com.unifiedotaku.app.data.local.database.dao

import androidx.room.*
import com.unifiedotaku.app.data.local.database.entities.LibraryItem
import com.unifiedotaku.app.data.local.database.entities.LibraryStatus
import com.unifiedotaku.app.data.local.database.entities.MediaType
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for LibraryItem operations.
 */
@Dao
interface LibraryDao {

    // ===== Query Operations =====

    @Query("SELECT * FROM library_items ORDER BY updatedAt DESC")
    fun getAll(): Flow<List<LibraryItem>>

    @Query("SELECT * FROM library_items WHERE type = :type ORDER BY updatedAt DESC")
    fun getByType(type: MediaType): Flow<List<LibraryItem>>

    @Query("""
        SELECT * FROM library_items 
        WHERE type = :type AND status = :status 
        ORDER BY updatedAt DESC
    """)
    fun getByTypeAndStatus(type: MediaType, status: LibraryStatus): Flow<List<LibraryItem>>

    @Query("SELECT * FROM library_items WHERE id = :id")
    suspend fun getById(id: String): LibraryItem?

    @Query("SELECT * FROM library_items WHERE id = :id")
    fun getByIdFlow(id: String): Flow<LibraryItem?>

    @Query("""
        SELECT * FROM library_items 
        WHERE title LIKE '%' || :query || '%' 
           OR titleAlternate LIKE '%' || :query || '%'
        ORDER BY updatedAt DESC
    """)
    fun search(query: String): Flow<List<LibraryItem>>

    @Query("SELECT COUNT(*) FROM library_items WHERE type = :type")
    suspend fun getCountByType(type: MediaType): Int

    @Query("SELECT COUNT(*) FROM library_items WHERE type = :type AND status = :status")
    suspend fun getCountByTypeAndStatus(type: MediaType, status: LibraryStatus): Int

    @Query("SELECT AVG(score) FROM library_items WHERE score > 0")
    suspend fun getMeanScore(): Float?

    @Query("SELECT * FROM library_items WHERE type = :type")
    fun getAllItemsByType(type: MediaType): Flow<List<LibraryItem>>

    // ===== Insert/Update Operations =====

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: LibraryItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<LibraryItem>)

    @Query("""
        UPDATE library_items 
        SET progress = :progress, updatedAt = :timestamp 
        WHERE id = :id
    """)
    suspend fun updateProgress(id: String, progress: Int, timestamp: Long = System.currentTimeMillis())

    @Query("""
        UPDATE library_items 
        SET status = :status, updatedAt = :timestamp 
        WHERE id = :id
    """)
    suspend fun updateStatus(id: String, status: LibraryStatus, timestamp: Long = System.currentTimeMillis())

    @Query("""
        UPDATE library_items 
        SET score = :score, updatedAt = :timestamp 
        WHERE id = :id
    """)
    suspend fun updateScore(id: String, score: Float?, timestamp: Long = System.currentTimeMillis())

    // ===== Delete Operations =====

    @Delete
    suspend fun delete(item: LibraryItem)

    @Query("DELETE FROM library_items WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM library_items WHERE type = :type")
    suspend fun deleteByType(type: MediaType)

    @Query("DELETE FROM library_items")
    suspend fun deleteAll()
}
