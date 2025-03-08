package com.ai_technologi.ar_application.auth.presentation.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ai_technologi.ar_application.auth.domain.model.AuthIntent
import com.ai_technologi.ar_application.auth.domain.model.AuthState
import com.ai_technologi.ar_application.auth.presentation.viewmodel.AuthViewModel

/**
 * Главный экран аутентификации.
 *
 * @param onAuthSuccess колбэк, вызываемый при успешной аутентификации
 * @param viewModel ViewModel для экрана аутентификации
 */
@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Обработка ошибок
    LaunchedEffect(state) {
        if (state is AuthState.Error) {
            snackbarHostState.showSnackbar(
                message = (state as AuthState.Error).message
            )
            viewModel.processIntent(AuthIntent.Reset)
        }
    }
    
    // Обработка успешной аутентификации
    LaunchedEffect(state) {
        if (state is AuthState.Authenticated) {
            onAuthSuccess()
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when (state) {
                is AuthState.Initial -> {
                    // Начальный экран с кнопкой для начала сканирования QR-кода
                    InitialAuthScreen(
                        onStartScan = {
                            viewModel.processIntent(AuthIntent.StartQrScan)
                        }
                    )
                }
                
                is AuthState.ScanningQrCode -> {
                    // Экран сканирования QR-кода
                    QrScanScreen(
                        onQrCodeScanned = { qrToken ->
                            viewModel.processIntent(AuthIntent.QrScanned(qrToken))
                        }
                    )
                }
                
                is AuthState.QrScanned -> {
                    // Переход к вводу PIN-кода
                    viewModel.processIntent(AuthIntent.EnterPin(""))
                }
                
                is AuthState.EnteringPin -> {
                    // Экран ввода PIN-кода
                    val enteringPinState = state as AuthState.EnteringPin
                    PinEntryScreen(
                        pin = enteringPinState.pin,
                        onPinChanged = { newPin ->
                            viewModel.processIntent(AuthIntent.EnterPin(newPin))
                        },
                        onPinConfirmed = {
                            viewModel.processIntent(AuthIntent.ConfirmPin)
                        }
                    )
                }
                
                is AuthState.Loading -> {
                    // Индикатор загрузки
                    CircularProgressIndicator()
                }
                
                is AuthState.Authenticated -> {
                    // Ничего не отображаем, будет выполнен переход на главный экран
                }
                
                is AuthState.Error -> {
                    // Ошибка отображается через Snackbar
                    Text(
                        text = "Произошла ошибка. Попробуйте еще раз.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
} 