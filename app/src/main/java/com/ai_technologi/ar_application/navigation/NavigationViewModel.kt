package com.ai_technologi.ar_application.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ai_technologi.ar_application.core.data.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * ViewModel для управления навигацией в приложении.
 *
 * @param sessionManager менеджер сессии для проверки аутентификации
 */
@HiltViewModel
class NavigationViewModel @Inject constructor(
    sessionManager: SessionManager
) : ViewModel() {

    /**
     * Начальный маршрут навигации.
     * Если пользователь аутентифицирован, то начинаем с экрана списка пользователей,
     * иначе с экрана аутентификации.
     */
    val startDestination: StateFlow<String> = sessionManager.authToken
        .map { token ->
            if (token.isNullOrEmpty()) {
                AppDestination.Auth.route
            } else {
                AppDestination.UsersList.route
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppDestination.Auth.route
        )
} 