package com.unifiedotaku.app.di

import com.unifiedotaku.app.data.extensions.ExtensionManager
import com.unifiedotaku.app.data.extensions.manga.ComixSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton
import com.google.gson.Gson

@Module
@InstallIn(SingletonComponent::class)
object ExtensionModule {

    @Provides
    @Singleton
    fun provideExtensionManager(
        comixSource: ComixSource
    ): ExtensionManager {
        val manager = ExtensionManager()
        // AnimeApiSource is the target scraper
        // manager.registerAnimeSource(animeApiSource) 
        manager.registerMangaSource(comixSource)
        return manager
    }



    @Provides
    @Singleton
    fun provideComixSource(client: OkHttpClient, gson: Gson): ComixSource {
        return ComixSource(client, gson)
    }
}
