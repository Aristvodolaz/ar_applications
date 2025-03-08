package com.ai_technologi.ar_application.videocall.data.repository

import com.ai_technologi.ar_application.core.network.ApiResult
import com.ai_technologi.ar_application.core.network.models.CallInfoResponse
import com.ai_technologi.ar_application.core.network.models.CallResponse
import com.ai_technologi.ar_application.core.network.models.EndCallResponse
import com.ai_technologi.ar_application.videocall.domain.model.Annotation
import com.ai_technologi.ar_application.videocall.domain.model.ChatMessage
import com.ai_technologi.ar_application.videocall.domain.model.SharedFile
import kotlinx.coroutines.flow.Flow
import org.webrtc.VideoTrack

/**
 * Интерфейс репозитория для работы с видеозвонками.
 */
interface VideoCallRepository {
    /**
     * Инициализация звонка.
     *
     * @param userId ID пользователя, которому звоним
     * @return результат инициализации звонка
     */
    suspend fun initiateCall(userId: String): ApiResult<CallResponse>

    /**
     * Получение информации о звонке.
     *
     * @param callId ID звонка
     * @return информация о звонке
     */
    suspend fun getCallInfo(callId: String): ApiResult<CallInfoResponse>

    /**
     * Завершение звонка.
     *
     * @param callId ID звонка
     * @return результат завершения звонка
     */
    suspend fun endCall(callId: String): ApiResult<EndCallResponse>

    /**
     * Инициализация WebRTC.
     *
     * @param callId ID звонка
     */
    suspend fun initializeWebRtc(callId: String)

    /**
     * Включение/выключение микрофона.
     *
     * @param enabled включен ли микрофон
     */
    fun toggleMicrophone(enabled: Boolean)

    /**
     * Включение/выключение камеры.
     *
     * @param enabled включена ли камера
     */
    fun toggleCamera(enabled: Boolean)

    /**
     * Получение локального видеопотока.
     *
     * @return локальный видеопоток
     */
    fun getLocalVideoTrack(): VideoTrack?

    /**
     * Получение удаленного видеопотока.
     *
     * @return удаленный видеопоток
     */
    fun getRemoteVideoTrack(): VideoTrack?

    /**
     * Добавление аннотации.
     *
     * @param annotation аннотация
     */
    suspend fun addAnnotation(annotation: Annotation)

    /**
     * Удаление аннотации.
     *
     * @param annotationId ID аннотации
     */
    suspend fun removeAnnotation(annotationId: String)

    /**
     * Получение списка аннотаций.
     *
     * @return поток списка аннотаций
     */
    fun getAnnotations(): Flow<List<Annotation>>

    /**
     * Отправка сообщения в чат.
     *
     * @param text текст сообщения
     */
    suspend fun sendMessage(text: String)

    /**
     * Получение списка сообщений.
     *
     * @return поток списка сообщений
     */
    fun getMessages(): Flow<List<ChatMessage>>

    /**
     * Отправка файла.
     *
     * @param filePath путь к файлу
     * @param fileName имя файла
     * @param fileType тип файла
     */
    suspend fun sendFile(filePath: String, fileName: String, fileType: String)

    /**
     * Получение списка файлов.
     *
     * @return поток списка файлов
     */
    fun getSharedFiles(): Flow<List<SharedFile>>

    /**
     * Логирование звонка.
     *
     * @param callId ID звонка
     * @param duration длительность звонка в секундах
     */
    suspend fun logCall(callId: String, duration: Long)

    /**
     * Освобождение ресурсов.
     */
    fun release()
} 