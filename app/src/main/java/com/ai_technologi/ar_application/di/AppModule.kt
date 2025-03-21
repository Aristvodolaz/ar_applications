package com.ai_technologi.ar_application.di

import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * Квалификатор для Nextcloud SharedPreferences
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class NextcloudPrefs

/**
 * Основной модуль Hilt для предоставления зависимостей на уровне приложения
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    @NextcloudPrefs
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("nextcloud_prefs", Context.MODE_PRIVATE)
    }
} 