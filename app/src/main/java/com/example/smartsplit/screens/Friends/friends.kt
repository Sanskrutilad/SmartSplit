package com.example.smartsplit.screens.Friends

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.smartsplit.Viewmodel.FriendsViewModel
import com.example.smartsplit.screens.history.HistoryItemCard


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen( navController: NavController, viewModel: FriendsViewModel = viewModel()) {

    val primaryColor = Color(0xFF2196F3)
    val accentColor = Color(0xFF2196F3)

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            primaryColor.copy(alpha = 0.15f),
            Color.White
        )
    )
    val friends by viewModel.friends.collectAsState()
    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("group") },
                    icon = { Icon(Icons.Default.Group, contentDescription = "Groups") },
                    label = { Text("Groups") }
                )
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Friends") },
                    label = { Text("Friends") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("history")},
                    icon = { Icon(Icons.Default.List, contentDescription = "History") },
                    label = { Text("History") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("profile")},
                    icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Account") },
                    label = { Text("Account") }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("addFriend") },
                containerColor = primaryColor
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add friend", tint = Color.White)
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientBrush)
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = accentColor,
                        modifier=Modifier.padding(start = 7.dp)
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Friends",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = accentColor,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier=Modifier.padding(start = 17.dp)
                )
            }
            Spacer(Modifier.height(8.dp))

            Spacer(Modifier.height(12.dp))

            if (friends.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = "No friends",
                            tint = accentColor,
                            modifier = Modifier.size(96.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "No friends yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray
                        )
                    }
                }
            }
            else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(friends) { friend ->
                        FriendCard(friendName = friend.name, accentColor = accentColor)
                    }
                }
            }
        }
    }
}

@Composable
fun FriendCard(friendName: String, accentColor: Color) {
    // Change parameter from friendName to friend if you want to access other fields
    // Or keep as is and just pass the name
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(accentColor.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = accentColor
                )
            }

            Spacer(Modifier.width(16.dp))

            Text(
                text = friendName, // This will now show the display name
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = Color.Black
            )

            Spacer(Modifier.weight(1f))

            IconButton(onClick = { /* TODO: Settle up */ }) {
                Icon(Icons.Default.Receipt, contentDescription = "Settle up", tint = accentColor)
            }
        }
    }
}

@Composable
fun AddFriendScreen(
    navController: NavController,
    viewModel: FriendsViewModel = viewModel()
) {
    val primaryColor = Color(0xFF2196F3)
    val accentColor = Color(0xFF2196F3)

    var email by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(primaryColor.copy(alpha = 0.15f), Color.White)
    )

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientBrush)
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(5.dp)
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = accentColor)
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Add Friends",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = accentColor,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Friend's Email ID") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(Icons.Default.Email, contentDescription = "Email Icon", tint = accentColor)
                }
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.sendFriendRequest(email) { success, msg ->
                        message = msg
                        if (success) {
                            navController.popBackStack()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
            ) {
                Text("Send Request", color = Color.White)
            }




            message?.let {
                Spacer(Modifier.height(16.dp))
                Text(it, color = if (it.contains("success")) Color.Green else Color.Red)
            }
        }
    }
}
