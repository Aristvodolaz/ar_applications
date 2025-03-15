package com.ai_technologi.ar_application.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ai_technologi.ar_application.core.util.DeviceUtils

/**
 * Кнопка с адаптивным интерфейсом для AR-устройств
 *
 * @param onClick действие при нажатии
 * @param text текст кнопки
 * @param modifier модификатор
 * @param icon иконка (опционально)
 * @param enabled включена ли кнопка
 * @param config конфигурация UI для AR устройств
 */
@Composable
fun ARButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    config: ARConfig? = null
) {
    val context = LocalContext.current
    val isARDevice = config?.isARDevice ?: DeviceUtils.isRokidDevice(context)
    
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = RoundedCornerShape(if (isARDevice) 16.dp else 8.dp),
        contentPadding = PaddingValues(
            horizontal = if (isARDevice) 24.dp else 16.dp,
            vertical = if (isARDevice) 16.dp else 12.dp
        ),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isARDevice) config?.highContrastAccent ?: MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary,
            contentColor = if (isARDevice) Color.Black else MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(if (isARDevice) 28.dp else 20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = config?.largeFontSize ?: MaterialTheme.typography.titleMedium.fontSize
                )
            )
        }
    }
}

/**
 * Кнопка с иконкой, оптимизированная для AR-очков.
 *
 * @param onClick действие при нажатии
 * @param icon иконка
 * @param contentDescription описание иконки для доступности
 * @param modifier модификатор
 * @param enabled включена ли кнопка
 */
@Composable
fun ARIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val config = LocalARAdaptiveUIConfig.current
    
    Surface(
        modifier = modifier
            .size(if (config?.isARDevice == true) 64.dp else 48.dp)
            .clickable(enabled = enabled, onClick = onClick),
        shape = CircleShape,
        color = if (config?.isARDevice == true) config.highContrastAccent else MaterialTheme.colorScheme.primary
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(if (config?.isARDevice == true) 32.dp else 24.dp),
                tint = if (config?.isARDevice == true) Color.Black else MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

/**
 * Карточка, оптимизированная для AR-очков.
 *
 * @param modifier модификатор
 * @param content содержимое карточки
 */
@Composable
fun ARCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val config = LocalARAdaptiveUIConfig.current
    
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(if (config?.isARDevice == true) 16.dp else 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (config?.isARDevice == true) {
                Color.Black.copy(alpha = 0.8f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (config?.isARDevice == true) {
            androidx.compose.foundation.BorderStroke(2.dp, config.highContrastAccent)
        } else {
            null
        }
    ) {
        Box(
            modifier = Modifier.padding(
                if (config?.isARDevice == true) config.largePadding else 16.dp
            )
        ) {
            content()
        }
    }
}

/**
 * Переключатель, оптимизированный для AR-очков.
 *
 * @param checked выбран ли переключатель
 * @param onCheckedChange колбэк при изменении состояния
 * @param modifier модификатор
 * @param enabled включен ли переключатель
 */
@Composable
fun ARSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val config = LocalARAdaptiveUIConfig.current
    
    val trackColor = if (checked) {
        config?.highContrastAccent ?: MaterialTheme.colorScheme.primary
    } else {
        Color.Gray
    }
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(trackColor)
            .size(
                width = if (config?.isARDevice == true) 80.dp else 56.dp,
                height = if (config?.isARDevice == true) 40.dp else 28.dp
            )
            .clickable(enabled = enabled) { onCheckedChange(!checked) },
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .padding(4.dp)
                .size(
                    if (config?.isARDevice == true) 32.dp else 20.dp
                )
                .clip(CircleShape)
                .background(Color.White)
                .align(if (checked) Alignment.CenterEnd else Alignment.CenterStart)
        ) {
            if (checked) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = config?.highContrastAccent ?: MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(if (config?.isARDevice == true) 24.dp else 16.dp)
                        .align(Alignment.Center)
                )
            }
        }
    }
}

/**
 * Элемент списка, оптимизированный для AR-очков.
 *
 * @param title заголовок
 * @param subtitle подзаголовок (опционально)
 * @param onClick действие при нажатии
 * @param modifier модификатор
 * @param leadingIcon иконка слева (опционально)
 * @param trailingIcon иконка справа (опционально)
 */
@Composable
fun ARListItem(
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    val config = LocalARAdaptiveUIConfig.current
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = if (config?.isARDevice == true) {
            Color.Black.copy(alpha = 0.8f)
        } else {
            MaterialTheme.colorScheme.surface
        }
    ) {
        Row(
            modifier = Modifier.padding(
                if (config?.isARDevice == true) config.largePadding else 16.dp
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leadingIcon != null) {
                Box(
                    modifier = Modifier.size(
                        if (config?.isARDevice == true) 48.dp else 40.dp
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    leadingIcon()
                }
                Spacer(modifier = Modifier.width(16.dp))
            }
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = if (config?.isARDevice == true) {
                        config.largeTextStyle
                    } else {
                        MaterialTheme.typography.titleMedium
                    }
                )
                
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        style = if (config?.isARDevice == true) {
                            config.mediumTextStyle
                        } else {
                            MaterialTheme.typography.bodyMedium
                        }
                    )
                }
            }
            
            if (trailingIcon != null) {
                Spacer(modifier = Modifier.width(16.dp))
                Box(
                    modifier = Modifier.size(
                        if (config?.isARDevice == true) 48.dp else 40.dp
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    trailingIcon()
                }
            }
        }
    }
}

/**
 * Заголовок с адаптивным интерфейсом для AR-устройств
 *
 * @param text текст заголовка
 * @param modifier модификатор
 * @param config конфигурация UI для AR устройств
 */
@Composable
fun ARHeading(
    text: String,
    modifier: Modifier = Modifier,
    config: ARConfig? = null
) {
    val context = LocalContext.current
    val isARDevice = config?.isARDevice ?: DeviceUtils.isRokidDevice(context)
    
    Text(
        text = text,
        style = MaterialTheme.typography.headlineMedium.copy(
            fontSize = config?.headerFontSize ?: MaterialTheme.typography.headlineMedium.fontSize
        ),
        color = if (isARDevice) Color.White else MaterialTheme.colorScheme.onBackground,
        textAlign = TextAlign.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = if (isARDevice) 16.dp else 8.dp)
    )
}

/**
 * Индикатор загрузки с адаптивным интерфейсом для AR-устройств
 *
 * @param text текст (опционально)
 * @param modifier модификатор
 * @param config конфигурация UI для AR устройств
 */
@Composable
fun ARLoadingIndicator(
    text: String? = null,
    modifier: Modifier = Modifier,
    config: ARConfig? = null
) {
    val context = LocalContext.current
    val isARDevice = config?.isARDevice ?: DeviceUtils.isRokidDevice(context)
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        androidx.compose.material3.CircularProgressIndicator(
            color = if (isARDevice) config?.highContrastAccent ?: MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary,
            strokeWidth = if (isARDevice) 4.dp else 2.dp
        )
        
        if (text != null) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = config?.largeFontSize ?: MaterialTheme.typography.bodyLarge.fontSize
                ),
                color = if (isARDevice) Color.White else MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
        }
    }
} 