package com.ai_technologi.ar_application.core.network

import com.ai_technologi.ar_application.core.network.models.AuthConfirmResponse
import com.ai_technologi.ar_application.core.network.models.AuthResponse
import com.ai_technologi.ar_application.core.network.models.CallInfoResponse
import com.ai_technologi.ar_application.core.network.models.CallResponse
import com.ai_technologi.ar_application.core.network.models.EndCallResponse
import com.ai_technologi.ar_application.core.network.models.NextCloudResponse
import com.ai_technologi.ar_application.core.network.models.UsersResponse
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Интерфейс для работы с NextCloud API.
 * Содержит методы для аутентификации, получения списка пользователей и работы с видеозвонками.
 */
interface NextCloudApi {

    /**
     * Аутентификация пользователя по QR-коду.
     *
     * @param qrToken токен из QR-кода
     * @return Response с токеном доступа
     */
    @FormUrlEncoded
    @POST("ocs/v2.php/apps/user_authentication_qr/api/v1/authenticate")
    suspend fun authenticateWithQrCode(
        @Field("qr_token") qrToken: String
    ): Response<NextCloudResponse<AuthResponse>>

    /**
     * Подтверждение аутентификации с помощью PIN-кода.
     *
     * @param token токен доступа
     * @param pin PIN-код
     * @return Response с результатом аутентификации
     */
    @FormUrlEncoded
    @POST("ocs/v2.php/apps/user_authentication_qr/api/v1/confirm")
    suspend fun confirmWithPin(
        @Header("Authorization") token: String,
        @Field("pin") pin: String
    ): Response<NextCloudResponse<AuthConfirmResponse>>

    /**
     * Получение списка пользователей.
     *
     * @param token токен доступа
     * @param search строка поиска (опционально)
     * @param limit максимальное количество пользователей (опционально)
     * @param offset смещение для пагинации (опционально)
     * @return Response со списком пользователей
     */
    @GET("ocs/v2.php/cloud/users")
    suspend fun getUsers(
        @Header("Authorization") token: String,
        @Query("search") search: String? = null,
        @Query("limit") limit: Int? = null,
        @Query("offset") offset: Int? = null
    ): Response<NextCloudResponse<UsersResponse>>

    /**
     * Инициализация видеозвонка.
     *
     * @param token токен доступа
     * @param userId ID пользователя, которому звоним
     * @return Response с информацией о звонке
     */
    @FormUrlEncoded
    @POST("ocs/v2.php/apps/spreed/api/v1/call/{userId}")
    suspend fun initiateCall(
        @Header("Authorization") token: String,
        @Path("userId") userId: String
    ): Response<NextCloudResponse<CallResponse>>

    /**
     * Получение информации о текущем звонке.
     *
     * @param token токен доступа
     * @param callId ID звонка
     * @return Response с информацией о звонке
     */
    @GET("ocs/v2.php/apps/spreed/api/v1/call/{callId}")
    suspend fun getCallInfo(
        @Header("Authorization") token: String,
        @Path("callId") callId: String
    ): Response<NextCloudResponse<CallInfoResponse>>

    /**
     * Завершение звонка.
     *
     * @param token токен доступа
     * @param callId ID звонка
     * @return Response с результатом завершения звонка
     */
    @POST("ocs/v2.php/apps/spreed/api/v1/call/{callId}/end")
    suspend fun endCall(
        @Header("Authorization") token: String,
        @Path("callId") callId: String
    ): Response<NextCloudResponse<EndCallResponse>>
} 