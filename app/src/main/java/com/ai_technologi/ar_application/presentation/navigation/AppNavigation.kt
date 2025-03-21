package com.ai_technologi.ar_application.presentation.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ai_technologi.ar_application.presentation.ui.HomeScreen
import com.ai_technologi.ar_application.presentation.ui.NextcloudCallScreen
import com.ai_technologi.ar_application.presentation.ui.NextcloudContactsScreen
import com.ai_technologi.ar_application.presentation.ui.WebViewScreen
import com.ai_technologi.ar_application.auth.presentation.ui.ScanLoginScreen
import com.ai_technologi.ar_application.auth.presentation.ui.EnterPinScreen
import com.ai_technologi.ar_application.auth.presentation.viewmodel.AuthViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import com.ai_technologi.ar_application.auth.domain.model.AuthState
import com.ai_technologi.ar_application.auth.domain.model.AuthIntent

/**
 * Маршруты навигации приложения
 */
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object WebView : Screen("webview/{url}") {
        fun createRoute(url: String): String {
            val encodedUrl = Uri.encode(url)
            return "webview/$encodedUrl"
        }
    }
    object NextcloudContacts : Screen("nextcloud/contacts")
    object NextcloudCall : Screen("nextcloud/call/{url}") {
        fun createRoute(url: String): String {
            val encodedUrl = Uri.encode(url)
            return "nextcloud/call/$encodedUrl"
        }
    }
    object ScanLogin : Screen("auth/scan_login")
    object EnterPin : Screen("auth/enter_pin/{login}") {
        fun createRoute(login: String): String {
            val encodedLogin = Uri.encode(login)
            return "auth/enter_pin/$encodedLogin"
        }
    }
}

/**
 * Навигация приложения
 *
 * @param navController контроллер навигации
 * @param startDestination начальный маршрут
 */
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.ScanLogin.route
) {
    // Получаем AuthViewModel для управления процессом аутентификации
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState = authViewModel.state.collectAsState().value
    
    // Обрабатываем изменения состояния аутентификации
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                // Когда получаем токен, переходим к списку контактов
                navController.navigate(Screen.NextcloudContacts.route) {
                    popUpTo(Screen.ScanLogin.route) { inclusive = true }
                }
            }
            is AuthState.ScanLogin -> {
                navController.navigate(Screen.ScanLogin.route)
            }
            is AuthState.EnterPin -> {
                navController.navigate(Screen.EnterPin.createRoute(authState.login))
            }
            else -> { /* Обрабатываем другие состояния */ }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.ScanLogin.route) {
            ScanLoginScreen(
                onLoginScanned = { login ->
                    authViewModel.processIntent(AuthIntent.SetLogin(login))
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.EnterPin.route) { backStackEntry ->
            val login = Uri.decode(backStackEntry.arguments?.getString("login") ?: "")
            
            EnterPinScreen(
                login = login,
                onPinEntered = { pin ->
                    authViewModel.processIntent(AuthIntent.AuthenticateWithPin(login, pin))
                },
                onBackClick = {
                    authViewModel.processIntent(AuthIntent.StartScanLogin)
                }
            )
        }
        
        composable(Screen.Home.route) {
            HomeScreen(
                onOpenWebView = { url ->
                    navController.navigate(Screen.WebView.createRoute(url))
                },
                onOpenNextcloudContacts = {
                    navController.navigate(Screen.NextcloudContacts.route)
                }
            )
        }
        
        composable(Screen.WebView.route) { backStackEntry ->
            val encodedUrl = backStackEntry.arguments?.getString("url") ?: ""
            val url = Uri.decode(encodedUrl)
            
            WebViewScreen(
                url = url,
                title = "",
                onBackPressed = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        }
                    }
                }
            )
        }
        
        composable(Screen.NextcloudContacts.route) {
            NextcloudContactsScreen(
                onContactSelected = { contact ->
                    navController.navigate(Screen.NextcloudCall.createRoute(contact.callUrl))
                },
                onBackPressed = {
                    // Просто закрываем приложение вместо разлогина
                    android.os.Process.killProcess(android.os.Process.myPid())
                }
            )
        }
        
        composable(Screen.NextcloudCall.route) { backStackEntry ->
            val encodedUrl = backStackEntry.arguments?.getString("url") ?: ""
            val url = Uri.decode(encodedUrl)
            
            NextcloudCallScreen(
                callUrl = url,
                onBackPressed = {
                    navController.navigate(Screen.NextcloudContacts.route)
                }
            )
        }
    }
} 