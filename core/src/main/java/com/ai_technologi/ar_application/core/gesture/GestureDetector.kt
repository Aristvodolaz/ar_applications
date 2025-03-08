package com.ai_technologi.ar_application.core.gesture

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.GestureDetector
import android.view.MotionEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs

/**
 * Класс для распознавания жестов в AR-очках.
 * Поддерживает как сенсорные жесты, так и жесты головой.
 *
 * @param context контекст приложения
 */
class ARGestureDetector(private val context: Context) {
    
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    
    private val _gestureFlow = MutableStateFlow<ARGesture?>(null)
    val gestureFlow: StateFlow<ARGesture?> = _gestureFlow.asStateFlow()
    
    private var lastRotationX = 0f
    private var lastRotationY = 0f
    private var lastRotationZ = 0f
    
    private val rotationThreshold = 0.15f // Порог для определения поворота головы
    
    // Обработчик сенсорных жестов
    private val touchGestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            _gestureFlow.value = ARGesture.TAP
            return true
        }
        
        override fun onDoubleTap(e: MotionEvent): Boolean {
            _gestureFlow.value = ARGesture.DOUBLE_TAP
            return true
        }
        
        override fun onLongPress(e: MotionEvent) {
            _gestureFlow.value = ARGesture.LONG_PRESS
        }
        
        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            if (e1 == null) return false
            
            val diffX = e2.x - e1.x
            val diffY = e2.y - e1.y
            
            if (abs(diffX) > abs(diffY)) {
                // Горизонтальный свайп
                if (diffX > 0) {
                    _gestureFlow.value = ARGesture.SWIPE_RIGHT
                } else {
                    _gestureFlow.value = ARGesture.SWIPE_LEFT
                }
            } else {
                // Вертикальный свайп
                if (diffY > 0) {
                    _gestureFlow.value = ARGesture.SWIPE_DOWN
                } else {
                    _gestureFlow.value = ARGesture.SWIPE_UP
                }
            }
            
            return true
        }
    })
    
    // Обработчик жестов головой
    private val sensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                val rotationMatrix = FloatArray(9)
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                
                val orientationValues = FloatArray(3)
                SensorManager.getOrientation(rotationMatrix, orientationValues)
                
                val rotX = orientationValues[1] // Наклон вперед/назад
                val rotY = orientationValues[2] // Наклон влево/вправо
                val rotZ = orientationValues[0] // Поворот головы
                
                // Определение жеста головой
                if (abs(rotX - lastRotationX) > rotationThreshold) {
                    if (rotX > lastRotationX) {
                        _gestureFlow.value = ARGesture.HEAD_DOWN
                    } else {
                        _gestureFlow.value = ARGesture.HEAD_UP
                    }
                } else if (abs(rotY - lastRotationY) > rotationThreshold) {
                    if (rotY > lastRotationY) {
                        _gestureFlow.value = ARGesture.HEAD_RIGHT
                    } else {
                        _gestureFlow.value = ARGesture.HEAD_LEFT
                    }
                } else if (abs(rotZ - lastRotationZ) > rotationThreshold) {
                    if (rotZ > lastRotationZ) {
                        _gestureFlow.value = ARGesture.HEAD_TILT_RIGHT
                    } else {
                        _gestureFlow.value = ARGesture.HEAD_TILT_LEFT
                    }
                }
                
                lastRotationX = rotX
                lastRotationY = rotY
                lastRotationZ = rotZ
            }
        }
        
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
            // Не используется
        }
    }
    
    /**
     * Обрабатывает сенсорное событие.
     *
     * @param event сенсорное событие
     * @return true, если событие обработано
     */
    fun onTouchEvent(event: MotionEvent): Boolean {
        return touchGestureDetector.onTouchEvent(event)
    }
    
    /**
     * Запускает распознавание жестов.
     */
    fun startDetection() {
        sensorManager.registerListener(
            sensorEventListener,
            rotationSensor,
            SensorManager.SENSOR_DELAY_GAME
        )
    }
    
    /**
     * Останавливает распознавание жестов.
     */
    fun stopDetection() {
        sensorManager.unregisterListener(sensorEventListener)
    }
    
    /**
     * Сбрасывает текущий жест.
     */
    fun resetGesture() {
        _gestureFlow.value = null
    }
}

/**
 * Перечисление поддерживаемых жестов.
 */
enum class ARGesture {
    // Сенсорные жесты
    TAP,                // Одиночное касание
    DOUBLE_TAP,         // Двойное касание
    LONG_PRESS,         // Долгое нажатие
    SWIPE_LEFT,         // Свайп влево
    SWIPE_RIGHT,        // Свайп вправо
    SWIPE_UP,           // Свайп вверх
    SWIPE_DOWN,         // Свайп вниз
    
    // Жесты головой
    HEAD_UP,            // Наклон головы вверх
    HEAD_DOWN,          // Наклон головы вниз
    HEAD_LEFT,          // Наклон головы влево
    HEAD_RIGHT,         // Наклон головы вправо
    HEAD_TILT_LEFT,     // Поворот головы влево
    HEAD_TILT_RIGHT     // Поворот головы вправо
} 