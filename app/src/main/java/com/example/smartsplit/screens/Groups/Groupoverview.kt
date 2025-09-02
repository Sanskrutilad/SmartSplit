
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.smartsplit.Viewmodel.Group
import com.example.smartsplit.Viewmodel.GroupViewModel
import com.example.smartsplit.Viewmodel.LoginScreenViewModel
import com.example.smartsplit.Viewmodel.logActivity
import com.google.firebase.auth.FirebaseAuth


val groupTypes = listOf(
    "Travel" to Icons.Default.Flight,
    "Family" to Icons.Default.Home,
    "Friends" to Icons.Default.Group,
    "Work" to Icons.Default.Work,
    "Grocery" to Icons.Default.ShoppingCart,
    "Other" to Icons.Default.NoteAdd
)
@Composable
fun NewGroupScreen(
    navController: NavController,
    groupId: String,
    viewModel: GroupViewModel = viewModel()
) {
    val primaryColor = Color(0xFF2196F3)
    val accentColor = Color(0xFF2196F3)
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(primaryColor.copy(alpha = 0.15f), Color.White)
    )
    var selectedTab by remember { mutableStateOf("Members") }
    var showLeaveDialog by remember { mutableStateOf(false) }
    var showInviteDialog by remember { mutableStateOf(false) }
    var inviteEmail by remember { mutableStateOf("") }
    var groupName by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf<String?>(null) }

    val message by viewModel.message.observeAsState("")
    val members by viewModel.groupMembers.observeAsState(emptyList())
    val pendingInvites by viewModel.pendingInvites.observeAsState(emptyList())
    var showDeleteDialog by remember { mutableStateOf(false) }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    var group by remember { mutableStateOf<Group?>(null) }

    LaunchedEffect(groupId) {
        viewModel.fetchGroupDetails(groupId) { fetchedGroup ->
            group = fetchedGroup
            groupName = fetchedGroup?.name ?: ""
        }
        viewModel.fetchGroupMembers(groupId)
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush)
            .padding(24.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = accentColor)
            }

            if (group?.createdBy == FirebaseAuth.getInstance().currentUser?.email) {
                TextButton(
                    onClick = {

                        showDeleteDialog = true
                    }
                ) {
                    Icon(Icons.Filled.ExitToApp, contentDescription = "Delete Group", tint = Color.Red)
                }
            } else {
                TextButton(onClick = { showLeaveDialog = true }) {
                    Icon(Icons.Filled.ExitToApp, contentDescription = "Leave Group", tint = Color.Blue)
                }
            }

        }
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Group, contentDescription = "Group Icon", tint = accentColor)
            }

            Spacer(Modifier.width(12.dp))

            Text(
                text = group?.name ?: "Loading...",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            )
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Type: ${group?.type ?: "..." }",
            style = MaterialTheme.typography.bodyMedium.copy(color = Color.DarkGray)
        )

        Text(
            text = "Created by: ${group?.createdBy ?: "..." }",
            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
        )

        Spacer(Modifier.height(20.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                GroupChip("Settle up", selectedTab == "Settle up") { selectedTab = "Settle up" }
            }
            item {
                GroupChip("Balance", selectedTab == "Balance") { selectedTab = "Balance" }
            }
            item {
                GroupChip("Total", selectedTab == "Total") { selectedTab = "Total" }
            }
            item {
                GroupChip("Members", selectedTab == "Members") { selectedTab = "Members" }
            }
        }

        Spacer(Modifier.height(20.dp))

        when (selectedTab) {
            "Members" -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors= CardDefaults.cardColors(Color.Transparent),
                    border = BorderStroke(1.dp, Color.Gray)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("Members:", fontWeight = FontWeight.SemiBold)

                        members.forEach { member ->
                            Text("• ${member.email ?: member.uid}", color = Color.DarkGray, fontSize = 14.sp)
                        }

                        if (pendingInvites.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            Text("Pending Invites:", fontWeight = FontWeight.SemiBold)
                            pendingInvites.forEach { member ->
                                Text("• ${member.email ?: member.uid} (Pending)", color = Color.Gray, fontSize = 12.sp)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(52.dp))
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
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

            "Settle up" -> {
                Text("💸 Settle up feature coming soon...", modifier = Modifier.padding(16.dp))
            }

            "Balance" -> {
                Text("📊 Balance details will be shown here.", modifier = Modifier.padding(16.dp))
            }

            "Total" -> {
                Text("🧾 Total expenses summary here.", modifier = Modifier.padding(16.dp))
            }
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = {  },
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


    // ✉️ Invite Member Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Group?") },
            text = { Text("This action cannot be undone. Are you sure you want to delete this group?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteGroup(groupId) // delete from Firestore
                        logActivity(
                            type = "GROUP_DELETED",
                            description = "Group '$groupName' was deleted",
                            relatedGroupId = groupId,
                            userId = currentUserId
                        )
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


    // 🔔 Show messages (like Toast)
    if (message.isNotEmpty()) {
        LaunchedEffect(message) {
            Log.d("UI", "Message: $message")
        }
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
