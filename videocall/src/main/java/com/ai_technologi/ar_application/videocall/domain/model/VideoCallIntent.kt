package com.ai_technologi.ar_application.videocall.domain.model

import com.ai_technologi.ar_application.core.mvi.MviIntent
import org.webrtc.VideoTrack

/**
 * Intent для экрана видеозвонка.
 */
sealed class VideoCallIntent : MviIntent {
    /**
     * Intent для инициализации звонка.
     *
     * @param userId ID пользователя, которому звоним
     */
    data class InitiateCall(val userId: String) : VideoCallIntent()

    /**
     * Intent для ответа на звонок.
     *
     * @param callId ID звонка
     */
    data class AnswerCall(val callId: String) : VideoCallIntent()

    /**
     * Intent для завершения звонка.
     */
    object EndCall : VideoCallIntent()

    /**
     * Intent для включения/выключения микрофона.
     *
     * @param enabled включен ли микрофон
     */
    data class ToggleMicrophone(val enabled: Boolean) : VideoCallIntent()

    /**
     * Intent для включения/выключения камеры.
     *
     * @param enabled включена ли камера
     */
    data class ToggleCamera(val enabled: Boolean) : VideoCallIntent()

    /**
     * Intent для обновления локального видеопотока.
     *
     * @param videoTrack локальный видеопоток
     */
    data class UpdateLocalVideoTrack(val videoTrack: VideoTrack) : VideoCallIntent()

    /**
     * Intent для обновления удаленного видеопотока.
     *
     * @param videoTrack удаленный видеопоток
     */
    data class UpdateRemoteVideoTrack(val videoTrack: VideoTrack) : VideoCallIntent()

    /**
     * Intent для добавления аннотации.
     *
     * @param annotation аннотация
     */
    data class AddAnnotation(val annotation: Annotation) : VideoCallIntent()

    /**
     * Intent для удаления аннотации.
     *
     * @param annotationId ID аннотации
     */
    data class RemoveAnnotation(val annotationId: String) : VideoCallIntent()

    /**
     * Intent для отправки сообщения в чат.
     *
     * @param text текст сообщения
     */
    data class SendMessage(val text: String) : VideoCallIntent()

    /**
     * Intent для отправки файла.
     *
     * @param filePath путь к файлу
     * @param fileName имя файла
     * @param fileType тип файла
     */
    data class SendFile(
        val filePath: String,
        val fileName: String,
        val fileType: String
    ) : VideoCallIntent()

    /**
     * Intent для обновления состояния звонка.
     */
    object UpdateCallState : VideoCallIntent()

    /**
     * Intent для сброса состояния.
     */
    object Reset : VideoCallIntent()
} 