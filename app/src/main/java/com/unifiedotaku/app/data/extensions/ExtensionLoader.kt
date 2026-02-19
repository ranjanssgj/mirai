package com.unifiedotaku.app.data.extensions

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import dalvik.system.PathClassLoader
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Loaded extension descriptor — one per installed extension APK.
 * Each APK may contain multiple sources (e.g. multi-lang).
 */
data class LoadedExtension(
    val pkgName: String,
    val name: String,
    val versionName: String,
    val sources: List<MangaSource>,
    val iconDrawable: android.graphics.drawable.Drawable? = null
)

/**
 * Scans installed packages for Tachiyomi/Komikku extension APKs,
 * loads their source classes via PathClassLoader, and wraps them
 * into MangaSource adapters using reflection.
 *
 * Supports packages with BOTH prefixes:
 *   - eu.kanade.tachiyomi.extension.*
 *   - com.unifiedotaku.extension.*
 */
@Singleton
class ExtensionLoader @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "ExtensionLoader"
        private const val METADATA_SOURCE_CLASS = "unifiedotaku.extension.class"
        private const val EXTENSION_FEATURE = "tachiyomi.extension"
    }

    /**
     * Load all installed extension APKs and return a list of
     * [LoadedExtension] descriptors, each containing [MangaSource] adapters.
     */
    fun loadExtensions(): List<LoadedExtension> {
        val pm = context.packageManager
        val packages = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getInstalledPackages(PackageManager.PackageInfoFlags.of(PackageManager.GET_CONFIGURATIONS.toLong() or PackageManager.GET_META_DATA.toLong() or PackageManager.GET_SIGNATURES.toLong()))
        } else {
            @Suppress("DEPRECATION")
            pm.getInstalledPackages(PackageManager.GET_CONFIGURATIONS or PackageManager.GET_META_DATA or PackageManager.GET_SIGNATURES)
        }

        return packages
            .filter { isExtensionPackage(it) }
            .mapNotNull { loadExtension(it) }
    }

    /**
     * Try to load a single extension APK by package name.
     */
    fun loadExtensionByPkg(pkgName: String): LoadedExtension? {
        return try {
            val flags = PackageManager.GET_CONFIGURATIONS or PackageManager.GET_META_DATA or PackageManager.GET_SIGNATURES
            val pkgInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(pkgName, PackageManager.PackageInfoFlags.of(flags.toLong()))
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(pkgName, flags)
            }
            if (isExtensionPackage(pkgInfo)) loadExtension(pkgInfo) else null
        } catch (e: PackageManager.NameNotFoundException) {
            Log.w(TAG, "Extension package not found: $pkgName")
            null
        }
    }

    private fun isExtensionPackage(pkgInfo: PackageInfo): Boolean {
        return pkgInfo.applicationInfo?.metaData?.getString(METADATA_SOURCE_CLASS) != null
    }

    /**
     * Load a single extension from its PackageInfo.
     */
    private fun loadExtension(pkgInfo: PackageInfo): LoadedExtension? {
        val pkgName = pkgInfo.packageName
        val appInfo = pkgInfo.applicationInfo ?: return null
        val pm = context.packageManager

        // Security: Validate signature (basic check - ensure it has a signature)
        // In a real production environment, you should check against a known trusted certificate hash.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (pkgInfo.signingInfo == null && pkgInfo.signatures == null) {
                Log.w(TAG, "Extension $pkgName is unsigned. Skipping.")
                return null
            }
        } else {
            @Suppress("DEPRECATION")
            if (pkgInfo.signatures == null || pkgInfo.signatures.isEmpty()) {
                Log.w(TAG, "Extension $pkgName is unsigned. Skipping.")
                return null
            }
        }

        val extName = pm.getApplicationLabel(appInfo).toString()
            .removePrefix("Tachiyomi: ")
            .removePrefix("Komikku: ")
        val versionName = pkgInfo.versionName ?: "0"

        val apkPath = appInfo.sourceDir ?: return null
        // Optimized directory for DexClassLoader (Required for API 26+)
        val optimizedDir = context.getDir("dex", Context.MODE_PRIVATE)

        val classNames = appInfo.metaData?.getString(METADATA_SOURCE_CLASS)
            ?.split(";")
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?.map { if (it.startsWith(".")) pkgName + it else it }
            ?: return null

        val classLoader = try {
            dalvik.system.DexClassLoader(
                apkPath,
                optimizedDir.absolutePath,
                null,
                context.classLoader // Parent MUST be context.classLoader
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create DexClassLoader for $pkgName", e)
            return null
        }

        val sources = mutableListOf<MangaSource>()
        for (className in classNames) {
            try {
                val clazz = Class.forName(className, false, classLoader)
                val instance = clazz.getDeclaredConstructor().newInstance()

                // Check for generic SourceFactory or direct Source
                val factoryMethod = try {
                    clazz.getMethod("createSources")
                } catch (_: NoSuchMethodException) { null }

                if (factoryMethod != null) {
                    @Suppress("UNCHECKED_CAST")
                    val created = factoryMethod.invoke(instance) as? List<Any> ?: emptyList()
                    created.forEach { src ->
                        sources.add(ReflectionSourceAdapter(src, pkgName))
                    }
                } else {
                    sources.add(ReflectionSourceAdapter(instance, pkgName))
                }
            } catch (e: Throwable) {
                Log.e(TAG, "Failed to load source class $className from $pkgName", e)
            }
        }

        if (sources.isEmpty()) {
            return null
        }

        val icon = try { appInfo.loadIcon(pm) } catch (_: Exception) { null }

        Log.i(TAG, "Loaded extension '$extName' ($pkgName) with ${sources.size} source(s)")
        return LoadedExtension(
            pkgName = pkgName,
            name = extName,
            versionName = versionName,
            sources = sources,
            iconDrawable = icon
        )
    }
}
