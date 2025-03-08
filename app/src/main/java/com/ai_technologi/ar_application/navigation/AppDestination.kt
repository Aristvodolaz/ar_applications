package com.ai_technologi.ar_application.navigation

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument

/**
 * Sealed класс для определения маршрутов навигации в приложении.
 */
sealed class AppDestination(
    val route: String,
    val arguments: List<NamedNavArgument> = emptyList()
) {
    /**
     * Экран аутентификации.
     */
    object Auth : AppDestination("auth")

    /**
     * Экран списка пользователей.
     */
    object UsersList : AppDestination("users_list")

    /**
     * Экран видеозвонка.
     */
    object VideoCall : AppDestination(
        route = "video_call",
        arguments = listOf(
            navArgument("userId") {
                type = NavType.StringType
            }
        )
    )
} 