package com.example.smartsplit.screens.Homescreen

import android.annotation.SuppressLint
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
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

    val isDark = true

    val primaryColor = Color(0xFF2196F3)
    val accentColor = primaryColor
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            primaryColor.copy(alpha = 0.15f),
            Color.White
        )
    )

    val darkBackground = Color.Black
    val darkText = Color.White
    val darkCardBg = Color(0xFF1C1C1C)
    val darkButtonBg = Color.White
    val darkButtonText = Color.Black

    val currentTextColor = if (isDark) darkText else Color(0xFF004C99)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "SmartSplit",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) darkText else Color(0xFF0077CC)
                        )
                    )
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search",
                            tint = if (isDark) darkText else Color(0xFF0077CC)
                        )
                    }
                    IconButton(onClick = { navController.navigate("creategroup") }) {
                        Icon(
                            imageVector = Icons.Filled.Group,
                            contentDescription = "addgroup",
                            tint = if (isDark) darkText else Color(0xFF0077CC)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isDark) darkBackground else Color.Transparent
                )
            )
        },
        bottomBar = {
            NavigationBar(containerColor = if (isDark) darkBackground else Color.White) {
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Filled.Group, contentDescription = "Groups") },
                    label = { Text("Groups", color = currentTextColor) }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { },
                    icon = { Icon(Icons.Filled.Person, contentDescription = "Friends") },
                    label = { Text("Friends", color = currentTextColor) }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = {navController.navigate("history") },
                    icon = { Icon(Icons.Filled.List, contentDescription = "Activity") },
                    label = { Text("History", color = currentTextColor) }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("profile") },
                    icon = { Icon(Icons.Filled.AccountCircle, contentDescription = "Account") },
                    label = { Text("Account", color = currentTextColor) }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("addexpense") },
                containerColor = if (isDark) darkButtonBg else Color(0xFF0077CC),
                shape = RoundedCornerShape(50)
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add Expense",
                    tint = if (isDark) darkButtonText else Color.White
                )
            }
        }
    ) { innerPadding ->
        if (myGroups.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (isDark) {
                            Modifier.background(color = darkBackground)
                        } else {
                            Modifier.background(brush = gradientBrush)
                        }
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
                    color = currentTextColor
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Create your first group to split expenses with friends!",
                    fontSize = 16.sp,
                    color = if (isDark) darkText.copy(alpha = 0.7f) else Color.Gray,
                    modifier = Modifier.padding(horizontal = 24.dp),
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = { navController.navigate("creategroup") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDark) darkButtonBg else Color(0xFF0077CC),
                        contentColor = if (isDark) darkButtonText else Color.White
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
                        contentColor = if (isDark) darkText else Color(0xFF0077CC)
                    )
                ) {
                    Text(text = "Join Group with Code", fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "💡 Tip: Use groups to manage trips, events, and shared expenses.",
                    fontSize = 14.sp,
                    color = if (isDark) darkText.copy(alpha = 0.7f) else Color.DarkGray,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (isDark) {
                            Modifier.background(color = darkBackground)
                        } else {
                            Modifier.background(brush = gradientBrush)
                        }
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
    var isPressed by remember { mutableStateOf(false) }
    val isDark = true

    // Colors
    val lightCardBg = Color.White
    val darkCardBg = Color(0xFF1C1C1C)
    val lightPressedBg = Color(0xFFE3F2FD)
    val darkPressedBg = Color(0xFF2C2C2C)

    val textPrimaryLight = Color(0xFF222222)
    val textSecondaryLight = Color(0xFF666666)
    val textPrimaryDark = Color.White
    val textSecondaryDark = Color(0xFFAAAAAA)

    val accent = Color(0xFF0077CC)

    // Animate padding, color, and scale on press
    val animatedPadding by animateDpAsState(targetValue = if (isPressed) 4.dp else 12.dp)
    val animatedColor by animateColorAsState(
        targetValue = if (isPressed) {
            if (isDark) darkPressedBg else lightPressedBg
        } else {
            if (isDark) darkCardBg else lightCardBg
        }
    )
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.97f else 1f)
    val elevation by animateDpAsState(targetValue = if (isPressed) 12.dp else 6.dp)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .padding(horizontal = animatedPadding, vertical = 8.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                        onClick()
                    }
                )
            },
        colors = CardDefaults.cardColors(containerColor = animatedColor),
        elevation = CardDefaults.cardElevation(elevation),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val icon = when (group.type.lowercase()) {
                "home" -> Icons.Default.Home
                "trip" -> Icons.Default.Flight
                "work" -> Icons.Default.Work
                else -> Icons.Default.Flight
            }
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .border(2.dp, accent, CircleShape)
                    .background( Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(20.dp))

            Column {
                Text(
                    text = group.name,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) textPrimaryDark else textPrimaryLight,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = group.type,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isDark) textSecondaryDark else textSecondaryLight
                )
            }
        }
    }
}
