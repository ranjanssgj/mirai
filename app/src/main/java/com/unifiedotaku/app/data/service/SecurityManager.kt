package com.unifiedotaku.app.data.service

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

private val Context.securityDataStore: DataStore<Preferences> by preferencesDataStore(name = "security")

/**
 * Manager for app security features (PIN, biometric, incognito).
 */
@Singleton
class SecurityManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val KEY_APP_LOCK_ENABLED = booleanPreferencesKey("app_lock_enabled")
        private val KEY_PIN_HASH = stringPreferencesKey("pin_hash")
        private val KEY_BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        private val KEY_INCOGNITO_MODE = booleanPreferencesKey("incognito_mode")
        private val KEY_LOCK_TIMEOUT = intPreferencesKey("lock_timeout_minutes")
        private val KEY_LAST_ACTIVE = longPreferencesKey("last_active_time")
    }

    private val dataStore = context.securityDataStore

    /**
     * Check if app lock is enabled.
     */
    val isAppLockEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_APP_LOCK_ENABLED] ?: false
    }

    /**
     * Check if biometric is enabled.
     */
    val isBiometricEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_BIOMETRIC_ENABLED] ?: false
    }

    /**
     * Check if incognito mode is active.
     */
    val isIncognitoMode: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_INCOGNITO_MODE] ?: false
    }

    /**
     * Get the stored PIN (hashed).
     */
    val storedPin: Flow<String> = dataStore.data.map { prefs ->
        prefs[KEY_PIN_HASH] ?: ""
    }

    /**
     * Get lock timeout in minutes.
     */
    val lockTimeout: Flow<Int> = dataStore.data.map { prefs ->
        prefs[KEY_LOCK_TIMEOUT] ?: 1 // Default 1 minute
    }

    /**
     * Enable app lock and set PIN.
     */
    suspend fun enableAppLock(pin: String) {
        dataStore.edit { prefs ->
            prefs[KEY_APP_LOCK_ENABLED] = true
            prefs[KEY_PIN_HASH] = hashPin(pin)
        }
    }

    /**
     * Disable app lock.
     */
    suspend fun disableAppLock() {
        dataStore.edit { prefs ->
            prefs[KEY_APP_LOCK_ENABLED] = false
            prefs[KEY_PIN_HASH] = ""
        }
    }

    /**
     * Enable or disable biometric authentication.
     */
    suspend fun setBiometricEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_BIOMETRIC_ENABLED] = enabled
        }
    }

    /**
     * Toggle incognito mode.
     */
    suspend fun setIncognitoMode(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_INCOGNITO_MODE] = enabled
        }
    }

    /**
     * Set lock timeout.
     */
    suspend fun setLockTimeout(minutes: Int) {
        dataStore.edit { prefs ->
            prefs[KEY_LOCK_TIMEOUT] = minutes
        }
    }

    /**
     * Update last active time.
     */
    suspend fun updateLastActive() {
        dataStore.edit { prefs ->
            prefs[KEY_LAST_ACTIVE] = System.currentTimeMillis()
        }
    }

    /**
     * Check if lock should be shown based on timeout.
     */
    suspend fun shouldShowLock(): Boolean {
        val prefs = dataStore.data.first()
        val enabled = prefs[KEY_APP_LOCK_ENABLED] ?: false
        val lastActive = prefs[KEY_LAST_ACTIVE] ?: 0L
        val timeout = prefs[KEY_LOCK_TIMEOUT] ?: 1

        if (enabled) {
            val elapsed = System.currentTimeMillis() - lastActive
            val timeoutMs = timeout * 60 * 1000L
            return elapsed > timeoutMs
        }
        return false
    }

    /**
     * Verify PIN.
     */
    fun verifyPin(input: String, storedHash: String): Boolean {
        return hashPin(input) == storedHash
    }

    /**
     * Simple hash function for PIN (in production, use proper hashing).
     */
    private fun hashPin(pin: String): String {
        // Simple base64 encoding for demo - use BCrypt or similar in production
        return android.util.Base64.encodeToString(
            pin.toByteArray(),
            android.util.Base64.NO_WRAP
        )
    }
}
