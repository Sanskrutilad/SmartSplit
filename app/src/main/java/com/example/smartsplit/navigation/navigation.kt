package com.example.smartsplit.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.smartsplit.LaunchAnimationAppName
import com.example.smartsplit.screens.Homescreen.GroupSectionScreen
import com.example.smartsplit.screens.Loginscreen.LoginScreen
import com.example.smartsplit.screens.Loginscreen.SignupScreen
import com.example.smartsplit.screens.Loginscreen.Welcomscreen
import com.example.smartsplit.screens.Profile.ProfileScreen
import com.example.smartsplit.screens.onboarding.OnboardingScreen1
import com.example.smartsplit.screens.onboarding.OnboardingScreen2
import com.example.smartsplit.screens.onboarding.OnboardingScreen3

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "splash") {
        composable("Welcomscreen") { Welcomscreen(navController) }
        composable("splash") { LaunchAnimationAppName(navController) }
        composable("onboardscreen1") { OnboardingScreen1(navController) }
        composable("onboardscreen2") { OnboardingScreen2(navController) }
        composable("onboardscreen3") { OnboardingScreen3(navController) }
        composable("Group") { GroupSectionScreen(navController) }
        composable("Signup") { SignupScreen(navController) }
        composable("login") { LoginScreen(navController) }
        composable("profile") { ProfileScreen(navController) }
    }
}