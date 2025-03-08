package com.ai_technologi.ar_application.videocall.domain.model

import com.ai_technologi.ar_application.core.mvi.MviState
import com.ai_technologi.ar_application.core.network.models.ParticipantInfo
import org.webrtc.VideoTrack

/**
 * Состояние экрана видеозвонка.
 */
sealed class VideoCallState : MviState {
    /**
     * Начальное состояние.
     */
    object Initial : VideoCallState()

    /**
     * Состояние загрузки.
     */
    object Loading : VideoCallState()

    /**
     * Состояние инициализации звонка.
     *
     * @param userId ID пользователя, которому звоним
     */
    data class Connecting(val userId: String) : VideoCallState()

    /**
     * Состояние активного звонка.
     *
     * @param callId ID звонка
     * @param participants список участников звонка
     * @param localVideoTrack локальный видеопоток (с камеры очков)
     * @param remoteVideoTrack удаленный видеопоток (от эксперта)
     * @param isMicEnabled включен ли микрофон
     * @param isCameraEnabled включена ли камера
     * @param annotations список аннотаций от эксперта
     * @param messages список сообщений в чате
     * @param sharedFiles список общих файлов
     */
    data class Active(
        val callId: String,
        val participants: List<ParticipantInfo>,
        val localVideoTrack: VideoTrack? = null,
        val remoteVideoTrack: VideoTrack? = null,
        val isMicEnabled: Boolean = true,
        val isCameraEnabled: Boolean = true,
        val annotations: List<Annotation> = emptyList(),
        val messages: List<ChatMessage> = emptyList(),
        val sharedFiles: List<SharedFile> = emptyList()
    ) : VideoCallState()

    /**
     * Состояние завершения звонка.
     *
     * @param callId ID звонка
     * @param duration длительность звонка в секундах
     */
    data class Ended(
        val callId: String,
        val duration: Long
    ) : VideoCallState()

    /**
     * Состояние ошибки.
     *
     * @param message сообщение об ошибке
     */
    data class Error(val message: String) : VideoCallState()
}

/**
 * Аннотация от эксперта.
 *
 * @param id уникальный идентификатор аннотации
 * @param type тип аннотации
 * @param points список точек аннотации
 * @param color цвет аннотации
 * @param timestamp время создания аннотации
 */
data class Annotation(
    val id: String,
    val type: AnnotationType,
    val points: List<Point>,
    val color: Int,
    val timestamp: Long
)

/**
 * Тип аннотации.
 */
enum class AnnotationType {
    ARROW, CIRCLE, RECTANGLE, FREEHAND
}

/**
 * Точка на экране.
 *
 * @param x координата X
 * @param y координата Y
 */
data class Point(val x: Float, val y: Float)

/**
 * Сообщение в чате.
 *
 * @param id уникальный идентификатор сообщения
 * @param senderId ID отправителя
 * @param senderName имя отправителя
 * @param text текст сообщения
 * @param timestamp время отправки сообщения
 */
data class ChatMessage(
    val id: String,
    val senderId: String,
    val senderName: String,
    val text: String,
    val timestamp: Long
)

/**
 * Общий файл.
 *
 * @param id уникальный идентификатор файла
 * @param name имя файла
 * @param type тип файла
 * @param url URL файла
 * @param size размер файла в байтах
 * @param senderId ID отправителя
 * @param senderName имя отправителя
 * @param timestamp время отправки файла
 */
data class SharedFile(
    val id: String,
    val name: String,
    val type: String,
    val url: String,
    val size: Long,
    val senderId: String,
    val senderName: String,
    val timestamp: Long
) 