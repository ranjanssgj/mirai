package com.unifiedotaku.app.data.extensions

import com.unifiedotaku.app.data.extensions.manga.ComixSource
import com.unifiedotaku.app.data.remote.api.RepoExtension
import com.unifiedotaku.app.data.remote.scraper.AniCliSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExtensionManager @Inject constructor(
    private val comixSource: ComixSource,
    private val extensionRepository: ExtensionRepository,
    private val extensionInstaller: ExtensionInstaller,
    private val aniCliSource: AniCliSource
) {
    // Dynamic source registry: maps extensionId -> MangaSource
    private val sourceRegistry = mutableMapOf<String, MangaSource>()
    
    init {
        // Register built-in sources
        sourceRegistry["comix.to"] = comixSource
    }

    fun getComixSource(): ComixSource? {
        return if (sourceRegistry.containsKey("comix.to")) comixSource else null
    }

    fun getMangaSource(name: String): MangaSource? {
        // Direct lookup first
        sourceRegistry[name]?.let { return it }
        // Alias lookup for Comix variants
        if (name == "Comix" || name == "Comix (Built-in)" || name == comixSource.name) {
            return sourceRegistry["comix.to"]
        }
        return null
    }

    fun getAllMangaSources(): List<MangaSource> {
        return sourceRegistry.values.toList()
    }

    fun getAllExtensionIds(): List<String> {
        return sourceRegistry.keys.toList()
    }

    fun getDefaultExtensionId(): String {
        return sourceRegistry.keys.firstOrNull() ?: "comix.to"
    }

    fun registerSource(id: String, source: MangaSource) {
        sourceRegistry[id] = source
    }

    fun isComixInstalled(): Boolean {
        return sourceRegistry.containsKey("comix.to")
    }

    suspend fun installComixExtension() {
        // Since it's built-in, just register it
        sourceRegistry["comix.to"] = comixSource
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
        if (pkg == "com.unifiedotaku.app") return true
        return extensionInstaller.isInstalled(pkg)
    }

    suspend fun installExtension(extension: RepoExtension) {
        val url = getExtensionApkUrl(extension)
        extensionInstaller.downloadAndInstall(url, "${extension.pkg}.apk")
    }
}
