package com.example.depremsafe

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.depremsafe.data.local.TokenManager
import com.example.depremsafe.ui.screens.*
import com.example.depremsafe.ui.theme.DepremSafeTheme
import com.example.depremsafe.viewmodel.ChatViewModel
import com.example.depremsafe.viewmodel.LoginViewModel
import com.example.depremsafe.viewmodel.LoginViewModelFactory
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            DepremSafeTheme {
                val navController = rememberNavController()
                val tokenManager = remember { TokenManager(applicationContext) }
                val scope = rememberCoroutineScope()
                var isLoggedIn by remember { mutableStateOf<Boolean?>(null) }

                // Login durumunu kontrol et
                LaunchedEffect(Unit) {
                    scope.launch {
                        tokenManager.isLoggedIn.collect { loggedIn ->
                            isLoggedIn = loggedIn
                            Log.d("MainActivity", "Login durumu: $loggedIn")
                        }
                    }
                }

                // Login durumu hazır olana kadar loading
                if (isLoggedIn != null) {
                    NavHost(
                        navController = navController,
                        startDestination = if (isLoggedIn == true) "home" else "onboarding"
                    ) {
                        // Onboarding ekranı
                        composable("onboarding") {
                            OnboardingScreen(
                                onFinished = {
                                    Log.d("MainActivity", "Onboarding tamamlandı")
                                    navController.navigate("login") {
                                        popUpTo("onboarding") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // Login ekranı
                        composable("login") {
                            val loginViewModel: LoginViewModel = viewModel(
                                factory = LoginViewModelFactory(applicationContext)
                            )
                            val loginUiState by loginViewModel.uiState.collectAsState()

                            // Login başarılı olduğunda kontrol et
                            LaunchedEffect(loginUiState.isLoggedIn) {
                                if (loginUiState.isLoggedIn) {
                                    Log.d("MainActivity", "Login başarılı. City: ${loginUiState.userCity}")

                                    // Şehir bilgisi yoksa profile setup'a git
                                    if (loginUiState.userCity.isNullOrEmpty()) {
                                        navController.navigate("profile_setup") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    } else {
                                        navController.navigate("home") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    }
                                }
                            }

                            LoginScreen(
                                viewModel = loginViewModel,
                                onLoginSuccess = { /* Navigation LaunchedEffect'te */ }
                            )
                        }

                        // Profile Setup ekranı
                        composable("profile_setup") {
                            val loginViewModel: LoginViewModel = viewModel(
                                factory = LoginViewModelFactory(applicationContext)
                            )
                            val loginUiState by loginViewModel.uiState.collectAsState()

                            ProfileSetupScreen(
                                userName = loginUiState.userName ?: "Kullanıcı",
                                onCitySelected = { city ->
                                    Log.d("MainActivity", "Şehir seçildi: $city")
                                    loginViewModel.updateCity(city)
                                    navController.navigate("home") {
                                        popUpTo("profile_setup") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // Home ekranı
                        composable("home") {
                            val loginViewModel: LoginViewModel = viewModel(
                                factory = LoginViewModelFactory(applicationContext)
                            )
                            val loginUiState by loginViewModel.uiState.collectAsState()

                            Log.d("MainActivity", "HomeScreen - Name: ${loginUiState.userName}, City: ${loginUiState.userCity}")

                            HomeScreen(
                                userName = loginUiState.userName,
                                userCity = loginUiState.userCity,
                                onSafeClick = {
                                    Log.d("MainActivity", "Güvendeyim butonuna tıklandı")
                                    navController.navigate("chat/safe")
                                },
                                onHelpClick = {
                                    Log.d("MainActivity", "Yardım butonuna tıklandı")
                                    navController.navigate("chat/help")
                                },
                                onGuideClick = {
                                    Log.d("MainActivity", "Rehber butonuna tıklandı")
                                    navController.navigate("guide")
                                }
                            )
                        }

                        // Chat ekranı - safe durumu için
                        composable("chat/safe") {
                            Log.d("MainActivity", "ChatScreen (safe) açılıyor")
                            val chatViewModel: ChatViewModel = viewModel()
                            ChatScreen(
                                isSafe = true,
                                onBackClick = {
                                    navController.popBackStack()
                                },
                                viewModel = chatViewModel
                            )
                        }

                        // Chat ekranı - help durumu için
                        composable("chat/help") {
                            Log.d("MainActivity", "ChatScreen (help) açılıyor")
                            val chatViewModel: ChatViewModel = viewModel()
                            ChatScreen(
                                isSafe = false,
                                onBackClick = {
                                    navController.popBackStack()
                                },
                                viewModel = chatViewModel
                            )
                        }

                        // Guide ekranı
                        composable("guide") {
                            GuideScreen(
                                onBackClick = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                } else {
                    // Loading ekranı
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}