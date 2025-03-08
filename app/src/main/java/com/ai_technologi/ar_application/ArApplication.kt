package com.ai_technologi.ar_application

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

/**
 * Основной класс приложения.
 * Аннотация @HiltAndroidApp необходима для работы Hilt.
 */
@HiltAndroidApp
class ArApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        
        // Инициализация Timber для логирования
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
} 