package com.ai_technologi.ar_application.videocall.di

import com.ai_technologi.ar_application.videocall.data.repository.VideoCallRepository
import com.ai_technologi.ar_application.videocall.data.repository.VideoCallRepositoryImpl
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Модуль Hilt для предоставления зависимостей модуля видеоконференций.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class VideoCallModule {

    /**
     * Связывает интерфейс VideoCallRepository с его реализацией VideoCallRepositoryImpl.
     *
     * @param repository реализация репозитория
     * @return интерфейс репозитория
     */
    @Binds
    @Singleton
    abstract fun bindVideoCallRepository(repository: VideoCallRepositoryImpl): VideoCallRepository

    companion object {
        /**
         * Предоставляет экземпляр FirebaseFirestore для логирования звонков.
         *
         * @return экземпляр FirebaseFirestore
         */
        @Provides
        @Singleton
        fun provideFirebaseFirestore(): FirebaseFirestore {
            return Firebase.firestore
        }
    }
} 