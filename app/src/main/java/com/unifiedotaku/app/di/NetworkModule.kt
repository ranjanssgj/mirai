package com.unifiedotaku.app.di


import com.unifiedotaku.app.data.remote.api.JikanApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    @Singleton
    fun provideGson(): com.google.gson.Gson {
        return com.google.gson.Gson()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(loggingInterceptor: HttpLoggingInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    @Named("JikanRetrofit")
    fun provideJikanRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.jikan.moe/v4/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }



    @Provides
    @Singleton
    fun provideJikanApi(@Named("JikanRetrofit") retrofit: Retrofit): JikanApi {
        return retrofit.create(JikanApi::class.java)
    }



    @Provides
    @Singleton
    @Named("MalRetrofit")
    fun provideMalRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(com.unifiedotaku.app.data.remote.api.MalApiService.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideMalApiService(@Named("MalRetrofit") retrofit: Retrofit): com.unifiedotaku.app.data.remote.api.MalApiService {
        return retrofit.create(com.unifiedotaku.app.data.remote.api.MalApiService::class.java)
    }

    @Provides
    @Singleton
    @Named("RepoRetrofit")
    fun provideRepoRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://raw.githubusercontent.com/yuzono/manga-repo/repo/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideRepoService(@Named("RepoRetrofit") retrofit: Retrofit): com.unifiedotaku.app.data.remote.api.RepoService {
        return retrofit.create(com.unifiedotaku.app.data.remote.api.RepoService::class.java)
    }
}
