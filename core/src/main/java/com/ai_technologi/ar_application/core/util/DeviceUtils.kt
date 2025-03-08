package com.ai_technologi.ar_application.core.util

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.view.Display
import android.view.WindowManager

/**
 * Утилитарный класс для определения типа устройства и адаптации интерфейса.
 */
object DeviceUtils {
    
    /**
     * Проверяет, является ли устройство AR-очками Rokid Max Pro.
     *
     * @param context контекст приложения
     * @return true, если устройство - AR-очки Rokid Max Pro
     */
    fun isRokidDevice(context: Context): Boolean {
        val manufacturer = Build.MANUFACTURER.lowercase()
        val model = Build.MODEL.lowercase()
        
        return manufacturer.contains("rokid") || 
               model.contains("rokid") || 
               model.contains("max pro") ||
               isARGlassesFormFactor(context)
    }
    
    /**
     * Проверяет, имеет ли устройство форм-фактор AR-очков.
     *
     * @param context контекст приложения
     * @return true, если устройство имеет форм-фактор AR-очков
     */
    private fun isARGlassesFormFactor(context: Context): Boolean {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            context.display
        } else {
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay
        }
        
        // AR-очки обычно имеют широкий экран с соотношением сторон больше 2:1
        val metrics = context.resources.displayMetrics
        val aspectRatio = metrics.widthPixels.toFloat() / metrics.heightPixels.toFloat()
        
        // Проверяем ориентацию экрана - AR-очки обычно имеют фиксированную ландшафтную ориентацию
        val isLandscape = context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        
        return isLandscape && aspectRatio > 2.0f
    }
    
    /**
     * Получает масштаб интерфейса в зависимости от типа устройства.
     *
     * @param context контекст приложения
     * @return масштаб интерфейса (1.0f для обычных устройств, больше для AR-очков)
     */
    fun getUIScale(context: Context): Float {
        return if (isRokidDevice(context)) {
            // Увеличенный масштаб для AR-очков
            1.5f
        } else {
            // Стандартный масштаб для обычных устройств
            1.0f
        }
    }
    
    /**
     * Получает оптимальный размер шрифта в зависимости от типа устройства.
     *
     * @param context контекст приложения
     * @param baseSize базовый размер шрифта
     * @return оптимальный размер шрифта
     */
    fun getFontSize(context: Context, baseSize: Float): Float {
        return baseSize * getUIScale(context)
    }
    
    /**
     * Получает оптимальный размер элемента интерфейса в зависимости от типа устройства.
     *
     * @param context контекст приложения
     * @param baseSize базовый размер элемента
     * @return оптимальный размер элемента
     */
    fun getElementSize(context: Context, baseSize: Float): Float {
        return baseSize * getUIScale(context)
    }
    
    /**
     * Получает оптимальный отступ в зависимости от типа устройства.
     *
     * @param context контекст приложения
     * @param basePadding базовый отступ
     * @return оптимальный отступ
     */
    fun getPadding(context: Context, basePadding: Float): Float {
        return basePadding * getUIScale(context)
    }
} 