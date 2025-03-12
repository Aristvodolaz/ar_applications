package com.ai_technologi.ar_application.core.util

/**
 * Класс для обработки результатов API-запросов.
 *
 * @param T тип данных результата
 */
sealed class ApiResult<out T> {
    /**
     * Успешный результат.
     *
     * @param data данные результата
     */
    data class Success<T>(val data: T) : ApiResult<T>()
    
    /**
     * Ошибка.
     *
     * @param message сообщение об ошибке
     * @param code код ошибки (опционально)
     */
    data class Error(val message: String, val code: Int? = null) : ApiResult<Nothing>()
    
    /**
     * Загрузка.
     */
    object Loading : ApiResult<Nothing>()
    
    /**
     * Преобразование результата.
     *
     * @param transform функция преобразования
     * @return преобразованный результат
     */
    inline fun <R> map(transform: (T) -> R): ApiResult<R> {
        return when (this) {
            is Success -> Success(transform(data))
            is Error -> this
            is Loading -> this
        }
    }
    
    /**
     * Получение данных результата или null, если результат не успешный.
     *
     * @return данные результата или null
     */
    fun getOrNull(): T? {
        return when (this) {
            is Success -> data
            else -> null
        }
    }
    
    /**
     * Получение данных результата или значения по умолчанию, если результат не успешный.
     *
     * @param defaultValue значение по умолчанию
     * @return данные результата или значение по умолчанию
     */
    fun getOrDefault(defaultValue: @UnsafeVariance T): T {
        return when (this) {
            is Success -> data
            else -> defaultValue
        }
    }
    
    /**
     * Получение сообщения об ошибке или null, если результат успешный.
     *
     * @return сообщение об ошибке или null
     */
    fun errorMessageOrNull(): String? {
        return when (this) {
            is Error -> message
            else -> null
        }
    }
} 