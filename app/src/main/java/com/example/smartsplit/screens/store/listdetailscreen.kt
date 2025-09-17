package com.example.smartsplit.screens.store

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.smartsplit.Viewmodel.ListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListDetailScreen(navController: NavController, listId: String) {
    val viewModel: ListViewModel = viewModel()
    val lists by viewModel.lists.collectAsState()
    val list = lists.find { it.id == listId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(list?.title ?: "New list") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = lightPrimaryColor,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("additem/$listId") },
                containerColor = lightPrimaryColor
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Item")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(lightGradientBrush)
                .padding(padding)
        ) {
            if (list == null || list.items.isEmpty()) {
                Text(
                    "This list is empty. Tap + to add some items",
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Column(Modifier.fillMaxSize()) {
                    LazyColumn(
                        modifier = Modifier.weight(1f)
                    ) {
                        items(list.items) { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    item.name,
                                    color = Color.Black,
                                    fontSize = 20.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                item.price?.let {
                                    Text(
                                        "₹$it",
                                        color = Color.Gray,
                                        fontSize = 18.sp,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                }
                            }
                            Divider()
                        }
                    }

                    // ✅ Show total
                    val total = list.items.sumOf { (it.price ?: 0.0) * it.quantity }
                    if (total > 0) {
                        Text(
                            "Total: ₹$total",
                            fontSize = 22.sp,
                            color = lightPrimaryColor,
                            modifier = Modifier
                                .align(Alignment.Start)
                                .padding(16.dp)
                        )
                    }


                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemsScreen(navController: NavController, listId: String, viewModel: ListViewModel = viewModel()) {
    val lists by viewModel.lists.collectAsState()
    val list = lists.find { it.id == listId }

    var showItemDialog by remember { mutableStateOf(false) }
    var showAmountDialog by remember { mutableStateOf(false) }
    var tempItemName by remember { mutableStateOf("") }
    var tempAmount by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(list?.title ?: "List") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showItemDialog = true },
                containerColor = lightPrimaryColor
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Item")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(lightGradientBrush)
        ) {
            if (list == null) {
                Text("List not found", modifier = Modifier.align(Alignment.Center))
            } else {
                Column(Modifier.fillMaxSize()) {
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(list.items) { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    item.name,
                                    color = Color.Black,
                                    fontSize = 20.sp,
                                    modifier = Modifier.weight(1f)
                                )

                                // ✅ Show price × quantity
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (item.price != null) {
                                        Text(
                                            "₹${item.price}",
                                            color = Color.Gray,
                                            fontSize = 18.sp,
                                            modifier = Modifier.padding(end = 8.dp)
                                        )
                                    }

                                    // ✅ Show quantity
                                    Text("x${item.quantity}", color = Color.Black, fontSize = 16.sp)

                                    // ✅ Increase quantity button
                                    IconButton(onClick = { viewModel.increaseQuantity(list.id, item) }) {
                                        Icon(Icons.Default.Add, contentDescription = "Increase Quantity")
                                    }

                                    // ✅ Delete item
                                    IconButton(onClick = { viewModel.removeItem(list.id, item) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Remove")
                                    }
                                }
                            }
                            Divider()
                        }
                    }


                    // ✅ Show total
                    val total = list.items.sumOf { (it.price ?: 0.0) * it.quantity }
                    if (total > 0) {
                        Text(
                            "Total: ₹$total",
                            fontSize = 22.sp,
                            color = lightPrimaryColor,
                            modifier = Modifier
                                .align(Alignment.Start)
                                .padding(16.dp)
                        )
                    }

                }
            }
        }
    }

    // ✅ Item Name Dialog
    if (showItemDialog) {
        AlertDialog(
            onDismissRequest = { showItemDialog = false },
            title = { Text("Enter Item Name") },
            text = {
                OutlinedTextField(
                    value = tempItemName,
                    onValueChange = { tempItemName = it },
                    placeholder = { Text("Item") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (tempItemName.isNotBlank()) {
                        showItemDialog = false
                        showAmountDialog = true
                    }
                }) { Text("Next") }
            },
            dismissButton = {
                TextButton(onClick = { showItemDialog = false }) { Text("Cancel") }
            }
        )
    }

    // ✅ Amount Dialog
    if (showAmountDialog) {
        AlertDialog(
            onDismissRequest = { showAmountDialog = false },
            title = { Text("Enter Amount") },
            text = {
                OutlinedTextField(
                    value = tempAmount,
                    onValueChange = { tempAmount = it },
                    placeholder = { Text("Price (optional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (list != null && tempItemName.isNotBlank()) {
                        val price = tempAmount.toDoubleOrNull()
                        viewModel.addItem(list.id, tempItemName.trim(), price)
                        tempItemName = ""
                        tempAmount = ""
                        showAmountDialog = false
                    }
                }) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = { showAmountDialog = false }) { Text("Cancel") }
            }
        )
    }
}
