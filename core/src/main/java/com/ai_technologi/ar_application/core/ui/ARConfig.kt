package com.ai_technologi.ar_application.core.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Класс конфигурации для адаптивного UI в AR-приложении.
 * Содержит параметры, которые могут отличаться для AR-устройств и обычных устройств.
 *
 * @param isARDevice флаг, указывающий, является ли устройство AR-устройством
 * @param largePadding отступы для AR-устройств
 * @param mediumPadding средние отступы
 * @param smallPadding малые отступы
 * @param largeIconSize размер больших иконок
 * @param mediumIconSize размер средних иконок
 * @param smallIconSize размер малых иконок
 * @param headerFontSize размер шрифта для заголовков
 * @param largeFontSize размер шрифта для большого текста
 * @param mediumFontSize размер шрифта для среднего текста
 * @param smallFontSize размер шрифта для малого текста
 * @param primaryColor основной цвет
 * @param secondaryColor вторичный цвет
 * @param accentColor акцентный цвет
 * @param highContrastAccent акцентный цвет с высоким контрастом для AR-устройств
 */
data class ARConfig(
    val isARDevice: Boolean = false,
    val largePadding: Dp = if (isARDevice) 32.dp else 16.dp,
    val mediumPadding: Dp = if (isARDevice) 16.dp else 8.dp,
    val smallPadding: Dp = if (isARDevice) 8.dp else 4.dp,
    val largeIconSize: Dp = if (isARDevice) 64.dp else 48.dp,
    val mediumIconSize: Dp = if (isARDevice) 48.dp else 32.dp,
    val smallIconSize: Dp = if (isARDevice) 32.dp else 24.dp,
    val headerFontSize: TextUnit = if (isARDevice) 28.sp else 20.sp,
    val largeFontSize: TextUnit = if (isARDevice) 24.sp else 18.sp,
    val mediumFontSize: TextUnit = if (isARDevice) 20.sp else 16.sp,
    val smallFontSize: TextUnit = if (isARDevice) 16.sp else 14.sp,
    val primaryColor: Color = Color(0xFF2196F3),
    val secondaryColor: Color = Color(0xFF03A9F4),
    val accentColor: Color = Color(0xFF00BCD4),
    val highContrastAccent: Color = Color(0xFFFF5722)
) 