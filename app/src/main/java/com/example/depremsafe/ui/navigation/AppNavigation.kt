package com.example.depremsafe.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.depremsafe.ui.screens.ChatScreen
import com.example.depremsafe.ui.screens.GuideScreen
import com.example.depremsafe.ui.screens.HomeScreen
import com.example.depremsafe.ui.screens.OnboardingScreen
import com.example.depremsafe.viewmodel.ChatViewModel

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Home : Screen("home")
    object Chat : Screen("chat/{isSafe}") {
        fun createRoute(isSafe: Boolean) = "chat/$isSafe"
    }
    object Guide : Screen("guide")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val chatViewModel: ChatViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.Onboarding.route
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinished = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onSafeClick = {
                    chatViewModel.resetChat()
                    chatViewModel.startConversation(true)
                    navController.navigate(Screen.Chat.createRoute(true))
                },
                onHelpClick = {
                    chatViewModel.resetChat()
                    chatViewModel.startConversation(false)
                    navController.navigate(Screen.Chat.createRoute(false))
                },
                onGuideClick = {
                    navController.navigate(Screen.Guide.route)
                }
            )
        }

        composable(
            route = Screen.Chat.route,
            arguments = listOf(navArgument("isSafe") { type = NavType.BoolType })
        ) { backStackEntry ->
            val isSafe = backStackEntry.arguments?.getBoolean("isSafe") ?: true
            ChatScreen(
                viewModel = chatViewModel,
                isSafe = isSafe,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Guide.route) {
            GuideScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
