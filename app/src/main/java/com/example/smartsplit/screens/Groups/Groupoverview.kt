import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.example.smartsplit.Viewmodel.LoginScreenViewModel


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
    type: String,
    groupId: String?,
    viewModel: LoginScreenViewModel = viewModel()
) {

    val primaryColor = Color(0xFF2196F3)
    val accentColor = Color(0xFF2196F3)
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(primaryColor.copy(alpha = 0.15f), Color.White)
    )

    var showLeaveDialog by remember { mutableStateOf(false) }
    var showInviteDialog by remember { mutableStateOf(false) }
    var inviteEmail by remember { mutableStateOf("") }

    val groups by viewModel.groups.observeAsState(emptyList())
    val message by viewModel.message.observeAsState("")

    // Listen to groups in Firestore
    LaunchedEffect(Unit) {
        viewModel.loadGroups()
    }

    // Find this group’s data
    val group = groups.find { it["id"] == groupId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush)
            .padding(24.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        // 🔝 Group Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = accentColor)
            }

            TextButton(onClick = { showLeaveDialog = true }) {
                Icon(Icons.Filled.ExitToApp, contentDescription = "Leave Group", tint = Color.Blue)
            }
        }

        Spacer(Modifier.height(16.dp))

        // ✅ Group Title
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
                text = group?.get("name") as? String ?: "$type Group",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            )
        }

        Spacer(Modifier.height(20.dp))

        // 🔘 Chips
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            GroupChip("Settle up")
            GroupChip("Balance")
            GroupChip("Total")
        }

        Spacer(Modifier.height(20.dp))

        // 🎯 Members list
        Text("Members:", fontWeight = FontWeight.SemiBold)
        val members = group?.get("members") as? List<*> ?: emptyList<String>()
        members.forEach { member ->
            Text("• $member", color = Color.DarkGray, fontSize = 14.sp)
        }

        Spacer(Modifier.weight(1f))

        // ➕ Add Members
        Button(
            onClick = { showInviteDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = accentColor),
            shape = RoundedCornerShape(50)
        ) {
            Icon(Icons.Filled.PersonAdd, contentDescription = null, tint = Color.White)
            Spacer(Modifier.width(8.dp))
            Text("Invite Members", color = Color.White)
        }

        Spacer(Modifier.height(12.dp))

        // ➕ Add Expense button
        Button(
            onClick = { /* TODO: Add expense */ },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = accentColor),
            shape = RoundedCornerShape(50)
        ) {
            Icon(Icons.Filled.Receipt, contentDescription = null, tint = Color.White)
            Spacer(Modifier.width(8.dp))
            Text("Add expense", color = Color.White)
        }
    }

    // ⚠️ Leave Group Dialog
    if (showLeaveDialog) {
        AlertDialog(
            onDismissRequest = { showLeaveDialog = false },
            title = { Text("Leave Group?") },
            text = { Text("Are you sure you want to leave this group?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLeaveDialog = false
                        // TODO: call viewModel.leaveGroup(groupId)
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

    // ✉️ Invite Member Dialog
    if (showInviteDialog) {
        AlertDialog(
            onDismissRequest = { showInviteDialog = false },
            title = { Text("Invite Member") },
            text = {
                OutlinedTextField(
                    value = inviteEmail,
                    onValueChange = { inviteEmail = it },
                    label = { Text("Enter email") }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.inviteUserToGroup(inviteEmail, groupId)
                        inviteEmail = ""
                        showInviteDialog = false
                    }
                ) {
                    Text("Send Invite", color = accentColor)
                }
            },
            dismissButton = {
                TextButton(onClick = { showInviteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // 🔔 Show messages (like Toast)
    if (message.isNotEmpty()) {
        LaunchedEffect(message) {
            Log.d("UI", "Message: $message")
            // You can use Toast or Snackbar here
        }
    }
}


@Composable
fun GroupChip(label: String) {
    OutlinedButton(
        onClick = { },
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.6f)),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
    ) {
        Text(label, color = Color.Black)
    }
}
