package com.unifiedotaku.app.ui.screens.more

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unifiedotaku.app.data.local.cache.CacheManager
import com.unifiedotaku.app.data.local.database.dao.SettingsDao
import com.unifiedotaku.app.data.local.database.dao.HistoryDao
import com.unifiedotaku.app.data.local.database.dao.LibraryDao
import com.unifiedotaku.app.data.local.database.entities.AppSettings
import com.unifiedotaku.app.data.local.database.entities.MediaType
import com.unifiedotaku.app.data.local.database.entities.Download
import com.unifiedotaku.app.data.service.DownloadManager
import com.unifiedotaku.app.data.service.SecurityManager
import com.unifiedotaku.app.ui.screens.player.AspectRatio
import com.unifiedotaku.app.ui.screens.reader.ReadingMode
import com.unifiedotaku.app.ui.screens.reader.ReaderBackground
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.util.*
import java.text.SimpleDateFormat

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsDao: SettingsDao,
    private val historyDao: HistoryDao,
    private val libraryDao: LibraryDao,
    private val cacheManager: CacheManager,
    private val downloadManager: DownloadManager,
    private val securityManager: SecurityManager,
    private val extensionManager: com.unifiedotaku.app.data.extensions.ExtensionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _downloads = MutableStateFlow<List<Download>>(emptyList())
    val downloads: StateFlow<List<Download>> = _downloads.asStateFlow()

    init {
        loadSettings()
        loadStats()
        observeDownloads()
        observeSecurity()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            settingsDao.getAll().collect { settingsList ->
                val settings = settingsList.associateBy { it.key }
                _uiState.update { state ->
                    state.copy(
                        theme = settings["theme"]?.value?.let { AppTheme.entries.find { t -> t.name == it } } ?: AppTheme.SYSTEM,
                        accentColor = settings["accent_color"]?.value?.let { AccentColor.entries.find { c -> c.name == it } } ?: AccentColor.WHITE,
                        useDynamicColors = settings["dynamic_colors"]?.value?.toBoolean() ?: false,
                        useAmoledBlack = settings["amoled_black"]?.value?.toBoolean() ?: true,
                        defaultQuality = settings[AppSettings.KEY_PLAYER_QUALITY]?.value ?: "auto",
                        autoPlay = settings["auto_play"]?.value?.toBoolean() ?: true,
                        malConnected = settings[AppSettings.KEY_MAL_TOKEN]?.value?.isNotEmpty() ?: false,
                        malUsername = settings["mal_username"]?.value ?: "",
                        installedExtensions = extensionManager.getAllAnimeSources().map { 
                            ExtensionItem(it.name, "1.0.0", true, "Multi") 
                        } + extensionManager.getAllMangaSources().map { 
                             ExtensionItem(it.name, "1.0.0", true, "Multi") 
                        }
                    )
                }
            }
        }
    }

    private fun observeSecurity() {
        viewModelScope.launch {
            securityManager.isAppLockEnabled.collect { enabled -> _uiState.update { it.copy(appLockEnabled = enabled) } }
        }
        viewModelScope.launch {
            securityManager.isIncognitoMode.collect { active -> _uiState.update { it.copy(incognitoMode = active) } }
        }
        viewModelScope.launch {
            securityManager.isBiometricEnabled.collect { enabled -> _uiState.update { it.copy(biometricUnlock = enabled) } }
        }
    }

    private fun loadStats() {
        viewModelScope.launch {
            val totalTime = historyDao.getTotalWatchTimeMs() ?: 0L
            val epCount = historyDao.getTotalEpisodesCompleted()
            val chapCount = historyDao.getTotalChaptersCompleted()
            val meanScore = libraryDao.getMeanScore() ?: 0f
            val watchHistory = historyDao.getAllWatchHistory().first()
            val heatmap = mutableMapOf<String, Int>()
            val df = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            watchHistory.forEach { heatmap[df.format(Date(it.lastWatchedAt))] = (heatmap[df.format(Date(it.lastWatchedAt))] ?: 0) + 1 }
            _uiState.update { it.copy(totalWatchTimeMs = totalTime, episodesCompleted = epCount, chaptersCompleted = chapCount, meanScore = meanScore, activityHeatmap = heatmap) }
        }
    }

    private fun observeDownloads() {
        viewModelScope.launch {
            downloadManager.getAllDownloads().collect { list ->
                _downloads.value = list
                _uiState.update { it.copy(downloadedSize = list.sumOf { d -> d.fileSize }) }
            }
        }
    }

    private fun saveSetting(key: String, value: String) = viewModelScope.launch { settingsDao.setValue(key, value) }

    fun setTheme(theme: AppTheme) { _uiState.update { it.copy(theme = theme) }; saveSetting("theme", theme.name) }
    fun setAccentColor(color: AccentColor) { _uiState.update { it.copy(accentColor = color) }; saveSetting("accent_color", color.name) }
    fun toggleAmoledBlack() { val newValue = !_uiState.value.useAmoledBlack; _uiState.update { it.copy(useAmoledBlack = newValue) }; saveSetting("amoled_black", newValue.toString()) }

    fun toggleAppLock() = viewModelScope.launch { if (_uiState.value.appLockEnabled) securityManager.disableAppLock() else securityManager.enableAppLock("0000") }
    fun toggleBiometricUnlock() = viewModelScope.launch { securityManager.setBiometricEnabled(!_uiState.value.biometricUnlock) }
    fun toggleIncognitoMode() = viewModelScope.launch { securityManager.setIncognitoMode(!_uiState.value.incognitoMode) }

    fun clearHistory() = viewModelScope.launch { historyDao.deleteAll(); loadStats() }
    fun clearCache() = viewModelScope.launch { cacheManager.clearAll(); _uiState.update { it.copy(cacheSize = 0L) } }
    fun resetAllSettings() = viewModelScope.launch { settingsDao.deleteAll(); securityManager.disableAppLock(); securityManager.setIncognitoMode(false); loadSettings() }
    fun clearLibrary() = viewModelScope.launch { libraryDao.deleteAll(); loadStats() }

    fun pauseDownload(id: String) = viewModelScope.launch { downloadManager.pauseDownload(id) }
    fun resumeDownload(id: String) = viewModelScope.launch { downloadManager.resumeDownload(id) }
    fun cancelDownload(id: String) = viewModelScope.launch { downloadManager.cancelDownload(id) }
    fun clearCompletedDownloads() = viewModelScope.launch { downloadManager.clearCompleted() }

    // Account
    fun disconnectMal() = viewModelScope.launch { saveSetting(AppSettings.KEY_MAL_TOKEN, ""); _uiState.update { it.copy(malConnected = false, malUsername = "") } }
    fun connectMal(token: String, username: String) = viewModelScope.launch { 
        saveSetting(AppSettings.KEY_MAL_TOKEN, token)
        saveSetting("mal_username", username)
        _uiState.update { it.copy(malConnected = true, malUsername = username) } 
    }
    fun disconnectAniList() = viewModelScope.launch { saveSetting("anilist_token", ""); _uiState.update { it.copy(aniListConnected = false, aniListUsername = "") } }
    fun connectAniList(token: String, username: String) = viewModelScope.launch { 
        saveSetting("anilist_token", token)
        saveSetting("anilist_username", username)
        _uiState.update { it.copy(aniListConnected = true, aniListUsername = username) } 
    }

    // Data
    fun setDownloadLocation(path: String) { _uiState.update { it.copy(downloadLocation = path) }; saveSetting("download_location", path) }
    fun toggleDownloadOnlyWifi() { val newValue = !_uiState.value.downloadOnlyOnWifi; _uiState.update { it.copy(downloadOnlyOnWifi = newValue) }; saveSetting("download_wifi_only", newValue.toString()) }

    fun toggleExtension(extensionName: String, enabled: Boolean) {
        _uiState.update { state ->
            val updatedExtensions = state.installedExtensions.map { 
                if (it.name == extensionName) it.copy(isEnabled = enabled) else it
            }
            state.copy(installedExtensions = updatedExtensions)
        }
        // In a real app, you would also persist this to preferences or database
        // extensionManager.setExtensionEnabled(extensionName, enabled)
    }

    // Player
    fun setDefaultQuality(quality: String) { _uiState.update { it.copy(defaultQuality = quality) }; saveSetting(AppSettings.KEY_PLAYER_QUALITY, quality) }
    fun setBufferSize(size: String) { _uiState.update { it.copy(bufferSize = size) }; saveSetting("buffer_size", size) }
    fun toggleAutoNext() { val newValue = !_uiState.value.autoPlayNext; _uiState.update { it.copy(autoPlayNext = newValue) }; saveSetting("auto_next", newValue.toString()) }
    fun toggleSkipIntro() { val newValue = !_uiState.value.skipIntroEnabled; _uiState.update { it.copy(skipIntroEnabled = newValue) }; saveSetting("skip_intro", newValue.toString()) }
    fun toggleSkipOutro() { val newValue = !_uiState.value.skipOutroEnabled; _uiState.update { it.copy(skipOutroEnabled = newValue) }; saveSetting("skip_outro", newValue.toString()) }
    fun setSubtitleSize(size: Int) { _uiState.update { it.copy(subtitleSize = size) }; saveSetting("subtitle_size", size.toString()) }
    fun setSubtitleColor(color: Int) { _uiState.update { it.copy(subtitleColor = color) }; saveSetting("subtitle_color", color.toString()) }
    fun setSubtitleBackground(bg: String) { _uiState.update { it.copy(subtitleBackground = bg) }; saveSetting("subtitle_background", bg) }

    // Reader
    fun setDefaultReadingMode(mode: ReadingMode) { _uiState.update { it.copy(defaultReadingMode = mode) }; saveSetting("reading_mode", mode.name) }
    fun toggleDefaultRtl() { val newValue = !_uiState.value.defaultRtl; _uiState.update { it.copy(defaultRtl = newValue) }; saveSetting("default_rtl", newValue.toString()) }
    fun setReaderBrightness(brightness: Float) { _uiState.update { it.copy(readerBrightness = brightness) }; saveSetting("reader_brightness", brightness.toString()) }
    fun setDefaultZoom(zoom: String) { _uiState.update { it.copy(defaultZoom = zoom) }; saveSetting("default_zoom", zoom) }
    fun setPageTransition(transition: String) { _uiState.update { it.copy(pageTransition = transition) }; saveSetting("page_transition", transition) }
    fun toggleVolumeKeysScroll() { val newValue = !_uiState.value.volumeKeysScroll; _uiState.update { it.copy(volumeKeysScroll = newValue) }; saveSetting("volume_keys_scroll", newValue.toString()) }
    fun toggleKeepScreenOn() { val newValue = !_uiState.value.keepScreenOn; _uiState.update { it.copy(keepScreenOn = newValue) }; saveSetting("keep_screen_on", newValue.toString()) }
}
