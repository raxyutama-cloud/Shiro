package com.mori.downloader.gameturbo.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mori.downloader.gameturbo.ui.viewmodel.GameTurboViewModel
import com.mori.downloader.gameturbo.ui.screen.GameListScreen
import com.mori.downloader.gameturbo.ui.screen.ProfileScreen
import com.mori.downloader.gameturbo.ui.screen.MonitorScreen
import com.mori.downloader.gameturbo.ui.screen.SettingsScreen
import com.mori.downloader.gameturbo.ui.theme.GameTurboTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GameTurboTheme {
                val navController = rememberNavController()
                val viewModel: GameTurboViewModel = hiltViewModel()
                
                NavHost(navController, startDestination = "games") {
                    composable("games") {
                        GameListScreen(onGameClick = { game ->
                            navController.navigate("profile/${game.packageName}")
                        }, viewModel = viewModel)
                    }
                    composable(
                        route = "profile/{packageName}",
                        arguments = listOf(navArgument("packageName") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val packageName = backStackEntry.getString()!!
                        ProfileScreen(packageName = packageName, viewModel = hiltViewModel())
                    }
                    composable("monitor") {
                        MonitorScreen(viewModel = hiltViewModel())
                    }
                    composable("settings") {
                        SettingsScreen(viewModel = hiltViewModel())
                    }
                }
            }
        }
    }
}