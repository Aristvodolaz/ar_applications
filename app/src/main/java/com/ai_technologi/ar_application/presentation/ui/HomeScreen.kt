package com.ai_technologi.ar_application.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ai_technologi.ar_application.core.ui.ARAdaptiveUIProvider
import com.ai_technologi.ar_application.core.ui.ARButton
import com.ai_technologi.ar_application.core.ui.ARHeading
import com.ai_technologi.ar_application.core.ui.LocalARAdaptiveUIConfig
import com.ai_technologi.ar_application.core.util.DeviceUtils

/**
 * Главный экран приложения
 *
 * @param onOpenWebView колбэк для открытия WebView
 * @param onOpenNextcloudContacts колбэк для открытия списка контактов Nextcloud
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenWebView: (String) -> Unit,
    onOpenNextcloudContacts: () -> Unit
) {
    ARAdaptiveUIProvider {
        val context = LocalContext.current
        val config = LocalARAdaptiveUIConfig.current?.config
        val isARDevice = config?.isARDevice ?: DeviceUtils.isRokidDevice(context)
        
        // URL для тестового WebView
        val defaultUrl = "https://ar.sitebill.site/fpin/29"
        var url by remember { mutableStateOf(defaultUrl) }
        
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "AR Application",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = config?.largeFontSize ?: MaterialTheme.typography.titleLarge.fontSize
                            )
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = if (isARDevice) Color.Black else MaterialTheme.colorScheme.surface,
                        titleContentColor = if (isARDevice) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                ARHeading(text = "WebView и Nextcloud")
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Секция WebView
                ARHeading(text = "WebView", Modifier.align(Alignment.Start))
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("URL") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Go
                    ),
                    keyboardActions = KeyboardActions(
                        onGo = {
                            if (url.isNotEmpty()) {
                                onOpenWebView(url)
                            }
                        }
                    ),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                ARButton(
                    onClick = { onOpenWebView(url) },
                    text = "Открыть WebView",
                    modifier = Modifier.width(250.dp),
                    icon = Icons.Default.Settings
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = { onOpenWebView(defaultUrl) },
                    modifier = Modifier.width(250.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Открыть тестовую ссылку")
                }
                
                Spacer(modifier = Modifier.height(48.dp))
                
                // Секция Nextcloud
                ARHeading(text = "Nextcloud", Modifier.align(Alignment.Start))
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Осуществление звонков через Nextcloud",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                ARButton(
                    onClick = onOpenNextcloudContacts,
                    text = "Открыть контакты Nextcloud",
                    modifier = Modifier.width(300.dp),
                    icon = Icons.Default.Call
                )
            }
        }
    }
} 