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

        /** Feature flag that marks a package as a Tachiyomi extension */
        private const val EXTENSION_FEATURE = "tachiyomi.extension"

        /** Metadata key for the source class name(s), semicolon-separated */
        private const val METADATA_SOURCE_CLASS = "tachiyomi.extension.class"

        /** Package prefixes we recognise */
        private val EXTENSION_PREFIXES = listOf(
            "eu.kanade.tachiyomi.extension.",
            "com.unifiedotaku.extension."
        )
    }

    @Suppress("DEPRECATION")
    private val packageFlags =
        PackageManager.GET_CONFIGURATIONS or
        PackageManager.GET_META_DATA or
        PackageManager.GET_SIGNATURES or
        (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) PackageManager.GET_SIGNING_CERTIFICATES else 0)

    /**
     * Load all installed extension APKs and return a list of
     * [LoadedExtension] descriptors, each containing [MangaSource] adapters.
     */
    fun loadExtensions(): List<LoadedExtension> {
        val pm = context.packageManager
        val installedPkgs = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getInstalledPackages(PackageManager.PackageInfoFlags.of(packageFlags.toLong()))
        } else {
            @Suppress("DEPRECATION")
            pm.getInstalledPackages(packageFlags)
        }

        return installedPkgs
            .filter { isExtensionPackage(it) }
            .mapNotNull { loadExtension(it) }
    }

    /**
     * Try to load a single extension APK by package name.
     */
    fun loadExtensionByPkg(pkgName: String): LoadedExtension? {
        return try {
            val pkgInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    pkgName,
                    PackageManager.PackageInfoFlags.of(packageFlags.toLong())
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(pkgName, packageFlags)
            }
            if (isExtensionPackage(pkgInfo)) loadExtension(pkgInfo) else null
        } catch (e: PackageManager.NameNotFoundException) {
            Log.w(TAG, "Extension package not found: $pkgName")
            null
        }
    }

    /**
     * Check whether a PackageInfo is a Tachiyomi extension:
     * either has the feature flag, or has a matching package prefix.
     */
    private fun isExtensionPackage(pkgInfo: PackageInfo): Boolean {
        val hasFeature = pkgInfo.reqFeatures?.any { it.name == EXTENSION_FEATURE } == true
        val hasPrefix = EXTENSION_PREFIXES.any { pkgInfo.packageName.startsWith(it) }
        val hasMeta = pkgInfo.applicationInfo?.metaData?.getString(METADATA_SOURCE_CLASS) != null
        return (hasFeature || hasPrefix) && hasMeta
    }

    /**
     * Load a single extension from its PackageInfo.
     */
    private fun loadExtension(pkgInfo: PackageInfo): LoadedExtension? {
        val pkgName = pkgInfo.packageName
        val appInfo = pkgInfo.applicationInfo ?: return null
        val pm = context.packageManager

        val extName = pm.getApplicationLabel(appInfo).toString()
            .removePrefix("Tachiyomi: ")
            .removePrefix("Komikku: ")
        val versionName = pkgInfo.versionName ?: "0"

        // Get the APK path for class loading
        val apkPath = appInfo.sourceDir ?: return null

        // Read which source class(es) to instantiate
        val classNames = appInfo.metaData?.getString(METADATA_SOURCE_CLASS)
            ?.split(";")
            ?.map { raw ->
                val trimmed = raw.trim()
                if (trimmed.startsWith(".")) pkgName + trimmed else trimmed
            }
            ?: return null

        // Create a class loader that can see the extension APK
        val classLoader = try {
            PathClassLoader(apkPath, null, context.classLoader)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create classloader for $pkgName", e)
            return null
        }

        // Instantiate sources
        val sources = mutableListOf<MangaSource>()
        for (className in classNames) {
            try {
                val clazz = Class.forName(className, false, classLoader)
                val instance = clazz.getDeclaredConstructor().newInstance()

                // Check if the object is a SourceFactory (creates multiple sources)
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
            Log.w(TAG, "No sources loaded from $pkgName")
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
