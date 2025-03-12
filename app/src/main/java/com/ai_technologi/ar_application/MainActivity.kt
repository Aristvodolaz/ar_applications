package com.ai_technologi.ar_application

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ai_technologi.ar_application.auth.presentation.ui.AuthScreen
import com.ai_technologi.ar_application.core.ui.ARAdaptiveUIProvider
import com.ai_technologi.ar_application.navigation.AppDestination
import com.ai_technologi.ar_application.navigation.NavigationViewModel
import com.ai_technologi.ar_application.ui.theme.Ar_applicationTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Устанавливаем SplashScreen
        installSplashScreen()
        
        // Настраиваем полноэкранный режим для AR-очков
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        super.onCreate(savedInstanceState)
        setContent {
            Ar_applicationTheme {
                // Оборачиваем все приложение в ARAdaptiveUIProvider для адаптивного интерфейса
                ARAdaptiveUIProvider {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        AppNavigation()
                    }
                }
            }
        }
    }
}

/**
 * Основная навигация приложения.
 */
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    navigationViewModel: NavigationViewModel = hiltViewModel()
) {
    val startDestination by navigationViewModel.startDestination.collectAsState()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Экран аутентификации
        composable(AppDestination.Auth.route) {
            AuthScreen(
                onAuthSuccess = {
                    navController.navigate(AppDestination.UsersList.route) {
                        popUpTo(AppDestination.Auth.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
        
        // Экран списка пользователей
//        composable(AppDestination.UsersList.route) {
            // TODO: Реализовать экран списка пользователей
            // UsersListScreen(
            //     onUserSelected = { userId ->
            //         navController.navigate("${AppDestination.VideoCall.route}/$userId")
            //     }
            // )
            
//            // Временное решение для тестирования видеозвонка
//            VideoCallScreen(
//                userId = "test_user",
//                onCallEnded = {
//                    navController.popBackStack()
//                }
//            )
//        }
//
//        // Экран видеозвонка
//        composable(
//            route = "${AppDestination.VideoCall.route}/{userId}",
//            arguments = AppDestination.VideoCall.arguments
//        ) { backStackEntry ->
//            val userId = backStackEntry.arguments?.getString("userId")
//            VideoCallScreen(
//                userId = userId,
//                onCallEnded = {
//                    navController.popBackStack()
//                }
//            )
//        }
//    }
//}