package com.ai_technologi.ar_application.core.di

import android.content.Context
import com.ai_technologi.ar_application.core.data.db.AppDatabase
import com.ai_technologi.ar_application.core.data.db.ContactDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Модуль Hilt для предоставления базы данных и DAO
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    /**
     * Предоставляет экземпляр базы данных
     */
    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }
    
    /**
     * Предоставляет DAO для работы с контактами
     */
    @Singleton
    @Provides
    fun provideContactDao(database: AppDatabase): ContactDao {
        return database.contactDao()
    }
} 