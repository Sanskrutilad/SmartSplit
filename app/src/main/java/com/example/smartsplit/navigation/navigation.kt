package com.example.smartsplit.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.smartsplit.LaunchAnimationAppName
import com.example.smartsplit.screens.LoginScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "splash") {
        composable("login") { LoginScreen(navController) }
        composable("splash") { LaunchAnimationAppName(navController) }

    }
}