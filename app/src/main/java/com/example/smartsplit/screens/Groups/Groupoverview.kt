
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.smartsplit.Viewmodel.ExpenseViewModel
import com.example.smartsplit.Viewmodel.Friend
import com.example.smartsplit.Viewmodel.FriendsViewModel
import com.example.smartsplit.Viewmodel.Group
import com.example.smartsplit.Viewmodel.GroupViewModel
import com.example.smartsplit.Viewmodel.LoginScreenViewModel
import com.example.smartsplit.Viewmodel.logActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch


val groupTypes = listOf(
    "Travel" to Icons.Default.Flight,
    "Family" to Icons.Default.Home,
    "Friends" to Icons.Default.Group,
    "Work" to Icons.Default.Work,
    "Grocery" to Icons.Default.ShoppingCart,
    "Other" to Icons.Default.NoteAdd
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewGroupScreen(
    navController: NavController,
    groupId: String,
    viewModel: GroupViewModel = viewModel(),
    friendsViewModel: FriendsViewModel = viewModel(),
    expenseViewModel: ExpenseViewModel = viewModel()
) {
    val primaryColor = Color(0xFF2196F3)
    val accentColor = Color(0xFF2196F3)
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(primaryColor.copy(alpha = 0.15f), Color.White)
    )

    // UI States
    var selectedTab by remember { mutableStateOf("Members") }
    var showLeaveDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showInviteDialog by remember { mutableStateOf(false) }
    var selectedFriend by remember { mutableStateOf<Friend?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val message by viewModel.message.observeAsState("")
    val members by viewModel.groupMembers.observeAsState(emptyList())
    val pendingInvites by viewModel.pendingInvites.observeAsState(emptyList())
    var group by remember { mutableStateOf<Group?>(null) }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email

    val expenses by expenseViewModel.groupExpenses.observeAsState(emptyList())
    LaunchedEffect(groupId) {
        expenseViewModel.fetchGroupExpenses(groupId)
    }
    // Friends list
    val friends by friendsViewModel.friends.collectAsState()

    // Fetch data
    LaunchedEffect(groupId) {
        viewModel.fetchGroupDetails(groupId) { fetchedGroup ->
            group = fetchedGroup
        }
        viewModel.fetchGroupMembers(groupId)
        friendsViewModel.fetchFriends(currentUserId)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientBrush)
                .padding(padding)
                .padding(24.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // Top Row with Back & Delete/Leave
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = accentColor)
                }

                if (group?.createdBy == currentUserEmail) {
                    TextButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Filled.ExitToApp, contentDescription = "Delete", tint = Color.Red)
                    }
                } else {
                    TextButton(onClick = { showLeaveDialog = true }) {
                        Icon(Icons.Filled.ExitToApp, contentDescription = "Leave", tint = Color.Blue)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Group Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Group, contentDescription = "Group", tint = accentColor)
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = group?.name ?: "Loading...",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    )
                    Text(
                        text = "Type: ${group?.type ?: "..."}",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.DarkGray)
                    )
                    Text(
                        text = "Created by: ${group?.createdBy ?: "..."}",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Tabs
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf("Settle up", "Balance", "Total", "Members").forEach { tab ->
                    item {
                        GroupChip(tab, selectedTab == tab) { selectedTab = tab }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Tab Content
            when (selectedTab) {
                "Members" -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(Color.Transparent),
                        border = BorderStroke(1.dp, Color.Gray)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("Members:", fontWeight = FontWeight.SemiBold)
                            members.forEach { m ->
                                Text("• ${m.email ?: m.uid}", color = Color.DarkGray, fontSize = 14.sp)
                            }

                            if (pendingInvites.isNotEmpty()) {
                                Spacer(Modifier.height(8.dp))
                                Text("Pending Invites:", fontWeight = FontWeight.SemiBold)
                                pendingInvites.forEach { m ->
                                    Text("• ${m.email ?: m.uid} (Pending)", color = Color.Gray, fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    // 🔹 Only show invite if creator
                    if (group?.createdBy == currentUserEmail) {
                        Spacer(Modifier.height(52.dp))
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Button(
                                onClick = { showInviteDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                                shape = RoundedCornerShape(50)
                            ) {
                                Icon(Icons.Filled.PersonAdd, contentDescription = null, tint = Color.White)
                                Spacer(Modifier.width(8.dp))
                                Text("Invite Members", color = Color.White)
                            }
                        }
                    }
                }
                "Settle up" -> {
                    val settlements = expenseViewModel.calculateSettlements(expenses)
                    val mySettlements = settlements.filter { it.from == currentUserId } // 🔹 Only what I owe

                    if (mySettlements.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(Color(0xFFE8F5E9)), // light green bg
                            border = BorderStroke(1.dp, Color(0xFF2E7D32))
                        ) {
                            Text(
                                "🎉 You’re all settled up!",
                                modifier = Modifier.padding(16.dp),
                                color = Color(0xFF2E7D32),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    } else {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(Color.White),
                            elevation= CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Your pending settlements:", fontWeight = FontWeight.SemiBold)

                                mySettlements.forEach { settlement ->
                                    val toEmail = members.find { it.uid == settlement.to }?.email ?: settlement.to
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Pay to $toEmail", fontWeight = FontWeight.Bold)
                                            Text("₹${"%.2f".format(settlement.amount)}", color = Color.Red)
                                        }

                                        Button(
                                            onClick = {
                                                // 🔹 Later we can add UPI intent here
                                            },
                                            shape = RoundedCornerShape(50),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                                        ) {
                                            Text("Pay", color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                "Balance" -> {
                    val settlements = expenseViewModel.calculateSettlements(expenses)

                    // Group settlements per member
                    val settlementsByMember = members.associate { member ->
                        val uid = member.uid
                        val email = member.email

                        val owes = settlements.filter { it.from == uid }
                        val lents = settlements.filter { it.to == uid }

                        email to (owes to lents)
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Balances",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                        )

                        settlementsByMember.forEach { (email, pair) ->
                            val (owes, lents) = pair

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(Color.White),
                                elevation = CardDefaults.cardElevation(3.dp),
                                border = BorderStroke(1.dp, Color.LightGray)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    // Member header
                                    Text(
                                        email ?: "Unknown",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Color(0xFF1565C0)
                                    )
                                    Spacer(Modifier.height(8.dp))

                                    if (owes.isEmpty() && lents.isEmpty()) {
                                        Text("• Settled up", color = Color.Gray, fontSize = 14.sp)
                                    } else {
                                        // Show owes
                                        owes.forEach { settlement ->
                                            val toEmail = members.find { it.uid == settlement.to }?.email ?: settlement.to
                                            Text(
                                                "• Owes to $toEmail: ₹${"%.2f".format(settlement.amount)}",
                                                color = Color(0xFFD32F2F),
                                                fontSize = 14.sp
                                            )
                                        }

                                        // Show lents
                                        lents.forEach { settlement ->
                                            val fromEmail = members.find { it.uid == settlement.from }?.email ?: settlement.from
                                            Text(
                                                "• Lent from $fromEmail: ₹${"%.2f".format(settlement.amount)}",
                                                color = Color(0xFF2E7D32),
                                                fontSize = 14.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                "Total" -> {
                    val totalSpending = expenses.sumOf { it.amount }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()), // 🔹 Makes column scrollable
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 🔹 Group Total Spending Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(Color.White),
                            elevation = CardDefaults.cardElevation(4.dp),
                            border = BorderStroke(1.dp, Color.Gray)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Group Total Spending", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "₹${"%.2f".format(totalSpending)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = Color(0xFF2196F3)
                                )
                            }
                        }

                        // 🔹 Cards for each expense
                        expenses.forEach { expense ->
                            val payer = members.find { it.uid == expense.paidBy }?.email ?: expense.paidBy

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(Color.White),
                                elevation = CardDefaults.cardElevation(2.dp),
                                border = BorderStroke(1.dp, Color.LightGray)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(expense.description, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "₹${"%.2f".format(expense.amount)}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Color(0xFF4CAF50)
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text("Paid by $payer", fontSize = 13.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }




            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = { /* Add expense logic */ },
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

        // 🔹 Invite Dialog (Only if creator)
        if (showInviteDialog && group?.createdBy == currentUserEmail) {
            AlertDialog(
                onDismissRequest = { showInviteDialog = false },
                title = { Text("Invite Friend to Group") },
                text = {
                    Column {
                        Text("Select a friend to invite:")
                        var expanded by remember { mutableStateOf(false) }

                        // 🔹 Get only friends NOT already in group or pending
                        val eligibleFriends = friends.filter { friend ->
                            members.none { it.uid == friend.uid } &&
                                    pendingInvites.none { it.uid == friend.uid }
                        }

                        Box {
                            OutlinedButton(onClick = { expanded = true }) {
                                Text(
                                    selectedFriend?.email
                                        ?: if (eligibleFriends.isEmpty()) "No eligible friends"
                                        else "Choose Friend"
                                )
                            }
                            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                eligibleFriends.forEach { friend ->
                                    DropdownMenuItem(
                                        text = { Text(friend.email) },
                                        onClick = {
                                            selectedFriend = friend
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        enabled = selectedFriend != null,
                        onClick = {
                            selectedFriend?.let { friend ->
                                friendsViewModel.sendGroupInvite(
                                    groupId = groupId,
                                    groupName = group?.name ?: "Group",
                                    toUserId = friend.uid
                                ) { _, msg ->
                                    scope.launch {
                                        snackbarHostState.showSnackbar(msg)
                                    }
                                }
                            }
                            showInviteDialog = false
                        }
                    ) {
                        Text("Send Invite")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showInviteDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // 🔹 Delete Dialog
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Group?") },
                text = { Text("This action cannot be undone. Are you sure you want to delete this group?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteDialog = false
                            viewModel.deleteGroup(groupId)
                            navController.popBackStack()
                        }
                    ) {
                        Text("Delete", color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel", color = Color.Gray)
                    }
                }
            )
        }
        if (showLeaveDialog) {
            AlertDialog(
                onDismissRequest = { showLeaveDialog = false },
                title = { Text("Leave Group?") },
                text = { Text("Are you sure you want to leave this group?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showLeaveDialog = false
                            viewModel.leaveGroup(groupId)
                            navController.popBackStack()
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

    if (message.isNotEmpty()) {
        LaunchedEffect(message) { Log.d("UI", "Message: $message") }
    }
}


@Composable
fun GroupChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        border = BorderStroke(1.dp, Color.Gray),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = if (isSelected) Color.White else Color.Black
        )
    }
}
