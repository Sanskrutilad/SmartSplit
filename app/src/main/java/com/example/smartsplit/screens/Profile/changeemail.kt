package com.example.smartsplit.screens.Profile

import androidx.compose.foundation.shape.RoundedCornerShape

import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateEmailScreen(
    email: String,
    onEmailChange: (String) -> Unit,
    onBack: () -> Unit,
    onNext: (String) -> Unit
) {
    val isValid = remember(email) {
        Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
    }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black
                )
            )
        },
        bottomBar = {
            // Bottom “Next” button
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(Color.Black)
                    .padding(16.dp)
            ) {
                Button(
                    onClick = { onNext(email.trim()) },
                    enabled = isValid,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Next")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Update Email Address",
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "This will change the email address you use\n" +
                        "to log in. In the future, you may need to\n" +
                        "choose email login instead of signing in with\n" +
                        "Apple, Facebook, or Google.",
                color = Color(0xFFB0B0B0),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            // Email input chip-like field
            TextField(
                value = email,
                onValueChange = onEmailChange,
                singleLine = true,
                placeholder = { Text("name@example.com") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                trailingIcon = {
                    if (email.isNotEmpty()) {
                        IconButton(onClick = { onEmailChange("") }) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Clear"
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF1E1E1E),
                    unfocusedContainerColor = Color(0xFF1E1E1E),
                    disabledContainerColor = Color(0xFF1E1E1E),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    cursorColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedPlaceholderColor = Color(0xFF8E8E93),
                    unfocusedPlaceholderColor = Color(0xFF8E8E93),
                    focusedTrailingIconColor = Color(0xFF8E8E93),
                    unfocusedTrailingIconColor = Color(0xFF8E8E93)
                )
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0x000000)
@Composable
private fun UpdateEmailScreenPreview() {
    var email by remember { mutableStateOf("sharmashri2004@gmail.com") }
    MaterialTheme(colorScheme = darkColorScheme()) {
        UpdateEmailScreen(
            email = email,
            onEmailChange = { email = it },
            onBack = {},
            onNext = {}
        )
    }
}
