package com.unifiedotaku.app.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object ExtensionModule {
    // ExtensionManager is now provided by @Inject constructor
    // ComixSource is provided by @Inject constructor

}
