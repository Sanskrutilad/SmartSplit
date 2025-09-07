package com.example.smartsplit.screens.Profile

// Jetpack Compose core
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast



import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

// Icons
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.text.style.TextAlign

// For previews
import androidx.compose.ui.tooling.preview.Preview

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.List
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.graphics.Brush
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.smartsplit.Viewmodel.LoginScreenViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController,   viewModel: LoginScreenViewModel = viewModel()) {
    val cardColor = MaterialTheme.colorScheme.surface
    val user by viewModel.user.observeAsState()

    LaunchedEffect(Unit) {
        viewModel.getUserData()
    }

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("Group")},
                    icon = { Icon(Icons.Filled.Group, contentDescription = "Groups") },
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
                    onClick = {navController.navigate("history") },
                    icon = { Icon(Icons.Filled.List, contentDescription = "Activity") },
                    label = { Text("History") }
                )
                NavigationBarItem(
                    selected = true,
                    onClick = {  },
                    icon = { Icon(Icons.Filled.AccountCircle, contentDescription = "Account") },
                    label = { Text("Account") }
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFE6F2FF),
                            Color(0xFFCCE5FF),
                            Color(0xFFB3DAFF)
                        )
                    )
                )
        ) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                // Profile Image + Name
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = Color.DarkGray,
                            modifier = Modifier.size(60.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "${user?.displayName}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color=Color(0xFF304674)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))

                // --- Profile Info Card ---
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.4f)),
                    colors = CardDefaults.cardColors(containerColor = cardColor)
                ) {
                    SettingsItem1(title = "Name", description = "${user?.displayName}") {navController.navigate("changeName")}
                    Divider()
                    SettingsItem1(title = "Phone Number", description = " ${user?.phone}") {navController.navigate("changephone")}
                    Divider()
                    SettingsItem1(title = "Email", description = "${user?.email}") { navController.navigate("updateEmail") }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Spacer(modifier = Modifier.height(16.dp))

                // --- Preferences Title ---
                Text(
                    text = "Preferences",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.4f)),
                    colors = CardDefaults.cardColors(containerColor = cardColor)
                ) {
                    SettingsItem(title = "Notifications", description = "") {}
                    Divider()
                    SettingsItem(title = "Dark Mode", description = "On") {navController.navigate("darkMode")}
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- Logout ---

                Text(
                    text = "Logout",
                    color = Color.Red,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            FirebaseAuth.getInstance().signOut()
                            navController.navigate("Welcomscreen") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                        .padding(16.dp),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Keep your expenses clear and your groups stress-free.",
                    fontSize = 13.sp,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )



                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun SettingsItem(title: String, description: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 18.sp, color=Color(0xFF304674))
            if (description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = description, fontSize = 13.sp, color = Color.Gray,
                )
            }
        }
        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = Color.Gray
        )
    }
}

@Composable
fun SettingsItem1(title: String, description: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 16.sp, color=Color(0xFF304674))
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = description, fontSize = 16.sp,)
        }
        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = Color.Gray
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteAccount(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val context = LocalContext.current

    var showDeleteDialog by remember { mutableStateOf(false) }
    var passwordInput by remember { mutableStateOf("") }
    var showPasswordError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = currentUser?.email ?: "Unknown User",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { showDeleteDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
        ) {
            Text("Delete Account")
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Account?") },
            text = {
                Column {
                    Text("Enter your password to confirm account deletion.")
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = {
                            passwordInput = it
                            showPasswordError = false
                        },
                        label = { Text("Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        isError = showPasswordError,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (showPasswordError) {
                        Text(
                            text = "Password is required",
                            color = Color.Red,
                            fontSize = 12.sp
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (passwordInput.isBlank()) {
                        showPasswordError = true
                        return@TextButton
                    }

                    val user = FirebaseAuth.getInstance().currentUser
                    val email = user?.email
                    if (email.isNullOrBlank()) {
                        Toast.makeText(context, "No email found", Toast.LENGTH_LONG).show()
                        return@TextButton
                    }

                    val credential = EmailAuthProvider.getCredential(email, passwordInput)

                    // Re-authenticate
                    user.reauthenticate(credential)
                        .addOnSuccessListener {
                            Log.d("DeleteAccount", "✅ Re-authentication success")

                            // Delete Firestore user data
                            FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(user.uid)
                                .delete()
                                .addOnSuccessListener {
                                    Log.d("DeleteAccount", "✅ Firestore user deleted")

                                    // Delete Auth account
                                    user.delete()
                                        .addOnSuccessListener {
                                            Log.d("DeleteAccount", "✅ Auth account deleted")
                                            Toast.makeText(context, "Account deleted", Toast.LENGTH_SHORT).show()

                                            // Close dialog and navigate to login
                                            showDeleteDialog = false
                                            navController.navigate("login_screen") {
                                                popUpTo("delete_account_screen") { inclusive = true }
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e("DeleteAccount", "❌ Auth delete failed: ${e.message}", e)
                                            if (e is FirebaseAuthRecentLoginRequiredException) {
                                                Toast.makeText(
                                                    context,
                                                    "Please log in again and retry.",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    "Auth delete failed: ${e.message}",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        }
                                }
                                .addOnFailureListener { e ->
                                    Log.e("DeleteAccount", "❌ Firestore delete failed: ${e.message}", e)
                                    Toast.makeText(
                                        context,
                                        "Firestore delete failed: ${e.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                        }
                        .addOnFailureListener { e ->
                            Log.e("DeleteAccount", "❌ Re-authentication failed: ${e.message}", e)
                            Toast.makeText(
                                context,
                                "Re-authentication failed: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                }) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
