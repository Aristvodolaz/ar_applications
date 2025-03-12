package com.ai_technologi.ar_application.auth.data.model

import com.google.gson.annotations.SerializedName

/**
 * Модель запроса для аутентификации через Nextcloud.
 *
 * @param login логин пользователя
 * @param pin PIN-код пользователя
 */
data class AuthRequest(
    @SerializedName("login")
    val login: String,
    
    @SerializedName("pin")
    val pin: String
) 