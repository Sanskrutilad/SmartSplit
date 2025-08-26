package com.example.smartsplit.screens.Groups

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController

val primaryBlue = Color(0xFF709DD0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(navController: NavController? = null) {
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var paidBy by remember { mutableStateOf("You") }
    var splitBy by remember { mutableStateOf("Equally") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Expense") },
                navigationIcon = {
                    IconButton(onClick = { navController?.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Save */ }) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = primaryBlue,
                    actionIconContentColor = primaryBlue
                )
            )
        },
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // With you and group section
            ElevatedCard(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("With you and:")
                    Spacer(modifier = Modifier.width(8.dp))
                    AssistChip(
                        onClick = {},
                        label = { Text("All of Trip") },
                        leadingIcon = {
                            Icon(Icons.Default.Flight, contentDescription = null, tint = primaryBlue)
                        },
                        colors = AssistChipDefaults.assistChipColors(containerColor = primaryBlue.copy(alpha = 0.1f))
                    )
                }
            }

            // Description input
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                placeholder = { Text("e.g. Dinner at café") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(Icons.Default.Receipt, contentDescription = null, tint = primaryBlue)
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryBlue,
                    cursorColor = primaryBlue
                )
            )

            // Amount input
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount") },
                placeholder = { Text("0.00") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Text("₹", color = primaryBlue, style = MaterialTheme.typography.titleMedium)
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryBlue,
                    cursorColor = primaryBlue
                )
            )

            // Paid by & Split row
            ElevatedCard(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Paid by")
                    AssistChip(
                        onClick = { /* Change payer */ },
                        label = { Text(paidBy) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = primaryBlue.copy(alpha = 0.1f))
                    )
                    Text("and split")
                    AssistChip(
                        onClick = { /* Change split method */ },
                        label = { Text(splitBy) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = primaryBlue.copy(alpha = 0.1f))
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Save button
            Button(
                onClick = { /* Save expense */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryBlue)
            ) {
                Text("Save Expense", color = Color.White, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AddExpenseScreenPreview() {
    AddExpenseScreen()
}
