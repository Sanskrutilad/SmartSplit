package com.example.smartsplit.screens.Groups

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController

val primaryColor = Color(0xFF2196F3)
val accentColor = Color(0xFF2196F3)
val gradientBrush = Brush.verticalGradient(
    colors = listOf(
        primaryColor.copy(alpha = 0.15f),
        Color.White
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(navController: NavController? = null) {
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var paidBy by remember { mutableStateOf("You") }
    var splitBy by remember { mutableStateOf("Equally") }
    var withGroup by remember { mutableStateOf("All of Trip") }

    var showSaveBtn by remember { mutableStateOf(false) }

    // Dialog state
    var showPaidByDialog by remember { mutableStateOf(false) }
    var showSplitDialog by remember { mutableStateOf(false) }
    var showGroupDialog by remember { mutableStateOf(false) }

    // Animate button visibility
    LaunchedEffect(description, amount) {
        showSaveBtn = description.isNotBlank() && amount.isNotBlank()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush)
            .padding(24.dp),
        verticalArrangement = Arrangement.Top
    ) {
        // Back Arrow
        IconButton(onClick = { navController?.popBackStack() }) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = accentColor
            )
        }

        Spacer(Modifier.height(12.dp))

        // Title
        Text(
            text = "Add expense",
            style = MaterialTheme.typography.headlineSmall.copy(
                color = accentColor,
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(Modifier.height(24.dp))

        // With you and group section
        ElevatedCard(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("With you and:")
                Spacer(modifier = Modifier.width(8.dp))
                AssistChip(
                    onClick = { showGroupDialog = true },
                    label = { Text(withGroup) },
                    leadingIcon = {
                        Icon(Icons.Default.Flight, contentDescription = null, tint = primaryColor)
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Description input
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            placeholder = { Text("e.g. Dinner at café") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            leadingIcon = {
                Icon(Icons.Default.Receipt, contentDescription = null, tint = primaryColor)
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = primaryColor,
                cursorColor = primaryColor
            )
        )

        Spacer(Modifier.height(16.dp))

        // Amount input
        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Amount") },
            placeholder = { Text("0.00") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            leadingIcon = {
                Text("₹", color = primaryColor, style = MaterialTheme.typography.titleMedium)
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = primaryColor,
                cursorColor = primaryColor
            )
        )

        Spacer(Modifier.height(36.dp))

        // Paid by & Split row
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            ),
            border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 30.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Paid by", color = Color.Black.copy(alpha = 0.8f))
                AssistChip(
                    onClick = { showPaidByDialog = true },
                    label = { Text(paidBy) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = primaryColor.copy(alpha = 0.1f)
                    )
                )
                Text("and split", color = Color.Black.copy(alpha = 0.8f))
                AssistChip(
                    onClick = { showSplitDialog = true },
                    label = { Text(splitBy) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = primaryColor.copy(alpha = 0.1f)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Animated Save button
        AnimatedVisibility(visible = showSaveBtn) {
            val scale by animateFloatAsState(targetValue = if (showSaveBtn) 1f else 0.8f)

            Button(
                onClick = { /* Save expense */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .graphicsLayer(scaleX = scale, scaleY = scale),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
            ) {
                Text(
                    "Save Expense",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }

    // 🔹 Paid By Dialog
    if (showPaidByDialog) {
        AlertDialog(
            onDismissRequest = { showPaidByDialog = false },
            title = { Text("Select payer") },
            text = {
                Column {
                    listOf("You", "Alice", "Bob").forEach { option ->
                        TextButton(onClick = {
                            paidBy = option
                            showPaidByDialog = false
                        }) { Text(option) }
                    }
                }
            },
            confirmButton = {}
        )
    }

    // 🔹 Split Method Dialog
    if (showSplitDialog) {
        AlertDialog(
            onDismissRequest = { showSplitDialog = false },
            title = { Text("Select split method") },
            text = {
                Column {
                    listOf("Equally", "By shares", "By percentage").forEach { option ->
                        TextButton(onClick = {
                            splitBy = option
                            showSplitDialog = false
                        }) { Text(option) }
                    }
                }
            },
            confirmButton = {}
        )
    }

    // 🔹 Group Dialog
    if (showGroupDialog) {
        AlertDialog(
            onDismissRequest = { showGroupDialog = false },
            title = { Text("Select group") },
            text = {
                Column {
                    listOf("All of Trip", "Roommates", "Friends").forEach { option ->
                        TextButton(onClick = {
                            withGroup = option
                            showGroupDialog = false
                        }) { Text(option) }
                    }
                }
            },
            confirmButton = {}
        )
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AddExpenseScreenPreview() {
    AddExpenseScreen()
}
