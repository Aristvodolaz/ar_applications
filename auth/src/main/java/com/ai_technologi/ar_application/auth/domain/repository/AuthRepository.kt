package com.ai_technologi.ar_application.auth.domain.repository

import com.ai_technologi.ar_application.core.util.ApiResult

/**
 * Интерфейс репозитория для аутентификации.
 */
interface AuthRepository {
    
    /**
     * Аутентификация пользователя через PIN-код.
     *
     * @param login логин пользователя
     * @param pin PIN-код пользователя
     * @return результат аутентификации
     */
    suspend fun authenticateWithPin(login: String, pin: String): ApiResult<String>
    
    /**
     * Сохранение токена аутентификации.
     *
     * @param token токен аутентификации
     */
    suspend fun saveAuthToken(token: String)
    
    /**
     * Получение токена аутентификации.
     *
     * @return токен аутентификации или null, если пользователь не аутентифицирован
     */
    suspend fun getAuthToken(): String?
    
    /**
     * Проверка, аутентифицирован ли пользователь.
     *
     * @return true, если пользователь аутентифицирован, иначе false
     */
    suspend fun isAuthenticated(): Boolean
    
    /**
     * Выход из системы.
     */
    suspend fun logout()
} 