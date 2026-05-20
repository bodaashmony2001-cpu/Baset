package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val userState by viewModel.currentUser.collectAsState()
                val navController = rememberNavController()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (userState == null) {
                        LoginScreen(
                            viewModel = viewModel,
                            onLoginSuccess = {
                                // Once logged in, flow automatically switches content
                            }
                        )
                    } else {
                        NavHost(
                            navController = navController,
                            startDestination = "dashboard"
                        ) {
                            composable("dashboard") {
                                DashboardScreen(
                                    viewModel = viewModel,
                                    navController = navController,
                                    onNavigateToAddProfile = {
                                        navController.navigate("add_profile")
                                    }
                                )
                            }
                            composable("profile_detail/{profileId}") { backStackEntry ->
                                val profileId = backStackEntry.arguments?.getString("profileId")?.toIntOrNull() ?: 0
                                ProfileDetailScreen(
                                    profileId = profileId,
                                    viewModel = viewModel,
                                    navController = navController
                                )
                            }
                            composable("add_profile") {
                                AddProfileScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = {
                                        navController.popBackStack()
                                    }
                                )
                            }
                            composable("chat_room") {
                                ChatRoomScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = {
                                        navController.popBackStack()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
