package com.ai_technologi.ar_application.auth.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ai_technologi.ar_application.core.ui.ARAdaptiveUIProvider
import com.ai_technologi.ar_application.core.ui.ARHeading
import com.ai_technologi.ar_application.core.ui.ARIconButton
import com.ai_technologi.ar_application.core.ui.LocalARAdaptiveUIConfig

/**
 * Экран для ввода PIN-кода.
 *
 * @param login логин пользователя
 * @param onPinEntered колбэк, вызываемый при вводе PIN-кода
 * @param onBackClick колбэк, вызываемый при нажатии кнопки "Назад"
 */
@Composable
fun EnterPinScreen(
    login: String,
    onPinEntered: (String) -> Unit,
    onBackClick: () -> Unit
) {
    ARAdaptiveUIProvider {
        val config = LocalARAdaptiveUIConfig.current
        var pin by remember { mutableStateOf("") }
        
        // Максимальная длина PIN-кода
        val maxPinLength = 4
        
        Scaffold(
            topBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    ARIconButton(
                        onClick = onBackClick,
                        icon = Icons.Default.ArrowBack,
                        contentDescription = "Назад",
                        modifier = Modifier.align(Alignment.CenterStart)
                    )
                    
                    ARHeading(
                        text = "Введите PIN-код",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Информация о пользователе
                Text(
                    text = "Логин: $login",
                    style = config?.largeTextStyle ?: MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Отображение введенного PIN-кода в виде точек
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    for (i in 0 until maxPinLength) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(
                                    if (i < pin.length) {
                                        config?.highContrastAccent ?: MaterialTheme.colorScheme.primary
                                    } else {
                                        Color.Gray
                                    }
                                )
                        )
                        
                        if (i < maxPinLength - 1) {
                            Spacer(modifier = Modifier.width(16.dp))
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Цифровая клавиатура
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Ряд 1-3
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        for (i in 1..3) {
                            PinButton(
                                number = i.toString(),
                                onClick = {
                                    if (pin.length < maxPinLength) {
                                        pin += i.toString()
                                        
                                        // Если PIN-код полностью введен, вызываем колбэк
                                        if (pin.length == maxPinLength) {
                                            onPinEntered(pin)
                                        }
                                    }
                                }
                            )
                        }
                    }
                    
                    // Ряд 4-6
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        for (i in 4..6) {
                            PinButton(
                                number = i.toString(),
                                onClick = {
                                    if (pin.length < maxPinLength) {
                                        pin += i.toString()
                                        
                                        // Если PIN-код полностью введен, вызываем колбэк
                                        if (pin.length == maxPinLength) {
                                            onPinEntered(pin)
                                        }
                                    }
                                }
                            )
                        }
                    }
                    
                    // Ряд 7-9
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        for (i in 7..9) {
                            PinButton(
                                number = i.toString(),
                                onClick = {
                                    if (pin.length < maxPinLength) {
                                        pin += i.toString()
                                        
                                        // Если PIN-код полностью введен, вызываем колбэк
                                        if (pin.length == maxPinLength) {
                                            onPinEntered(pin)
                                        }
                                    }
                                }
                            )
                        }
                    }
                    
                    // Ряд с 0 и кнопкой удаления
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Пустая кнопка для выравнивания
                        Box(
                            modifier = Modifier.size(72.dp)
                        )
                        
                        // Кнопка 0
                        PinButton(
                            number = "0",
                            onClick = {
                                if (pin.length < maxPinLength) {
                                    pin += "0"
                                    
                                    // Если PIN-код полностью введен, вызываем колбэк
                                    if (pin.length == maxPinLength) {
                                        onPinEntered(pin)
                                    }
                                }
                            }
                        )
                        
                        // Кнопка удаления
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .clickable {
                                    if (pin.isNotEmpty()) {
                                        pin = pin.dropLast(1)
                                    }
                                }
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Удалить",
                                tint = config?.highContrastText ?: MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Кнопка для ввода цифры PIN-кода.
 *
 * @param number цифра
 * @param onClick колбэк, вызываемый при нажатии кнопки
 */
@Composable
private fun PinButton(
    number: String,
    onClick: () -> Unit
) {
    val config = LocalARAdaptiveUIConfig.current
    
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(Color.DarkGray.copy(alpha = 0.2f))
            .clickable(onClick = onClick)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number,
            style = config?.headerTextStyle?.copy(fontWeight = FontWeight.Bold) 
                ?: MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = config?.highContrastText ?: MaterialTheme.colorScheme.onSurface
        )
    }
} 