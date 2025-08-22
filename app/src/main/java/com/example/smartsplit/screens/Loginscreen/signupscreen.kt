package com.example.smartsplit.screens.Loginscreen


import android.util.Log
import androidx.compose.runtime.Composable

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.smartsplit.Viewmodel.LoginScreenViewModel

@Composable
fun SignupScreen(
    navController: NavHostController,
    viewModel: LoginScreenViewModel = viewModel()
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var countryCode by remember { mutableStateOf("+91") }
    var currency by remember { mutableStateOf("INR (₹)") }
    var passwordVisible by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf(false) }
    val showMoreFields = fullName.isNotEmpty()

    val primaryColor = Color(0xFF2196F3) // 🔵 Blue
    val accentColor = primaryColor

    // 🌈 Gradient background
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            primaryColor.copy(alpha = 0.15f),
            Color.White
        )
    )

    val loading by viewModel.loading.observeAsState(false)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush) // ✅ Gradient applied
            .padding(24.dp)
    ) {
        // 🔙 Back Arrow
        IconButton(onClick = { navController.popBackStack() }) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = accentColor
            )
        }

        Spacer(Modifier.height(12.dp))

        // 📝 Title
        Text(
            text = "Create your account",
            style = MaterialTheme.typography.headlineSmall.copy(
                color = accentColor,
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = "Sign up to get started with your expenses.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(Modifier.height(24.dp))

        // 👤 Full Name with photo icon
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Full name") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.width(8.dp))

            // 📷 Circle Camera Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(accentColor)
                    .clickable { Log.d("Signup", "Camera clicked for profile photo") },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Add Photo",
                    tint = Color.White
                )
            }
        }

        // ✨ Animate rest of the fields when full name is typed
        AnimatedVisibility(
            visible = showMoreFields,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column {
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email address") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(Modifier.height(12.dp))

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

                Text(
                    text = "Must be at least 8 characters",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(Modifier.height(12.dp))

                // 📱 Country code + Phone
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = countryCode,
                        onValueChange = {},
                        label = { Text("Code") },
                        modifier = Modifier.width(100.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    OutlinedTextField(
                        value = phone,
                        onValueChange = {
                            if (it.length <= 10 && it.all { ch -> ch.isDigit() }) {
                                phone = it
                                phoneError = false
                            }
                        },
                        label = { Text("Phone number") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(Modifier.height(16.dp))

                // 💰 Currency Selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Your default currency is $currency.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "Change »",
                        color = accentColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            Log.d("Signup", "Currency picker clicked")
                        }
                    )
                }

                Spacer(Modifier.height(20.dp))

                Text(
                    text = "By signing up, you agree to our Terms of Use and Privacy Policy.",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(Modifier.height(16.dp))

                // 🔵 Sign Up Button
                Button(
                    onClick = {
                        Log.d(
                            "Signup",
                            "Sign Up button clicked with email=$email, passwordLength=${password.length}"
                        )

                        if (email.isNotBlank() && password.length >= 8) {
                            Log.d("Signup", "Inputs valid → calling Firebase signup")
                            viewModel.createUserWithEmailAndPassword(email, password) {
                                Log.d("Signup", "Signup success → navigating to onboardscreen1")
                                navController.navigate("onboardscreen1")
                            }
                        } else {
                            if (email.isBlank()) {
                                Log.e("Signup", "Signup failed → Email is blank")
                            }
                            if (password.length < 8) {
                                Log.e(
                                    "Signup",
                                    "Signup failed → Password too short (len=${password.length})"
                                )
                            }
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
                        Text("Sign Up", color = Color.White)
                    }
                }
            }
        }
    }
}
