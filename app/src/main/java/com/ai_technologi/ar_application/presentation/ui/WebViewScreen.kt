package com.ai_technologi.ar_application.presentation.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import com.ai_technologi.ar_application.core.ui.ARAdaptiveUIProvider
import com.ai_technologi.ar_application.core.ui.ARWebView
import com.ai_technologi.ar_application.core.ui.LocalARAdaptiveUIConfig
import com.ai_technologi.ar_application.core.util.DeviceUtils
import androidx.compose.ui.platform.LocalContext
import timber.log.Timber

/**
 * Экран для отображения веб-страницы
 *
 * @param url URL для загрузки
 * @param title заголовок экрана
 * @param onBackPressed колбэк при нажатии кнопки "Назад"
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebViewScreen(
    url: String,
    title: String = "Веб-страница",
    onBackPressed: () -> Unit
) {
    ARAdaptiveUIProvider {
        val context = LocalContext.current
        val config = LocalARAdaptiveUIConfig.current?.config
        val isARDevice = config?.isARDevice ?: DeviceUtils.isRokidDevice(context)
        val snackbarHostState = remember { SnackbarHostState() }
        
        // Состояние для заголовка страницы
        var pageTitle by remember { mutableStateOf(title) }
        var errorState by remember { mutableStateOf<String?>(null) }
        
        // Обработка ошибок
        errorState?.let { error ->
            LaunchedEffect(error) {
                snackbarHostState.showSnackbar(error)
                errorState = null
            }
        }
        
        Scaffold(
            topBar = {
                // Отображаем верхнюю панель только если заголовок не пустой
                if (pageTitle.isNotEmpty()) {
                    TopAppBar(
                        title = {
                            Text(
                                text = pageTitle,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontSize = config?.largeFontSize ?: MaterialTheme.typography.titleLarge.fontSize
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = onBackPressed) {
                                Icon(
                                    imageVector = Icons.Default.Home,
                                    contentDescription = "На главную",
                                    tint = if (isARDevice) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = if (isARDevice) Color.Black else MaterialTheme.colorScheme.surface,
                            titleContentColor = if (isARDevice) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                ARWebView(
                    url = url,
                    modifier = Modifier.fillMaxSize(),
                    config = LocalARAdaptiveUIConfig.current?.config,
                    onPageFinished = { loadedUrl ->
                        Timber.d("Веб-страница загружена: $loadedUrl")
                    },
                    onError = { error ->
                        Timber.e("Ошибка загрузки веб-страницы: $error")
                        errorState = error
                    }
                )
            }
        }
    }
} 