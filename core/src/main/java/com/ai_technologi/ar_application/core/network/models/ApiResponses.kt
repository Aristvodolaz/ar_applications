package com.ai_technologi.ar_application.core.network.models

import com.google.gson.annotations.SerializedName

/**
 * Базовый класс для всех ответов от NextCloud API.
 */
data class NextCloudResponse<T>(
    @SerializedName("ocs") val ocs: OcsWrapper<T>
)

/**
 * Обертка для данных в ответе от NextCloud API.
 */
data class OcsWrapper<T>(
    @SerializedName("meta") val meta: Meta,
    @SerializedName("data") val data: T
)

/**
 * Метаданные ответа от NextCloud API.
 */
data class Meta(
    @SerializedName("status") val status: String,
    @SerializedName("statuscode") val statusCode: Int,
    @SerializedName("message") val message: String
)

/**
 * Ответ на запрос аутентификации по QR-коду.
 */
data class AuthResponse(
    @SerializedName("token") val token: String,
    @SerializedName("server") val server: String
)

/**
 * Ответ на запрос подтверждения аутентификации с PIN-кодом.
 */
data class AuthConfirmResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("user") val user: UserInfo?
)

/**
 * Информация о пользователе.
 */
data class UserInfo(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("displayname") val displayName: String,
    @SerializedName("email") val email: String,
    @SerializedName("avatar") val avatarUrl: String?,
    @SerializedName("role") val role: String
)


/**
 * Ответ на запрос инициализации звонка.
 */
data class CallResponse(
    @SerializedName("callId") val callId: String,
    @SerializedName("status") val status: String
)

/**
 * Информация о звонке.
 */
data class CallInfoResponse(
    @SerializedName("callId") val callId: String,
    @SerializedName("status") val status: String,
    @SerializedName("participants") val participants: List<ParticipantInfo>,
    @SerializedName("startTime") val startTime: Long,
    @SerializedName("duration") val duration: Long?
)

/**
 * Информация об участнике звонка.
 */
data class ParticipantInfo(
    @SerializedName("userId") val userId: String,
    @SerializedName("displayName") val displayName: String,
    @SerializedName("status") val status: String,
    @SerializedName("avatarUrl") val avatarUrl: String?
)

/**
 * Ответ на запрос завершения звонка.
 */
data class EndCallResponse(
    @SerializedName("success") val success: Boolean
) 