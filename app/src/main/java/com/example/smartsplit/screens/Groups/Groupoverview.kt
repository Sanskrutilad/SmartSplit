package com.example.smartsplit.screens.Groups

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController


@Composable
fun NewGroupScreen(navController: NavController) {
    val primaryColor = Color(0xFF2196F3) // 🔵 Blue
    val accentColor = Color(0xFF2196F3)  // Same blue for icons/text
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            primaryColor.copy(alpha = 0.15f),
            Color.White
        )
    )

    var showLeaveDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush) // ✅ Gradient background
            .padding(24.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        // 🔝 Group Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back Button
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = accentColor
                )
            }

            // 🚪 Leave Group Button
            TextButton(onClick = { showLeaveDialog = true }) {
                Icon(
                    imageVector = Icons.Filled.ExitToApp,
                    contentDescription = "Leave Group",
                    tint = Color.Blue
                )
                Spacer(Modifier.width(4.dp))
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Flight,
                    contentDescription = "Group Icon",
                    tint = accentColor,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Text(
                text = "Newgroup",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            )
        }

        Spacer(Modifier.height(20.dp))

        // 🔘 Chips (Settle up, Trip Pass, Charts)
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            GroupChip("Settle up")
            GroupChip("Balance")
            GroupChip("Total")
        }

        // ⬇️ Push content to center
        Spacer(modifier = Modifier.weight(1f))

        // 🎯 Centered block
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(start = 70.dp)
        ) {
            Text(
                "You're the only one here!",
                color = Color.DarkGray,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { /* Add members */ },
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                shape = RoundedCornerShape(50)
            ) {
                Icon(Icons.Filled.PersonAdd, contentDescription = null, tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text("Add group members", color = Color.White)
            }

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = { /* Share link */ },
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.dp, accentColor)
            ) {
                Text("Share group link", color = accentColor)
            }
        }

        // ⬇️ Push Add Expense button to bottom
        Spacer(modifier = Modifier.weight(1f))

        // ➕ Add Expense Floating Button
        Button(
            onClick = { /* Add expense */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = accentColor),
            shape = RoundedCornerShape(50)
        ) {
            Icon(Icons.Filled.Receipt, contentDescription = null, tint = Color.White)
            Spacer(Modifier.width(8.dp))
            Text("Add expense", color = Color.White)
        }
    }

    // ⚠️ Leave Group Confirmation Dialog
    if (showLeaveDialog) {
        AlertDialog(
            onDismissRequest = { showLeaveDialog = false },
            title = { Text("Leave Group?") },
            text = { Text("Are you sure you want to leave this group? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLeaveDialog = false
                        // TODO: Handle leave group logic here
                    }
                ) {
                    Text("Leave", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }
}

@Composable
fun GroupChip(label: String) {
    OutlinedButton(
        onClick = { },
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.6f)),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
    ) {
        Text(label, color = Color.Black)
    }
}
