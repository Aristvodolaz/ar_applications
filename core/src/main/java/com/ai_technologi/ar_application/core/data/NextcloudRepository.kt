package com.ai_technologi.ar_application.core.data

import com.ai_technologi.ar_application.core.di.IoDispatcher
import com.ai_technologi.ar_application.core.network.ApiResult
import com.ai_technologi.ar_application.core.network.NextCloudApi
import com.ai_technologi.ar_application.core.network.models.NextcloudContactResponse
import com.ai_technologi.ar_application.core.network.models.RoomResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton


/**
 * Репозиторий для работы с данными Nextcloud
 */
@Singleton
class NextcloudRepository @Inject constructor(
    private val api: NextCloudApi,
    private val sessionManager: SessionManager,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    
    // Сохраняем последний результат для быстрого доступа (in-memory кэш)
    private var lastContactsResult: List<NextcloudContactResponse>? = null
    private var lastFilter: String? = null
    private var lastSearch: String? = null
    private var lastFetchTime: Long = 0
    
    // Постоянный URL для тестового звонка
    private val testCallUrl = "https://ar.sitebill.site/call/c5i63ood"
    
    /**
     * Получение списка контактов Nextcloud
     * 
     * @param search строка поиска (опционально)
     * @param filter фильтрация контактов (all, favorites, recent)
     * @param forceRefresh принудительно обновить данные с сервера
     * @return Flow с результатом запроса
     */
    fun getContacts(
        search: String? = null, 
        filter: String = "all",
        forceRefresh: Boolean = false
    ): Flow<ApiResult<List<NextcloudContactResponse>>> = flow {
        // Сначала отправляем состояние загрузки
        emit(ApiResult.Loading)
        
        // Проверяем in-memory кэш если не требуется принудительное обновление
        val currentTime = System.currentTimeMillis()
        val cacheExpired = (currentTime - lastFetchTime) > CACHE_LIFETIME
        
        if (!forceRefresh && !cacheExpired && 
            lastContactsResult != null && 
            lastFilter == filter &&
            lastSearch == search) {
            // Используем кэшированные данные
            lastContactsResult?.let {
                emit(ApiResult.Success(it))
                return@flow
            }
        }
        
        try {
            // Получаем сохраненный токен
            val savedToken = sessionManager.getAuthTokenBlocking()
            val token = "Bearer " + (savedToken ?: "")
            

            // Если токен пустой, используем токен по умолчанию для демонстрации
            val authToken = if (token == "Bearer ") {
                "Bearer pIErFQXvOZ8XGMEvnMMwUuRVQGtPISfyXupObEOCBhXGoZ72uXyspGH89RpGu1uvPnJBVxn5"
            } else {
                token
            }
            

            // Маппинг фильтров из наших значений в значения Nextcloud
            val ncFilter = when(filter) {
                "favorites" -> "favorites"
                "recent" -> "recent"
                "groups" -> "groups"
                else -> "all"
            }
            
            // Выполняем запрос к API
            val response = api.getContacts(
                token = authToken,
                search = search,
                filter = ncFilter,
                itemType = "call",
                shareType = 0,
                limit = 50
            )
            

            // Обрабатываем ответ
            if (response.isSuccessful) {
                // Для совместимости с версией API используем безопасные обращения
                val responseBody = response.body()
                // Используем структуру NextCloudResponse<AutocompleteData>
                val exactUsers = responseBody?.ocs?.data?.exact?.users ?: emptyList()
                val regularUsers = responseBody?.ocs?.data?.users ?: emptyList()
                

                // Удаляем дубликаты (контакты могут быть как в точных совпадениях, так и в обычном списке)
                val combinedContacts = (exactUsers + regularUsers).distinctBy { contact -> contact.id }
                
                // Добавляем тестовый контакт, если список пуст
                val finalContacts = if (combinedContacts.isEmpty()) {
                    createTestContacts()
                } else {
                    combinedContacts
                }
                

                // Сохраняем в in-memory кэш
                lastContactsResult = finalContacts
                lastFilter = filter
                lastSearch = search
                lastFetchTime = currentTime
                
                // Отправляем результат
                emit(ApiResult.Success(finalContacts))
            } else {
                // Обработка ошибки от сервера
                val errorCode = response.code()
                val errorMessage = "Ошибка загрузки контактов: $errorCode - ${response.message()}"

                // Использование тестовых контактов при ошибке
                val testContacts = createTestContacts()
                lastContactsResult = testContacts
                lastFetchTime = currentTime
                
                emit(ApiResult.Success(testContacts))
            }
        } catch (e: IOException) {
            // Обработка ошибки сети
            val errorMessage = "Ошибка сети: ${e.message}"

            // При ошибке сети используем кэшированные или тестовые данные
            if (lastContactsResult != null && lastContactsResult!!.isNotEmpty()) {
                emit(ApiResult.Success(lastContactsResult!!))
            } else {
                val testContacts = createTestContacts()
                lastContactsResult = testContacts
                lastFetchTime = System.currentTimeMillis()
                
                emit(ApiResult.Success(testContacts))
            }
        } catch (e: Exception) {
            // Обработка других ошибок
            val errorMessage = "Неизвестная ошибка: ${e.message}"

            // Создаем тестовые контакты
            val testContacts = createTestContacts()
            lastContactsResult = testContacts
            lastFetchTime = System.currentTimeMillis()
            
            emit(ApiResult.Success(testContacts))
        }
    }.catch { e ->
        // Перехватываем все необработанные исключения
        val errorMessage = "Критическая ошибка: ${e.message}"

        // Последняя попытка - использовать кэшированные или тестовые данные
        if (lastContactsResult != null && lastContactsResult!!.isNotEmpty()) {
            emit(ApiResult.Success(lastContactsResult!!))
        } else {
            val testContacts = createTestContacts()
            emit(ApiResult.Success(testContacts))
        }
    }.flowOn(ioDispatcher)
    
    /**
     * Создает тестовые контакты для демонстрации
     */
    private fun createTestContacts(): List<NextcloudContactResponse> {
        return listOf(
            NextcloudContactResponse(
                id = "test1",
                name = "Тестовый контакт 1",
                displayName = "Тестовый контакт 1",
                status = "online",
                avatarUrl = null,
                callUrl = testCallUrl,
                isFavorite = true
            ),
            NextcloudContactResponse(
                id = "test2",
                name = "Тестовый контакт 2",
                displayName = "Тестовый контакт 2",
                status = "away",
                avatarUrl = null,
                callUrl = testCallUrl,
                isFavorite = false
            ),
            NextcloudContactResponse(
                id = "c5i63ood",
                name = "Тестовый звонок",
                displayName = "Тестовый звонок",
                status = "online",
                avatarUrl = null,
                callUrl = testCallUrl,
                isFavorite = true
            )
        )
    }
    
    /**
     * Имитация обновления статуса контакта
     */
    suspend fun updateContactStatus(id: String, status: String) {
        // Обновляем статус в in-memory кэше, если он есть
        lastContactsResult = lastContactsResult?.map { contact ->
            if (contact.id == id) {
                contact.copy(status = status)
            } else {
                contact
            }
        }
        
        // В реальном приложении здесь можно отправить запрос на сервер для 
        // обновления статуса пользователя через API
    }
    
    /**
     * Получение статуса пользователя
     */
    suspend fun getUserStatus(userId: String): ApiResult<String> {
        try {
            val savedToken = sessionManager.getAuthTokenBlocking()
            val token = "Bearer " + (savedToken ?: "")
            
            val authToken = if (token == "Bearer ") {
                "Bearer pIErFQXvOZ8XGMEvnMMwUuRVQGtPISfyXupObEOCBhXGoZ72uXyspGH89RpGu1uvPnJBVxn5"
            } else {
                token
            }
            
            val response = api.getUserStatus(authToken, userId)
            
            // Обработка ответа упрощена, в реальном приложении нужно парсить JSON-ответ
            if (response.isSuccessful) {
                return ApiResult.Success("online")
            } else {
                return ApiResult.Error(message = "Не удалось получить статус пользователя")
            }
        } catch (e: Exception) {
            return ApiResult.Error(message = "Ошибка при получении статуса: ${e.message}")
        }
    }
    
    /**
     * Очистка кэша контактов
     */
    fun clearContactsCache() {
        lastContactsResult = null
        lastFilter = null
        lastSearch = null
        lastFetchTime = 0
    }
    
    /**
     * Получение списка пользователей через cloud/users API
     * 
     * @param search строка поиска (опционально)
     * @param limit максимальное количество пользователей
     * @param forceRefresh принудительно обновить данные с сервера
     * @return Flow с результатом запроса
     */
    fun getCloudUsers(
        search: String? = null, 
        limit: Int? = 50,
        forceRefresh: Boolean = false
    ): Flow<ApiResult<List<NextcloudContactResponse>>> = flow {
        // Сначала отправляем состояние загрузки
        emit(ApiResult.Loading)
        
        try {
            // Получаем сохраненный токен
            val savedToken = sessionManager.getAuthTokenBlocking()
            val token = "Bearer " + (savedToken ?: "")
            

            // Если токен пустой, используем токен по умолчанию для демонстрации
            val authToken = if (token == "Bearer ") {
                "Bearer pIErFQXvOZ8XGMEvnMMwUuRVQGtPISfyXupObEOCBhXGoZ72uXyspGH89RpGu1uvPnJBVxn5"
            } else {
                token
            }
            
            // Выполняем запрос к API
            val response = api.getCloudUsers(
                token = authToken,
                search = search,
                limit = limit
            )
            

            // Обрабатываем ответ
            if (response.isSuccessful) {
                val responseBody = response.body()
                val userIds = responseBody?.ocs?.data?.users ?: emptyList()
                

                if (userIds.isEmpty()) {
                    // Если список пустой, возвращаем тестовые контакты
                    val testContacts = createTestContacts()
                    emit(ApiResult.Success(testContacts))
                } else {
                    // Преобразуем список ID пользователей в объекты NextcloudContactResponse
                    val contacts = userIds.map { userId ->
                        NextcloudContactResponse(
                            id = userId.toString(),
                            name = userId.toString(),
                            displayName = userId.toString(),
                            status = "online", // Предполагаем, что все онлайн
                            avatarUrl = null,
                            callUrl = "https://ar.sitebill.site/call/$userId",
                            isFavorite = false
                        )
                    }
                    

                    // Кэшируем результат
                    lastContactsResult = contacts
                    lastFetchTime = System.currentTimeMillis()
                    
                    // Отправляем результат
                    emit(ApiResult.Success(contacts))
                }
            } else {
                // Обработка ошибки от сервера
                val errorCode = response.code()
                val errorMessage = "Ошибка загрузки пользователей: $errorCode - ${response.message()}"

                // Использование тестовых контактов при ошибке
                val testContacts = createTestContacts()
                emit(ApiResult.Success(testContacts))
            }
        } catch (e: Exception) {
            // Обработка всех ошибок
            val errorMessage = "Ошибка при получении пользователей: ${e.message}"

            // Возвращаем тестовые контакты при ошибке
            val testContacts = createTestContacts()
            emit(ApiResult.Success(testContacts))
        }
    }.flowOn(ioDispatcher)
    
    /**
     * Создание комнаты для звонка с пользователем
     * 
     * @param userId ID пользователя, с которым нужно создать комнату
     * @param customRoomName Пользовательское имя для комнаты (опционально)
     * @return ApiResult с информацией о созданной комнате
     */
    suspend fun createCallRoom(userId: String, customRoomName: String? = null): ApiResult<RoomResponse> {
        try {
            // Получаем сохраненный токен
            val savedToken = sessionManager.getAuthTokenBlocking()
            val token = "Bearer " + (savedToken ?: "")
            

            // Если токен пустой, используем токен по умолчанию для демонстрации
            val authToken = if (token == "Bearer ") {
                "Bearer pIErFQXvOZ8XGMEvnMMwUuRVQGtPISfyXupObEOCBhXGoZ72uXyspGH89RpGu1uvPnJBVxn5"
            } else {
                token
            }
            
            // Устанавливаем название комнаты
            val roomName = customRoomName ?: "Звонок с $userId"
            
            // Выполняем запрос к API
            val response = api.createRoom(
                token = authToken,
                roomName = roomName,
                roomType = 2, // Групповая комната
                participants = listOf(userId)
            )
            

            // Обрабатываем ответ
            if (response.isSuccessful) {
                val roomData = response.body()?.ocs?.data
                
                return if (roomData != null) {
                    ApiResult.Success(roomData)
                } else {
                    ApiResult.Error(message = "Ошибка при создании комнаты: данные отсутствуют")
                }
            } else {
                // Обработка ошибки от сервера
                val errorCode = response.code()
                val errorMessage = "Ошибка создания комнаты: $errorCode - ${response.message()}"

                return ApiResult.Error(message = errorMessage)
            }
        } catch (e: Exception) {
            // Обработка ошибок
            val errorMessage = "Ошибка при создании комнаты для звонка: ${e.message}"

            return ApiResult.Error(message = errorMessage)
        }
    }
    
    companion object {
        private const val CACHE_LIFETIME = 60_000L // 1 минута
    }
} 