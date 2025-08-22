package com.example.smartsplit.screens.Profile
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
@Composable
fun DarkModeSettingsScreen(
    selectedOption: String = "On",
    onOptionSelected: (String) -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val options = listOf("Automatic", "On", "Off")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White) // light mode bg
            .padding(16.dp)
    ) {
        // Top bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.Black,
                modifier = Modifier
                    .size(28.dp)
                    .clickable { onBackClick() }
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Dark mode",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }

        // Radio options
        options.forEach { option ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF98bad5)) // light gray card
                    .clickable { onOptionSelected(option) }
                    .padding(horizontal = 16.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedOption == option,
                    onClick = { onOptionSelected(option) },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = Color(0xFF1976D2), // blue highlight
                        unselectedColor = Color.Gray
                    )
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = option,
                    fontSize = 16.sp,
                    color = Color.Black
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignupScreenPreview5() {
    MaterialTheme {
        DarkModeSettingsScreen()
    }
}
