package com.ai_technologi.ar_application.auth.data.repository

import com.ai_technologi.ar_application.core.network.ApiResult
import com.ai_technologi.ar_application.core.network.models.AuthConfirmResponse
import com.ai_technologi.ar_application.core.network.models.AuthResponse

/**
 * Интерфейс репозитория для работы с аутентификацией.
 */
interface AuthRepository {
    /**
     * Аутентификация по QR-коду.
     *
     * @param qrToken токен из QR-кода
     * @return результат аутентификации
     */
    suspend fun authenticateWithQrCode(qrToken: String): ApiResult<AuthResponse>

    /**
     * Подтверждение аутентификации с помощью PIN-кода.
     *
     * @param token токен доступа
     * @param pin PIN-код
     * @return результат подтверждения
     */
    suspend fun confirmWithPin(token: String, pin: String): ApiResult<AuthConfirmResponse>

    /**
     * Сохранение токена аутентификации.
     *
     * @param token токен доступа
     */
    suspend fun saveAuthToken(token: String)

    /**
     * Сохранение информации о пользователе.
     *
     * @param userId ID пользователя
     * @param userName имя пользователя
     * @param userRole роль пользователя
     */
    suspend fun saveUserInfo(userId: String, userName: String, userRole: String)

    /**
     * Очистка данных сессии.
     */
    suspend fun clearSession()
} 