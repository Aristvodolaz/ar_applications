package com.ai_technologi.ar_application.auth.presentation.viewmodel

import com.ai_technologi.ar_application.auth.data.repository.AuthRepository
import com.ai_technologi.ar_application.auth.domain.model.AuthIntent
import com.ai_technologi.ar_application.auth.domain.model.AuthState
import com.ai_technologi.ar_application.core.mvi.MviViewModel
import com.ai_technologi.ar_application.core.network.ApiResult
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel для экрана аутентификации.
 *
 * @param repository репозиторий для работы с аутентификацией
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : MviViewModel<AuthState, AuthIntent>(AuthState.Initial) {

    /**
     * Обработка Intent.
     *
     * @param intent Intent, который нужно обработать
     */
    override suspend fun handleIntent(intent: AuthIntent) {
        when (intent) {
            is AuthIntent.StartQrScan -> handleStartQrScan()
            is AuthIntent.QrScanned -> handleQrScanned(intent.qrToken)
            is AuthIntent.EnterPin -> handleEnterPin(intent.pin)
            is AuthIntent.ConfirmPin -> handleConfirmPin()
            is AuthIntent.Reset -> handleReset()
        }
    }

    /**
     * Обработка Intent для начала сканирования QR-кода.
     */
    private fun handleStartQrScan() {
        updateState { AuthState.ScanningQrCode }
    }

    /**
     * Обработка Intent для обработки результата сканирования QR-кода.
     *
     * @param qrToken токен из QR-кода
     */
    private suspend fun handleQrScanned(qrToken: String) {
        updateState { AuthState.Loading }
        
        when (val result = repository.authenticateWithQrCode(qrToken)) {
            is ApiResult.Success -> {
                val token = result.data.token
                repository.saveAuthToken(token)
                updateState { AuthState.QrScanned(token) }
            }
            is ApiResult.Error -> {
                Timber.e("Ошибка аутентификации: ${result.message}")
                updateState { AuthState.Error(result.message) }
            }
            is ApiResult.Loading -> {
                // Ничего не делаем, уже в состоянии Loading
            }
        }
    }

    /**
     * Обработка Intent для ввода PIN-кода.
     *
     * @param pin PIN-код
     */
    private fun handleEnterPin(pin: String) {
        val currentState = state.value
        if (currentState is AuthState.QrScanned) {
            updateState { AuthState.EnteringPin(currentState.token, pin) }
        } else if (currentState is AuthState.EnteringPin) {
            updateState { currentState.copy(pin = pin) }
        }
    }

    /**
     * Обработка Intent для подтверждения PIN-кода.
     */
    private suspend fun handleConfirmPin() {
        val currentState = state.value
        if (currentState !is AuthState.EnteringPin) {
            return
        }

        updateState { AuthState.Loading }
        
        when (val result = repository.confirmWithPin(currentState.token, currentState.pin)) {
            is ApiResult.Success -> {
                val response = result.data
                if (response.success && response.user != null) {
                    val user = response.user
                    repository.saveUserInfo(user.id, user.displayName, user.role)
                    updateState { 
                        AuthState.Authenticated(
                            userId = user.id,
                            userName = user.displayName,
                            userRole = user.role
                        ) 
                    }
                } else {
                    updateState { AuthState.Error("Неверный PIN-код") }
                }
            }
            is ApiResult.Error -> {
                Timber.e("Ошибка подтверждения PIN-кода: ${result.message}")
                updateState { AuthState.Error(result.message) }
            }
            is ApiResult.Loading -> {
                // Ничего не делаем, уже в состоянии Loading
            }
        }
    }

    /**
     * Обработка Intent для сброса состояния аутентификации.
     */
    private suspend fun handleReset() {
        repository.clearSession()
        updateState { AuthState.Initial }
    }
} 