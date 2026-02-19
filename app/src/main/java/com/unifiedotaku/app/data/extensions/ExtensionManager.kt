package com.unifiedotaku.app.data.extensions

import android.util.Log
import com.unifiedotaku.app.data.remote.api.RepoExtension
import com.unifiedotaku.app.data.remote.scraper.AniCliSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExtensionManager @Inject constructor(
    private val extensionLoader: ExtensionLoader,
    private val extensionRepository: ExtensionRepository,
    private val extensionInstaller: ExtensionInstaller,
    private val aniCliSource: AniCliSource
) {
    companion object {
        private const val TAG = "ExtensionManager"
    }

    private val _loadedExtensions = MutableStateFlow<List<LoadedExtension>>(emptyList())
    val loadedExtensions: StateFlow<List<LoadedExtension>> = _loadedExtensions.asStateFlow()

    private val sourceRegistry = ConcurrentHashMap<String, MangaSource>()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private var initialized = false

    /**
     * Loads extensions from the system.
     * This is now a suspend function running on IO dispatcher.
     */
    suspend fun loadInstalledExtensions() = withContext(Dispatchers.IO) {
        if (_isRefreshing.value) return@withContext
        _isRefreshing.value = true

        Log.i(TAG, "Scanning for installed manga extensions...")
        try {
            val extensions = extensionLoader.loadExtensions()
            _loadedExtensions.value = extensions
            
            // Populate registry
            sourceRegistry.clear()
            extensions.flatMap { it.sources }.forEach { source ->
                sourceRegistry[source.id.toString()] = source // Use ID as key
                // Also map by name if needed, but ID is safer
            }
            
            Log.i(TAG, "Extensions loaded: ${extensions.size}, Sources: ${sourceRegistry.size}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load extensions", e)
        } finally {
            _isRefreshing.value = false
            initialized = true
        }
    }

    /**
     * Force reload extensions (e.g. after install/uninstall).
     */
    suspend fun reloadExtensions() {
        initialized = false // Force re-initialization logic if needed, though loadInstalledExtensions handles it
        loadInstalledExtensions()
    }

    fun getMangaSource(id: String): MangaSource? {
        // Direct lookup by ID
        return sourceRegistry[id]
    }
    
    // For legacy name-based lookup if really needed, but try to avoid
    fun getMangaSourceByName(name: String): MangaSource? {
        return sourceRegistry.values.firstOrNull { it.name == name }
    }

    fun getAllMangaSources(): List<MangaSource> {
        return sourceRegistry.values.toList()
    }

    fun getAllExtensionIds(): List<String> {
        return _loadedExtensions.value.map { it.pkgName }
    }

    fun getDefaultExtensionId(): String {
        return sourceRegistry.keys.firstOrNull() ?: ""
    }

    fun registerSource(id: String, source: MangaSource) {
        sourceRegistry[id] = source
    }

    fun isComixInstalled(): Boolean {
        // Legacy compat — check if any source is available
        return sourceRegistry.isNotEmpty()
    }

    suspend fun installComixExtension() {
        // No-op: there's no built-in source anymore.
        // Extensions must be installed separately.
    }

    suspend fun getAvailableExtensions(): List<RepoExtension> {
        return extensionRepository.getAvailableExtensions()
    }

    fun getExtensionApkUrl(extension: RepoExtension): String {
        return extensionRepository.getApkUrl(extension)
    }

    fun getAllAnimeSources(): List<AnimeSource> {
        return listOf(aniCliSource)
    }

    fun isExtensionInstalled(pkg: String): Boolean {
        // Check if any loaded extension has this package
        if (_loadedExtensions.value.any { it.pkgName == pkg }) return true
        return extensionInstaller.isInstalled(pkg)
    }

    suspend fun installExtension(extension: RepoExtension) {
        val url = getExtensionApkUrl(extension)
        extensionInstaller.downloadAndInstall(url, "${extension.pkg}.apk")
    }
}
