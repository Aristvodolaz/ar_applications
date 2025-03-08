package com.ai_technologi.ar_application.videocall.presentation.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import com.ai_technologi.ar_application.core.ui.LocalARAdaptiveUIConfig
import com.ai_technologi.ar_application.core.util.DeviceUtils
import com.ai_technologi.ar_application.videocall.domain.model.Annotation
import com.ai_technologi.ar_application.videocall.domain.model.AnnotationType
import com.ai_technologi.ar_application.videocall.domain.model.Point

/**
 * Слой для отображения и создания аннотаций.
 * Адаптирован для работы как на обычных устройствах, так и на AR-очках.
 *
 * @param annotations список аннотаций
 * @param onAnnotationAdded колбэк, вызываемый при добавлении аннотации
 * @param onAnnotationRemoved колбэк, вызываемый при удалении аннотации
 * @param createAnnotation функция для создания аннотации
 * @param modifier модификатор
 */
@Composable
fun AnnotationLayer(
    annotations: List<Annotation>,
    onAnnotationAdded: (Annotation) -> Unit,
    onAnnotationRemoved: (String) -> Unit,
    createAnnotation: (AnnotationType, List<Point>, Int) -> Annotation,
    modifier: Modifier = Modifier
) {
    var currentPoints by remember { mutableStateOf<List<Point>>(emptyList()) }
    var isDrawing by remember { mutableStateOf(false) }
    val currentAnnotationType by remember { mutableStateOf(AnnotationType.FREEHAND) }
    val currentColor by remember { mutableStateOf(Color.Red.toArgb()) }
    
    val context = LocalContext.current
    val config = LocalARAdaptiveUIConfig.current
    val isARDevice = config?.isARDevice ?: DeviceUtils.isRokidDevice(context)
    
    // Увеличенная толщина линий для AR-очков
    val strokeWidth = if (isARDevice) 8f else 5f
    
    Box(modifier = modifier) {
        // Отображение существующих аннотаций
        Canvas(modifier = Modifier.fillMaxSize()) {
            annotations.forEach { annotation ->
                when (annotation.type) {
                    AnnotationType.FREEHAND -> {
                        if (annotation.points.size > 1) {
                            val path = Path()
                            val firstPoint = annotation.points.first()
                            path.moveTo(firstPoint.x * size.width, firstPoint.y * size.height)
                            
                            annotation.points.drop(1).forEach { point ->
                                path.lineTo(point.x * size.width, point.y * size.height)
                            }
                            
                            drawPath(
                                path = path,
                                color = Color(annotation.color),
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }
                    }
                    AnnotationType.ARROW -> {
                        if (annotation.points.size >= 2) {
                            val start = annotation.points.first()
                            val end = annotation.points.last()
                            
                            // Рисуем линию
                            drawLine(
                                color = Color(annotation.color),
                                start = Offset(start.x * size.width, start.y * size.height),
                                end = Offset(end.x * size.width, end.y * size.height),
                                strokeWidth = strokeWidth
                            )
                            
                            // Рисуем наконечник стрелки
                            val angle = Math.atan2(
                                (end.y - start.y).toDouble(),
                                (end.x - start.x).toDouble()
                            )
                            val arrowLength = if (isARDevice) 45f else 30f
                            val arrowAngle = Math.PI / 6 // 30 градусов
                            
                            val arrowPoint1 = Offset(
                                (end.x * size.width - arrowLength * Math.cos(angle - arrowAngle)).toFloat(),
                                (end.y * size.height - arrowLength * Math.sin(angle - arrowAngle)).toFloat()
                            )
                            
                            val arrowPoint2 = Offset(
                                (end.x * size.width - arrowLength * Math.cos(angle + arrowAngle)).toFloat(),
                                (end.y * size.height - arrowLength * Math.sin(angle + arrowAngle)).toFloat()
                            )
                            
                            drawLine(
                                color = Color(annotation.color),
                                start = Offset(end.x * size.width, end.y * size.height),
                                end = arrowPoint1,
                                strokeWidth = strokeWidth
                            )
                            
                            drawLine(
                                color = Color(annotation.color),
                                start = Offset(end.x * size.width, end.y * size.height),
                                end = arrowPoint2,
                                strokeWidth = strokeWidth
                            )
                        }
                    }
                    AnnotationType.CIRCLE -> {
                        if (annotation.points.size >= 2) {
                            val center = annotation.points.first()
                            val edge = annotation.points.last()
                            
                            val radius = kotlin.math.sqrt(
                                (edge.x - center.x) * (edge.x - center.x) +
                                (edge.y - center.y) * (edge.y - center.y)
                            ) * kotlin.math.min(size.width, size.height)
                            
                            drawCircle(
                                color = Color(annotation.color),
                                center = Offset(center.x * size.width, center.y * size.height),
                                radius = radius,
                                style = Stroke(width = strokeWidth)
                            )
                        }
                    }
                    AnnotationType.RECTANGLE -> {
                        if (annotation.points.size >= 2) {
                            val start = annotation.points.first()
                            val end = annotation.points.last()
                            
                            drawRect(
                                color = Color(annotation.color),
                                topLeft = Offset(
                                    kotlin.math.min(start.x, end.x) * size.width,
                                    kotlin.math.min(start.y, end.y) * size.height
                                ),
                                size = androidx.compose.ui.geometry.Size(
                                    kotlin.math.abs(end.x - start.x) * size.width,
                                    kotlin.math.abs(end.y - start.y) * size.height
                                ),
                                style = Stroke(width = strokeWidth)
                            )
                        }
                    }
                }
            }
            
            // Отображение текущей аннотации
            if (isDrawing && currentPoints.isNotEmpty()) {
                when (currentAnnotationType) {
                    AnnotationType.FREEHAND -> {
                        if (currentPoints.size > 1) {
                            val path = Path()
                            val firstPoint = currentPoints.first()
                            path.moveTo(firstPoint.x * size.width, firstPoint.y * size.height)
                            
                            currentPoints.drop(1).forEach { point ->
                                path.lineTo(point.x * size.width, point.y * size.height)
                            }
                            
                            drawPath(
                                path = path,
                                color = Color(currentColor),
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }
                    }
                    AnnotationType.ARROW -> {
                        if (currentPoints.size >= 2) {
                            val start = currentPoints.first()
                            val end = currentPoints.last()
                            
                            // Рисуем линию
                            drawLine(
                                color = Color(currentColor),
                                start = Offset(start.x * size.width, start.y * size.height),
                                end = Offset(end.x * size.width, end.y * size.height),
                                strokeWidth = strokeWidth
                            )
                            
                            // Рисуем наконечник стрелки
                            val angle = Math.atan2(
                                (end.y - start.y).toDouble(),
                                (end.x - start.x).toDouble()
                            )
                            val arrowLength = if (isARDevice) 45f else 30f
                            val arrowAngle = Math.PI / 6 // 30 градусов
                            
                            val arrowPoint1 = Offset(
                                (end.x * size.width - arrowLength * Math.cos(angle - arrowAngle)).toFloat(),
                                (end.y * size.height - arrowLength * Math.sin(angle - arrowAngle)).toFloat()
                            )
                            
                            val arrowPoint2 = Offset(
                                (end.x * size.width - arrowLength * Math.cos(angle + arrowAngle)).toFloat(),
                                (end.y * size.height - arrowLength * Math.sin(angle + arrowAngle)).toFloat()
                            )
                            
                            drawLine(
                                color = Color(currentColor),
                                start = Offset(end.x * size.width, end.y * size.height),
                                end = arrowPoint1,
                                strokeWidth = strokeWidth
                            )
                            
                            drawLine(
                                color = Color(currentColor),
                                start = Offset(end.x * size.width, end.y * size.height),
                                end = arrowPoint2,
                                strokeWidth = strokeWidth
                            )
                        }
                    }
                    AnnotationType.CIRCLE -> {
                        if (currentPoints.size >= 2) {
                            val center = currentPoints.first()
                            val edge = currentPoints.last()
                            
                            val radius = kotlin.math.sqrt(
                                (edge.x - center.x) * (edge.x - center.x) +
                                (edge.y - center.y) * (edge.y - center.y)
                            ) * kotlin.math.min(size.width, size.height)
                            
                            drawCircle(
                                color = Color(currentColor),
                                center = Offset(center.x * size.width, center.y * size.height),
                                radius = radius,
                                style = Stroke(width = strokeWidth)
                            )
                        }
                    }
                    AnnotationType.RECTANGLE -> {
                        if (currentPoints.size >= 2) {
                            val start = currentPoints.first()
                            val end = currentPoints.last()
                            
                            drawRect(
                                color = Color(currentColor),
                                topLeft = Offset(
                                    kotlin.math.min(start.x, end.x) * size.width,
                                    kotlin.math.min(start.y, end.y) * size.height
                                ),
                                size = androidx.compose.ui.geometry.Size(
                                    kotlin.math.abs(end.x - start.x) * size.width,
                                    kotlin.math.abs(end.y - start.y) * size.height
                                ),
                                style = Stroke(width = strokeWidth)
                            )
                        }
                    }
                }
            }
        }
        
        // Обработка жестов для создания аннотаций
        // Используем нормализованные координаты (0.0-1.0) для независимости от размера экрана
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            isDrawing = true
                            val normalizedX = offset.x / size.width
                            val normalizedY = offset.y / size.height
                            currentPoints = listOf(Point(normalizedX, normalizedY))
                        },
                        onDrag = { change, _ ->
                            val normalizedX = change.position.x / size.width
                            val normalizedY = change.position.y / size.height
                            
                            // Ограничиваем координаты в пределах 0.0-1.0
                            val clampedX = normalizedX.coerceIn(0f, 1f)
                            val clampedY = normalizedY.coerceIn(0f, 1f)
                            
                            val newPoint = Point(clampedX, clampedY)
                            currentPoints = currentPoints + newPoint
                        },
                        onDragEnd = {
                            if (currentPoints.size > 1) {
                                val annotation = createAnnotation(
                                    currentAnnotationType,
                                    currentPoints,
                                    currentColor
                                )
                                onAnnotationAdded(annotation)
                            }
                            isDrawing = false
                            currentPoints = emptyList()
                        },
                        onDragCancel = {
                            isDrawing = false
                            currentPoints = emptyList()
                        }
                    )
                }
        )
    }
} 