package com.ai_technologi.ar_application.presentation.ui

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ai_technologi.ar_application.core.data.NextcloudRepository
import com.ai_technologi.ar_application.core.network.ApiResult
import com.ai_technologi.ar_application.core.network.models.NextcloudContactResponse
import com.ai_technologi.ar_application.di.NextcloudPrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.random.Random

/**
 * Модель данных для состояния UI экрана контактов
 */
data class NextcloudContactsUiState(
    val contacts: List<NextcloudContactResponse> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val selectedGroup: String = "Все контакты",
    val showSettings: Boolean = false,
    val serverUrl: String = "",
    val token: String = "",
    val username: String = "",
    val lastUpdated: Long = 0,
    val areSettingsConfigured: Boolean = true,
    val refreshing: Boolean = false
) {
    // Получаем отфильтрованный список контактов
    val filteredContacts: List<NextcloudContactResponse>
        get() = contacts
}

/**
 * Модель данных контакта Nextcloud для UI
 */
data class NextcloudContact(
    val id: String,
    val name: String,
    val avatarUrl: String? = null,
    val callUrl: String,
    val isFavorite: Boolean = false,
    val isOnline: Boolean = true
)

/**
 * ViewModel для экрана контактов Nextcloud
 */
@HiltViewModel
class NextcloudContactsViewModel @Inject constructor(
    private val repository: NextcloudRepository,
    @NextcloudPrefs private val sharedPreferences: SharedPreferences
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(NextcloudContactsUiState())
    val uiState: StateFlow<NextcloudContactsUiState> = _uiState
    
    private var contactsRefreshJob: Job? = null
    private var statusUpdateJob: Job? = null
    
    // Константы для настроек по умолчанию
    private val DEFAULT_SERVER_URL = "https://ar.sitebill.site"
    private val DEFAULT_TOKEN = "pIErFQXvOZ8XGMEvnMMwUuRVQGtPISfyXupObEOCBhXGoZ72uXyspGH89RpGu1uvPnJBVxn5"
    private val DEFAULT_USERNAME = "mister"
    
    // Время последнего обновления контактов
    private var lastContactsRefreshTime = 0L
    
    // Минимальный интервал обновления контактов (мс)
    private val MIN_REFRESH_INTERVAL = 30_000L // 30 секунд
    
    // Дата формат для отображения времени последнего обновления
    private val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    
    init {
        // Инициализируем начальное состояние
        _uiState.update { it.copy(areSettingsConfigured = true) }
        
        // Загружаем настройки
        loadSettings()
        
        // Загружаем контакты сразу при запуске
        Timber.d("Инициализация ViewModel, запуск загрузки контактов...")
        refreshContacts(forceRefresh = true)
        
        // Запускаем таймер автообновления
        startContactsRefreshTimer()
        
        // Запускаем периодическое обновление статусов
        startStatusUpdateTimer()
    }
    
    /**
     * Загрузка настроек из SharedPreferences
     */
    private fun loadSettings() {
        val serverUrl = sharedPreferences.getString("nextcloud_server_url", DEFAULT_SERVER_URL) ?: DEFAULT_SERVER_URL
        val token = sharedPreferences.getString("nextcloud_token", DEFAULT_TOKEN) ?: DEFAULT_TOKEN
        val username = sharedPreferences.getString("nextcloud_username", DEFAULT_USERNAME) ?: DEFAULT_USERNAME
        
        // Считаем, что настройки всегда сконфигурированы, поскольку у нас есть значения по умолчанию
        val configured = true
        
        _uiState.update { currentState ->
            currentState.copy(
                serverUrl = serverUrl,
                token = token,
                username = username,
                areSettingsConfigured = configured
            )
        }
    }
    
    /**
     * Запуск таймера для автоматического обновления контактов
     */
    private fun startContactsRefreshTimer() {
        contactsRefreshJob?.cancel()
        contactsRefreshJob = viewModelScope.launch {
            while (true) {
                delay(MIN_REFRESH_INTERVAL) // Обновляем каждые 30 секунд
                // Проверяем, прошло ли достаточно времени с момента последнего обновления
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastContactsRefreshTime >= MIN_REFRESH_INTERVAL) {
                    refreshContacts(silent = true)
                }
            }
        }
    }
    
    /**
     * Запуск таймера для обновления статусов контактов
     */
    private fun startStatusUpdateTimer() {
        statusUpdateJob?.cancel()
        statusUpdateJob = viewModelScope.launch {
            while (true) {
                delay(15_000) // Обновляем статусы каждые 15 секунд
                updateRandomContactStatus()
            }
        }
    }
    
    /**
     * Обновление случайного статуса контакта (для демонстрации)
     */
    private fun updateRandomContactStatus() {
        val contacts = _uiState.value.contacts
        if (contacts.isEmpty()) return
        
        viewModelScope.launch {
            try {
                // Выбираем случайный контакт
                val randomIndex = Random.nextInt(contacts.size)
                val contactToUpdate = contacts[randomIndex]
                
                // Сгенерируем новый статус (онлайн/офлайн)
                val newStatus = if (Random.nextDouble() > 0.5) "online" else "offline"
                
                // Если статус не изменился, не обновляем
                if (contactToUpdate.status == newStatus) return@launch
                
                // Обновляем статус через репозиторий
                repository.updateContactStatus(contactToUpdate.id, newStatus)
                
                // Обновляем UI с измененным контактом
                val updatedContacts = contacts.toMutableList()
                updatedContacts[randomIndex] = contactToUpdate.copy(status = newStatus)
                
                _uiState.update { it.copy(contacts = updatedContacts) }
                
                Timber.d("Обновлен статус контакта ${contactToUpdate.displayName} на $newStatus")
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при обновлении статуса контакта")
            }
        }
    }
    
    /**
     * Загрузка контактов с сервера
     * 
     * @param forceRefresh принудительно обновить данные с сервера
     * @param silent не показывать индикатор загрузки
     */
    fun loadContacts(forceRefresh: Boolean = false, silent: Boolean = false) {
        // Если уже загружаем, то не запускаем новую загрузку
        if (_uiState.value.isLoading) return
        
        // Проверяем настроены ли параметры подключения
        if (!_uiState.value.areSettingsConfigured) {
            _uiState.update { currentState ->
                currentState.copy(
                    errorMessage = "Необходимо настроить подключение к Nextcloud",
                    isLoading = false
                )
            }
            return
        }
        
        // Обновляем UI состояние
        if (!silent) {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        } else {
            _uiState.update { it.copy(refreshing = true) }
        }
        
        viewModelScope.launch {
            // Используем новый метод API для получения списка пользователей
            Timber.d("Запуск загрузки пользователей через cloud/users API")
            
            repository.getCloudUsers(
                search = _uiState.value.searchQuery.takeIf { it.isNotEmpty() },
                limit = 50,
                forceRefresh = forceRefresh
            ).collect { result ->
                when (result) {
                    is ApiResult.Success -> {
                        Timber.d("Загружено ${result.data.size} пользователей через cloud/users API")
                        lastContactsRefreshTime = System.currentTimeMillis()
                        _uiState.update { currentState ->
                            currentState.copy(
                                contacts = result.data,
                                isLoading = false,
                                refreshing = false,
                                errorMessage = null,
                                lastUpdated = System.currentTimeMillis()
                            )
                        }
                    }
                    is ApiResult.Error -> {
                        Timber.e("Ошибка загрузки пользователей: ${result.message}")
                        _uiState.update { currentState ->
                            currentState.copy(
                                isLoading = false,
                                refreshing = false,
                                errorMessage = result.message
                            )
                        }
                    }
                    ApiResult.Loading -> {
                        // Уже обработали в начале функции
                        if (!silent) {
                            _uiState.update { it.copy(isLoading = true) }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Принудительное обновление списка контактов
     */
    fun refreshContacts(forceRefresh: Boolean = true, silent: Boolean = false) {
        Timber.d("Запуск обновления контактов (forceRefresh=$forceRefresh, silent=$silent)")
        loadContacts(forceRefresh = forceRefresh, silent = silent)
    }
    
    /**
     * Преобразование модели контакта API в модель UI
     */
    fun mapToUiContact(contactResponse: NextcloudContactResponse): NextcloudContact {
        return NextcloudContact(
            id = contactResponse.id,
            name = contactResponse.displayName ?: contactResponse.name,
            avatarUrl = contactResponse.avatarUrl,
            callUrl = contactResponse.fullCallUrl,
            isFavorite = contactResponse.isFavorite,
            isOnline = contactResponse.status == "online"
        )
    }
    
    /**
     * Сохранение настроек в SharedPreferences
     */
    fun saveSettings(serverUrl: String, token: String, username: String) {
        sharedPreferences.edit().apply {
            putString("nextcloud_server_url", serverUrl)
            putString("nextcloud_token", token)
            putString("nextcloud_username", username)
            apply()
        }
        
        _uiState.update { currentState ->
            currentState.copy(
                serverUrl = serverUrl,
                token = token,
                username = username,
                showSettings = false
            )
        }
        
        // Обновляем статус настроек
        updateSettings()
        
        // Перезагружаем контакты с новыми настройками
        loadContacts(forceRefresh = true)
    }
    
    /**
     * Установка состояния показа экрана настроек
     */
    fun setShowSettings(show: Boolean) {
        _uiState.update { it.copy(showSettings = show) }
    }
    
    /**
     * Установка поискового запроса
     */
    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        loadContacts()
    }
    
    /**
     * Установка выбранной группы
     */
    fun setSelectedGroup(group: String) {
        _uiState.update { it.copy(selectedGroup = group) }
        loadContacts()
    }
    
    /**
     * Очистка кэша контактов
     */
    fun clearCache() {
        viewModelScope.launch {
            try {
                repository.clearContactsCache()
                // Перезагружаем контакты после очистки кэша
                loadContacts(forceRefresh = true)
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при очистке кэша")
            }
        }
    }
    
    /**
     * Проверка настроен ли доступ к Nextcloud
     */
    private fun isSettingsConfigured(serverUrl: String, token: String): Boolean {
        // Считаем, что настройки всегда сконфигурированы, поскольку у нас есть значения по умолчанию
        return true
    }
    
    /**
     * Обновление состояния настроек
     */
    fun updateSettings() {
        _uiState.update { currentState ->
            currentState.copy(
                areSettingsConfigured = isSettingsConfigured(currentState.serverUrl, currentState.token)
            )
        }
    }
    
    /**
     * Создание комнаты для звонка и получение URL звонка
     * 
     * @param userId ID пользователя, с которым создается звонок
     * @param customName Пользовательское название для комнаты (опционально)
     * @param callback Колбэк с результатом (URL звонка или null в случае ошибки)
     */
    fun createCallRoom(userId: String, customName: String? = null, callback: (String?) -> Unit) {
        viewModelScope.launch {
            // Создаем комнату через репозиторий
            Timber.d("Запрос на создание комнаты для звонка с пользователем $userId")
            
            val result = repository.createCallRoom(userId, customName)
            
            when (result) {
                is ApiResult.Success -> {
                    val room = result.data
                    val callUrl = room.callUrl
                    Timber.d("Комната создана успешно, URL звонка: $callUrl")
                    
                    // Вызываем колбэк с URL звонка
                    callback(callUrl)
                }
                is ApiResult.Error -> {
                    Timber.e("Ошибка при создании комнаты: ${result.message}")
                    
                    // Показываем сообщение об ошибке
                    _uiState.update { currentState ->
                        currentState.copy(errorMessage = "Не удалось создать комнату для звонка: ${result.message}")
                    }
                    
                    // Вызываем колбэк с null (ошибка)
                    callback(null)
                }
                else -> {
                    // Не должно происходить
                    callback(null)
                }
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        contactsRefreshJob?.cancel()
        statusUpdateJob?.cancel()
    }
} 