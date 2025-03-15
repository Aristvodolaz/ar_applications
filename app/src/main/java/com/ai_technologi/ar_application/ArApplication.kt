package com.ai_technologi.ar_application

import android.app.Application
import android.webkit.WebView
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import com.ai_technologi.ar_application.BuildConfig
import java.io.File

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
        
        // Инициализация WebView
        initWebView()
    }
    
    /**
     * Инициализация WebView
     * Создаем необходимые директории для кэша и настраиваем WebView
     */
    private fun initWebView() {
        try {
            // Создаем директории для кэша WebView
            val webViewCacheDir = File(cacheDir, "WebView")
            if (!webViewCacheDir.exists()) {
                webViewCacheDir.mkdirs()
            }
            
            val httpCacheDir = File(webViewCacheDir, "HTTP Cache")
            if (!httpCacheDir.exists()) {
                httpCacheDir.mkdirs()
            }
            
            val codeCacheDir = File(httpCacheDir, "Code Cache")
            if (!codeCacheDir.exists()) {
                codeCacheDir.mkdirs()
            }
            
            val jsDir = File(codeCacheDir, "js")
            if (!jsDir.exists()) {
                jsDir.mkdirs()
            }
            
            val wasmDir = File(codeCacheDir, "wasm")
            if (!wasmDir.exists()) {
                wasmDir.mkdirs()
            }
            
            // Предварительная инициализация WebView
            WebView.setDataDirectorySuffix("webview_data")
            
            // Создаем и сразу уничтожаем WebView для инициализации
            val webView = WebView(this)
            webView.destroy()
            
            Timber.d("WebView успешно инициализирован")
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при инициализации WebView")
        }
    }
} 