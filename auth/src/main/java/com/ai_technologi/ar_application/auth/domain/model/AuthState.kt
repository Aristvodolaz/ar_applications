package com.ai_technologi.ar_application.auth.domain.model

/**
 * Модель состояния для экрана аутентификации.
 */
sealed class AuthState {
    /**
     * Начальное состояние.
     */
    object Initial : AuthState()
    
    /**
     * Состояние сканирования логина.
     */
    object ScanLogin : AuthState()
    
    /**
     * Состояние ввода PIN-кода.
     *
     * @param login логин пользователя
     */
    data class EnterPin(val login: String) : AuthState()
    
    /**
     * Состояние загрузки.
     */
    object Loading : AuthState()
    
    /**
     * Состояние успешной аутентификации.
     *
     * @param token токен аутентификации
     */
    data class Success(val token: String) : AuthState()
    
    /**
     * Состояние ошибки.
     *
     * @param message сообщение об ошибке
     */
    data class Error(val message: String) : AuthState()
} 