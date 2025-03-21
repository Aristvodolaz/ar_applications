package com.ai_technologi.ar_application.videocall.data.webrtc

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.webrtc.AudioSource
import org.webrtc.AudioTrack
import org.webrtc.Camera2Enumerator
import org.webrtc.CameraVideoCapturer
import org.webrtc.DataChannel
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.RtpReceiver
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import org.webrtc.SurfaceTextureHelper
import org.webrtc.VideoSource
import org.webrtc.VideoTrack
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Класс для работы с WebRTC.
 *
 * @param context контекст приложения
 */
@Singleton
class WebRtcClient @Inject constructor(
    private val context: Context
) {
    companion object {
        private const val LOCAL_TRACK_ID = "local_track"
        private const val LOCAL_STREAM_ID = "local_stream"
    }

    private val scope = CoroutineScope(Dispatchers.IO)
    
    private val _localVideoTrackFlow = MutableStateFlow<VideoTrack?>(null)
    val localVideoTrackFlow: StateFlow<VideoTrack?> = _localVideoTrackFlow.asStateFlow()
    
    private val _remoteVideoTrackFlow = MutableStateFlow<VideoTrack?>(null)
    val remoteVideoTrackFlow: StateFlow<VideoTrack?> = _remoteVideoTrackFlow.asStateFlow()
    
    private val _signalingStateFlow = MutableStateFlow<SignalingState>(SignalingState.STABLE)
    val signalingStateFlow: StateFlow<SignalingState> = _signalingStateFlow.asStateFlow()
    
    private val _connectionStateFlow = MutableStateFlow<ConnectionState>(ConnectionState.NEW)
    val connectionStateFlow: StateFlow<ConnectionState> = _connectionStateFlow.asStateFlow()
    
    private val _dataChannelFlow = MutableStateFlow<DataChannel?>(null)
    val dataChannelFlow: StateFlow<DataChannel?> = _dataChannelFlow.asStateFlow()
    
    private val rootEglBase: EglBase = EglBase.create()
    private var peerConnectionFactory: PeerConnectionFactory? = null
    private var peerConnection: PeerConnection? = null
    private var localAudioSource: AudioSource? = null
    private var localVideoSource: VideoSource? = null
    private var localAudioTrack: AudioTrack? = null
    private var localVideoTrack: VideoTrack? = null
    private var videoCapturer: CameraVideoCapturer? = null
    private var surfaceTextureHelper: SurfaceTextureHelper? = null
    
    private var isMicrophoneEnabled = true
    private var isCameraEnabled = true
    
    /**
     * Инициализация WebRTC.
     */
    fun initialize() {
        scope.launch {
            initializePeerConnectionFactory()
            createPeerConnection()
            startLocalStream()
        }
    }
    
    /**
     * Инициализация фабрики соединений.
     */
    private fun initializePeerConnectionFactory() {
        val options = PeerConnectionFactory.InitializationOptions.builder(context)
            .setEnableInternalTracer(true)
            .createInitializationOptions()
        
        PeerConnectionFactory.initialize(options)
        
        val videoEncoderFactory = DefaultVideoEncoderFactory(rootEglBase.eglBaseContext, true, true)
        val videoDecoderFactory = DefaultVideoDecoderFactory(rootEglBase.eglBaseContext)
        
        peerConnectionFactory = PeerConnectionFactory.builder()
            .setVideoEncoderFactory(videoEncoderFactory)
            .setVideoDecoderFactory(videoDecoderFactory)
            .setOptions(PeerConnectionFactory.Options())
            .createPeerConnectionFactory()
    }
    
    /**
     * Создание соединения.
     */
    private fun createPeerConnection() {
        val iceServers = listOf(
            PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer(),
            PeerConnection.IceServer.builder("stun:stun1.l.google.com:19302").createIceServer()
        )
        
        val rtcConfig = PeerConnection.RTCConfiguration(iceServers)
        rtcConfig.sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
        
        peerConnection = peerConnectionFactory?.createPeerConnection(rtcConfig, object : PeerConnection.Observer {
            override fun onSignalingChange(signalingState: PeerConnection.SignalingState?) {
                signalingState?.let {
                    _signalingStateFlow.value = when (it) {
                        PeerConnection.SignalingState.STABLE -> SignalingState.STABLE
                        PeerConnection.SignalingState.HAVE_LOCAL_OFFER -> SignalingState.HAVE_LOCAL_OFFER
                        PeerConnection.SignalingState.HAVE_LOCAL_PRANSWER -> SignalingState.HAVE_LOCAL_PRANSWER
                        PeerConnection.SignalingState.HAVE_REMOTE_OFFER -> SignalingState.HAVE_REMOTE_OFFER
                        PeerConnection.SignalingState.HAVE_REMOTE_PRANSWER -> SignalingState.HAVE_REMOTE_PRANSWER
                        PeerConnection.SignalingState.CLOSED -> SignalingState.CLOSED
                    }
                }
            }

            override fun onIceConnectionChange(iceConnectionState: PeerConnection.IceConnectionState?) {
                Timber.d("onIceConnectionChange: $iceConnectionState")
            }

            override fun onIceConnectionReceivingChange(receiving: Boolean) {
                Timber.d("onIceConnectionReceivingChange: $receiving")
            }

            override fun onIceGatheringChange(iceGatheringState: PeerConnection.IceGatheringState?) {
                Timber.d("onIceGatheringChange: $iceGatheringState")
            }

            override fun onIceCandidate(iceCandidate: IceCandidate?) {
                Timber.d("onIceCandidate: $iceCandidate")
                // Отправка ICE-кандидата на сервер сигнализации
            }

            override fun onIceCandidatesRemoved(iceCandidates: Array<out IceCandidate>?) {
                Timber.d("onIceCandidatesRemoved: ${iceCandidates?.size}")
            }

            override fun onAddStream(mediaStream: MediaStream?) {
                Timber.d("onAddStream: ${mediaStream?.videoTracks?.size}")
                mediaStream?.videoTracks?.firstOrNull()?.let { videoTrack ->
                    _remoteVideoTrackFlow.value = videoTrack
                }
            }

            override fun onRemoveStream(mediaStream: MediaStream?) {
                Timber.d("onRemoveStream")
                _remoteVideoTrackFlow.value = null
            }

            override fun onDataChannel(dataChannel: DataChannel?) {
                Timber.d("onDataChannel: ${dataChannel?.label()}")
                _dataChannelFlow.value = dataChannel
            }

            override fun onRenegotiationNeeded() {
                Timber.d("onRenegotiationNeeded")
                // Создание нового предложения
            }

            override fun onAddTrack(rtpReceiver: RtpReceiver?, mediaStreams: Array<out MediaStream>?) {
                Timber.d("onAddTrack: ${mediaStreams?.size}")
                rtpReceiver?.track()?.let { track ->
                    if (track.kind() == "video") {
                        _remoteVideoTrackFlow.value = track as VideoTrack
                    }
                }
            }
        })
    }
    
    /**
     * Запуск локального потока.
     */
    private fun startLocalStream() {
        // Создание аудиопотока
        localAudioSource = peerConnectionFactory?.createAudioSource(MediaConstraints())
        localAudioTrack = peerConnectionFactory?.createAudioTrack("audio_track", localAudioSource)
        
        // Создание видеопотока
        videoCapturer = createCameraCapturer()
        
        if (videoCapturer != null) {
            surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", rootEglBase.eglBaseContext)
            localVideoSource = peerConnectionFactory?.createVideoSource(videoCapturer?.isScreencast ?: false)
            videoCapturer?.initialize(surfaceTextureHelper, context, localVideoSource?.capturerObserver)
            
            // Запуск захвата видео
            videoCapturer?.startCapture(1280, 720, 30)
            
            localVideoTrack = peerConnectionFactory?.createVideoTrack(LOCAL_TRACK_ID, localVideoSource)
            localVideoTrack?.setEnabled(isCameraEnabled)
            _localVideoTrackFlow.value = localVideoTrack
            
            // Добавление треков в соединение
            val mediaStream = peerConnectionFactory?.createLocalMediaStream(LOCAL_STREAM_ID)
            mediaStream?.addTrack(localVideoTrack)
            mediaStream?.addTrack(localAudioTrack)
            
            peerConnection?.addStream(mediaStream)
        }
    }
    
    /**
     * Создание захватчика камеры.
     *
     * @return захватчик камеры
     */
    private fun createCameraCapturer(): CameraVideoCapturer? {
        val cameraEnumerator = Camera2Enumerator(context)
        
        // Получение ID камеры AR-очков Rokid Max Pro
        val cameraId = getRokidCameraId()
        
        return if (cameraId != null && cameraEnumerator.isCameraFrontFacing(cameraId)) {
            cameraEnumerator.createCapturer(cameraId, null)
        } else {
            // Если не удалось найти камеру AR-очков, используем фронтальную камеру
            val deviceNames = cameraEnumerator.deviceNames
            
            // Сначала ищем фронтальную камеру
            for (deviceName in deviceNames) {
                if (cameraEnumerator.isCameraFrontFacing(deviceName)) {
                    return cameraEnumerator.createCapturer(deviceName, null)
                }
            }
            
            // Если фронтальной камеры нет, используем любую доступную
            for (deviceName in deviceNames) {
                return cameraEnumerator.createCapturer(deviceName, null)
            }
            
            null
        }
    }
    
    /**
     * Получение ID камеры AR-очков Rokid Max Pro.
     *
     * @return ID камеры AR-очков
     */
    private fun getRokidCameraId(): String? {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraIds = cameraManager.cameraIdList
        
        for (cameraId in cameraIds) {
            val characteristics = cameraManager.getCameraCharacteristics(cameraId)
            val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
            
            // Проверяем, что это фронтальная камера
            if (facing == CameraMetadata.LENS_FACING_FRONT) {
                // Дополнительные проверки для определения камеры AR-очков
                // Можно проверить разрешение, угол обзора и другие параметры
                return cameraId
            }
        }
        
        return null
    }
    
    /**
     * Создание предложения.
     *
     * @param callback колбэк с результатом создания предложения
     */
    fun createOffer(callback: (SessionDescription?) -> Unit) {
        peerConnection?.createOffer(object : SdpObserver {
            override fun onCreateSuccess(sessionDescription: SessionDescription?) {
                peerConnection?.setLocalDescription(object : SdpObserver {
                    override fun onCreateSuccess(p0: SessionDescription?) {}
                    override fun onSetSuccess() {
                        callback(sessionDescription)
                    }
                    override fun onCreateFailure(p0: String?) {}
                    override fun onSetFailure(p0: String?) {}
                }, sessionDescription)
            }
            override fun onSetSuccess() {}
            override fun onCreateFailure(p0: String?) {}
            override fun onSetFailure(p0: String?) {}
        }, MediaConstraints())
    }
    
    /**
     * Создание ответа.
     *
     * @param callback колбэк с результатом создания ответа
     */
    fun createAnswer(callback: (SessionDescription?) -> Unit) {
        peerConnection?.createAnswer(object : SdpObserver {
            override fun onCreateSuccess(sessionDescription: SessionDescription?) {
                peerConnection?.setLocalDescription(object : SdpObserver {
                    override fun onCreateSuccess(p0: SessionDescription?) {}
                    override fun onSetSuccess() {
                        callback(sessionDescription)
                    }
                    override fun onCreateFailure(p0: String?) {}
                    override fun onSetFailure(p0: String?) {}
                }, sessionDescription)
            }
            override fun onSetSuccess() {}
            override fun onCreateFailure(p0: String?) {}
            override fun onSetFailure(p0: String?) {}
        }, MediaConstraints())
    }
    
    /**
     * Установка удаленного описания сессии.
     *
     * @param sessionDescription описание сессии
     * @param callback колбэк с результатом установки
     */
    fun setRemoteDescription(sessionDescription: SessionDescription, callback: (Boolean) -> Unit) {
        peerConnection?.setRemoteDescription(object : SdpObserver {
            override fun onCreateSuccess(p0: SessionDescription?) {}
            override fun onSetSuccess() {
                callback(true)
            }
            override fun onCreateFailure(p0: String?) {}
            override fun onSetFailure(p0: String?) {
                callback(false)
            }
        }, sessionDescription)
    }
    
    /**
     * Добавление ICE-кандидата.
     *
     * @param iceCandidate ICE-кандидат
     */
    fun addIceCandidate(iceCandidate: IceCandidate) {
        peerConnection?.addIceCandidate(iceCandidate)
    }
    
    /**
     * Создание канала данных.
     *
     * @param label метка канала
     * @return канал данных
     */
    fun createDataChannel(label: String): DataChannel? {
        val dataChannelInit = DataChannel.Init()
        dataChannelInit.ordered = true
        
        val dataChannel = peerConnection?.createDataChannel(label, dataChannelInit)
        _dataChannelFlow.value = dataChannel
        
        return dataChannel
    }
    
    /**
     * Отправка сообщения через канал данных.
     *
     * @param message сообщение
     * @return успешность отправки
     */
    fun sendMessage(message: String): Boolean {
        val dataChannel = _dataChannelFlow.value ?: return false
        
        if (dataChannel.state() != DataChannel.State.OPEN) {
            return false
        }
        
        val buffer = DataChannel.Buffer(
            java.nio.ByteBuffer.wrap(message.toByteArray()),
            false
        )
        
        return dataChannel.send(buffer)
    }
    
    /**
     * Включение/выключение микрофона.
     *
     * @param enabled включен ли микрофон
     */
    fun toggleMicrophone(enabled: Boolean) {
        isMicrophoneEnabled = enabled
        localAudioTrack?.setEnabled(enabled)
    }
    
    /**
     * Включение/выключение камеры.
     *
     * @param enabled включена ли камера
     */
    fun toggleCamera(enabled: Boolean) {
        isCameraEnabled = enabled
        localVideoTrack?.setEnabled(enabled)
    }
    
    /**
     * Получение локального видеопотока.
     *
     * @return локальный видеопоток
     */
    fun getLocalVideoTrack(): VideoTrack? {
        return localVideoTrack
    }
    
    /**
     * Получение удаленного видеопотока.
     *
     * @return удаленный видеопоток
     */
    fun getRemoteVideoTrack(): VideoTrack? {
        return _remoteVideoTrackFlow.value
    }
    
    /**
     * Освобождение ресурсов.
     */
    fun release() {
        videoCapturer?.stopCapture()
        videoCapturer?.dispose()
        surfaceTextureHelper?.dispose()
        peerConnection?.close()
        peerConnectionFactory?.dispose()
        
        videoCapturer = null
        surfaceTextureHelper = null
        localVideoSource = null
        localAudioSource = null
        localVideoTrack = null
        localAudioTrack = null
        peerConnection = null
        peerConnectionFactory = null
        
        _localVideoTrackFlow.value = null
        _remoteVideoTrackFlow.value = null
        _dataChannelFlow.value = null
    }
    
    /**
     * Переключение камеры
     *
     * @param cameraId идентификатор камеры для переключения
     */
    fun switchCamera(cameraId: Int) {
        try {
            videoCapturer?.let { capturer ->
                if (capturer is CameraVideoCapturer) {
                    // Если передан специальный идентификатор для внешней камеры
                    if (cameraId == -1) {
                        // Получаем список доступных камер
                        val cameraEnumerator = Camera2Enumerator(context)
                        val deviceNames = cameraEnumerator.deviceNames
                        
                        // Ищем первую внешнюю камеру
                        val externalCamera = deviceNames.firstOrNull { deviceName ->
                            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
                            val characteristics = cameraManager.getCameraCharacteristics(deviceName)
                            val lensFacing = characteristics.get(CameraCharacteristics.LENS_FACING)
                            
                            // Если камера не фронтальная и не основная, считаем её внешней
                            lensFacing != CameraMetadata.LENS_FACING_FRONT && 
                            lensFacing != CameraMetadata.LENS_FACING_BACK
                        }
                        
                        if (externalCamera != null) {
                            capturer.switchCamera(null, externalCamera)
                            Timber.d("Переключено на внешнюю камеру: $externalCamera")
                        } else {
                            Timber.e("Внешняя камера не найдена")
                        }
                    } else {
                        // Переключаемся на камеру по идентификатору (фронтальная или основная)
                        capturer.switchCamera(null, cameraId)
                        Timber.d("Переключено на камеру с ID: $cameraId")
                    }
                }
            } ?: run {
                Timber.e("Не удалось переключить камеру: videoCapturer не инициализирован")
            }
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при переключении камеры")
            throw e
        }
    }
}

/**
 * Состояние сигнализации.
 */
enum class SignalingState {
    STABLE, HAVE_LOCAL_OFFER, HAVE_LOCAL_PRANSWER, HAVE_REMOTE_OFFER, HAVE_REMOTE_PRANSWER, CLOSED
}

/**
 * Состояние соединения.
 */
enum class ConnectionState {
    NEW, CONNECTING, CONNECTED, DISCONNECTED, FAILED, CLOSED
} 