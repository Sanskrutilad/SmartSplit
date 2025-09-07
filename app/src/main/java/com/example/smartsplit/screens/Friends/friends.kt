package com.example.smartsplit.screens.Friends

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.smartsplit.Viewmodel.Expense
import com.example.smartsplit.Viewmodel.ExpenseViewModel
import com.example.smartsplit.Viewmodel.FriendsViewModel
import com.example.smartsplit.Viewmodel.GroupViewModel
import com.example.smartsplit.Viewmodel.Settlement
import com.example.smartsplit.Viewmodel.formatDate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.collections.associate
import kotlin.math.abs


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(
    navController: NavController,
    viewModel: FriendsViewModel = viewModel(),
    expenseViewModel: ExpenseViewModel = viewModel()
) {
    val primaryColor = Color(0xFF2196F3)
    val accentColor = Color(0xFF2196F3)
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            primaryColor.copy(alpha = 0.15f),
            Color.White
        )
    )

    val friends by viewModel.friends.collectAsState()
    val friendExpenses by expenseViewModel.friendExpenses.observeAsState(emptyList()) // Add this line
    val friendSettlements by expenseViewModel.friendSettlements.observeAsState(emptyList())

    // Remove the unused combinedExpenses line since we're not using it
    // val combinedExpenses by expenseViewModel.combinedExpenses.observeAsState(emptyList())

    // Calculate balances for each friend
    val friendBalances = remember(friends, friendExpenses, friendSettlements) {
        friends.associate { friend ->
            val balance = friendExpenses
                .filter { it.friendId == friend.uid }
                .sumOf { expense ->
                    if (expense.paidBy == currentUserId) {
                        expense.amount - (expense.splits[friend.uid] ?: 0.0)
                    } else {
                        (expense.splits[currentUserId] ?: 0.0) - expense.amount
                    }
                }

            // Subtract settlements
            val netBalance = balance - friendSettlements
                .filter { it.from == currentUserId && it.to == friend.uid }
                .sumOf { it.amount } +
                    friendSettlements
                        .filter { it.from == friend.uid && it.to == currentUserId }
                        .sumOf { it.amount }

            friend.uid to netBalance
        }
    }

    // Update LaunchedEffect to only fetch friend expenses
    LaunchedEffect(currentUserId) {
        viewModel.fetchFriends(currentUserId)
        expenseViewModel.fetchAllFriendExpenses(currentUserId)
        // Fetch settlements for all friends
        friends.forEach { friend ->
            expenseViewModel.fetchFriendSettlements(currentUserId, friend.uid)
        }
    }

    // Rest of your Scaffold code remains the same...
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
                        val balance = friendBalances[friend.uid] ?: 0.0
                        FriendCard(
                            friendName = friend.name,
                            balance = balance,
                            accentColor = accentColor,
                            onClick = {
                                navController.navigate("friendDetails/${friend.uid}")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FriendCard(
    friendName: String,
    balance: Double,
    accentColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = friendName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = if (balance > 0) "Owes you ₹${"%.2f".format(balance)}"
                    else if (balance < 0) "You owe ₹${"%.2f".format(-balance)}"
                    else "Settled up",
                    style = MaterialTheme.typography.bodyMedium,
                    color = when {
                        balance > 0 -> Color(0xFF2E7D32) // Green for owes you
                        balance < 0 -> Color(0xFFD32F2F) // Red for you owe
                        else -> Color.Gray // Gray for settled
                    }
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "View details",
                tint = accentColor
            )
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendDetailsScreen(
    navController: NavController,
    friendId: String,
    viewModel: FriendsViewModel = viewModel(),
    expenseViewModel: ExpenseViewModel = viewModel(),
    groupViewModel: GroupViewModel = viewModel() // Add GroupViewModel
) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val friendStateFlow = remember(friendId) { viewModel.getFriend(friendId) }
    val friend by friendStateFlow.collectAsState(initial = null)
    val friendExpenses by expenseViewModel.friendExpenses.observeAsState(emptyList())
    val friendSettlements by expenseViewModel.friendSettlements.observeAsState(emptyList())
    val sharedGroups by groupViewModel.sharedGroupsWithFriend.observeAsState(emptyList()) // Add shared groups

    // Add database reference
    val db = FirebaseFirestore.getInstance()

    LaunchedEffect(friendId) {
        expenseViewModel.fetchFriendExpenses(currentUserId, friendId)
        expenseViewModel.fetchFriendSettlements(currentUserId, friendId)
        groupViewModel.fetchSharedGroupsWithFriend(currentUserId, friendId) // Fetch shared groups
    }

    // Filter out settled expenses and calculate net balance (ONLY direct expenses)
    val (unsettledExpenses, settledExpenseIds) = remember(friendExpenses, friendSettlements) {
        val allSettledExpenseIds = friendSettlements.flatMap { it.expenseIds }.toSet()
        val unsettled = friendExpenses.filter { it.id !in allSettledExpenseIds }
        Pair(unsettled, allSettledExpenseIds)
    }

    // Calculate total balance from DIRECT expenses only (no group expenses)
    val totalBalance = remember(unsettledExpenses) {
        unsettledExpenses.sumOf { expense ->
            if (expense.paidBy == currentUserId) {
                // You paid, friend owes you
                expense.amount - (expense.splits[friendId] ?: 0.0)
            } else {
                // Friend paid, you owe friend
                (expense.splits[currentUserId] ?: 0.0) - expense.amount
            }
        }
    }

    // Show settlement button if there's a balance
    var showSettlementDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        friend?.name ?: friend?.email ?: "Friend Details",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (abs(totalBalance) > 0.01) {
                        IconButton(
                            onClick = { showSettlementDialog = true },
                            enabled = abs(totalBalance) > 0.01
                        ) {
                            Icon(
                                Icons.Default.AttachMoney,
                                contentDescription = "Settle up",
                                tint = if (abs(totalBalance) > 0.01) MaterialTheme.colorScheme.primary else Color.Gray
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(16.dp)
        ) {
            // Friend info card
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = friend?.name ?: "Friend",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = friend?.email ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Balance Summary (ONLY direct expenses)
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Direct Balance",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Text(
                        text = if (totalBalance > 0) "Owes you ₹${"%.2f".format(totalBalance)}"
                        else if (totalBalance < 0) "You owe ₹${"%.2f".format(-totalBalance)}"
                        else "Settled up",
                        style = MaterialTheme.typography.headlineMedium,
                        color = when {
                            totalBalance > 0 -> Color(0xFF2E7D32)
                            totalBalance < 0 -> Color(0xFFD32F2F)
                            else -> Color.Gray
                        },
                        fontWeight = FontWeight.Bold
                    )

                    if (abs(totalBalance) > 0.01) {
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = { showSettlementDialog = true }
                        ) {
                            Text("Settle Up")
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Shared Groups Section
            if (sharedGroups.isNotEmpty()) {
                Text(
                    text = "Shared Groups",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))

                sharedGroups.forEach { group ->
                    GroupCard(
                        groupName = group.name,
                        memberCount = group.memberCount,
                        onClick = {
                            navController.navigate("groupDetails/${group.id}")
                        }
                    )
                    Spacer(Modifier.height(8.dp))
                }
                Spacer(Modifier.height(16.dp))
            }

            // Direct Expenses List - Only show unsettled direct expenses
            Text(
                text = "Direct Expenses",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            if (unsettledExpenses.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No direct expenses", color = Color.Gray)
                }
            } else {
                unsettledExpenses.forEach { expense ->
                    FriendExpenseCard(expense, currentUserId, friendId)
                    Spacer(Modifier.height(8.dp))
                }
            }

            // Show settlement history if available
            if (friendSettlements.isNotEmpty()) {
                Spacer(Modifier.height(24.dp))
                Text(
                    text = "Settlement History",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                friendSettlements.forEach { settlement ->
                    SettlementHistoryCard(settlement, currentUserId, friend?.name ?: "Friend")
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }

    // Settlement Dialog
    if (showSettlementDialog) {
        AlertDialog(
            onDismissRequest = { showSettlementDialog = false },
            title = { Text("Settle Up") },
            text = {
                Text("Are you sure you want to mark this balance as settled?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        val expenseIds = unsettledExpenses.map { it.id }
                        expenseViewModel.markFriendSettlementPaid(
                            currentUserId,
                            friendId,
                            abs(totalBalance),
                            expenseIds
                        ) { success ->
                            if (success) {
                                showSettlementDialog = false
                            }
                        }
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showSettlementDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
@Composable
fun SettlementHistoryCard(settlement: ExpenseViewModel.FriendSettlement, currentUserId: String, friendName: String) {
    val isYouPaying = settlement.from == currentUserId
    val amountText = if (isYouPaying) {
        "You paid ${friendName} ₹${"%.2f".format(settlement.amount)}"
    } else {
        "${friendName} paid you ₹${"%.2f".format(settlement.amount)}"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = amountText,
                    fontWeight = FontWeight.Medium,
                    color = if (isYouPaying) Color(0xFFD32F2F) else Color(0xFF2E7D32)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Settled on ${formatDate(settlement.createdAt)}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Settled",
                tint = Color(0xFF2E7D32)
            )
        }
    }
}
// Group Card Component
@Composable
fun GroupCard(
    groupName: String,
    memberCount: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Group,
                contentDescription = "Group",
                tint = Color(0xFF2196F3),
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = groupName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "$memberCount members",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "View group",
                tint = Color.Gray
            )
        }
    }
}

// Simplified FriendExpenseCard (only for direct expenses)
@Composable
fun FriendExpenseCard(expense: ExpenseViewModel.FriendExpense, currentUserId: String, friendId: String) {
    val amount = if (expense.paidBy == currentUserId) {
        expense.splits[friendId] ?: 0.0
    } else {
        expense.splits[currentUserId] ?: 0.0
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = expense.description,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "₹${"%.2f".format(amount)}",
                color = if (expense.paidBy == currentUserId) Color(0xFF2E7D32) else Color(0xFFD32F2F),
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Paid by: ${if (expense.paidBy == currentUserId) "You" else "Friend"}",
                fontSize = 12.sp,
                color = Color.Gray
            )
            Text(
                text = formatDate(expense.createdAt),
                fontSize = 12.sp,
                color = Color.Gray
            )
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
