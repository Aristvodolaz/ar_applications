package com.ai_technologi.ar_application.core.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ai_technologi.ar_application.core.R
import kotlinx.coroutines.delay

/**
 * Красивый экран запроса разрешений с анимацией и информативным описанием.
 *
 * @param title Заголовок экрана
 * @param description Описание, зачем нужны разрешения
 * @param permissionIcon Иконка для отображения типа разрешения
 * @param buttonText Текст кнопки запроса разрешений
 * @param onRequestPermission Функция, вызываемая при нажатии на кнопку запроса разрешений
 * @param config Конфигурация UI для AR устройств
 */
@Composable
fun PermissionScreen(
    title: String,
    description: String,
    permissionIcon: ImageVector = Icons.Default.Lock,
    buttonText: String = "Предоставить разрешение",
    onRequestPermission: () -> Unit,
    config: ARConfig? = null
) {
    var showAnimation by remember { mutableStateOf(true) }
    val animatedAlpha by animateFloatAsState(
        targetValue = if (showAnimation) 1f else 0.6f,
        animationSpec = tween(durationMillis = 1000)
    )
    
    LaunchedEffect(key1 = showAnimation) {
        while (true) {
            delay(2000)
            showAnimation = !showAnimation
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = if (config?.isARDevice == true) 32.dp else 24.dp,
                    vertical = if (config?.isARDevice == true) 48.dp else 32.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Иконка разрешения с анимацией
            Box(
                modifier = Modifier
                    .size(if (config?.isARDevice == true) 160.dp else 120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = permissionIcon,
                    contentDescription = "Разрешение",
                    modifier = Modifier
                        .size(if (config?.isARDevice == true) 80.dp else 60.dp)
                        .alpha(animatedAlpha),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Заголовок
            Text(
                text = title,
                style = if (config?.isARDevice == true) {
                    MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                },
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Описание
            Text(
                text = description,
                style = if (config?.isARDevice == true) {
                    MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 20.sp
                    )
                } else {
                    MaterialTheme.typography.bodyLarge
                },
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Кнопка запроса разрешений
            Button(
                onClick = onRequestPermission,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (config?.isARDevice == true) 64.dp else 56.dp),
                shape = RoundedCornerShape(if (config?.isARDevice == true) 16.dp else 12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = buttonText,
                    style = if (config?.isARDevice == true) {
                        MaterialTheme.typography.titleLarge
                    } else {
                        MaterialTheme.typography.titleMedium
                    },
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

/**
 * Экран запроса разрешения камеры.
 *
 * @param onRequestPermission Функция, вызываемая при нажатии на кнопку запроса разрешений
 * @param config Конфигурация UI для AR устройств
 */
@Composable
fun CameraPermissionScreen(
    onRequestPermission: () -> Unit,
    config: ARConfig? = null
) {
    PermissionScreen(
        title = "Доступ к камере",
        description = "Для работы приложения необходим доступ к камере. Это позволит вам использовать видеосвязь и сканировать QR-коды.",
        permissionIcon = Icons.Default.Check,
        buttonText = "Разрешить доступ к камере",
        onRequestPermission = onRequestPermission,
        config = config
    )
}

/**
 * Экран запроса разрешения микрофона.
 *
 * @param onRequestPermission Функция, вызываемая при нажатии на кнопку запроса разрешений
 * @param config Конфигурация UI для AR устройств
 */
@Composable
fun MicrophonePermissionScreen(
    onRequestPermission: () -> Unit,
    config: ARConfig? = null
) {
    PermissionScreen(
        title = "Доступ к микрофону",
        description = "Для голосовой связи необходим доступ к микрофону. Это позволит вам общаться с другими участниками во время видеозвонка.",
        permissionIcon = Icons.Default.Call,
        buttonText = "Разрешить доступ к микрофону",
        onRequestPermission = onRequestPermission,
        config = config
    )
}

/**
 * Экран запроса нескольких разрешений (камера и микрофон).
 *
 * @param onRequestPermissions Функция, вызываемая при нажатии на кнопку запроса разрешений
 * @param config Конфигурация UI для AR устройств
 */
@Composable
fun VideoCallPermissionsScreen(
    onRequestPermissions: () -> Unit,
    config: ARConfig? = null
) {
    PermissionScreen(
        title = "Доступ к камере и микрофону",
        description = "Для видеозвонка необходим доступ к камере и микрофону. Это позволит вам видеть и слышать собеседника, а также передавать ваше видео и голос.",
        buttonText = "Разрешить доступ",
        onRequestPermission = onRequestPermissions,
        config = config
    )
} 