package com.ai_technologi.ar_application.core.ui

import android.content.Context
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ai_technologi.ar_application.core.util.DeviceUtils

/**
 * Класс, содержащий параметры адаптивного интерфейса для AR.
 *
 * @param context контекст приложения
 */
class ARAdaptiveUIConfig(context: Context) {
    val isARDevice = DeviceUtils.isRokidDevice(context)
    val uiScale = DeviceUtils.getUIScale(context)
    
    // Конфигурация для использования в компонентах
    val config = ARConfig(
        isARDevice = isARDevice
    )
    
    // Размеры элементов интерфейса
    val buttonSize = 56.dp * uiScale
    val iconSize = 24.dp * uiScale
    val touchTargetSize = 48.dp * uiScale
    
    // Отступы
    val smallPadding = 4.dp * uiScale
    val mediumPadding = 8.dp * uiScale
    val largePadding = 16.dp * uiScale
    
    // Размеры шрифтов
    val smallFontSize = 12.sp * uiScale
    val mediumFontSize = 16.sp * uiScale
    val largeFontSize = 20.sp * uiScale
    val headerFontSize = 24.sp * uiScale
    
    // Цвета для AR (высокий контраст)
    val highContrastBackground = androidx.compose.ui.graphics.Color(0xFF000000)
    val highContrastText = androidx.compose.ui.graphics.Color(0xFFFFFFFF)
    val highContrastAccent = androidx.compose.ui.graphics.Color(0xFF00FF00) // Зеленый для лучшей видимости в AR
    
    // Стили текста
    val smallTextStyle = TextStyle(
        fontSize = smallFontSize,
        color = highContrastText
    )
    
    val mediumTextStyle = TextStyle(
        fontSize = mediumFontSize,
        color = highContrastText
    )
    
    val largeTextStyle = TextStyle(
        fontSize = largeFontSize,
        color = highContrastText
    )
    
    val headerTextStyle = TextStyle(
        fontSize = headerFontSize,
        color = highContrastText
    )
    
    // Стандартные отступы для контента
    val contentPadding = PaddingValues(
        horizontal = largePadding,
        vertical = mediumPadding
    )
}

// Локальный провайдер для доступа к конфигурации AR UI
val LocalARAdaptiveUIConfig = compositionLocalOf<ARAdaptiveUIConfig?> { null }

/**
 * Composable-функция, предоставляющая конфигурацию адаптивного интерфейса для AR.
 *
 * @param content содержимое, которое будет использовать конфигурацию
 */
@Composable
fun ARAdaptiveUIProvider(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val config = remember { ARAdaptiveUIConfig(context) }
    
    CompositionLocalProvider(LocalARAdaptiveUIConfig provides config) {
        content()
    }
}

/**
 * Расширение для получения размера с учетом масштаба AR.
 *
 * @param scale масштаб
 * @return масштабированный размер
 */
fun Dp.scaleForAR(scale: Float): Dp = this * scale

/**
 * Composable-функция для получения размера кнопки с учетом AR.
 *
 * @return модификатор с размером кнопки
 */
@Composable
fun Modifier.arButtonSize(): Modifier {
    val config = LocalARAdaptiveUIConfig.current
    return if (config != null) {
        this.size(config.buttonSize)
    } else {
        this.size(56.dp)
    }
}

/**
 * Composable-функция для получения размера иконки с учетом AR.
 *
 * @return модификатор с размером иконки
 */
@Composable
fun Modifier.arIconSize(): Modifier {
    val config = LocalARAdaptiveUIConfig.current
    return if (config != null) {
        this.size(config.iconSize)
    } else {
        this.size(24.dp)
    }
}

/**
 * Composable-функция для получения стиля текста с учетом AR.
 *
 * @param baseStyle базовый стиль текста
 * @return стиль текста с учетом AR
 */
@Composable
fun getARTextStyle(baseStyle: TextStyle): TextStyle {
    val config = LocalARAdaptiveUIConfig.current
    return if (config != null && config.isARDevice) {
        baseStyle.copy(
            fontSize = baseStyle.fontSize * config.uiScale,
            color = config.highContrastText
        )
    } else {
        baseStyle
    }
}

/**
 * Composable-функция для получения отступов с учетом AR.
 *
 * @param basePadding базовые отступы
 * @return отступы с учетом AR
 */
@Composable
fun getARPadding(basePadding: PaddingValues): PaddingValues {
    val config = LocalARAdaptiveUIConfig.current
    return if (config != null && config.isARDevice) {
        PaddingValues(
            start = basePadding.calculateStartPadding(LocalLayoutDirection.current) * config.uiScale,
            top = basePadding.calculateTopPadding() * config.uiScale,
            end = basePadding.calculateEndPadding(LocalLayoutDirection.current) * config.uiScale,
            bottom = basePadding.calculateBottomPadding() * config.uiScale
        )
    } else {
        basePadding
    }
} 