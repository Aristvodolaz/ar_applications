package com.ai_technologi.ar_application.auth.di

import com.ai_technologi.ar_application.auth.data.repository.AuthRepository
import com.ai_technologi.ar_application.auth.data.repository.AuthRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Модуль Hilt для предоставления зависимостей модуля аутентификации.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    /**
     * Связывает интерфейс AuthRepository с его реализацией AuthRepositoryImpl.
     *
     * @param repository реализация репозитория
     * @return интерфейс репозитория
     */
    @Binds
    @Singleton
    abstract fun bindAuthRepository(repository: AuthRepositoryImpl): AuthRepository
} 