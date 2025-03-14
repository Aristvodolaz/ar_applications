package com.ai_technologi.ar_application.core.ui

import androidx.camera.core.CameraSelector
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

/**
 * Перечисление доступных камер на устройстве
 */
enum class CameraType(val title: String, val icon: ImageVector, val selector: Int) {
    FRONT("Фронтальная", Icons.Default.Call , CameraSelector.LENS_FACING_FRONT),
    BACK("Основная", Icons.Default.Info, CameraSelector.LENS_FACING_BACK),
    EXTERNAL("Внешняя", Icons.Default.Settings, -1) // Для внешних камер
}

/**
 * Диалог выбора камеры
 *
 * @param availableCameras список доступных камер
 * @param selectedCamera текущая выбранная камера
 * @param onCameraSelected колбэк при выборе камеры
 * @param onDismiss колбэк при закрытии диалога
 * @param config конфигурация UI для AR устройств
 */
@Composable
fun CameraSelectorDialog(
    availableCameras: List<CameraType>,
    selectedCamera: CameraType,
    onCameraSelected: (CameraType) -> Unit,
    onDismiss: () -> Unit,
    config: ARConfig? = null
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(if (config?.isARDevice == true) 16.dp else 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (config?.isARDevice == true) {
                    Color.Black.copy(alpha = 0.8f)
                } else {
                    MaterialTheme.colorScheme.surface
                }
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        if (config?.isARDevice == true) config.largePadding else 16.dp
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Выберите камеру",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontSize = config?.headerFontSize ?: MaterialTheme.typography.headlineSmall.fontSize
                    ),
                    color = if (config?.isARDevice == true) Color.White else MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                availableCameras.forEach { camera ->
                    CameraOption(
                        camera = camera,
                        isSelected = camera == selectedCamera,
                        onClick = {
                            onCameraSelected(camera)
                            onDismiss()
                        },
                        config = config
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

/**
 * Элемент выбора камеры
 *
 * @param camera тип камеры
 * @param isSelected выбрана ли эта камера
 * @param onClick колбэк при выборе камеры
 * @param config конфигурация UI для AR устройств
 */
@Composable
private fun CameraOption(
    camera: CameraType,
    isSelected: Boolean,
    onClick: () -> Unit,
    config: ARConfig? = null
) {
    val backgroundColor = if (isSelected) {
        if (config?.isARDevice == true) config.highContrastAccent else MaterialTheme.colorScheme.primary
    } else {
        if (config?.isARDevice == true) Color.DarkGray else MaterialTheme.colorScheme.surfaceVariant
    }
    
    val textColor = if (isSelected) {
        if (config?.isARDevice == true) Color.Black else MaterialTheme.colorScheme.onPrimary
    } else {
        if (config?.isARDevice == true) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(if (config?.isARDevice == true) 16.dp else 8.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(
                horizontal = if (config?.isARDevice == true) config.mediumPadding else 16.dp,
                vertical = if (config?.isARDevice == true) config.mediumPadding else 12.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(if (config?.isARDevice == true) 48.dp else 40.dp)
                .clip(CircleShape)
                .background(if (config?.isARDevice == true) Color.Black.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface)
                .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) {
                        if (config?.isARDevice == true) Color.White else MaterialTheme.colorScheme.primary
                    } else {
                        if (config?.isARDevice == true) Color.Gray else MaterialTheme.colorScheme.outline
                    },
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = camera.icon,
                contentDescription = camera.title,
                tint = if (config?.isARDevice == true) Color.White else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(if (config?.isARDevice == true) 32.dp else 24.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Text(
            text = camera.title,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = config?.largeFontSize ?: MaterialTheme.typography.bodyLarge.fontSize
            ),
            color = textColor
        )
    }
}

/**
 * Кнопка переключения камеры
 *
 * @param onClick колбэк при нажатии на кнопку
 * @param modifier модификатор
 * @param config конфигурация UI для AR устройств
 */
@Composable
fun CameraSwitchButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    config: ARConfig? = null
) {
    Box(
        modifier = modifier
            .size(if (config?.isARDevice == true) 64.dp else 48.dp)
            .clip(CircleShape)
            .background(
                if (config?.isARDevice == true) {
                    config.highContrastAccent.copy(alpha = 0.8f)
                } else {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = "Переключить камеру",
            tint = if (config?.isARDevice == true) Color.Black else Color.White,
            modifier = Modifier.size(if (config?.isARDevice == true) 40.dp else 32.dp)
        )
    }
}

/**
 * Компонент для отображения информации о текущей камере
 *
 * @param currentCamera текущая выбранная камера
 * @param onClick колбэк при нажатии для смены камеры
 * @param modifier модификатор
 * @param config конфигурация UI для AR устройств
 */
@Composable
fun CurrentCameraInfo(
    currentCamera: CameraType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    config: ARConfig? = null
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(if (config?.isARDevice == true) 16.dp else 8.dp))
            .background(
                if (config?.isARDevice == true) {
                    Color.Black.copy(alpha = 0.7f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
                }
            )
            .clickable(onClick = onClick)
            .padding(
                horizontal = if (config?.isARDevice == true) config.mediumPadding else 8.dp,
                vertical = if (config?.isARDevice == true) config.smallPadding else 4.dp
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = currentCamera.icon,
            contentDescription = null,
            tint = if (config?.isARDevice == true) Color.White else MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(if (config?.isARDevice == true) 24.dp else 20.dp)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = currentCamera.title,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = config?.mediumFontSize ?: MaterialTheme.typography.bodyMedium.fontSize
            ),
            color = if (config?.isARDevice == true) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
} 