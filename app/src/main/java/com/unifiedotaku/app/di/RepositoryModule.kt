package com.unifiedotaku.app.di

import com.unifiedotaku.app.data.repository.AnimeRepository
import com.unifiedotaku.app.data.repository.AnimeRepositoryImpl
import com.unifiedotaku.app.data.repository.MangaRepository
import com.unifiedotaku.app.data.repository.MangaRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAnimeRepository(
        animeRepositoryImpl: AnimeRepositoryImpl
    ): AnimeRepository

    @Binds
    @Singleton
    abstract fun bindMangaRepository(
        mangaRepositoryImpl: MangaRepositoryImpl
    ): MangaRepository

    @Binds
    @Singleton
    abstract fun bindSyncRepository(
        syncRepositoryImpl: com.unifiedotaku.app.data.repository.SyncRepositoryImpl
    ): com.unifiedotaku.app.data.repository.SyncRepository
}
