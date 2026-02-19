package com.unifiedotaku.app

import android.app.Application
import com.unifiedotaku.app.data.extensions.ExtensionManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Application class for Unified Otaku app.
 * Annotated with @HiltAndroidApp to enable Hilt dependency injection.
 */
@HiltAndroidApp
class UnifiedOtakuApp : Application() {

    @Inject
    lateinit var extensionManager: com.unifiedotaku.app.data.extensions.ExtensionManager

    private val extensionReceiver = object : android.content.BroadcastReceiver() {
        override fun onReceive(context: android.content.Context, intent: android.content.Intent) {
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                extensionManager.reloadExtensions()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        
        val filter = android.content.IntentFilter().apply {
            addAction(android.content.Intent.ACTION_PACKAGE_ADDED)
            addAction(android.content.Intent.ACTION_PACKAGE_REMOVED)
            addAction(android.content.Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        }
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(extensionReceiver, filter, android.content.Context.RECEIVER_EXPORTED)
        } else {
            registerReceiver(extensionReceiver, filter)
        }
        
        // Initial load
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            extensionManager.loadInstalledExtensions()
        }
    }
    
    override fun onTerminate() {
        super.onTerminate()
        unregisterReceiver(extensionReceiver)
    }
}
