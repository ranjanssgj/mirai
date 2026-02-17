package com.unifiedotaku.app.data.extensions

import android.util.Log
import com.unifiedotaku.app.data.remote.api.RepoExtension
import com.unifiedotaku.app.data.remote.scraper.AniCliSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    // Dynamic source registry: maps extensionId -> MangaSource
    private val sourceRegistry = mutableMapOf<String, MangaSource>()

    // Loaded extension descriptors (for UI display)
    private val _loadedExtensions = MutableStateFlow<List<LoadedExtension>>(emptyList())
    val loadedExtensions: StateFlow<List<LoadedExtension>> = _loadedExtensions.asStateFlow()

    // Whether initial loading has been done
    private var initialized = false

    /**
     * Scan installed extension APKs and register all their manga sources.
     * Call this once on app startup (e.g. from ViewModel init).
     */
    fun loadInstalledExtensions() {
        if (initialized) return
        initialized = true

        Log.i(TAG, "Scanning for installed manga extensions...")
        val extensions = extensionLoader.loadExtensions()
        _loadedExtensions.value = extensions

        for (ext in extensions) {
            for (source in ext.sources) {
                val id = "${ext.pkgName}:${source.name}"
                sourceRegistry[id] = source
                Log.i(TAG, "Registered source: $id (${source.name})")
            }
        }

        Log.i(TAG, "Loaded ${extensions.size} extension(s) with ${sourceRegistry.size} source(s)")
    }

    /**
     * Refresh extensions (e.g. after an APK install/uninstall).
     */
    fun refreshExtensions() {
        sourceRegistry.clear()
        initialized = false
        loadInstalledExtensions()
    }

    fun getMangaSource(name: String): MangaSource? {
        // Direct lookup
        sourceRegistry[name]?.let { return it }
        // Partial match by source name
        return sourceRegistry.values.firstOrNull { it.name == name }
    }

    fun getAllMangaSources(): List<MangaSource> {
        return sourceRegistry.values.toList()
    }

    fun getAllExtensionIds(): List<String> {
        return sourceRegistry.keys.toList()
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
