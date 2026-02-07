package com.unifiedotaku.app.data.local.database.dao

import androidx.room.*
import com.unifiedotaku.app.data.local.database.entities.AppSettings
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for app settings (key-value store).
 */
@Dao
interface SettingsDao {

    @Query("SELECT * FROM app_settings WHERE `key` = :key LIMIT 1")
    suspend fun get(key: String): AppSettings?

    @Query("SELECT * FROM app_settings WHERE `key` = :key LIMIT 1")
    fun getFlow(key: String): Flow<AppSettings?>

    @Query("SELECT value FROM app_settings WHERE `key` = :key LIMIT 1")
    suspend fun getValue(key: String): String?

    @Query("SELECT * FROM app_settings")
    fun getAll(): Flow<List<AppSettings>>

    @Query("SELECT * FROM app_settings WHERE `key` LIKE :prefix || '%'")
    fun getByPrefix(prefix: String): Flow<List<AppSettings>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun set(setting: AppSettings)

    @Transaction
    suspend fun setValue(key: String, value: String) {
        set(AppSettings(key = key, value = value, updatedAt = System.currentTimeMillis()))
    }

    @Query("DELETE FROM app_settings WHERE `key` = :key")
    suspend fun delete(key: String)

    @Query("DELETE FROM app_settings WHERE `key` LIKE :prefix || '%'")
    suspend fun deleteByPrefix(prefix: String)

    @Query("DELETE FROM app_settings")
    suspend fun deleteAll()
}
