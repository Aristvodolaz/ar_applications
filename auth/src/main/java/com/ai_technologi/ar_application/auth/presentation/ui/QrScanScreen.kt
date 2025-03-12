package com.ai_technologi.ar_application.auth.presentation.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.ai_technologi.ar_application.auth.domain.model.AuthIntent
import com.ai_technologi.ar_application.auth.presentation.util.QrCodeAnalyzer
import com.ai_technologi.ar_application.auth.presentation.viewmodel.AuthViewModel
import com.ai_technologi.ar_application.core.ui.ARAdaptiveUIProvider
import com.ai_technologi.ar_application.core.ui.CameraPermissionScreen
import com.ai_technologi.ar_application.core.ui.LocalARAdaptiveUIConfig
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import timber.log.Timber
import java.util.concurrent.Executors

/**
 * Экран сканирования QR-кода.
 *
 * @param onQrCodeScanned колбэк, вызываемый при успешном сканировании QR-кода
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun QrScanScreen(
    onQrCodeScanned: (String) -> Unit
) {
    ARAdaptiveUIProvider {
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current
        val config = LocalARAdaptiveUIConfig.current?.config
        
        // Запрос разрешения на использование камеры
        val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
        var hasCameraPermission by remember { mutableStateOf(false) }
        
        val permissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            hasCameraPermission = isGranted
        }
        
        LaunchedEffect(key1 = true) {
            if (!cameraPermissionState.status.isGranted) {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            } else {
                hasCameraPermission = true
            }
        }
        
        Box(modifier = Modifier.fillMaxSize()) {
            if (hasCameraPermission) {
                AndroidView(
                    factory = { context ->
                        val previewView = PreviewView(context).apply {
                            scaleType = PreviewView.ScaleType.FILL_CENTER
                        }
                        
                        val cameraExecutor = Executors.newSingleThreadExecutor()
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                        
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }
                            
                            val imageAnalyzer = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()
                                .also {
                                    it.setAnalyzer(
                                        cameraExecutor,
                                        QrCodeAnalyzer { qrCode ->
                                            Timber.d("QR код отсканирован: $qrCode")
                                            onQrCodeScanned(qrCode)
                                            cameraExecutor.shutdown()
                                        }
                                    )
                                }
                            
                            try {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    CameraSelector.DEFAULT_BACK_CAMERA,
                                    preview,
                                    imageAnalyzer
                                )
                            } catch (e: Exception) {
                                Timber.e(e, "Ошибка при привязке камеры")
                            }
                        }, ContextCompat.getMainExecutor(context))
                        
                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )
                
                // Инструкции для пользователя
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Text(
                        text = "Наведите камеру на QR-код для аутентификации",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 32.dp)
                    )
                }
            } else {
                // Отображаем экран запроса разрешений
                CameraPermissionScreen(
                    onRequestPermission = {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    },
                    config = config
                )
            }
            
            // Индикатор загрузки
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp)
            )
        }
    }
} 