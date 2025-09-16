package com.example.smartsplit.screens.store

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.material3.AlertDialogDefaults.containerColor
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListDetailScreen(navController : NavController) {
    val lightPrimaryColor = Color(0xFF2196F3)
    val lightGradientBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFFE6F2FF),
            Color(0xFFCCE5FF),
            Color(0xFFB3DAFF)
        )
    )
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New list") },
                navigationIcon = {
                    IconButton(onClick = {navController.navigate("storelist")}) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = com.example.smartsplit.screens.store.lightPrimaryColor,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {navController.navigate("additem")},
                containerColor = com.example.smartsplit.screens.store.lightPrimaryColor
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Item")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize().background(lightGradientBrush)
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Text("This list is empty. Tap to add some items")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemsScreen(navController: NavController) {
    var text by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    // Auto-focus textfield when entering screen → show keyboard
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    // Local colors and gradient defined here
    val lightPrimaryColor = Color(0xFF2196F3)
    val lightGradientBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFFE6F2FF),
            Color(0xFFCCE5FF),
            Color(0xFFB3DAFF)
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    TextField(
                        value = text,
                        onValueChange = { text = it },
                        placeholder = { Text("Add item" , fontSize = 20.sp ,color = Color.White)  },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        singleLine = true,
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = lightPrimaryColor
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: voice input */ }) {
                        Icon(
                            Icons.Default.Mic,
                            contentDescription = "Voice",
                            tint = lightPrimaryColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = lightPrimaryColor,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(lightGradientBrush)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(listOf("milk", "bread", "eggs", "butter", "cheese", "toilet paper", "chicken")) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "+",
                            color = lightPrimaryColor,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(end = 8.dp),
                            fontSize = 28.sp
                        )
                        Spacer(Modifier.width(45.dp))
                        Text(item, color = Color.Black , fontSize = 20.sp)
                    }
                    Divider()
                }
            }
        }
    }
}
