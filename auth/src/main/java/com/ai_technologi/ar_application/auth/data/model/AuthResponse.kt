package com.ai_technologi.ar_application.auth.data.model

import com.google.gson.annotations.SerializedName

/**
 * Модель ответа для аутентификации через Nextcloud.
 *
 * @param server URL сервера Nextcloud
 * @param loginName имя пользователя
 * @param appPassword пароль приложения (токен доступа)
 */
data class AuthResponse(
    @SerializedName("server")
    val server: String,
    
    @SerializedName("loginName")
    val loginName: String,
    
    @SerializedName("appPassword")
    val appPassword: String
) 