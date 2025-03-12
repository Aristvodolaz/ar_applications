package com.ai_technologi.ar_application.auth.data.repository

import android.content.SharedPreferences
import com.ai_technologi.ar_application.auth.data.api.NextcloudAuthApi
import com.ai_technologi.ar_application.auth.data.model.AuthRequest
import com.ai_technologi.ar_application.auth.domain.repository.AuthRepository
import com.ai_technologi.ar_application.core.util.ApiResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Реализация репозитория для аутентификации.
 *
 * @param api API для аутентификации
 * @param sharedPreferences хранилище для токена аутентификации
 */
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val api: NextcloudAuthApi,
    private val sharedPreferences: SharedPreferences
) : AuthRepository {
    
    companion object {
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_SERVER_URL = "server_url"
        private const val KEY_LOGIN_NAME = "login_name"
        private const val NEXTCLOUD_BASE_URL = "https://nextcloud.sitebill.site"
    }
    
    /**
     * Аутентификация пользователя через PIN-код.
     *
     * @param login логин пользователя
     * @param pin PIN-код пользователя
     * @return результат аутентификации
     */
    override suspend fun authenticateWithPin(login: String, pin: String): ApiResult<String> {
        return withContext(Dispatchers.IO) {
            try {
                val request = AuthRequest(login, pin)
                val response = api.authenticateWithPin(request)
                
                // Сохраняем данные аутентификации
                saveAuthToken(response.appPassword)
                saveServerUrl(response.server)
                saveLoginName(response.loginName)
                
                ApiResult.Success(response.appPassword)
            } catch (e: HttpException) {
                ApiResult.Error("Ошибка сервера: ${e.code()}")
            } catch (e: IOException) {
                ApiResult.Error("Ошибка сети: ${e.message}")
            } catch (e: Exception) {
                ApiResult.Error("Неизвестная ошибка: ${e.message}")
            }
        }
    }
    
    /**
     * Сохранение токена аутентификации.
     *
     * @param token токен аутентификации
     */
    override suspend fun saveAuthToken(token: String) {
        withContext(Dispatchers.IO) {
            sharedPreferences.edit().putString(KEY_AUTH_TOKEN, token).apply()
        }
    }
    
    /**
     * Сохранение URL сервера.
     *
     * @param serverUrl URL сервера
     */
    private suspend fun saveServerUrl(serverUrl: String) {
        withContext(Dispatchers.IO) {
            sharedPreferences.edit().putString(KEY_SERVER_URL, serverUrl).apply()
        }
    }
    
    /**
     * Сохранение имени пользователя.
     *
     * @param loginName имя пользователя
     */
    private suspend fun saveLoginName(loginName: String) {
        withContext(Dispatchers.IO) {
            sharedPreferences.edit().putString(KEY_LOGIN_NAME, loginName).apply()
        }
    }
    
    /**
     * Получение токена аутентификации.
     *
     * @return токен аутентификации или null, если пользователь не аутентифицирован
     */
    override suspend fun getAuthToken(): String? {
        return withContext(Dispatchers.IO) {
            sharedPreferences.getString(KEY_AUTH_TOKEN, null)
        }
    }
    
    /**
     * Проверка, аутентифицирован ли пользователь.
     *
     * @return true, если пользователь аутентифицирован, иначе false
     */
    override suspend fun isAuthenticated(): Boolean {
        return getAuthToken() != null
    }
    
    /**
     * Выход из системы.
     */
    override suspend fun logout() {
        withContext(Dispatchers.IO) {
            sharedPreferences.edit()
                .remove(KEY_AUTH_TOKEN)
                .remove(KEY_SERVER_URL)
                .remove(KEY_LOGIN_NAME)
                .apply()
        }
    }
} 