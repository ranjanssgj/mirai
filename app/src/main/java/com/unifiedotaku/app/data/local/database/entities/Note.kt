package com.unifiedotaku.app.data.local.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * User notes attached to a series.
 */
@Entity(
    tableName = "notes",
    foreignKeys = [
        ForeignKey(
            entity = LibraryItem::class,
            parentColumns = ["id"],
            childColumns = ["seriesId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("seriesId", unique = true)]
)
data class Note(
    @PrimaryKey(autoGenerate = true) 
    val id: Long = 0,
    val seriesId: String,                  // FK to LibraryItem (one note per series)
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
