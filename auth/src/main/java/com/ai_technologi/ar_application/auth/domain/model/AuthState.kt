package com.ai_technologi.ar_application.auth.domain.model

import com.ai_technologi.ar_application.core.mvi.MviState

/**
 * Состояние экрана аутентификации.
 */
sealed class AuthState : MviState {
    /**
     * Начальное состояние.
     */
    object Initial : AuthState()

    /**
     * Состояние загрузки.
     */
    object Loading : AuthState()

    /**
     * Состояние сканирования QR-кода.
     */
    object ScanningQrCode : AuthState()

    /**
     * Состояние после успешного сканирования QR-кода.
     * Пользователь должен ввести PIN-код.
     *
     * @param token токен, полученный после сканирования QR-кода
     */
    data class QrScanned(val token: String) : AuthState()

    /**
     * Состояние ввода PIN-кода.
     *
     * @param token токен, полученный после сканирования QR-кода
     * @param pin текущий введенный PIN-код
     */
    data class EnteringPin(val token: String, val pin: String = "") : AuthState()

    /**
     * Состояние успешной аутентификации.
     *
     * @param userId ID пользователя
     * @param userName имя пользователя
     * @param userRole роль пользователя
     */
    data class Authenticated(
        val userId: String,
        val userName: String,
        val userRole: String
    ) : AuthState()

    /**
     * Состояние ошибки.
     *
     * @param message сообщение об ошибке
     */
    data class Error(val message: String) : AuthState()
} 