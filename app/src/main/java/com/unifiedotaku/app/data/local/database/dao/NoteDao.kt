package com.unifiedotaku.app.data.local.database.dao

import androidx.room.*
import com.unifiedotaku.app.data.local.database.entities.Note
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for user notes.
 */
@Dao
interface NoteDao {

    @Query("SELECT * FROM notes WHERE seriesId = :seriesId LIMIT 1")
    fun getNoteForSeries(seriesId: String): Flow<Note?>

    @Query("SELECT * FROM notes WHERE seriesId = :seriesId LIMIT 1")
    suspend fun getNoteForSeriesSync(seriesId: String): Note?

    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun getAllNotes(): Flow<List<Note>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(note: Note)

    @Query("""
        UPDATE notes 
        SET content = :content, updatedAt = :timestamp 
        WHERE seriesId = :seriesId
    """)
    suspend fun updateContent(
        seriesId: String, 
        content: String, 
        timestamp: Long = System.currentTimeMillis()
    )

    @Query("DELETE FROM notes WHERE seriesId = :seriesId")
    suspend fun deleteForSeries(seriesId: String)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM notes")
    suspend fun deleteAll()
}
