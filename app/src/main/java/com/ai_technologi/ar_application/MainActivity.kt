package com.ai_technologi.ar_application

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.ai_technologi.ar_application.core.ui.ARAdaptiveUIProvider
import com.ai_technologi.ar_application.presentation.navigation.AppNavigation
import com.ai_technologi.ar_application.presentation.navigation.Screen
import com.ai_technologi.ar_application.ui.theme.Ar_applicationTheme
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var navController: NavHostController
    
    // Регистрируем обработчик результата запроса разрешений
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissions.entries.forEach { entry ->
            Timber.d("Разрешение ${entry.key}: ${if (entry.value) "предоставлено" else "отклонено"}")
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        // Устанавливаем SplashScreen
        installSplashScreen()
        
        // Настраиваем полноэкранный режим для AR-очков
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        super.onCreate(savedInstanceState)
        
        // Проверяем и запрашиваем необходимые разрешения
        checkAndRequestPermissions()
        
        // Обработка системной кнопки "Назад"
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Получаем текущий маршрут
                val currentRoute = navController.currentDestination?.route ?: ""
                
                when {
                    // Если на экране звонка, возвращаемся к контактам
                    currentRoute.startsWith("nextcloud/call") -> {
                        navController.navigate(Screen.NextcloudContacts.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                        }
                    }
                    // Если на экране WebView, возвращаемся на главный экран
                    currentRoute.startsWith("webview") -> {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true
                            }
                        }
                    }
                    // Если на экране контактов Nextcloud или на главном экране, закрываем приложение
                    currentRoute == Screen.NextcloudContacts.route || currentRoute == Screen.Home.route -> {
                        finish()
                    }
                    // Иначе, возвращаемся на главный экран
                    else -> {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true
                            }
                        }
                    }
                }
            }
        })
        
        // Инициализация логирования
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        
        // Проверка и запрос необходимых разрешений для Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkAndRequestNotificationPermission()
        }
        
        // Запрос разрешений для Bluetooth (для AR-устройств)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            checkAndRequestBluetoothPermissions()
        }
        
        setContent {
            Ar_applicationTheme(
                darkTheme = isSystemInDarkTheme()
            ) {
                ARAdaptiveUIProvider() {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        navController = rememberNavController()
                        AppNavigation(
                            navController = navController,
                            startDestination = Screen.NextcloudContacts.route
                        )
                    }
                }
            }
        }
    }
    
    /**
     * Проверяет и запрашивает необходимые разрешения
     */
    private fun checkAndRequestPermissions() {
        val permissionsToRequest = mutableListOf<String>()
        
        // Добавляем разрешения для камеры и микрофона (нужны для видеозвонков)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.CAMERA)
        }
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.RECORD_AUDIO)
        }
        
        // Добавляем разрешения для Bluetooth
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH_CONNECT)
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH_ADMIN)
            }
        }
        
        // Запрашиваем разрешения, если необходимо
        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }
    
    /**
     * Проверяем, является ли устройство AR-устройством
     */
    private fun isARDevice(): Boolean {
        // Здесь можно добавить логику определения AR-устройства
        // Пример для Rokid Air:
        return Build.MODEL.contains("air", ignoreCase = true) ||
                Build.MANUFACTURER.contains("rokid", ignoreCase = true)
    }
    
    /**
     * Проверяем и запрашиваем разрешение на уведомления для Android 13+
     */
    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_CODE
                )
            }
        }
    }
    
    /**
     * Проверяем и запрашиваем разрешения для Bluetooth для Android 12+
     */
    private fun checkAndRequestBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ),
                    BLUETOOTH_PERMISSION_CODE
                )
            }
        }
    }
    
    companion object {
        private const val NOTIFICATION_PERMISSION_CODE = 101
        private const val BLUETOOTH_PERMISSION_CODE = 102
    }
}