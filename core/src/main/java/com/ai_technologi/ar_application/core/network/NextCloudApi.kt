package com.ai_technologi.ar_application.core.network

import com.ai_technologi.ar_application.core.network.models.AuthConfirmResponse
import com.ai_technologi.ar_application.core.network.models.AuthResponse
import com.ai_technologi.ar_application.core.network.models.CallInfoResponse
import com.ai_technologi.ar_application.core.network.models.CallResponse
import com.ai_technologi.ar_application.core.network.models.ContactsResponse
import com.ai_technologi.ar_application.core.network.models.EndCallResponse
import com.ai_technologi.ar_application.core.network.models.NextCloudResponse
import com.ai_technologi.ar_application.core.network.models.UsersResponse
import com.ai_technologi.ar_application.core.network.models.RoomResponse
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
    suspend fun getCloudUsers(
        @Header("Authorization") token: String,
        @Query("search") search: String? = null,
        @Query("limit") limit: Int? = null,
        @Query("offset") offset: Int? = null,
        @Query("format") format: String = "json"
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

    /**
     * Получение списка контактов
     * 
     * Эндпоинт аналогичен тому, что используется в Nextcloud Talk
     * 
     * @param token Bearer токен для авторизации
     * @param search строка поиска (опционально)
     * @param filter фильтрация контактов (all, favorites, recent, groups)
     * @param itemType тип элемента (call для звонков)
     * @param shareType тип общего доступа (0 для пользователей)
     * @param limit максимальное количество результатов
     * @return Response с данными контактов
     */
    @GET("ocs/v2.php/core/autocomplete/get")
    suspend fun getContacts(
        @Header("Authorization") token: String,
        @Query("search") search: String? = null,
        @Query("filter") filter: String = "all",
        @Query("itemType") itemType: String = "call",
        @Query("shareTypes[]") shareType: Int = 0,
        @Query("limit") limit: Int = 50
    ): Response<ContactsResponse>
    
    /**
     * Альтернативный эндпоинт для получения контактов через Nextcloud Address Book
     * (используется, если основной эндпоинт недоступен)
     */
    @GET("ocs/v2.php/apps/dav/api/v1/addressbooks/contacts")
    suspend fun getAddressBookContacts(
        @Header("Authorization") token: String,
        @Query("search") search: String? = null
    ): Response<ContactsResponse>
    
    /**
     * Получение информации о статусе пользователя
     */
    @GET("ocs/v2.php/apps/user_status/api/v1/user_status")
    suspend fun getUserStatus(
        @Header("Authorization") token: String,
        @Query("userId") userId: String
    ): Response<Any>
    
    /**
     * Создание комнаты для звонка
     * 
     * @param token Bearer токен для авторизации
     * @param roomName название комнаты
     * @param roomType тип комнаты (1 - one-to-one, 2 - group, 3 - public)
     * @param participants список участников (идентификаторы пользователей)
     * @return Response с информацией о созданной комнате
     */
    @FormUrlEncoded
    @POST("ocs/v2.php/apps/spreed/api/v4/room")
    suspend fun createRoom(
        @Header("Authorization") token: String,
        @Field("roomName") roomName: String,
        @Field("roomType") roomType: Int = 2, // По умолчанию - групповая комната
        @Field("participants[]") participants: List<String>,
        @Query("format") format: String = "json"
    ): Response<NextCloudResponse<RoomResponse>>
} 