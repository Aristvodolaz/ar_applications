package com.ai_technologi.ar_application.videocall.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.ai_technologi.ar_application.core.mvi.MviViewModel
import com.ai_technologi.ar_application.core.network.ApiResult
import com.ai_technologi.ar_application.videocall.data.repository.VideoCallRepository
import com.ai_technologi.ar_application.videocall.domain.model.Annotation
import com.ai_technologi.ar_application.videocall.domain.model.AnnotationType
import com.ai_technologi.ar_application.videocall.domain.model.Point
import com.ai_technologi.ar_application.videocall.domain.model.VideoCallIntent
import com.ai_technologi.ar_application.videocall.domain.model.VideoCallState
import com.ai_technologi.ar_application.core.ui.CameraType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

/**
 * ViewModel для экрана видеозвонка.
 *
 * @param repository репозиторий для работы с видеозвонками
 */
@HiltViewModel
class VideoCallViewModel @Inject constructor(
    private val repository: VideoCallRepository
) : MviViewModel<VideoCallState, VideoCallIntent>(VideoCallState.Initial) {

    init {
        // Наблюдение за аннотациями
        repository.getAnnotations()
            .onEach { annotations ->
                val currentState = state.value
                if (currentState is VideoCallState.Active) {
                    updateState {
                        currentState.copy(annotations = annotations)
                    }
                }
            }
            .launchIn(viewModelScope)
        
        // Наблюдение за сообщениями
        repository.getMessages()
            .onEach { messages ->
                val currentState = state.value
                if (currentState is VideoCallState.Active) {
                    updateState {
                        currentState.copy(messages = messages)
                    }
                }
            }
            .launchIn(viewModelScope)
        
        // Наблюдение за файлами
        repository.getSharedFiles()
            .onEach { files ->
                val currentState = state.value
                if (currentState is VideoCallState.Active) {
                    updateState {
                        currentState.copy(sharedFiles = files)
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    /**
     * Обработка Intent.
     *
     * @param intent Intent, который нужно обработать
     */
    override suspend fun handleIntent(intent: VideoCallIntent) {
        when (intent) {
            is VideoCallIntent.InitiateCall -> handleInitiateCall(intent.userId)
            is VideoCallIntent.AnswerCall -> handleAnswerCall(intent.callId)
            is VideoCallIntent.EndCall -> handleEndCall()
            is VideoCallIntent.ToggleMicrophone -> handleToggleMicrophone(intent.enabled)
            is VideoCallIntent.ToggleCamera -> handleToggleCamera(intent.enabled)
            is VideoCallIntent.UpdateLocalVideoTrack -> handleUpdateLocalVideoTrack(intent.videoTrack)
            is VideoCallIntent.UpdateRemoteVideoTrack -> handleUpdateRemoteVideoTrack(intent.videoTrack)
            is VideoCallIntent.AddAnnotation -> handleAddAnnotation(intent.annotation)
            is VideoCallIntent.RemoveAnnotation -> handleRemoveAnnotation(intent.annotationId)
            is VideoCallIntent.SendMessage -> handleSendMessage(intent.text)
            is VideoCallIntent.SendFile -> handleSendFile(intent.filePath, intent.fileName, intent.fileType)
            is VideoCallIntent.UpdateCallState -> handleUpdateCallState()
            is VideoCallIntent.Reset -> handleReset()
        }
    }

    /**
     * Обработка Intent для инициализации звонка.
     *
     * @param userId ID пользователя, которому звоним
     */
    private suspend fun handleInitiateCall(userId: String) {
        updateState { VideoCallState.Loading }
        
        updateState { VideoCallState.Connecting(userId) }
        
        when (val result = repository.initiateCall(userId)) {
            is ApiResult.Success -> {
                val callId = result.data.callId
                
                // Инициализация WebRTC
                repository.initializeWebRtc(callId)
                
                // Получение информации о звонке
                when (val callInfoResult = repository.getCallInfo(callId)) {
                    is ApiResult.Success -> {
                        val callInfo = callInfoResult.data
                        
                        // Получение видеопотоков
                        val localVideoTrack = repository.getLocalVideoTrack()
                        val remoteVideoTrack = repository.getRemoteVideoTrack()
                        
                        updateState {
                            VideoCallState.Active(
                                callId = callId,
                                participants = callInfo.participants,
                                localVideoTrack = localVideoTrack,
                                remoteVideoTrack = remoteVideoTrack,
                                isMicEnabled = true,
                                isCameraEnabled = true
                            )
                        }
                    }
                    is ApiResult.Error -> {
                        Timber.e("Ошибка получения информации о звонке: ${callInfoResult.message}")
                        updateState { VideoCallState.Error(callInfoResult.message) }
                    }
                    is ApiResult.Loading -> {
                        // Ничего не делаем, уже в состоянии Loading
                    }
                }
            }
            is ApiResult.Error -> {
                Timber.e("Ошибка инициализации звонка: ${result.message}")
                updateState { VideoCallState.Error(result.message) }
            }
            is ApiResult.Loading -> {
                // Ничего не делаем, уже в состоянии Loading
            }
        }
    }

    /**
     * Обработка Intent для ответа на звонок.
     *
     * @param callId ID звонка
     */
    private suspend fun handleAnswerCall(callId: String) {
        updateState { VideoCallState.Loading }
        
        // Инициализация WebRTC
        repository.initializeWebRtc(callId)
        
        // Получение информации о звонке
        when (val callInfoResult = repository.getCallInfo(callId)) {
            is ApiResult.Success -> {
                val callInfo = callInfoResult.data
                
                // Получение видеопотоков
                val localVideoTrack = repository.getLocalVideoTrack()
                val remoteVideoTrack = repository.getRemoteVideoTrack()
                
                updateState {
                    VideoCallState.Active(
                        callId = callId,
                        participants = callInfo.participants,
                        localVideoTrack = localVideoTrack,
                        remoteVideoTrack = remoteVideoTrack,
                        isMicEnabled = true,
                        isCameraEnabled = true
                    )
                }
            }
            is ApiResult.Error -> {
                Timber.e("Ошибка получения информации о звонке: ${callInfoResult.message}")
                updateState { VideoCallState.Error(callInfoResult.message) }
            }
            is ApiResult.Loading -> {
                // Ничего не делаем, уже в состоянии Loading
            }
        }
    }

    /**
     * Обработка Intent для завершения звонка.
     */
    private suspend fun handleEndCall() {
        val currentState = state.value
        if (currentState !is VideoCallState.Active) {
            return
        }
        
        val callId = currentState.callId
        
        when (val result = repository.endCall(callId)) {
            is ApiResult.Success -> {
                val duration = result.data.duration ?: 0
                updateState { VideoCallState.Ended(callId, duration) }
            }
            is ApiResult.Error -> {
                Timber.e("Ошибка завершения звонка: ${result.message}")
                updateState { VideoCallState.Error(result.message) }
            }
            is ApiResult.Loading -> {
                // Ничего не делаем
            }
        }
    }

    /**
     * Обработка Intent для включения/выключения микрофона.
     *
     * @param enabled включен ли микрофон
     */
    private fun handleToggleMicrophone(enabled: Boolean) {
        val currentState = state.value
        if (currentState !is VideoCallState.Active) {
            return
        }
        
        repository.toggleMicrophone(enabled)
        
        updateState {
            (currentState as VideoCallState.Active).copy(isMicEnabled = enabled)
        }
    }

    /**
     * Обработка Intent для включения/выключения камеры.
     *
     * @param enabled включена ли камера
     */
    private fun handleToggleCamera(enabled: Boolean) {
        val currentState = state.value
        if (currentState !is VideoCallState.Active) {
            return
        }
        
        repository.toggleCamera(enabled)
        
        updateState {
            (currentState as VideoCallState.Active).copy(isCameraEnabled = enabled)
        }
    }

    /**
     * Обработка Intent для обновления локального видеопотока.
     *
     * @param videoTrack локальный видеопоток
     */
    private fun handleUpdateLocalVideoTrack(videoTrack: org.webrtc.VideoTrack) {
        val currentState = state.value
        if (currentState !is VideoCallState.Active) {
            return
        }
        
        updateState {
            (currentState as VideoCallState.Active).copy(localVideoTrack = videoTrack)
        }
    }

    /**
     * Обработка Intent для обновления удаленного видеопотока.
     *
     * @param videoTrack удаленный видеопоток
     */
    private fun handleUpdateRemoteVideoTrack(videoTrack: org.webrtc.VideoTrack) {
        val currentState = state.value
        if (currentState !is VideoCallState.Active) {
            return
        }
        
        updateState {
            (currentState as VideoCallState.Active).copy(remoteVideoTrack = videoTrack)
        }
    }

    /**
     * Обработка Intent для добавления аннотации.
     *
     * @param annotation аннотация
     */
    private suspend fun handleAddAnnotation(annotation: Annotation) {
        repository.addAnnotation(annotation)
    }

    /**
     * Обработка Intent для удаления аннотации.
     *
     * @param annotationId ID аннотации
     */
    private suspend fun handleRemoveAnnotation(annotationId: String) {
        repository.removeAnnotation(annotationId)
    }

    /**
     * Обработка Intent для отправки сообщения в чат.
     *
     * @param text текст сообщения
     */
    private suspend fun handleSendMessage(text: String) {
        repository.sendMessage(text)
    }

    /**
     * Обработка Intent для отправки файла.
     *
     * @param filePath путь к файлу
     * @param fileName имя файла
     * @param fileType тип файла
     */
    private suspend fun handleSendFile(filePath: String, fileName: String, fileType: String) {
        repository.sendFile(filePath, fileName, fileType)
    }

    /**
     * Обработка Intent для обновления состояния звонка.
     */
    private suspend fun handleUpdateCallState() {
        val currentState = state.value
        if (currentState !is VideoCallState.Active) {
            return
        }
        
        val callId = currentState.callId
        
        when (val result = repository.getCallInfo(callId)) {
            is ApiResult.Success -> {
                val callInfo = result.data
                
                // Получение видеопотоков
                val localVideoTrack = repository.getLocalVideoTrack()
                val remoteVideoTrack = repository.getRemoteVideoTrack()
                
                updateState {
                    (currentState as VideoCallState.Active).copy(
                        participants = callInfo.participants,
                        localVideoTrack = localVideoTrack,
                        remoteVideoTrack = remoteVideoTrack
                    )
                }
            }
            is ApiResult.Error -> {
                Timber.e("Ошибка обновления состояния звонка: ${result.message}")
            }
            is ApiResult.Loading -> {
                // Ничего не делаем
            }
        }
    }

    /**
     * Обработка Intent для сброса состояния.
     */
    private fun handleReset() {
        repository.release()
        updateState { VideoCallState.Initial }
    }

    /**
     * Создание аннотации.
     *
     * @param type тип аннотации
     * @param points список точек аннотации
     * @param color цвет аннотации
     * @return аннотация
     */
    fun createAnnotation(type: AnnotationType, points: List<Point>, color: Int): Annotation {
        return Annotation(
            id = UUID.randomUUID().toString(),
            type = type,
            points = points,
            color = color,
            timestamp = System.currentTimeMillis()
        )
    }

    /**
     * Освобождение ресурсов при уничтожении ViewModel.
     */
    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            repository.release()
        }
    }

    /**
     * Переключение камеры
     *
     * @param cameraType тип камеры для переключения
     */
    fun switchCamera(cameraType: CameraType) {
        viewModelScope.launch {
            try {
                // Сначала отключаем текущую камеру
                webRTCClient.disableVideo()
                
                // Небольшая задержка для корректного переключения
                delay(300)
                
                // Переключаем камеру в WebRTC клиенте
                webRTCClient.switchCamera(cameraType.selector)
                
                // Если камера была включена, включаем её снова
                if (_state.value.activeState?.isCameraEnabled == true) {
                    webRTCClient.enableVideo()
                }
                
                Timber.d("Камера переключена на ${cameraType.title}")
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при переключении камеры")
                
                // В случае ошибки показываем уведомление
                _state.update { currentState ->
                    currentState.copy(
                        snackbarMessage = "Ошибка при переключении камеры: ${e.localizedMessage}"
                    )
                }
            }
        }
    }
} 