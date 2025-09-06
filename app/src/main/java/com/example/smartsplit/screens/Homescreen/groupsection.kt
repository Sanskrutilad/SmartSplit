package com.example.smartsplit.screens.Homescreen

import android.annotation.SuppressLint
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Work
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.smartsplit.Viewmodel.Group
import com.example.smartsplit.Viewmodel.GroupViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupSectionScreen(
    navController: NavHostController,
    viewModel: GroupViewModel = viewModel()
) {
    val myGroups by viewModel.myGroups.observeAsState(emptyList())

    LaunchedEffect(Unit) {
        viewModel.fetchMyGroups()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "SmartSplit",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0077CC)
                        )
                    )
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search",
                            tint = Color(0xFF0077CC)
                        )
                    }
                    IconButton(onClick = { navController.navigate("creategroup") }) {
                        Icon(
                            imageVector = Icons.Filled.Group,
                            contentDescription = "addgroup",
                            tint = Color(0xFF0077CC)
                        )
                    }
                    IconButton(onClick = { navController.navigate("notification") }) {
                        Icon(
                            imageVector = Icons.Filled.Notifications,
                            contentDescription = "addgroup",
                            tint = Color(0xFF0077CC)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Filled.Group, contentDescription = "Groups")},
                    label = { Text("Groups") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = {navController.navigate("friends") },
                    icon = { Icon(Icons.Filled.Person, contentDescription = "Friends") },
                    label = { Text("Friends") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("history") },
                    icon = { Icon(Icons.Filled.List, contentDescription = "Activity") },
                    label = { Text("History") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("profile") },
                    icon = { Icon(Icons.Filled.AccountCircle, contentDescription = "Account") },
                    label = { Text("Account") }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("addexpense") },
                containerColor = Color(0xFF0077CC),
                shape = RoundedCornerShape(50)
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add Expense",
                    tint = Color.White
                )
            }
        }
    ) { innerPadding ->
        if (myGroups.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFE6F2FF),
                                Color(0xFFCCE5FF)
                            )
                        )
                    )
                    .padding(innerPadding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "No groups yet",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF004C99)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Create your first group to split expenses with friends!",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 24.dp),
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = { navController.navigate("creategroup") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0077CC),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Text(text = "Create Group", fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(0.8f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF0077CC)
                    )
                ) {
                    Text(text = "Join Group with Code", fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "💡 Tip: Use groups to manage trips, events, and shared expenses.",
                    fontSize = 14.sp,
                    color = Color.DarkGray,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFE6F2FF),
                                Color(0xFFCCE5FF)
                            )
                        )
                    )
                    .padding(innerPadding)
                    .padding(8.dp)
            ) {
                items(myGroups) { group ->
                    GroupCard(group = group, onClick = {
                        navController.navigate("GroupOverview/${group.id}")
                    })
                }
            }
        }
    }
}

@Composable
fun GroupCard(group: Group, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circle with icon
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0077CC).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                val icon = when (group.type.lowercase()) {
                    "travel" -> Icons.Default.Flight
                    "work" -> Icons.Default.Work
                    "friends" -> Icons.Default.Person
                    "family" -> Icons.Default.Home
                    else -> Icons.Default.List
                }

                Icon(
                    imageVector = icon,
                    contentDescription = group.type,
                    tint = Color(0xFF0077CC),
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = group.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF222222)
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = group.type,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                )
                Text(
                    text = group.createdBy,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            Icon(
                imageVector = Icons.Default.ArrowForwardIos,
                contentDescription = "Go to group",
                tint = Color.Gray,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

