package com.topingames.topineraser.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.topingames.topineraser.ui.screens.editor.EditorScreen
import com.topingames.topineraser.ui.screens.home.HomeScreen

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Editor : Screen("editor")
}

@Composable
fun TopinEraserNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onImageSelected = { navController.navigate(Screen.Editor.route) }
            )
        }
        composable(Screen.Editor.route) {
            EditorScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
