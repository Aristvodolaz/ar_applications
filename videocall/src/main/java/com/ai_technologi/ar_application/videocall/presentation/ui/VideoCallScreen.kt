package com.ai_technologi.ar_application.videocall.presentation.ui

import android.Manifest
import android.view.MotionEvent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.ai_technologi.ar_application.core.ui.ARAdaptiveUIProvider
import com.ai_technologi.ar_application.core.ui.ARButton
import com.ai_technologi.ar_application.core.ui.ARHeading
import com.ai_technologi.ar_application.core.ui.ARIconButton
import com.ai_technologi.ar_application.core.ui.ARLoadingIndicator
import com.ai_technologi.ar_application.core.ui.LocalARAdaptiveUIConfig
import com.ai_technologi.ar_application.core.util.DeviceUtils
import com.ai_technologi.ar_application.videocall.domain.model.VideoCallIntent
import com.ai_technologi.ar_application.videocall.domain.model.VideoCallState
import com.ai_technologi.ar_application.videocall.presentation.gesture.rememberVideoCallGestureManager
import com.ai_technologi.ar_application.videocall.presentation.ui.components.AnnotationLayer
import com.ai_technologi.ar_application.videocall.presentation.ui.components.ChatPanel
import com.ai_technologi.ar_application.videocall.presentation.ui.components.FilesPanel
import com.ai_technologi.ar_application.videocall.presentation.ui.components.ParticipantsPanel
import com.ai_technologi.ar_application.videocall.presentation.viewmodel.VideoCallViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import org.webrtc.SurfaceViewRenderer

/**
 * Экран видеозвонка.
 *
 * @param userId ID пользователя, которому звоним (null, если отвечаем на звонок)
 * @param callId ID звонка (null, если инициируем звонок)
 * @param onCallEnded колбэк, вызываемый при завершении звонка
 * @param viewModel ViewModel для экрана видеозвонка
 */
@OptIn(ExperimentalPermissionsApi::class, ExperimentalComposeUiApi::class)
@Composable
fun VideoCallScreen(
    userId: String? = null,
    callId: String? = null,
    onCallEnded: () -> Unit,
    viewModel: VideoCallViewModel = hiltViewModel()
) {
    ARAdaptiveUIProvider {
        val state by viewModel.state.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current
        val config = LocalARAdaptiveUIConfig.current
        val isARDevice = config?.isARDevice ?: DeviceUtils.isRokidDevice(context)
        
        // Инициализация менеджера жестов
        val gestureManager = rememberVideoCallGestureManager(viewModel)
        
        // Запрос разрешений
        val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
        val microphonePermissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
        
        var hasCameraPermission by remember { mutableStateOf(false) }
        var hasMicrophonePermission by remember { mutableStateOf(false) }
        
        val permissionsLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            hasCameraPermission = permissions[Manifest.permission.CAMERA] ?: false
            hasMicrophonePermission = permissions[Manifest.permission.RECORD_AUDIO] ?: false
        }
        
        // Запрос разрешений при запуске экрана
        LaunchedEffect(key1 = true) {
            if (!cameraPermissionState.status.isGranted || !microphonePermissionState.status.isGranted) {
                permissionsLauncher.launch(
                    arrayOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO
                    )
                )
            } else {
                hasCameraPermission = true
                hasMicrophonePermission = true
            }
        }
        
        // Инициализация звонка при запуске экрана
        LaunchedEffect(key1 = userId, key2 = callId) {
            if (userId != null) {
                viewModel.processIntent(VideoCallIntent.InitiateCall(userId))
            } else if (callId != null) {
                viewModel.processIntent(VideoCallIntent.AnswerCall(callId))
            }
        }
        
        // Обработка ошибок
        LaunchedEffect(state) {
            if (state is VideoCallState.Error) {
                snackbarHostState.showSnackbar(
                    message = (state as VideoCallState.Error).message
                )
                onCallEnded()
            }
        }
        
        // Обработка завершения звонка
        LaunchedEffect(state) {
            if (state is VideoCallState.Ended) {
                onCallEnded()
            }
        }
        
        // Обновление состояния звонка каждые 5 секунд
        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    viewModel.processIntent(VideoCallIntent.UpdateCallState)
                }
            }
            
            lifecycleOwner.lifecycle.addObserver(observer)
            
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }
        
        // Обработка сенсорных событий для жестов
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInteropFilter { event ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN,
                        MotionEvent.ACTION_MOVE,
                        MotionEvent.ACTION_UP,
                        MotionEvent.ACTION_CANCEL -> {
                            gestureManager.gestureDetector.onTouchEvent(event)
                            true
                        }
                        else -> false
                    }
                }
        ) {
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    when (state) {
                        is VideoCallState.Initial -> {
                            // Начальное состояние
                            ARLoadingIndicator(
                                text = "Подготовка к звонку..."
                            )
                        }
                        
                        is VideoCallState.Loading -> {
                            // Состояние загрузки
                            ARLoadingIndicator()
                        }
                        
                        is VideoCallState.Connecting -> {
                            // Состояние подключения
                            val connectingState = state as VideoCallState.Connecting
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                            ) {
                                ARLoadingIndicator(
                                    text = "Подключение к ${connectingState.userId}..."
                                )
                            }
                        }
                        
                        is VideoCallState.Active -> {
                            // Активный звонок
                            val activeState = state as VideoCallState.Active
                            
                            // Отображение видеопотоков
                            Box(modifier = Modifier.fillMaxSize()) {
                                // Удаленный видеопоток (от эксперта)
                                activeState.remoteVideoTrack?.let { videoTrack ->
                                    AndroidView(
                                        factory = { context ->
                                            SurfaceViewRenderer(context).apply {
                                                setMirror(false)
                                                videoTrack.addSink(this)
                                            }
                                        },
                                        modifier = Modifier.fillMaxSize(),
                                        update = { renderer ->
                                            videoTrack.addSink(renderer)
                                        },
                                        onRelease = { renderer ->
                                            videoTrack.removeSink(renderer)
                                            renderer.release()
                                        }
                                    )
                                }
                                
                                // Локальный видеопоток (с камеры очков)
                                activeState.localVideoTrack?.let { videoTrack ->
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(16.dp)
                                            .size(
                                                width = if (isARDevice) 180.dp else 120.dp,
                                                height = if (isARDevice) 240.dp else 160.dp
                                            )
                                    ) {
                                        AndroidView(
                                            factory = { context ->
                                                SurfaceViewRenderer(context).apply {
                                                    setMirror(true)
                                                    videoTrack.addSink(this)
                                                }
                                            },
                                            modifier = Modifier.fillMaxSize(),
                                            update = { renderer ->
                                                videoTrack.addSink(renderer)
                                            },
                                            onRelease = { renderer ->
                                                videoTrack.removeSink(renderer)
                                                renderer.release()
                                            }
                                        )
                                    }
                                }
                                
                                // Слой для аннотаций
                                AnnotationLayer(
                                    annotations = activeState.annotations,
                                    onAnnotationAdded = { annotation ->
                                        viewModel.processIntent(VideoCallIntent.AddAnnotation(annotation))
                                    },
                                    onAnnotationRemoved = { annotationId ->
                                        viewModel.processIntent(VideoCallIntent.RemoveAnnotation(annotationId))
                                    },
                                    createAnnotation = { type, points, color ->
                                        viewModel.createAnnotation(type, points, color)
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                                
                                // Панель управления звонком
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .fillMaxWidth()
                                        .background(Color.Black.copy(alpha = 0.5f))
                                        .padding(
                                            vertical = if (isARDevice) 24.dp else 16.dp,
                                            horizontal = if (isARDevice) 32.dp else 16.dp
                                        )
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Кнопка микрофона
                                        ARIconButton(
                                            onClick = {
                                                viewModel.processIntent(
                                                    VideoCallIntent.ToggleMicrophone(!activeState.isMicEnabled)
                                                )
                                            },
                                            icon = if (activeState.isMicEnabled) Icons.Default.Mic else Icons.Default.MicOff,
                                            contentDescription = "Микрофон"
                                        )
                                        
                                        // Кнопка завершения звонка
                                        ARIconButton(
                                            onClick = {
                                                viewModel.processIntent(VideoCallIntent.EndCall)
                                            },
                                            icon = Icons.Default.CallEnd,
                                            contentDescription = "Завершить звонок",
                                            modifier = Modifier.size(if (isARDevice) 80.dp else 64.dp)
                                        )
                                        
                                        // Кнопка камеры
                                        ARIconButton(
                                            onClick = {
                                                viewModel.processIntent(
                                                    VideoCallIntent.ToggleCamera(!activeState.isCameraEnabled)
                                                )
                                            },
                                            icon = if (activeState.isCameraEnabled) Icons.Default.Videocam else Icons.Default.VideocamOff,
                                            contentDescription = "Камера"
                                        )
                                    }
                                }
                                
                                // Панель участников
                                ParticipantsPanel(
                                    participants = activeState.participants,
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(
                                            top = if (isARDevice) 24.dp else 16.dp,
                                            start = if (isARDevice) 24.dp else 16.dp
                                        )
                                )
                                
                                // Панель чата (только для обычных устройств или по запросу для AR)
                                if (!isARDevice) {
                                    ChatPanel(
                                        messages = activeState.messages,
                                        onSendMessage = { text ->
                                            viewModel.processIntent(VideoCallIntent.SendMessage(text))
                                        },
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(16.dp)
                                            .width(300.dp)
                                    )
                                }
                                
                                // Панель файлов (только для обычных устройств или по запросу для AR)
                                if (!isARDevice) {
                                    FilesPanel(
                                        files = activeState.sharedFiles,
                                        onSendFile = { filePath, fileName, fileType ->
                                            viewModel.processIntent(
                                                VideoCallIntent.SendFile(filePath, fileName, fileType)
                                            )
                                        },
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(16.dp)
                                            .width(300.dp)
                                    )
                                }
                                
                                // Инструкции по жестам для AR-устройств
                                if (isARDevice) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopCenter)
                                            .padding(top = 24.dp)
                                            .background(Color.Black.copy(alpha = 0.7f))
                                            .padding(8.dp)
                                    ) {
                                        Text(
                                            text = "Свайп влево: микрофон | Свайп вправо: камера | Двойное нажатие: завершить",
                                            style = config?.mediumTextStyle ?: MaterialTheme.typography.bodyMedium,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                        
                        is VideoCallState.Ended -> {
                            // Звонок завершен
                            val endedState = state as VideoCallState.Ended
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                            ) {
                                ARHeading(text = "Звонок завершен")
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Text(
                                    text = "Длительность: ${formatDuration(endedState.duration)}",
                                    style = config?.largeTextStyle ?: MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center
                                )
                                
                                Spacer(modifier = Modifier.height(32.dp))
                                
                                ARButton(
                                    onClick = onCallEnded,
                                    text = "Вернуться",
                                    modifier = Modifier.width(200.dp)
                                )
                            }
                        }
                        
                        is VideoCallState.Error -> {
                            // Ошибка
                            val errorState = state as VideoCallState.Error
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                            ) {
                                ARHeading(text = "Ошибка")
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Text(
                                    text = errorState.message,
                                    style = config?.largeTextStyle?.copy(color = Color.Red) 
                                        ?: MaterialTheme.typography.bodyLarge.copy(color = Color.Red),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 32.dp)
                                )
                                
                                Spacer(modifier = Modifier.height(32.dp))
                                
                                ARButton(
                                    onClick = onCallEnded,
                                    text = "Вернуться",
                                    modifier = Modifier.width(200.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Форматирование длительности звонка.
 *
 * @param seconds длительность в секундах
 * @return отформатированная строка
 */
private fun formatDuration(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, secs)
    } else {
        String.format("%02d:%02d", minutes, secs)
    }
} 