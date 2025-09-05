package com.example.smartsplit.screens.Groups

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.smartsplit.Viewmodel.ExpenseViewModel
import com.example.smartsplit.Viewmodel.Friend
import com.example.smartsplit.Viewmodel.FriendsViewModel
import com.example.smartsplit.Viewmodel.Group
import com.example.smartsplit.Viewmodel.GroupMember
import com.example.smartsplit.Viewmodel.GroupViewModel
import com.google.firebase.auth.FirebaseAuth

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
fun AddExpenseScreen(
    navController: NavController? = null,
    friendsViewModel: FriendsViewModel = viewModel(),
    groupViewModel: GroupViewModel = viewModel(),
    expenseViewModel: ExpenseViewModel = viewModel() // To post expense
) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var paidBy: String? by remember { mutableStateOf("You") }
    var splitBy by remember { mutableStateOf("Equally") }

    // selected target (friend OR group)
    var selectedFriend by remember { mutableStateOf<Friend?>(null) }
    var selectedGroup by remember { mutableStateOf<Group?>(null) }

    // Dialog state
    var showWithDialog by remember { mutableStateOf(false) }
    var showPaidByDialog by remember { mutableStateOf(false) }
    var showSplitDialog by remember { mutableStateOf(false) }

    // Load data
    val friends by friendsViewModel.friends.collectAsState()
    val groups by groupViewModel.myGroups.observeAsState(emptyList())
    LaunchedEffect(currentUserId) {
        friendsViewModel.fetchFriends(currentUserId)
        groupViewModel.fetchMyGroups()
    }

    var showSaveBtn by remember { mutableStateOf(false) }
    LaunchedEffect(description, amount) {
        showSaveBtn = description.isNotBlank() && amount.isNotBlank()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        IconButton(onClick = { navController?.popBackStack() }) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFF2196F3))
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text = "Add expense",
            style = MaterialTheme.typography.headlineSmall.copy(
                color = Color(0xFF2196F3),
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(Modifier.height(24.dp))

        // With who?
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("With you and:")
            Spacer(Modifier.width(8.dp))
            AssistChip(
                onClick = { showWithDialog = true },
                label = {
                    Text(
                        when {
                            selectedGroup != null -> "Group: ${selectedGroup!!.name}"
                            selectedFriend != null -> "Friend: ${selectedFriend!!.email}"
                            else -> "Choose"
                        }
                    )
                },
                leadingIcon = {
                    Icon(
                        if (selectedGroup != null) Icons.Default.Group else Icons.Default.Person,
                        contentDescription = null,
                        tint = Color(0xFF2196F3)
                    )
                }
            )
        }

        Spacer(Modifier.height(16.dp))

        // Description input
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        // Amount input
        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Amount") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))

        // Paid by & Split
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AssistChip(onClick = { showPaidByDialog = true }, label = { Text("Paid by: $paidBy") })
            AssistChip(onClick = { showSplitDialog = true }, label = { Text("Split: $splitBy") })
        }

        Spacer(modifier = Modifier.weight(1f))

        AnimatedVisibility(visible = showSaveBtn && (selectedFriend != null || selectedGroup != null)) {
            Button(
                onClick = {
                    expenseViewModel.addExpense(
                        description = description,
                        amount = amount.toDouble(),
                        paidBy = paidBy,
                        splitBy = splitBy,
                        groupId = selectedGroup?.id,
                        friendId = selectedFriend?.uid
                    )
                    navController?.popBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Expense", color = Color.White)
            }
        }
    }

    // 🔹 With (friend or group) dialog
    if (showWithDialog) {
        AlertDialog(
            onDismissRequest = { showWithDialog = false },
            title = { Text("Select Friend or Group") },
            text = {
                Column {
                    Text("Friends", fontWeight = FontWeight.Bold)
                    friends.forEach { f ->
                        TextButton(onClick = {
                            selectedFriend = f
                            selectedGroup = null
                            showWithDialog = false
                        }) { Text(f.email) }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Groups", fontWeight = FontWeight.Bold)
                    groups.forEach { g ->
                        TextButton(onClick = {
                            selectedGroup = g
                            selectedFriend = null
                            showWithDialog = false
                        }) { Text(g.name) }
                    }
                }
            },
            confirmButton = {}
        )
    }

    // Load group members if group is selected
    val groupMembers by groupViewModel.groupMembers.observeAsState(emptyList())

    LaunchedEffect(selectedGroup) {
        selectedGroup?.let {
            groupViewModel.fetchGroupMembers(it.id) // fetch when group selected
        }
    }

// 🔹 Paid By dialog
    if (showPaidByDialog) {
        AlertDialog(
            onDismissRequest = { showPaidByDialog = false },
            title = { Text("Select Payer") },
            text = {
                Column {
                    if (selectedGroup != null) {
                        // Group case
                        groupMembers.forEach { member ->
                            TextButton(onClick = {
                                paidBy = if (member.uid == currentUserId) "You" else member.email
                                showPaidByDialog = false
                            }) { if (member.uid == currentUserId) "You" else member.email?.let { Text(it) } }
                        }
                    } else if (selectedFriend != null) {
                        // Friend case
                        listOf("You", selectedFriend!!.email).forEach { option ->
                            TextButton(onClick = {
                                paidBy = option
                                showPaidByDialog = false
                            }) { Text(option) }
                        }
                    } else {
                        Text("Please select a friend or group first")
                    }
                }
            },
            confirmButton = {}
        )
    }


    // For custom split inputs (shares or percentages)
    // Keep track of split inputs
    var splitInputs by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

// 🔹 Split method dialog
    if (showSplitDialog) {
        AlertDialog(
            onDismissRequest = { showSplitDialog = false },
            title = { Text("Select split method") },
            text = {
                Column {
                    listOf("Equally", "By shares", "By percentage").forEach { option ->
                        TextButton(onClick = {
                            splitBy = option
                            if (option == "Equally") {
                                // Reset inputs since no manual entry needed
                                splitInputs = emptyMap()
                                showSplitDialog = false
                            }
                        }) { Text(option) }
                    }

                    // If shares/percentage selected → show inputs for members
                    if (splitBy == "By shares" || splitBy == "By percentage") {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = if (splitBy == "By shares") "Enter shares" else "Enter percentages",
                            fontWeight = FontWeight.Bold
                        )

                        val membersList = when {
                            selectedGroup != null -> groupMembers
                            selectedFriend != null -> listOf(
                                GroupMember(uid = currentUserId, email = "You", accepted = true),
                                GroupMember(uid = selectedFriend!!.uid, email = selectedFriend!!.email, accepted = true)
                            )
                            else -> emptyList()
                        }

                        membersList.forEach { member ->
                            OutlinedTextField(
                                value = splitInputs[member.uid] ?: "",
                                onValueChange = { value ->
                                    splitInputs = splitInputs.toMutableMap().apply { put(member.uid, value) }
                                },
                                label = {
                                    if (member.uid == currentUserId) "You"
                                    else member.email?.let {
                                        Text(
                                            it
                                        )
                                    }
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                if (splitBy == "By shares" || splitBy == "By percentage") {
                    TextButton(onClick = { showSplitDialog = false }) {
                        Text("Done")
                    }
                }
            }
        )
    }

}



@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AddExpenseScreenPreview() {
    AddExpenseScreen()
}
