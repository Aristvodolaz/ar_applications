package com.ai_technologi.ar_application.videocall.presentation.gesture

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.ai_technologi.ar_application.core.gesture.ARGesture
import com.ai_technologi.ar_application.core.gesture.ARGestureDetector
import com.ai_technologi.ar_application.videocall.domain.model.AnnotationType
import com.ai_technologi.ar_application.videocall.domain.model.Point
import com.ai_technologi.ar_application.videocall.domain.model.VideoCallIntent
import com.ai_technologi.ar_application.videocall.domain.model.VideoCallState
import com.ai_technologi.ar_application.videocall.presentation.viewmodel.VideoCallViewModel
import kotlinx.coroutines.delay

/**
 * Менеджер жестов для видеозвонка.
 * Обрабатывает жесты пользователя и преобразует их в действия.
 *
 * @param context контекст приложения
 * @param viewModel ViewModel видеозвонка
 */
class VideoCallGestureManager(
    context: Context,
    private val viewModel: VideoCallViewModel
) {
    private val gestureDetector = ARGestureDetector(context)
    
    // Текущий тип аннотации
    private var currentAnnotationType = AnnotationType.FREEHAND
    
    // Текущий цвет аннотации
    private var currentAnnotationColor = android.graphics.Color.RED
    
    /**
     * Запускает обработку жестов.
     */
    fun startGestureDetection() {
        gestureDetector.startDetection()
    }
    
    /**
     * Останавливает обработку жестов.
     */
    fun stopGestureDetection() {
        gestureDetector.stopDetection()
    }
    
    /**
     * Обрабатывает жест.
     *
     * @param gesture жест
     * @param currentState текущее состояние видеозвонка
     */
    fun handleGesture(gesture: ARGesture?, currentState: VideoCallState) {
        if (gesture == null || currentState !is VideoCallState.Active) return
        
        when (gesture) {
            // Жесты для управления звонком
            ARGesture.DOUBLE_TAP -> {
                // Завершение звонка
                viewModel.processIntent(VideoCallIntent.EndCall)
            }
            
            ARGesture.SWIPE_LEFT -> {
                // Включение/выключение микрофона
                viewModel.processIntent(
                    VideoCallIntent.ToggleMicrophone(!currentState.isMicEnabled)
                )
            }
            
            ARGesture.SWIPE_RIGHT -> {
                // Включение/выключение камеры
                viewModel.processIntent(
                    VideoCallIntent.ToggleCamera(!currentState.isCameraEnabled)
                )
            }
            
            // Жесты для управления аннотациями
            ARGesture.SWIPE_UP -> {
                // Смена типа аннотации
                currentAnnotationType = when (currentAnnotationType) {
                    AnnotationType.FREEHAND -> AnnotationType.ARROW
                    AnnotationType.ARROW -> AnnotationType.CIRCLE
                    AnnotationType.CIRCLE -> AnnotationType.RECTANGLE
                    AnnotationType.RECTANGLE -> AnnotationType.FREEHAND
                }
            }
            
            ARGesture.SWIPE_DOWN -> {
                // Смена цвета аннотации
                currentAnnotationColor = when (currentAnnotationColor) {
                    android.graphics.Color.RED -> android.graphics.Color.GREEN
                    android.graphics.Color.GREEN -> android.graphics.Color.BLUE
                    android.graphics.Color.BLUE -> android.graphics.Color.YELLOW
                    android.graphics.Color.YELLOW -> android.graphics.Color.RED
                    else -> android.graphics.Color.RED
                }
            }
            
            ARGesture.LONG_PRESS -> {
                // Удаление последней аннотации
                val annotations = currentState.annotations
                if (annotations.isNotEmpty()) {
                    viewModel.processIntent(
                        VideoCallIntent.RemoveAnnotation(annotations.last().id)
                    )
                }
            }
            
            // Жесты головой
            ARGesture.HEAD_UP, ARGesture.HEAD_DOWN, ARGesture.HEAD_LEFT, ARGesture.HEAD_RIGHT -> {
                // Создание аннотации в центре экрана
                val centerX = 0.5f
                val centerY = 0.5f
                
                val endX = when (gesture) {
                    ARGesture.HEAD_LEFT -> centerX - 0.2f
                    ARGesture.HEAD_RIGHT -> centerX + 0.2f
                    else -> centerX
                }
                
                val endY = when (gesture) {
                    ARGesture.HEAD_UP -> centerY - 0.2f
                    ARGesture.HEAD_DOWN -> centerY + 0.2f
                    else -> centerY
                }
                
                val points = listOf(
                    Point(centerX, centerY),
                    Point(endX, endY)
                )
                
                val annotation = viewModel.createAnnotation(
                    AnnotationType.ARROW,
                    points,
                    currentAnnotationColor
                )
                
                viewModel.processIntent(VideoCallIntent.AddAnnotation(annotation))
            }
            
            else -> {
                // Другие жесты не обрабатываются
            }
        }
        
        // Сброс жеста после обработки
        gestureDetector.resetGesture()
    }
}

/**
 * Composable-функция для использования менеджера жестов в видеозвонке.
 *
 * @param viewModel ViewModel видеозвонка
 */
@Composable
fun rememberVideoCallGestureManager(viewModel: VideoCallViewModel): VideoCallGestureManager {
    val context = LocalContext.current
    val manager = remember { VideoCallGestureManager(context, viewModel) }
    val state by viewModel.state.collectAsState()
    val gesture by manager.gestureDetector.gestureFlow.collectAsState()
    
    // Запуск обработки жестов при создании компонента
    LaunchedEffect(Unit) {
        manager.startGestureDetection()
    }
    
    // Обработка жестов
    LaunchedEffect(gesture) {
        if (gesture != null) {
            manager.handleGesture(gesture, state)
            delay(500) // Задержка для предотвращения случайных двойных жестов
        }
    }
    
    // Остановка обработки жестов при уничтожении компонента
    DisposableEffect(Unit) {
        onDispose {
            manager.stopGestureDetection()
        }
    }
    
    return manager
} 