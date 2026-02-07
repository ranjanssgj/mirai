package com.unifiedotaku.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for Unified Otaku app.
 * Annotated with @HiltAndroidApp to enable Hilt dependency injection.
 */
@HiltAndroidApp
class UnifiedOtakuApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize any app-wide components here
    }
}
