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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Экран ввода PIN-кода.
 *
 * @param pin текущий PIN-код
 * @param onPinChanged колбэк, вызываемый при изменении PIN-кода
 * @param onPinConfirmed колбэк, вызываемый при подтверждении PIN-кода
 */
@Composable
fun PinEntryScreen(
    pin: String,
    onPinChanged: (String) -> Unit,
    onPinConfirmed: () -> Unit
) {
    val maxPinLength = 4
    
    // Автоматическое подтверждение PIN-кода при вводе всех цифр
    LaunchedEffect(pin) {
        if (pin.length == maxPinLength) {
            onPinConfirmed()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Введите PIN-код",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Отображение введенных цифр в виде точек
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            for (i in 0 until maxPinLength) {
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(
                            if (i < pin.length) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Цифровая клавиатура
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (i in 1..3) {
                    NumberButton(number = i.toString()) {
                        if (pin.length < maxPinLength) {
                            onPinChanged(pin + i.toString())
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (i in 4..6) {
                    NumberButton(number = i.toString()) {
                        if (pin.length < maxPinLength) {
                            onPinChanged(pin + i.toString())
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (i in 7..9) {
                    NumberButton(number = i.toString()) {
                        if (pin.length < maxPinLength) {
                            onPinChanged(pin + i.toString())
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Пустая кнопка для выравнивания
                Box(modifier = Modifier.size(72.dp))
                
                NumberButton(number = "0") {
                    if (pin.length < maxPinLength) {
                        onPinChanged(pin + "0")
                    }
                }
                
                // Кнопка удаления
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .clickable {
                            if (pin.isNotEmpty()) {
                                onPinChanged(pin.dropLast(1))
                            }
                        }
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Backspace,
                        contentDescription = "Удалить",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

/**
 * Кнопка с цифрой для ввода PIN-кода.
 *
 * @param number цифра на кнопке
 * @param onClick колбэк, вызываемый при нажатии на кнопку
 */
@Composable
private fun NumberButton(
    number: String,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(
                if (isPressed) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable {
                isPressed = true
                onClick()
                isPressed = false
            }
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
} 