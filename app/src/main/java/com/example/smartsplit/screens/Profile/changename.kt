package com.example.smartsplit.screens.Profile

import accentColor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun ChangeNameScreen(
    navController: NavController,
    onSaveClick: (String) -> Unit = {}
) {
    var name by remember { mutableStateOf("shrikant sharma") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White) // light mode background
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Back button (top-left)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            verticalAlignment = Alignment.Top
        ) {
            IconButton(onClick = {navController.popBackStack()}) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = accentColor
                )
            }
        }

        // Profile icon (use material icon instead of painterResource)
        Surface(
            modifier = Modifier.size(90.dp),
            shape = CircleShape,
            color = Color.LightGray.copy(alpha = 0.5f)
        ) {
            Icon(
                imageVector = Icons.Filled.AccountCircle, // ✅ Material icon
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
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color.Gray,
                unfocusedBorderColor = Color.LightGray,
                cursorColor = Color.Black,

            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Save button
        Button(
            onClick = { onSaveClick(name) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)) // Blue button
        ) {
            Text(
                text = "Save",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}


