package com.ai_technologi.ar_application.core.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Базовый абстрактный класс для всех ViewModel в паттерне MVI.
 * Обрабатывает Intent и обновляет State.
 *
 * @param S тип State
 * @param I тип Intent
 */
abstract class MviViewModel<S : MviState, I : MviIntent>(initialState: S) : ViewModel() {

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<S> = _state.asStateFlow()

    /**
     * Обрабатывает Intent и обновляет State.
     *
     * @param intent Intent, который нужно обработать
     */
    fun processIntent(intent: I) {
        viewModelScope.launch {
            handleIntent(intent)
        }
    }

    /**
     * Абстрактный метод для обработки Intent.
     * Должен быть реализован в конкретных ViewModel.
     *
     * @param intent Intent, который нужно обработать
     */
    protected abstract suspend fun handleIntent(intent: I)

    /**
     * Обновляет текущий State.
     *
     * @param reducer функция, которая принимает текущий State и возвращает новый State
     */
    protected fun updateState(reducer: (S) -> S) {
        val newState = reducer(_state.value)
        _state.value = newState
    }
} 