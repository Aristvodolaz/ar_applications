package com.ai_technologi.ar_application.auth.presentation.util

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Анализатор изображений для сканирования QR-кодов.
 *
 * @param onQrCodeDetected колбэк, вызываемый при обнаружении QR-кода
 */
class QrCodeAnalyzer(
    private val onQrCodeDetected: (String) -> Unit
) : ImageAnalysis.Analyzer {
    
    private val isProcessing = AtomicBoolean(false)
    private val scanner = BarcodeScanning.getClient()
    
    /**
     * Анализирует изображение на наличие QR-кода.
     *
     * @param imageProxy изображение для анализа
     */
    override fun analyze(imageProxy: ImageProxy) {
        if (isProcessing.get()) {
            imageProxy.close()
            return
        }
        
        isProcessing.set(true)
        
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(
                mediaImage,
                imageProxy.imageInfo.rotationDegrees
            )
            
            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    if (barcodes.isNotEmpty()) {
                        for (barcode in barcodes) {
                            if (barcode.valueType == Barcode.TYPE_URL) {
                                barcode.url?.url?.let { url ->
                                    onQrCodeDetected(url)
                                    return@addOnSuccessListener
                                }
                            } else if (barcode.valueType == Barcode.TYPE_TEXT) {
                                barcode.rawValue?.let { text ->
                                    onQrCodeDetected(text)
                                    return@addOnSuccessListener
                                }
                            }
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Timber.e(e, "Ошибка при сканировании QR-кода")
                }
                .addOnCompleteListener {
                    isProcessing.set(false)
                    imageProxy.close()
                }
        } else {
            isProcessing.set(false)
            imageProxy.close()
        }
    }
} 