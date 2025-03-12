package com.ai_technologi.ar_application.auth.domain.model

import com.ai_technologi.ar_application.core.mvi.MviIntent

/**
 * Модель интентов для экрана аутентификации.
 */
sealed class AuthIntent : MviIntent {
    /**
     * Интент для начала сканирования логина.
     */
    object StartScanLogin : AuthIntent()
    
    /**
     * Интент для установки логина.
     *
     * @param login логин пользователя
     */
    data class SetLogin(val login: String) : AuthIntent()
    
    /**
     * Интент для аутентификации с PIN-кодом.
     *
     * @param login логин пользователя
     * @param pin PIN-код пользователя
     */
    data class AuthenticateWithPin(val login: String, val pin: String) : AuthIntent()
    
    /**
     * Интент для сброса состояния.
     */
    object Reset : AuthIntent()
} 