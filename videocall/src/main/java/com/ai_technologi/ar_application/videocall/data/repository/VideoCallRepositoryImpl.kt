package com.ai_technologi.ar_application.videocall.data.repository

import com.ai_technologi.ar_application.core.data.SessionManager
import com.ai_technologi.ar_application.core.network.ApiResult
import com.ai_technologi.ar_application.core.network.NextCloudApi
import com.ai_technologi.ar_application.core.network.models.CallInfoResponse
import com.ai_technologi.ar_application.core.network.models.CallResponse
import com.ai_technologi.ar_application.core.network.models.EndCallResponse
import com.ai_technologi.ar_application.videocall.data.webrtc.NextCloudSignalingClient
import com.ai_technologi.ar_application.videocall.data.webrtc.WebRtcClient
import com.ai_technologi.ar_application.videocall.domain.model.Annotation
import com.ai_technologi.ar_application.videocall.domain.model.ChatMessage
import com.ai_technologi.ar_application.videocall.domain.model.SharedFile
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription
import org.webrtc.VideoTrack
import timber.log.Timber
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Реализация репозитория для работы с видеозвонками.
 *
 * @param api API для работы с NextCloud
 * @param sessionManager менеджер сессии для получения токена аутентификации
 * @param webRtcClient клиент для работы с WebRTC
 * @param signalingClient клиент для работы с сигнализацией WebRTC
 * @param firestore база данных Firestore для логирования
 */
@Singleton
class VideoCallRepositoryImpl @Inject constructor(
    private val api: NextCloudApi,
    private val sessionManager: SessionManager,
    private val webRtcClient: WebRtcClient,
    private val signalingClient: NextCloudSignalingClient,
    private val firestore: FirebaseFirestore
) : VideoCallRepository {
    
    private val scope = CoroutineScope(Dispatchers.IO)
    
    private val _annotationsFlow = MutableStateFlow<List<Annotation>>(emptyList())
    private val _messagesFlow = MutableStateFlow<List<ChatMessage>>(emptyList())
    private val _sharedFilesFlow = MutableStateFlow<List<SharedFile>>(emptyList())
    
    private var currentCallId: String? = null
    private var callStartTime: Long = 0
    
    init {
        // Наблюдение за удаленным SDP и ICE-кандидатами
        scope.launch {
            signalingClient.remoteSessionDescriptionFlow.collect { sessionDescription ->
                sessionDescription?.let {
                    webRtcClient.setRemoteDescription(it) { success ->
                        if (success) {
                            if (it.type == SessionDescription.Type.OFFER) {
                                // Если получили предложение, создаем ответ
                                webRtcClient.createAnswer { answer ->
                                    answer?.let { sdp ->
                                        scope.launch {
                                            currentCallId?.let { callId ->
                                                signalingClient.answerCall(callId, sdp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        scope.launch {
            signalingClient.remoteIceCandidatesFlow.collect { iceCandidates ->
                iceCandidates.forEach { iceCandidate ->
                    webRtcClient.addIceCandidate(iceCandidate)
                }
            }
        }
        
        // Наблюдение за локальными ICE-кандидатами
        scope.launch {
            webRtcClient.signalingStateFlow.collect { state ->
                // Обработка изменений состояния сигнализации
            }
        }
    }
    
    /**
     * Инициализация звонка.
     *
     * @param userId ID пользователя, которому звоним
     * @return результат инициализации звонка
     */
    override suspend fun initiateCall(userId: String): ApiResult<CallResponse> {
        return try {
            val token = "Bearer ${sessionManager.authToken.first() ?: ""}"
            val response = api.initiateCall(token, userId)
            
            if (response.isSuccessful && response.body() != null) {
                val callResponse = response.body()!!.ocs.data
                currentCallId = callResponse.callId
                callStartTime = System.currentTimeMillis()
                
                // Инициализация WebRTC
                initializeWebRtc(callResponse.callId)
                
                ApiResult.Success(callResponse)
            } else {
                Timber.e("Ошибка инициализации звонка: ${response.errorBody()?.string()}")
                ApiResult.Error(message = "Ошибка инициализации звонка")
            }
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при инициализации звонка")
            ApiResult.Error(e, "Ошибка при инициализации звонка: ${e.message}")
        }
    }
    
    /**
     * Получение информации о звонке.
     *
     * @param callId ID звонка
     * @return информация о звонке
     */
    override suspend fun getCallInfo(callId: String): ApiResult<CallInfoResponse> {
        return try {
            val token = "Bearer ${sessionManager.authToken.first() ?: ""}"
            val response = api.getCallInfo(token, callId)
            
            if (response.isSuccessful && response.body() != null) {
                val callInfoResponse = response.body()!!.ocs.data
                ApiResult.Success(callInfoResponse)
            } else {
                Timber.e("Ошибка получения информации о звонке: ${response.errorBody()?.string()}")
                ApiResult.Error(message = "Ошибка получения информации о звонке")
            }
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при получении информации о звонке")
            ApiResult.Error(e, "Ошибка при получении информации о звонке: ${e.message}")
        }
    }
    
    /**
     * Завершение звонка.
     *
     * @param callId ID звонка
     * @return результат завершения звонка
     */
    override suspend fun endCall(callId: String): ApiResult<EndCallResponse> {
        return try {
            val token = "Bearer ${sessionManager.authToken.first() ?: ""}"
            val response = api.endCall(token, callId)
            
            if (response.isSuccessful && response.body() != null) {
                val endCallResponse = response.body()!!.ocs.data
                
                // Логирование звонка
                val duration = (System.currentTimeMillis() - callStartTime) / 1000
                logCall(callId, duration)
                
                // Освобождение ресурсов
                signalingClient.endCall(callId)
                webRtcClient.release()
                
                currentCallId = null
                callStartTime = 0
                
                _annotationsFlow.value = emptyList()
                _messagesFlow.value = emptyList()
                _sharedFilesFlow.value = emptyList()
                
                ApiResult.Success(endCallResponse)
            } else {
                Timber.e("Ошибка завершения звонка: ${response.errorBody()?.string()}")
                ApiResult.Error(message = "Ошибка завершения звонка")
            }
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при завершении звонка")
            ApiResult.Error(e, "Ошибка при завершении звонка: ${e.message}")
        }
    }
    
    /**
     * Инициализация WebRTC.
     *
     * @param callId ID звонка
     */
    override suspend fun initializeWebRtc(callId: String) {
        webRtcClient.initialize()
        
        // Создание предложения
        webRtcClient.createOffer { sessionDescription ->
            sessionDescription?.let { sdp ->
                scope.launch {
                    val callId = signalingClient.initiateCall(callId, sdp)
                    callId?.let {
                        currentCallId = it
                    }
                }
            }
        }
        
        // Создание канала данных для аннотаций
        webRtcClient.createDataChannel("annotations")
    }
    
    /**
     * Включение/выключение микрофона.
     *
     * @param enabled включен ли микрофон
     */
    override fun toggleMicrophone(enabled: Boolean) {
        webRtcClient.toggleMicrophone(enabled)
    }
    
    /**
     * Включение/выключение камеры.
     *
     * @param enabled включена ли камера
     */
    override fun toggleCamera(enabled: Boolean) {
        webRtcClient.toggleCamera(enabled)
    }
    
    /**
     * Получение локального видеопотока.
     *
     * @return локальный видеопоток
     */
    override fun getLocalVideoTrack(): VideoTrack? {
        return webRtcClient.getLocalVideoTrack()
    }
    
    /**
     * Получение удаленного видеопотока.
     *
     * @return удаленный видеопоток
     */
    override fun getRemoteVideoTrack(): VideoTrack? {
        return webRtcClient.getRemoteVideoTrack()
    }
    
    /**
     * Добавление аннотации.
     *
     * @param annotation аннотация
     */
    override suspend fun addAnnotation(annotation: Annotation) {
        val annotations = _annotationsFlow.value.toMutableList()
        annotations.add(annotation)
        _annotationsFlow.value = annotations
        
        // Отправка аннотации через канал данных
        val annotationJson = """
            {
                "type": "annotation",
                "data": {
                    "id": "${annotation.id}",
                    "type": "${annotation.type}",
                    "points": ${annotation.points.map { "{\"x\":${it.x},\"y\":${it.y}}" }},
                    "color": ${annotation.color},
                    "timestamp": ${annotation.timestamp}
                }
            }
        """.trimIndent()
        
        webRtcClient.sendMessage(annotationJson)
    }
    
    /**
     * Удаление аннотации.
     *
     * @param annotationId ID аннотации
     */
    override suspend fun removeAnnotation(annotationId: String) {
        val annotations = _annotationsFlow.value.toMutableList()
        annotations.removeAll { it.id == annotationId }
        _annotationsFlow.value = annotations
        
        // Отправка команды удаления аннотации через канал данных
        val removeAnnotationJson = """
            {
                "type": "remove_annotation",
                "data": {
                    "id": "$annotationId"
                }
            }
        """.trimIndent()
        
        webRtcClient.sendMessage(removeAnnotationJson)
    }
    
    /**
     * Получение списка аннотаций.
     *
     * @return поток списка аннотаций
     */
    override fun getAnnotations(): Flow<List<Annotation>> {
        return _annotationsFlow.asStateFlow()
    }
    
    /**
     * Отправка сообщения в чат.
     *
     * @param text текст сообщения
     */
    override suspend fun sendMessage(text: String) {
        val userId = sessionManager.userId.first() ?: ""
        val userName = sessionManager.userName.first() ?: ""
        
        val message = ChatMessage(
            id = UUID.randomUUID().toString(),
            senderId = userId,
            senderName = userName,
            text = text,
            timestamp = System.currentTimeMillis()
        )
        
        val messages = _messagesFlow.value.toMutableList()
        messages.add(message)
        _messagesFlow.value = messages
        
        // Отправка сообщения через канал данных
        val messageJson = """
            {
                "type": "message",
                "data": {
                    "id": "${message.id}",
                    "senderId": "${message.senderId}",
                    "senderName": "${message.senderName}",
                    "text": "${message.text}",
                    "timestamp": ${message.timestamp}
                }
            }
        """.trimIndent()
        
        webRtcClient.sendMessage(messageJson)
        
        // Сохранение сообщения в Firestore
        currentCallId?.let { callId ->
            firestore.collection("calls")
                .document(callId)
                .collection("messages")
                .document(message.id)
                .set(message)
                .await()
        }
    }
    
    /**
     * Получение списка сообщений.
     *
     * @return поток списка сообщений
     */
    override fun getMessages(): Flow<List<ChatMessage>> {
        return _messagesFlow.asStateFlow()
    }
    
    /**
     * Отправка файла.
     *
     * @param filePath путь к файлу
     * @param fileName имя файла
     * @param fileType тип файла
     */
    override suspend fun sendFile(filePath: String, fileName: String, fileType: String) {
        val userId = sessionManager.userId.first() ?: ""
        val userName = sessionManager.userName.first() ?: ""
        
        // Загрузка файла на сервер NextCloud
        val token = "Bearer ${sessionManager.authToken.first() ?: ""}"
        val file = File(filePath)
        
        // TODO: Реализовать загрузку файла на сервер NextCloud
        // Это зависит от конкретного API NextCloud
        
        // Пример URL файла
        val fileUrl = "https://nextcloud.example.com/remote.php/dav/files/$userId/$fileName"
        
        val sharedFile = SharedFile(
            id = UUID.randomUUID().toString(),
            name = fileName,
            type = fileType,
            url = fileUrl,
            size = file.length(),
            senderId = userId,
            senderName = userName,
            timestamp = System.currentTimeMillis()
        )
        
        val files = _sharedFilesFlow.value.toMutableList()
        files.add(sharedFile)
        _sharedFilesFlow.value = files
        
        // Отправка информации о файле через канал данных
        val fileJson = """
            {
                "type": "file",
                "data": {
                    "id": "${sharedFile.id}",
                    "name": "${sharedFile.name}",
                    "type": "${sharedFile.type}",
                    "url": "${sharedFile.url}",
                    "size": ${sharedFile.size},
                    "senderId": "${sharedFile.senderId}",
                    "senderName": "${sharedFile.senderName}",
                    "timestamp": ${sharedFile.timestamp}
                }
            }
        """.trimIndent()
        
        webRtcClient.sendMessage(fileJson)
        
        // Сохранение информации о файле в Firestore
        currentCallId?.let { callId ->
            firestore.collection("calls")
                .document(callId)
                .collection("files")
                .document(sharedFile.id)
                .set(sharedFile)
                .await()
        }
    }
    
    /**
     * Получение списка файлов.
     *
     * @return поток списка файлов
     */
    override fun getSharedFiles(): Flow<List<SharedFile>> {
        return _sharedFilesFlow.asStateFlow()
    }
    
    /**
     * Логирование звонка.
     *
     * @param callId ID звонка
     * @param duration длительность звонка в секундах
     */
    override suspend fun logCall(callId: String, duration: Long) {
        try {
            val userId = sessionManager.userId.first() ?: ""
            val userName = sessionManager.userName.first() ?: ""
            
            val callLog = hashMapOf(
                "callId" to callId,
                "userId" to userId,
                "userName" to userName,
                "startTime" to callStartTime,
                "duration" to duration,
                "timestamp" to System.currentTimeMillis()
            )
            
            firestore.collection("call_logs")
                .document(callId)
                .set(callLog)
                .await()
            
            Timber.d("Звонок успешно залогирован: $callId, длительность: $duration сек.")
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при логировании звонка")
        }
    }
    
    /**
     * Освобождение ресурсов.
     */
    override fun release() {
        webRtcClient.release()
        signalingClient.release()
        
        currentCallId = null
        callStartTime = 0
        
        _annotationsFlow.value = emptyList()
        _messagesFlow.value = emptyList()
        _sharedFilesFlow.value = emptyList()
    }
} 