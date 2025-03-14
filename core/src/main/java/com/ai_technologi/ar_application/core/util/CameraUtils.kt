package com.ai_technologi.ar_application.core.util

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.util.Log
import androidx.camera.core.CameraSelector
import com.ai_technologi.ar_application.core.ui.CameraType

/**
 * Утилиты для работы с камерами устройства
 */
object CameraUtils {
    
    /**
     * Получение списка доступных камер на устройстве
     *
     * @param context контекст приложения
     * @return список доступных типов камер
     */
    fun getAvailableCameras(context: Context): List<CameraType> {
        val availableCameras = mutableListOf<CameraType>()
        
        try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraIdList = cameraManager.cameraIdList
            
            // Проверяем наличие фронтальной камеры
            if (hasCameraWithLensFacing(cameraManager, CameraSelector.LENS_FACING_FRONT)) {
                availableCameras.add(CameraType.FRONT)
            }
            
            // Проверяем наличие основной камеры
            if (hasCameraWithLensFacing(cameraManager, CameraSelector.LENS_FACING_BACK)) {
                availableCameras.add(CameraType.BACK)
            }
            
            // Проверяем наличие внешних камер
            val externalCameras = cameraIdList.filter { cameraId ->
                val characteristics = cameraManager.getCameraCharacteristics(cameraId)
                val lensFacing = characteristics.get(CameraCharacteristics.LENS_FACING)
                val isExternal = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_APERTURES)?.isNotEmpty() == true
                
                // Если камера не фронтальная и не основная, и имеет информацию о диафрагме, считаем её внешней
                lensFacing != CameraSelector.LENS_FACING_FRONT && 
                lensFacing != CameraSelector.LENS_FACING_BACK && 
                isExternal
            }
            
            if (externalCameras.isNotEmpty()) {
                availableCameras.add(CameraType.EXTERNAL)
            }
            
            Log.d("Найдено камер:", availableCameras.size.toString())
            
        } catch (e: Exception) {
            Log.e(e.localizedMessage, "Ошибка при получении списка камер")
            
            // В случае ошибки возвращаем стандартный набор камер
            return listOf(CameraType.FRONT, CameraType.BACK)
        }
        
        // Если камер не найдено, возвращаем стандартный набор
        if (availableCameras.isEmpty()) {
            return listOf(CameraType.FRONT, CameraType.BACK)
        }
        
        return availableCameras
    }
    
    /**
     * Проверка наличия камеры с указанным направлением объектива
     *
     * @param cameraManager менеджер камер
     * @param lensFacing направление объектива (фронтальная, основная)
     * @return true, если камера с указанным направлением доступна
     */
    private fun hasCameraWithLensFacing(cameraManager: CameraManager, lensFacing: Int): Boolean {
        try {
            val cameraIdList = cameraManager.cameraIdList
            
            for (cameraId in cameraIdList) {
                val characteristics = cameraManager.getCameraCharacteristics(cameraId)
                val cameraLensFacing = characteristics.get(CameraCharacteristics.LENS_FACING)
                
                if (cameraLensFacing == lensFacing) {
                    return true
                }
            }
        } catch (e: Exception) {
            Log.e(e.localizedMessage , "Ошибка при проверке наличия камеры с направлением $lensFacing")
        }
        
        return false
    }
    
    /**
     * Получение CameraSelector для указанного типа камеры
     *
     * @param cameraType тип камеры
     * @return CameraSelector для указанного типа камеры
     */
    fun getCameraSelectorForType(cameraType: CameraType): CameraSelector {
        return when (cameraType) {
            CameraType.FRONT -> CameraSelector.DEFAULT_FRONT_CAMERA
            CameraType.BACK -> CameraSelector.DEFAULT_BACK_CAMERA
            CameraType.EXTERNAL -> {
                // Для внешних камер используем специальный селектор
                CameraSelector.Builder()
                    .requireLensFacing(cameraType.selector)
                    .build()
            }
        }
    }
} 