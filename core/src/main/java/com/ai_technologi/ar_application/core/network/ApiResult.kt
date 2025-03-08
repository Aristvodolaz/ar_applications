package com.ai_technologi.ar_application.core.network

/**
 * Класс для обработки результатов сетевых запросов.
 * Может быть одним из трех состояний: Success, Error или Loading.
 */
sealed class ApiResult<out T> {
    /**
     * Успешный результат с данными.
     *
     * @param data данные, полученные от API
     */
    data class Success<T>(val data: T) : ApiResult<T>()

    /**
     * Ошибка при выполнении запроса.
     *
     * @param exception исключение, которое произошло
     * @param message сообщение об ошибке
     */
    data class Error(val exception: Throwable? = null, val message: String = "") : ApiResult<Nothing>()

    /**
     * Состояние загрузки.
     */
    object Loading : ApiResult<Nothing>()
} 