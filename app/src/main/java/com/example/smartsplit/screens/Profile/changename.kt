package com.example.smartsplit.screens.Profile

import accentColor
import android.R.attr.onClick
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.smartsplit.Viewmodel.LoginScreenViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangeNameScreen(
    navController: NavController,
    viewModel: LoginScreenViewModel = viewModel()
) {
    val user by viewModel.user.observeAsState()

    LaunchedEffect(Unit) {
        viewModel.getUserData()
    }
    val primaryColor = Color(0xFF2196F3) // 🔵 Blue
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            primaryColor.copy(alpha = 0.15f),
            Color.White
        )
    )
    var name by remember { mutableStateOf("") }
    LaunchedEffect(user) {
        name = user?.displayName?.takeIf { it.isNotBlank() } ?: ""
    }




    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = gradientBrush)
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Back button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            verticalAlignment = Alignment.Top
        ) {
            IconButton(onClick = { navController.popBackStack() },
                modifier = Modifier.padding(top = 48.dp)) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = accentColor
                )
            }
        }

        // Profile icon
        Surface(
            modifier = Modifier.size(90.dp),
            shape = CircleShape,
            color = Color.LightGray.copy(alpha = 0.5f)
        ) {
            Icon(
                imageVector = Icons.Filled.AccountCircle,
                contentDescription = "Profile",
                tint = Color.DarkGray,
                modifier = Modifier.fillMaxSize().padding(8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Title
        Text(
            text = "Change Name",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Name input
        // Name input using UpdateEmailScreen style
        TextField(
            value = name,
            onValueChange = { name = it },
            singleLine = true,
            placeholder = { Text("Enter your name", color = Color.Gray) },
            trailingIcon = {
                if (name.isNotEmpty()) {
                    IconButton(onClick = { name = "" }) {
                        Icon(Icons.Filled.Close, contentDescription = "Clear", tint = primaryColor)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            shape = RoundedCornerShape(28.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White.copy(alpha = 0.2f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.1f),
                cursorColor = primaryColor,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            )
        )

        Spacer(modifier = Modifier.height(24.dp))
        // Save button
        Button(
            onClick = {
                val updated = user!!.copy(
                    displayName = name,
                )
                viewModel.updateUserData(updated)
                navController.popBackStack()
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5))
        ) {
            Text("Save", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

