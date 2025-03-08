package com.ai_technologi.ar_application.core.di

import com.ai_technologi.ar_application.core.network.NextCloudApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Модуль Hilt для настройки сетевых компонентов.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * Базовый URL для NextCloud API.
     * В реальном приложении должен быть получен из конфигурации или настроек.
     */
    private const val BASE_URL = "https://nextcloud.example.com/"

    /**
     * Предоставляет экземпляр OkHttpClient для выполнения HTTP-запросов.
     *
     * @return настроенный OkHttpClient
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Предоставляет экземпляр Retrofit для работы с REST API.
     *
     * @param okHttpClient клиент для выполнения HTTP-запросов
     * @return настроенный Retrofit
     */
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * Предоставляет экземпляр NextCloudApi для работы с NextCloud API.
     *
     * @param retrofit экземпляр Retrofit
     * @return реализация NextCloudApi
     */
    @Provides
    @Singleton
    fun provideNextCloudApi(retrofit: Retrofit): NextCloudApi {
        return retrofit.create(NextCloudApi::class.java)
    }
} 