package com.ai_technologi.ar_application.auth.di

import android.content.Context
import android.content.SharedPreferences
import com.ai_technologi.ar_application.auth.data.api.NextcloudAuthApi
import com.ai_technologi.ar_application.auth.data.repository.AuthRepositoryImpl
import com.ai_technologi.ar_application.auth.domain.repository.AuthRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Qualifier
import javax.inject.Singleton
import javax.inject.Named

/**
 * Квалификатор для Auth SharedPreferences
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthPrefs

/**
 * Модуль Hilt для аутентификации.
 */
@Module
@InstallIn(SingletonComponent::class)
object AuthModule {
    
    /**
     * Предоставляет базовый URL для Nextcloud.
     *
     * @return базовый URL
     */
    @Provides
    @Singleton
    @Named("auth_base_url")
    fun provideBaseUrl(): String = "https://ar.sitebill.site/"
    
    /**
     * Предоставляет API для аутентификации.
     *
     * @param retrofit экземпляр Retrofit из NetworkModule
     * @return API для аутентификации
     */
    @Provides
    @Singleton
    fun provideNextcloudAuthApi(retrofit: Retrofit): NextcloudAuthApi {
        return retrofit.create(NextcloudAuthApi::class.java)
    }
    
    /**
     * Предоставляет SharedPreferences для хранения токена аутентификации.
     *
     * @param context контекст приложения
     * @return экземпляр SharedPreferences
     */
    @Provides
    @Singleton
    @AuthPrefs
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    }
    
    /**
     * Предоставляет репозиторий для аутентификации.
     *
     * @param api API для аутентификации
     * @param sharedPreferences хранилище для токена аутентификации
     * @return репозиторий для аутентификации
     */
    @Provides
    @Singleton
    fun provideAuthRepository(
        api: NextcloudAuthApi,
        @AuthPrefs sharedPreferences: SharedPreferences
    ): AuthRepository {
        return AuthRepositoryImpl(api, sharedPreferences)
    }
} 