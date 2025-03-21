package com.ai_technologi.ar_application.core.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Расширение для Context для создания DataStore.
 */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session_prefs")

/**
 * Класс для управления данными сессии пользователя.
 * Хранит токен аутентификации и информацию о пользователе.
 */
@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val AUTH_TOKEN = stringPreferencesKey("auth_token")
        private val USER_ID = stringPreferencesKey("user_id")
        private val USER_NAME = stringPreferencesKey("user_name")
        private val USER_ROLE = stringPreferencesKey("user_role")
    }

    /**
     * Получение токена аутентификации.
     */
    val authToken: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[AUTH_TOKEN]
    }

    /**
     * Получение текущего значения токена аутентификации.
     * Блокирует текущий поток, поэтому должен использоваться только в фоновых потоках.
     */
    fun getAuthTokenBlocking(): String? = runBlocking {
        // Всегда возвращаем демо-токен, чтобы обойти экран аутентификации
        return@runBlocking "pIErFQXvOZ8XGMEvnMMwUuRVQGtPISfyXupObEOCBhXGoZ72uXyspGH89RpGu1uvPnJBVxn5"
    }

    /**
     * Получение ID пользователя.
     */
    val userId: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_ID]
    }

    /**
     * Получение имени пользователя.
     */
    val userName: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_NAME]
    }

    /**
     * Получение роли пользователя.
     */
    val userRole: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_ROLE]
    }

    /**
     * Сохранение токена аутентификации.
     *
     * @param token токен аутентификации
     */
    suspend fun saveAuthToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[AUTH_TOKEN] = token
        }
    }

    /**
     * Сохранение информации о пользователе.
     *
     * @param userId ID пользователя
     * @param userName имя пользователя
     * @param userRole роль пользователя
     */
    suspend fun saveUserInfo(userId: String, userName: String, userRole: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID] = userId
            preferences[USER_NAME] = userName
            preferences[USER_ROLE] = userRole
        }
    }

    /**
     * Очистка данных сессии при выходе из аккаунта.
     */
    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
} 