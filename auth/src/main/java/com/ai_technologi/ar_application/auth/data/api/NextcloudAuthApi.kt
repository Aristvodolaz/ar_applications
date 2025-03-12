package com.ai_technologi.ar_application.auth.data.api

import com.ai_technologi.ar_application.auth.data.model.AuthRequest
import com.ai_technologi.ar_application.auth.data.model.AuthResponse
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * API-интерфейс для аутентификации через Nextcloud.
 */
interface NextcloudAuthApi {
    
    /**
     * Аутентификация пользователя через PIN-код.
     *
     * @param request запрос с логином и PIN-кодом
     * @return ответ с результатом аутентификации
     */
    @POST("login/pin")
    suspend fun authenticateWithPin(@Body request: AuthRequest): AuthResponse
} 