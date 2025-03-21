package com.ai_technologi.ar_application.presentation.ui

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import com.ai_technologi.ar_application.core.ui.ARAdaptiveUIProvider
import com.ai_technologi.ar_application.core.ui.ARWebView
import com.ai_technologi.ar_application.core.ui.LocalARAdaptiveUIConfig
import com.ai_technologi.ar_application.core.util.DeviceUtils
import timber.log.Timber

/**
 * Экран для осуществления звонков через Nextcloud Talk
 *
 * @param callUrl URL звонка Nextcloud Talk
 * @param onBackPressed колбэк при нажатии кнопки "Назад"
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NextcloudCallScreen(
    callUrl: String,
    onBackPressed: () -> Unit
) {
    ARAdaptiveUIProvider {
        val context = LocalContext.current
        val config = LocalARAdaptiveUIConfig.current?.config
        val isARDevice = config?.isARDevice ?: DeviceUtils.isRokidDevice(context)
        val snackbarHostState = remember { SnackbarHostState() }
        
        // Получаем настройки Nextcloud из SharedPreferences
        val prefs = remember { context.getSharedPreferences("nextcloud_prefs", Context.MODE_PRIVATE) }
        val serverUrl = remember { prefs.getString("server_url", "") ?: "" }
        val token = remember { prefs.getString("token", "") ?: "" }
        val username = remember { prefs.getString("username", "") ?: "" }
        
        var errorState by remember { mutableStateOf<String?>(null) }
        var isLoading by remember { mutableStateOf(true) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        
        // Обработка ошибок
        errorState?.let { error ->
            LaunchedEffect(error) {
                snackbarHostState.showSnackbar(error)
                errorState = null
            }
        }
        
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = { 
                        Text(
                            text = "Видеозвонок Nextcloud",
                            style = if (isARDevice) MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ) else MaterialTheme.typography.titleLarge
                        ) 
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    navigationIcon = {
                        IconButton(onClick = onBackPressed) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Назад",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                ARWebView(
                    url = callUrl,
                    modifier = Modifier.fillMaxSize(),
                    config = LocalARAdaptiveUIConfig.current?.config,
                    authToken = token,
                    authUsername = username,
                    onPageFinished = { url ->
                        Timber.d("Страница Nextcloud Call загружена: $url")
                        // Страница загружена, скрываем индикатор загрузки
                        isLoading = false
                    },
                    onError = { error ->
                        Timber.e("Ошибка загрузки Nextcloud Call: $error")
                        // При ошибке показываем сообщение
                        errorMessage = error
                        isLoading = false
                    },
                    autoJoinCall = true,
                    keepScreenOn = true
                )
            }
        }
    }
} 