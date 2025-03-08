package com.ai_technologi.ar_application.auth.presentation.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ai_technologi.ar_application.core.R

/**
 * Начальный экран аутентификации.
 *
 * @param onStartScan колбэк, вызываемый при нажатии на кнопку начала сканирования
 */
@Composable
fun InitialAuthScreen(
    onStartScan: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Логотип приложения
        Image(
            painter = painterResource(id = R.drawable.ic_logo),
            contentDescription = "Логотип",
            modifier = Modifier.size(120.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Заголовок
        Text(
            text = "AR-приложение для Rokid Max Pro",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Описание
        Text(
            text = "Для входа в приложение отсканируйте QR-код и введите PIN-код",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Кнопка начала сканирования
        Button(
            onClick = onStartScan,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            Text(
                text = "Сканировать QR-код",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
} 