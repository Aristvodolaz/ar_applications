package com.ai_technologi.ar_application.auth.presentation.ui

import android.Manifest
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ai_technologi.ar_application.auth.domain.model.AuthIntent
import com.ai_technologi.ar_application.core.ui.ARAdaptiveUIProvider
import com.ai_technologi.ar_application.core.ui.ARButton
import com.ai_technologi.ar_application.core.ui.ARHeading
import com.ai_technologi.ar_application.core.ui.ARIconButton
import com.ai_technologi.ar_application.core.ui.LocalARAdaptiveUIConfig
import com.ai_technologi.ar_application.core.ui.CameraPermissionScreen
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

/**
 * Экран для сканирования логина.
 *
 * @param onLoginScanned колбэк, вызываемый при сканировании логина
 * @param onBackClick колбэк, вызываемый при нажатии кнопки "Назад"
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScanLoginScreen(
    onLoginScanned: (String) -> Unit,
    onBackClick: () -> Unit
) {
    ARAdaptiveUIProvider {
        val config = LocalARAdaptiveUIConfig.current?.config
        val context = LocalContext.current
        
        // Состояние для ручного ввода логина
        var manualLogin by remember { mutableStateOf("") }
        var isManualEntry by remember { mutableStateOf(false) }
        
        // Запрос разрешения на использование камеры
        val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
        
        // Контракт для сканирования QR-кода
        val scanLauncher = rememberScanLauncher { result ->
            if (result.isNotEmpty()) {
                onLoginScanned(result)
            }
        }
        
        // Запуск сканирования при получении разрешения на использование камеры
        LaunchedEffect(cameraPermissionState.status.isGranted) {
            if (cameraPermissionState.status.isGranted && !isManualEntry) {
                scanQrCode(scanLauncher)
            }
        }
        
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
                        text = "Сканирование логина",
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
                if (isManualEntry) {
                    // Ручной ввод логина
                    Text(
                        text = "Введите логин вручную",
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = config?.largeFontSize ?: MaterialTheme.typography.bodyLarge.fontSize),
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    TextField(
                        value = manualLogin,
                        onValueChange = { manualLogin = it },
                        label = { Text("Логин") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    ARButton(
                        onClick = { onLoginScanned(manualLogin) },
                        text = "Продолжить",
                        enabled = manualLogin.isNotEmpty()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    ARButton(
                        onClick = {
                            isManualEntry = false
                            if (cameraPermissionState.status.isGranted) {
                                scanQrCode(scanLauncher)
                            } else {
                                cameraPermissionState.launchPermissionRequest()
                            }
                        },
                        text = "Сканировать QR-код"
                    )
                } else {
                    // Сканирование QR-кода
                    if (!cameraPermissionState.status.isGranted) {
                        // Запрос разрешения на использование камеры
                        CameraPermissionScreen(
                            onRequestPermission = { cameraPermissionState.launchPermissionRequest() },
                            config = config
                        )
                    } else {
                        // Инструкции по сканированию
                        Text(
                            text = "Наведите камеру на QR-код с логином",
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = config?.largeFontSize ?: MaterialTheme.typography.bodyLarge.fontSize),
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        // Иконка QR-кода
                        Box(
                            modifier = Modifier
                                .size(200.dp)
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Здесь можно добавить анимацию или изображение QR-кода
                            if (config != null) {
                                Text(
                                    text = "QR",
                                    style = MaterialTheme.typography.headlineLarge.copy(fontSize = config.headerFontSize * 3),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        ARButton(
                            onClick = { scanQrCode(scanLauncher) },
                            text = "Сканировать снова"
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        ARButton(
                            onClick = { isManualEntry = true },
                            text = "Ввести логин вручную"
                        )
                    }
                }
            }
        }
    }
}

/**
 * Запуск сканирования QR-кода.
 *
 * @param scanLauncher лаунчер для сканирования
 */
private fun scanQrCode(scanLauncher: ScanLauncher) {
    val options = ScanOptions().apply {
        setDesiredBarcodeFormats(ScanOptions.QR_CODE)
        setPrompt("Наведите камеру на QR-код с логином")
        setBeepEnabled(false)
        setOrientationLocked(false)
    }
    scanLauncher.launch(options)
}

/**
 * Функция для создания лаунчера сканирования.
 *
 * @param onResult колбэк, вызываемый при получении результата сканирования
 * @return лаунчер для сканирования
 */
@Composable
fun rememberScanLauncher(onResult: (String) -> Unit): ScanLauncher {
    return androidx.activity.compose.rememberLauncherForActivityResult(
        contract = ScanContract(),
        onResult = { result ->
            if (result.contents != null) {
                onResult(result.contents)
            }
        }
    )
}

/**
 * Тип для лаунчера сканирования.
 */
typealias ScanLauncher = androidx.activity.compose.ManagedActivityResultLauncher<ScanOptions, com.journeyapps.barcodescanner.ScanIntentResult> 