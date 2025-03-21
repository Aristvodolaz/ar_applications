package com.ai_technologi.ar_application.auth.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ai_technologi.ar_application.auth.domain.model.AuthIntent
import com.ai_technologi.ar_application.auth.domain.model.AuthState
import com.ai_technologi.ar_application.auth.domain.repository.AuthRepository
import com.ai_technologi.ar_application.core.util.ApiResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel для экрана аутентификации.
 *
 * @param authRepository репозиторий для аутентификации
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow<AuthState>(AuthState.Initial)
    val state: StateFlow<AuthState> = _state.asStateFlow()
    
    init {
        // Начинаем процесс с экрана сканирования логина, независимо от наличия 
        // сохраненного токена. Раскомментируйте код ниже, если нужно 
        // автоматически входить с сохраненным токеном
        
        _state.value = AuthState.ScanLogin
        
        /*
        viewModelScope.launch {
            if (authRepository.isAuthenticated()) {
                val token = authRepository.getAuthToken() ?: ""
                _state.value = AuthState.Success(token)
            } else {
                _state.value = AuthState.ScanLogin
            }
        }
        */
    }
    
    /**
     * Обработка интентов.
     *
     * @param intent интент
     */
    fun processIntent(intent: AuthIntent) {
        when (intent) {
            is AuthIntent.StartScanLogin -> {
                _state.value = AuthState.ScanLogin
            }
            
            is AuthIntent.SetLogin -> {
                _state.value = AuthState.EnterPin(intent.login)
            }
            
            is AuthIntent.AuthenticateWithPin -> {
                authenticateWithPin(intent.login, intent.pin)
            }
            
            is AuthIntent.Reset -> {
                // При сбросе очищаем сессию и возвращаемся к начальному экрану
                viewModelScope.launch {
//                    authRepository.clearSession()
                    _state.value = AuthState.ScanLogin
                }
            }
        }
    }
    
    /**
     * Аутентификация с PIN-кодом.
     *
     * @param login логин пользователя
     * @param pin PIN-код пользователя
     */
    private fun authenticateWithPin(login: String, pin: String) {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            
            when (val result = authRepository.authenticateWithPin(login, pin)) {
                is ApiResult.Success -> {
                    _state.value = AuthState.Success(result.data)
                }
                
                is ApiResult.Error -> {
                    _state.value = AuthState.Error(result.message)
                }
                
                is ApiResult.Loading -> {
                    // Уже установлено выше
                }
            }
        }
    }
} 