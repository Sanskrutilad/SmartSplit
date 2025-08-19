package com.example.smartsplit.screens.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.delay

@Composable
fun OnboardingScreen1(
    userName: String = "Sanskruti"
) {
    var showCard by remember { mutableStateOf(false) }

    // Trigger card after delay
    LaunchedEffect(Unit) {
        delay(800) // wait 0.8 sec before showing card
        showCard = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFEBE0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(30.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Welcome Text
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Hello $userName 👋",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF222222)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Track your shared bills and see who owes whom.",
                    fontSize = 17.sp,
                    color = Color(0xFF666666),
                    lineHeight = 22.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Animated Card (pops up after welcome)
            AnimatedVisibility(
                visible = showCard,
                enter = androidx.compose.animation.fadeIn(animationSpec = tween(600)) +
                        androidx.compose.animation.expandVertically(animationSpec = tween(600))
            ) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "💰 Balance Overview",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color(0xFF0D47A1)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🏖 Weekend Trip", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFFFEBEE), shape = RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Owe ₹750", color = Color(0xFFD32F2F), fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🍕 Pizza Night", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFE8F5E9), shape = RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Lent ₹300", color = Color(0xFF2E7D32), fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🎬 Movie Tickets", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFFFEBEE), shape = RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Owe ₹150", color = Color(0xFFD32F2F), fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(150.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(Color.Blue, RoundedCornerShape(50))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(Color.LightGray, RoundedCornerShape(50))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(Color.LightGray, RoundedCornerShape(50))
                )
            }

        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun OnboardingScreen1Preview() {
    OnboardingScreen1()
}
