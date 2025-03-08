package com.ai_technologi.ar_application.auth.domain.model

import com.ai_technologi.ar_application.core.mvi.MviIntent

/**
 * Intent для экрана аутентификации.
 */
sealed class AuthIntent : MviIntent {
    /**
     * Intent для начала сканирования QR-кода.
     */
    object StartQrScan : AuthIntent()

    /**
     * Intent для обработки результата сканирования QR-кода.
     *
     * @param qrToken токен из QR-кода
     */
    data class QrScanned(val qrToken: String) : AuthIntent()

    /**
     * Intent для ввода PIN-кода.
     *
     * @param pin PIN-код
     */
    data class EnterPin(val pin: String) : AuthIntent()

    /**
     * Intent для подтверждения PIN-кода.
     */
    object ConfirmPin : AuthIntent()

    /**
     * Intent для сброса состояния аутентификации.
     */
    object Reset : AuthIntent()
} 