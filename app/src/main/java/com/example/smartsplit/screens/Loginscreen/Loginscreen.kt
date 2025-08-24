package com.example.smartsplit.screens.Loginscreen

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavHostController
import com.example.smartsplit.Viewmodel.LoginScreenViewModel

@Composable
fun LoginScreen(
    navController: NavHostController,
    viewModel: LoginScreenViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val loading by viewModel.loading.observeAsState(false)
    val primaryColor = Color(0xFF2196F3) // 🔵 Blue
    val accentColor = primaryColor

    // 🌈 Gradient background
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            primaryColor.copy(alpha = 0.15f),
            Color.White
        )
    )
    Column(
        modifier = Modifier
            .fillMaxSize().background(gradientBrush)
            .padding(24.dp),
        verticalArrangement = Arrangement.Top
    ) {
        // Back Arrow
        IconButton(onClick = { navController.popBackStack() }) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = accentColor
            )
        }

        Spacer(Modifier.height(12.dp))

        // Title
        Text(
            text = "Log in",
            style = MaterialTheme.typography.headlineSmall.copy(
                color = accentColor,
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = "Enter your email and password to continue.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(Modifier.height(24.dp))

        // Email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email address") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(Modifier.height(12.dp))

        // Password
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val icon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = icon, contentDescription = null)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(Modifier.height(8.dp))

        // Forgot password
        Text(
            text = "Forgot your password?",
            color = accentColor,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .align(Alignment.End)
                .clickable {
                    Log.d("Login", "Forgot password clicked for email=$email")
                    // TODO: handle password reset
                }
        )

        Spacer(Modifier.height(24.dp))

        // Log in Button
        Button(
            onClick = {
                Log.d("Login", "Login button clicked with email=$email, passwordLength=${password.length}")
                if (email.isNotBlank() && password.isNotBlank()) {
                    Log.d("Login", "Inputs valid → calling Firebase signIn")
                    viewModel.signInWithEmailAndPassword(email, password) {
                        Log.d("Login", "Login success → navigating to Home")
                        navController.navigate("Group") {
                            popUpTo("loginscreen") { inclusive = true }
                        }
                    }
                } else {
                    if (email.isBlank()) Log.e("Login", "Login failed → Email is blank")
                    if (password.isBlank()) Log.e("Login", "Login failed → Password is blank")
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = accentColor),
            shape = RoundedCornerShape(12.dp),
            enabled = !loading
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Log in", color = Color.White)
            }
        }
    }
}
