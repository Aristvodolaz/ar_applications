package com.ai_technologi.ar_application.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ai_technologi.ar_application.core.ui.ARAdaptiveUIProvider
import com.ai_technologi.ar_application.core.ui.LocalARAdaptiveUIConfig
import com.ai_technologi.ar_application.core.util.DeviceUtils
import timber.log.Timber
import android.content.Context

import androidx.compose.material.icons.filled.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.activity.compose.BackHandler

/**
 * Модель данных настроек Nextcloud
 */
data class NextcloudSettings(
    val serverUrl: String,
    val token: String,
    val username: String
)

/**
 * Предустановленные настройки Nextcloud
 */
private val DEFAULT_NEXTCLOUD_SETTINGS = NextcloudSettings(
    serverUrl = "https://ar.sitebill.site",
    token = "pIErFQXvOZ8XGMEvnMMwUuRVQGtPISfyXupObEOCBhXGoZ72uXyspGH89RpGu1uvPnJBVxn5",
    username = "mister"
)

/**
 * Экран для выбора контактов Nextcloud
 *
 * @param onContactSelected колбэк при выборе контакта
 * @param onBackPressed колбэк при нажатии кнопки "Назад"
 * @param viewModel ViewModel для экрана контактов
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NextcloudContactsScreen(
    onContactSelected: (NextcloudContact) -> Unit,
    onBackPressed: () -> Unit,
    viewModel: NextcloudContactsViewModel = hiltViewModel()
) {
    // Обработчик нажатия системной кнопки назад
    BackHandler {
        onBackPressed()
    }
    
    ARAdaptiveUIProvider {
        val context = LocalContext.current
        val config = LocalARAdaptiveUIConfig.current?.config
        val isARDevice = config?.isARDevice ?: DeviceUtils.isRokidDevice(context)
        val snackbarHostState = remember { SnackbarHostState() }
        
        // Загружаем настройки из SharedPreferences или используем предустановленные
        val prefs = remember { context.getSharedPreferences("nextcloud_prefs", Context.MODE_PRIVATE) }
        val savedServerUrl = remember { prefs.getString("server_url", DEFAULT_NEXTCLOUD_SETTINGS.serverUrl) ?: DEFAULT_NEXTCLOUD_SETTINGS.serverUrl }
        val savedToken = remember { prefs.getString("token", DEFAULT_NEXTCLOUD_SETTINGS.token) ?: DEFAULT_NEXTCLOUD_SETTINGS.token }
        val savedUsername = remember { prefs.getString("username", DEFAULT_NEXTCLOUD_SETTINGS.username) ?: DEFAULT_NEXTCLOUD_SETTINGS.username }
        
        // Сохраняем предустановленные настройки, если это первый запуск
        LaunchedEffect(Unit) {
            if (prefs.getString("server_url", null) == null) {
                prefs.edit()
                    .putString("server_url", DEFAULT_NEXTCLOUD_SETTINGS.serverUrl)
                    .putString("token", DEFAULT_NEXTCLOUD_SETTINGS.token)
                    .putString("username", DEFAULT_NEXTCLOUD_SETTINGS.username)
                    .apply()
                Timber.d("Сохранены предустановленные настройки Nextcloud")
            }
        }
        
        var showSettings by remember { mutableStateOf(false) } // Изменено на false, так как токен уже установлен
        var serverUrl by remember { mutableStateOf(savedServerUrl) }
        var token by remember { mutableStateOf(savedToken) }
        var username by remember { mutableStateOf(savedUsername) }
        var passwordVisible by remember { mutableStateOf(false) }
        
        // Получаем состояние UI
        val uiState by viewModel.uiState.collectAsState()
        
        // Группы для фильтрации
        val groups = remember {
            listOf(
                "Все контакты",
                "Избранные", 
                "Недавние",
                "Группы"
            )
        }
        
        // Отображаем ошибку, если она есть
        LaunchedEffect(uiState.errorMessage) {
            uiState.errorMessage?.let { error ->
                snackbarHostState.showSnackbar(
                    message = error,
                    duration = SnackbarDuration.Short
                )
            }
        }
        
        // URL для тестового звонка
        val testCallUrl = "$serverUrl/index.php/call/9up76ljh?token=$token"
        
        // Обработка сохранения настроек
        fun saveSettings() {
            prefs.edit()
                .putString("server_url", serverUrl)
                .putString("token", token)
                .putString("username", username)
                .apply()
            
            Timber.d("Настройки Nextcloud сохранены")
            
            // Сохраняем настройки во ViewModel
            viewModel.saveSettings(serverUrl, token, username)
            
            // Если настройки сконфигурированы, возвращаемся к списку контактов
            if (viewModel.uiState.value.areSettingsConfigured) {
                showSettings = false
            }
        }
        
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = { 
                        Text(
                            text = if (showSettings) "Настройки Nextcloud" else "Контакты",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        ) 
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    navigationIcon = {
                        // Пустой блок, так как это главный экран
                    },
                    actions = {
                        IconButton(onClick = { showSettings = !showSettings }) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Настройки",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                )
            },
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {
                if (showSettings) {
                    // Настройки
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Настройте подключение к вашему серверу Nextcloud",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = config?.largeFontSize ?: MaterialTheme.typography.bodyLarge.fontSize
                            ),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        OutlinedTextField(
                            value = serverUrl,
                            onValueChange = { serverUrl = it },
                            label = { Text("URL сервера Nextcloud") },
                            placeholder = { Text("Например: https://cloud.example.com") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("Имя пользователя") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = token,
                            onValueChange = { token = it },
                            label = { Text("Токен аутентификации") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { saveSettings() }),
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Search else Icons.Default.Search,
                                        contentDescription = if (passwordVisible) "Скрыть токен" else "Показать токен"
                                    )
                                }
                            }
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Button(
                            onClick = { saveSettings() },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Сохранить",
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Сохранить настройки")
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Divider()
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Text(
                            text = "Как получить токен Nextcloud:",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        LazyColumn {
                            item {
                                Text(
                                    text = "1. Войдите в свою учетную запись Nextcloud через браузер",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                Text(
                                    text = "2. Перейдите в \"Настройки\" -> \"Безопасность\"",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                Text(
                                    text = "3. В разделе \"Токены приложений\" создайте новый токен",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                Text(
                                    text = "4. Скопируйте созданный токен и вставьте его в поле выше",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                        }
                    }
                } else {
                    // Список контактов
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Поле поиска
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = { 
                                viewModel.setSearchQuery(it)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Поиск контактов") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Поиск",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Search
                            ),
                            keyboardActions = KeyboardActions(
                                onSearch = { /* обрабатывается в onValueChange */ }
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            colors = androidx.compose.material3.TextFieldDefaults.outlinedTextFieldColors(
                                cursorColor = MaterialTheme.colorScheme.primary,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Горизонтальный список групп
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            items(groups) { group ->
                                Card(
                                    modifier = Modifier
                                        .padding(end = 8.dp)
                                        .clickable { 
                                            viewModel.setSelectedGroup(group)
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (uiState.selectedGroup == group) 
                                            MaterialTheme.colorScheme.primary 
                                        else 
                                            MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                ) {
                                    Text(
                                        text = group,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                        color = if (uiState.selectedGroup == group) 
                                            MaterialTheme.colorScheme.onPrimary 
                                        else 
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Кнопка для тестового звонка
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { 
                                    // Создаем тестовую комнату
                                    val roomName = "Тестовый звонок"
                                    viewModel.createCallRoom(
                                        userId = "admin", // Используем admin как пользователя по умолчанию для тестового звонка
                                        customName = roomName
                                    ) { callUrl ->
                                        if (callUrl != null) {
                                            // Если комната создана успешно, переходим к звонку
                                            val callUrlWithToken = if (token.isNotEmpty() && !callUrl.contains("token=")) {
                                                "$callUrl?token=$token"
                                            } else {
                                                callUrl
                                            }
                                            
                                            onContactSelected(
                                                NextcloudContact(
                                                    id = "test_room",
                                                    name = "Тестовый звонок",
                                                    callUrl = callUrlWithToken,
                                                    isFavorite = true,
                                                    isOnline = true
                                                )
                                            )
                                        }
                                    }
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = 2.dp
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = "Позвонить",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = "Тестовый звонок",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontSize = config?.largeFontSize ?: MaterialTheme.typography.bodyLarge.fontSize,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Заголовок списка
                        Text(
                            text = uiState.selectedGroup,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = config?.mediumFontSize ?: MaterialTheme.typography.titleMedium.fontSize,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                        )
                        
                        // Индикатор ошибки
                        if (uiState.errorMessage != null && !uiState.isLoading) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Ошибка",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    val errorText = uiState.errorMessage ?: "Неизвестная ошибка"
                                    Text(
                                        text = errorText,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                        
                        // Отображаем время последнего обновления
                        if (uiState.lastUpdated > 0) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Последнее обновление: ${formatDateTime(uiState.lastUpdated)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                )
                                
                                IconButton(
                                    onClick = { viewModel.refreshContacts() },
                                    enabled = !uiState.isLoading && !uiState.refreshing
                                ) {
                                    if (uiState.refreshing) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = "Обновить",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                        
                        // Показываем индикатор загрузки
                        if (uiState.isLoading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                        
                        // Показываем пустое состояние
                        if (uiState.filteredContacts.isEmpty() && !uiState.isLoading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                        modifier = Modifier.size(48.dp)
                                    )
                                    
                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    Text(
                                        text = "Контакты не найдены",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                        textAlign = TextAlign.Center
                                    )
                                    
                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    Button(
                                        onClick = { viewModel.refreshContacts() }
                                    ) {
                                        Text("Обновить")
                                    }
                                }
                            }
                        } else if (!uiState.isLoading) {
                            // Список контактов
                            LazyColumn {
                                items(uiState.filteredContacts) { contactResponse ->
                                    // Создаем модель UI контакта
                                    val contact = viewModel.mapToUiContact(contactResponse)
                                    
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { 
                                                // Создаем комнату для звонка с пользователем
                                                val roomName = "Звонок с ${contact.name}"
                                                viewModel.createCallRoom(
                                                    userId = contactResponse.id, 
                                                    customName = roomName
                                                ) { callUrl ->
                                                    if (callUrl != null) {
                                                        // Если комната создана успешно, переходим к звонку
                                                        val callUrlWithToken = if (token.isNotEmpty() && !callUrl.contains("token=")) {
                                                            "$callUrl?token=$token"
                                                        } else {
                                                            callUrl
                                                        }
                                                        
                                                        onContactSelected(
                                                            contact.copy(callUrl = callUrlWithToken)
                                                        )
                                                    }
                                                }
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 12.dp, horizontal = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Аватар с индикатором статуса
                                            Box {
                                                Box(
                                                    modifier = Modifier
                                                        .size(48.dp)
                                                        .clip(CircleShape)
                                                        .background(
                                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = contact.name.take(1).uppercase(),
                                                        style = MaterialTheme.typography.titleLarge.copy(
                                                            fontWeight = FontWeight.Bold
                                                        ),
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                                // Индикатор статуса онлайн
                                                if (contact.isOnline) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(14.dp)
                                                            .clip(CircleShape)
                                                            .background(Color.Green)
                                                            .align(Alignment.BottomEnd)
                                                            .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                                                    )
                                                }
                                            }
                                            
                                            Spacer(modifier = Modifier.width(16.dp))
                                            
                                            // Имя контакта
                                            Column {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = contact.name,
                                                        style = MaterialTheme.typography.bodyLarge.copy(
                                                            fontWeight = FontWeight.Medium
                                                        ),
                                                        color = MaterialTheme.colorScheme.onBackground
                                                    )
                                                    
                                                    if (contact.isFavorite) {
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Icon(
                                                            imageVector = Icons.Default.Star,
                                                            contentDescription = "Избранное",
                                                            tint = MaterialTheme.colorScheme.primary,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                }
                                                
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(8.dp)
                                                            .clip(CircleShape)
                                                            .background(if (contact.isOnline) Color.Green else Color.Gray)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = if (contact.isOnline) "Онлайн" else "Не в сети",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                                    )
                                                }
                                            }
                                            
                                            Spacer(modifier = Modifier.weight(1f))
                                            
                                            // Кнопки действий
                                            Row {
                                                // Видеозвонок
                                                IconButton(
                                                    onClick = { 
                                                        // Создаем комнату для звонка с пользователем
                                                        val roomName = "Звонок с ${contact.name}"
                                                        viewModel.createCallRoom(
                                                            userId = contactResponse.id, 
                                                            customName = roomName
                                                        ) { callUrl ->
                                                            if (callUrl != null) {
                                                                // Если комната создана успешно, переходим к звонку
                                                                val callUrlWithToken = if (token.isNotEmpty() && !callUrl.contains("token=")) {
                                                                    "$callUrl?token=$token"
                                                                } else {
                                                                    callUrl
                                                                }
                                                                
                                                                onContactSelected(
                                                                    contact.copy(callUrl = callUrlWithToken)
                                                                )
                                                            }
                                                        }
                                                    },
                                                    modifier = Modifier
                                                        .background(
                                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                                            shape = CircleShape
                                                        )
                                                        .padding(4.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Call,
                                                        contentDescription = "Видеозвонок",
                                                        tint = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                                
                                                Spacer(modifier = Modifier.width(8.dp))
                                                
                                                // Обычный звонок или чат
                                                IconButton(
                                                    onClick = { 
                                                        // Создаем комнату для звонка с пользователем
                                                        val roomName = "Чат с ${contact.name}"
                                                        viewModel.createCallRoom(
                                                            userId = contactResponse.id, 
                                                            customName = roomName
                                                        ) { callUrl ->
                                                            if (callUrl != null) {
                                                                // Если комната создана успешно, переходим к звонку
                                                                val callUrlWithToken = if (token.isNotEmpty() && !callUrl.contains("token=")) {
                                                                    "$callUrl?token=$token"
                                                                } else {
                                                                    callUrl
                                                                }
                                                                
                                                                onContactSelected(
                                                                    contact.copy(callUrl = callUrlWithToken)
                                                                )
                                                            }
                                                        }
                                                    }
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Call,
                                                        contentDescription = "Позвонить",
                                                        tint = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }
                                        }
                                        
                                        Divider(
                                            modifier = Modifier.padding(start = 64.dp),
                                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Форматирует временную метку в читаемый формат
 */
private fun formatDateTime(timestamp: Long): String {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    return dateFormat.format(Date(timestamp))
}