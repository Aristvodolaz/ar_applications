package com.ai_technologi.ar_application.auth.data.repository

import com.ai_technologi.ar_application.core.data.SessionManager
import com.ai_technologi.ar_application.core.network.ApiResult
import com.ai_technologi.ar_application.core.network.NextCloudApi
import com.ai_technologi.ar_application.core.network.models.AuthConfirmResponse
import com.ai_technologi.ar_application.core.network.models.AuthResponse
import timber.log.Timber
import javax.inject.Inject

/**
 * Реализация репозитория для работы с аутентификацией.
 *
 * @param api API для работы с NextCloud
 * @param sessionManager менеджер сессии для хранения данных пользователя
 */
class AuthRepositoryImpl @Inject constructor(
    private val api: NextCloudApi,
    private val sessionManager: SessionManager
) : AuthRepository {

    /**
     * Аутентификация по QR-коду.
     *
     * @param qrToken токен из QR-кода
     * @return результат аутентификации
     */
    override suspend fun authenticateWithQrCode(qrToken: String): ApiResult<AuthResponse> {
        return try {
            val response = api.authenticateWithQrCode(qrToken)
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!.ocs.data
                ApiResult.Success(authResponse)
            } else {
                Timber.e("Ошибка аутентификации: ${response.errorBody()?.string()}")
                ApiResult.Error(message = "Ошибка аутентификации")
            }
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при аутентификации по QR-коду")
            ApiResult.Error(e, "Ошибка при аутентификации: ${e.message}")
        }
    }

    /**
     * Подтверждение аутентификации с помощью PIN-кода.
     *
     * @param token токен доступа
     * @param pin PIN-код
     * @return результат подтверждения
     */
    override suspend fun confirmWithPin(token: String, pin: String): ApiResult<AuthConfirmResponse> {
        return try {
            val response = api.confirmWithPin("Bearer $token", pin)
            if (response.isSuccessful && response.body() != null) {
                val confirmResponse = response.body()!!.ocs.data
                ApiResult.Success(confirmResponse)
            } else {
                Timber.e("Ошибка подтверждения PIN-кода: ${response.errorBody()?.string()}")
                ApiResult.Error(message = "Неверный PIN-код")
            }
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при подтверждении PIN-кода")
            ApiResult.Error(e, "Ошибка при подтверждении: ${e.message}")
        }
    }

    /**
     * Сохранение токена аутентификации.
     *
     * @param token токен доступа
     */
    override suspend fun saveAuthToken(token: String) {
        sessionManager.saveAuthToken(token)
    }

    /**
     * Сохранение информации о пользователе.
     *
     * @param userId ID пользователя
     * @param userName имя пользователя
     * @param userRole роль пользователя
     */
    override suspend fun saveUserInfo(userId: String, userName: String, userRole: String) {
        sessionManager.saveUserInfo(userId, userName, userRole)
    }

    /**
     * Очистка данных сессии.
     */
    override suspend fun clearSession() {
        sessionManager.clearSession()
    }
} 