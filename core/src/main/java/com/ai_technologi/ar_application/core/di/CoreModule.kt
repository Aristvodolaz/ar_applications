package com.ai_technologi.ar_application.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * Квалификатор для IO диспетчера корутин
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

/**
 * Основной модуль для зависимостей в core
 */
@Module
@InstallIn(SingletonComponent::class)
object CoreModule {
    
    /**
     * Предоставляет диспетчер корутин для IO операций
     */
    @IoDispatcher
    @Provides
    @Singleton
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
} 