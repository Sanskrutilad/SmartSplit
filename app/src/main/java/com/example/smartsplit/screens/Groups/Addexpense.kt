package com.example.smartsplit.screens.Groups

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.smartsplit.Viewmodel.ExpenseViewModel
import com.example.smartsplit.Viewmodel.Friend
import com.example.smartsplit.Viewmodel.FriendsViewModel
import com.example.smartsplit.Viewmodel.Group
import com.example.smartsplit.Viewmodel.GroupMember
import com.example.smartsplit.Viewmodel.GroupViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlin.math.abs

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
    expenseViewModel: ExpenseViewModel = viewModel()
) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var splitBy by remember { mutableStateOf("Equally") }

    // store payer correctly
    var paidByUid by remember { mutableStateOf(currentUserId) }
    var paidByLabel by remember { mutableStateOf("You") }

    // selected target (friend OR group)
    var selectedFriend by remember { mutableStateOf<Friend?>(null) }
    var selectedGroup by remember { mutableStateOf<Group?>(null) }

    // dialogs
    var showWithDialog by remember { mutableStateOf(false) }
    var showPaidByDialog by remember { mutableStateOf(false) }
    var showSplitDialog by remember { mutableStateOf(false) }

    // friends & groups
    val friends by friendsViewModel.friends.collectAsState()
    val groups by groupViewModel.myGroups.observeAsState(emptyList())
    LaunchedEffect(currentUserId) {
        friendsViewModel.fetchFriends(currentUserId)
        groupViewModel.fetchMyGroups()
    }

    // group members MUST be above any usage
    val groupMembers by groupViewModel.groupMembers.observeAsState(emptyList())
    LaunchedEffect(selectedGroup) {
        selectedGroup?.let { groupViewModel.fetchGroupMembers(it.id) }
    }

    // custom split inputs MUST be above usage
    var splitInputs by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

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
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = primaryColor)
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text = "Add expense",
            style = MaterialTheme.typography.headlineSmall.copy(
                color = primaryColor,
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(Modifier.height(24.dp))

        // With who?
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("With you and:")
            Spacer(Modifier.width(8.dp))
            AssistChip(
                onClick = {showWithDialog = true  },
                label = {
                    Text(
                        when {
                            selectedGroup != null -> "Group: ${selectedGroup!!.name}"
                            selectedFriend != null -> "Friend: ${selectedFriend!!.name}" // use name
                            else -> "Choose"
                        }
                    )

                },
                leadingIcon = {
                    Icon(
                        if (selectedGroup != null) Icons.Default.Group else Icons.Default.Person,
                        contentDescription = null,
                        tint = primaryColor
                    )
                }
            )
        }

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Amount") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AssistChip(onClick = { showPaidByDialog = true }, label = { Text("Paid by: $paidByLabel") })
            AssistChip(onClick = { showSplitDialog = true }, label = { Text("Split: $splitBy") })
        }

        Spacer(modifier = Modifier.weight(1f))

        AnimatedVisibility(visible = showSaveBtn && (selectedFriend != null || selectedGroup != null)) {
            Button(
                onClick = {
                    val amt = amount.toDoubleOrNull() ?: return@Button
                    val membersList: List<GroupMember> = when {
                        selectedGroup != null -> groupMembers
                        selectedFriend != null -> listOf(
                            GroupMember(uid = currentUserId, email = "You", accepted = true),
                            GroupMember(uid = selectedFriend!!.uid, email = selectedFriend!!.email, accepted = true)
                        )
                        else -> emptyList()
                    }

                    // optional: simple validation for custom splits
                    if (splitBy == "By percentage") {
                        val totalPct = membersList.sumOf { (splitInputs[it.uid]?.toDoubleOrNull() ?: 0.0) }
                        if (kotlin.math.abs(totalPct - 100.0) > 0.01) return@Button
                    }
                    if (splitBy == "By shares") {
                        val totalShares = splitInputs.values.sumOf { it.toDoubleOrNull() ?: 0.0 }
                        if (totalShares <= 0.0) return@Button
                    }

                    expenseViewModel.addExpense(
                        description = description,
                        amount = amt,
                        paidBy = paidByUid,          // ✅ pass UID
                        splitBy = splitBy,
                        members = membersList,
                        groupId = selectedGroup?.id,
                        friendId = selectedFriend?.uid,
                        splitInputs = splitInputs
                    )

                    navController?.popBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Expense", color = Color.White)
            }
        }
    }

    // Friend or Group dialog
    if (showWithDialog) {
        SelectFriendOrGroupScreen(
            friends = friends,
            groups = groups,
            onFriendSelected = { friend ->
                selectedFriend = friend
                selectedGroup = null
                paidByUid = currentUserId
                paidByLabel = "You"
                showWithDialog = false
            },
            onGroupSelected = { group ->
                selectedGroup = group
                selectedFriend = null
                paidByUid = currentUserId
                paidByLabel = "You"
                showWithDialog = false
            },
            onClose = { showWithDialog = false }
        )
    }

    // Paid By dialog
    if (showPaidByDialog) {
        AlertDialog(
            onDismissRequest = { showPaidByDialog = false },
            title = { Text("Select Payer", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (selectedGroup != null) {
                        groupMembers.forEach { member ->
                            ElevatedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        paidByUid = member.uid
                                        paidByLabel = if (member.uid == currentUserId) "You" else (member.email ?: member.uid)
                                        showPaidByDialog = false
                                    },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray)
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        if (member.uid == currentUserId) "You" else (member.email ?: member.uid),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    } else if (selectedFriend != null) {
                        listOf(
                            GroupMember(uid = currentUserId, email = "You", accepted = true),
                            GroupMember(uid = selectedFriend!!.uid, email = selectedFriend!!.email, accepted = true)
                        ).forEach { m ->
                            ElevatedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        paidByUid = m.uid
                                        paidByLabel = if (m.uid == currentUserId) "You" else (m.email ?: m.uid)
                                        showPaidByDialog = false
                                    },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray)
                                    Spacer(Modifier.width(8.dp))
                                    Text(if (m.uid == currentUserId) "You" else (m.email ?: m.uid))
                                }
                            }
                        }
                    } else {
                        Text("Please select a friend or group first", color = Color.Gray)
                    }
                }
            },
            confirmButton = {}
        )
    }

    if (showSplitDialog) {
        // Define membersList outside the AlertDialog so it's accessible in both text and confirmButton
        val membersList = when {
            selectedGroup != null -> groupMembers
            selectedFriend != null -> listOf(
                GroupMember(uid = currentUserId, email = "You", accepted = true),
                GroupMember(uid = selectedFriend!!.uid, email = selectedFriend!!.email, accepted = true)
            )
            else -> emptyList()
        }

        AlertDialog(
            onDismissRequest = { showSplitDialog = false },
            title = { Text("Select Split Method", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // 🔹 Selection options
                    listOf("Equally", "By shares", "By percentage").forEach { option ->
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    splitBy = option
                                    if (option == "Equally") {
                                        splitInputs = emptyMap()
                                        showSplitDialog = false
                                    }
                                },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = if (splitBy == option) Color(0xFFE3F2FD) else Color.White
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    when (option) {
                                        "By shares" -> Icons.Default.PieChart
                                        "By percentage" -> Icons.Default.Percent
                                        else -> Icons.Default.Equalizer
                                    },
                                    contentDescription = null,
                                    tint = if (splitBy == option) Color(0xFF1565C0) else Color.Gray
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    option,
                                    fontWeight = if (splitBy == option) FontWeight.Bold else FontWeight.Medium,
                                    color = if (splitBy == option) Color(0xFF1565C0) else Color.Black
                                )
                            }
                        }
                    }

                    // 🔹 Show input fields if "By shares" or "By percentage" is chosen
                    if (splitBy == "By shares" || splitBy == "By percentage") {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = if (splitBy == "By shares") "Enter shares" else "Enter percentages",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1565C0)
                        )

                        // 🔹 Input for each member - REMOVED THE DUPLICATE membersList DEFINITION HERE
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 200.dp)
                        ) {
                            items(membersList) { member ->
                                OutlinedTextField(
                                    value = splitInputs[member.uid] ?: "",
                                    onValueChange = { value ->
                                        // Only allow numbers and decimal point
                                        if (value.matches(Regex("^\\d*\\.?\\d*$")) || value.isEmpty()) {
                                            splitInputs = splitInputs.toMutableMap().apply {
                                                put(member.uid, value)
                                            }
                                        }
                                    },
                                    label = {
                                        Text(if (member.uid == currentUserId) "You" else (member.email ?: member.uid))
                                    },
                                    placeholder = {
                                        Text(if (splitBy == "By shares") "Enter share" else "Enter %")
                                    },
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number,
                                        imeAction = ImeAction.Done
                                    ),
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    colors = TextFieldDefaults.outlinedTextFieldColors(
                                        focusedBorderColor = Color(0xFF1565C0),
                                        focusedLabelColor = Color(0xFF1565C0),
                                        cursorColor = Color(0xFF1565C0)
                                    )
                                )
                            }
                        }

                        // Show validation message
                        if (splitBy == "By percentage") {
                            val totalPercent = membersList.sumOf {
                                (splitInputs[it.uid]?.toDoubleOrNull() ?: 0.0)
                            }
                            Text(
                                text = "Total: ${"%.1f".format(totalPercent)}%",
                                color = if (abs(totalPercent - 100.0) < 0.1) Color(0xFF2E7D32) else Color(0xFFD32F2F),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                if (splitBy == "By shares" || splitBy == "By percentage") {
                    Button(
                        onClick = {
                            // Validate inputs before closing
                            if (splitBy == "By percentage") {
                                val totalPercent = membersList.sumOf {
                                    (splitInputs[it.uid]?.toDoubleOrNull() ?: 0.0)
                                }
                                if (abs(totalPercent - 100.0) < 0.1) {
                                    showSplitDialog = false
                                } else {
                                    // Show error message - you could add a snackbar or toast here
                                    // For now, we'll just keep the dialog open
                                }
                            } else {
                                showSplitDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
                    ) {
                        Text("Done", color = Color.White)
                    }
                }
            },
            dismissButton = {
                if (splitBy == "By shares" || splitBy == "By percentage") {
                    TextButton(onClick = { showSplitDialog = false }) {
                        Text("Cancel", color = Color.Gray)
                    }
                }
            }
        )
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectFriendOrGroupScreen(
    friends: List<Friend>,
    groups: List<Group>,
    onFriendSelected: (Friend) -> Unit,
    onGroupSelected: (Group) -> Unit,
    onClose: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredFriends = remember(searchQuery) {
        if (searchQuery.isNotBlank()) {
            friends.filter { it.name.contains(searchQuery, ignoreCase = true) }
        } else emptyList()
    }
    val filteredGroups = remember(searchQuery) {
        if (searchQuery.isNotBlank()) {
            groups.filter { it.name.contains(searchQuery, ignoreCase = true) }
        } else emptyList()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Friend or Group") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // 🔍 Styled Search Input
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                singleLine = true,
                placeholder = { Text("Search friends or groups", color = Color.Gray) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Filled.Close, contentDescription = "Clear", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White.copy(alpha = 0.2f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.1f),
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredFriends.isEmpty() && filteredGroups.isEmpty() && searchQuery.isNotBlank()) {
                Text("No results found", style = MaterialTheme.typography.bodyMedium)
            }

            LazyColumn {
                if (filteredFriends.isNotEmpty()) {
                    item { Text("Friends", style = MaterialTheme.typography.titleMedium) }
                    items(filteredFriends) { friend ->
                        ListItem(
                            leadingContent = {
                                Icon(Icons.Default.Person, contentDescription = null)
                            },
                            headlineContent = { Text(friend.name) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onFriendSelected(friend) }
                        )
                    }
                }

                if (filteredGroups.isNotEmpty()) {
                    item { Text("Groups", style = MaterialTheme.typography.titleMedium) }
                    items(filteredGroups) { group ->
                        ListItem(
                            leadingContent = {
                                Icon(Icons.Default.Group, contentDescription = null)
                            },
                            headlineContent = { Text(group.name) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onGroupSelected(group) }
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun SelectListItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = primaryColor)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold)
                Text(subtitle, fontSize = 13.sp, color = Color.Gray)
            }
        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AddExpenseScreenPreview() {
    AddExpenseScreen()
}
