package com.ai_technologi.ar_application.auth.presentation.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import com.ai_technologi.ar_application.core.ui.ARAdaptiveUIProvider
import com.ai_technologi.ar_application.core.ui.ARButton
import com.ai_technologi.ar_application.core.ui.ARHeading
import com.ai_technologi.ar_application.core.ui.ARLoadingIndicator
import com.ai_technologi.ar_application.core.ui.LocalARAdaptiveUIConfig

/**
 * Экран аутентификации.
 *
 * @param onAuthSuccess колбэк, вызываемый при успешной аутентификации
 * @param viewModel ViewModel для экрана аутентификации
 */
@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    ARAdaptiveUIProvider {
        val config = LocalARAdaptiveUIConfig.current
        val state by viewModel.state.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }
        
        // Обработка успешной аутентификации
        LaunchedEffect(state) {
            if (state is AuthState.Success) {
                onAuthSuccess()
            }
        }
        
        // Обработка ошибок
        LaunchedEffect(state) {
            if (state is AuthState.Error) {
                snackbarHostState.showSnackbar(
                    message = (state as AuthState.Error).message
                )
                viewModel.processIntent(AuthIntent.Reset)
            }
        }
        
        // При запуске экрана сразу переходим к сканированию QR-кода
        LaunchedEffect(Unit) {
            if (state is AuthState.Initial) {
                viewModel.processIntent(AuthIntent.StartScanLogin)
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
                        // Начальный экран (не должен отображаться, так как сразу переходим к сканированию)
                        ARLoadingIndicator(text = "Подготовка к сканированию...")
                    }
                    
                    is AuthState.ScanLogin -> {
                        // Экран сканирования QR-кода
                        ScanLoginScreen(
                            onLoginScanned = { login ->
                                viewModel.processIntent(AuthIntent.SetLogin(login))
                            },
                            onBackClick = {
                                viewModel.processIntent(AuthIntent.Reset)
                            }
                        )
                    }
                    
                    is AuthState.EnterPin -> {
                        // Экран ввода PIN-кода
                        val login = (state as AuthState.EnterPin).login
                        EnterPinScreen(
                            login = login,
                            onPinEntered = { pin ->
                                viewModel.processIntent(AuthIntent.AuthenticateWithPin(login, pin))
                            },
                            onBackClick = {
                                viewModel.processIntent(AuthIntent.Reset)
                            }
                        )
                    }
                    
                    is AuthState.Loading -> {
                        // Экран загрузки
                        ARLoadingIndicator(text = "Выполняется вход...")
                    }
                    
                    is AuthState.Success -> {
                        // Успешная аутентификация
                        // Ничего не отображаем, так как будет выполнен переход на следующий экран
                    }
                    
                    is AuthState.Error -> {
                        // Ошибка аутентификации
                        // Отображается снэкбар с ошибкой и возвращаемся к начальному экрану
                    }
                }
            }
        }
    }
} 