package com.example.smartsplit.Viewmodel

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import com.google.firebase.Timestamp


data class Group(
    val id: String = "",
    val name: String = "",
    val type: String = "",
    val createdBy: String = ""
)
data class GroupMember(
    val uid: String = "",
    val role: String = "member",
    val accepted: Boolean = false,
    val invitedAt: Timestamp? = null,
    val joinedAt: Timestamp? = null,
    var email: String? = null
)


class GroupViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _createdGroupId = MutableLiveData<String?>()
    val createdGroupId: LiveData<String?> = _createdGroupId

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    private val _myGroups = MutableLiveData<List<Group>>(emptyList())
    val myGroups: LiveData<List<Group>> = _myGroups

    private val _groupMembers = MutableLiveData<List<GroupMember>>(emptyList())
    val groupMembers: LiveData<List<GroupMember>> = _groupMembers

    private val _pendingInvites = MutableLiveData<List<GroupMember>>(emptyList())
    val pendingInvites: LiveData<List<GroupMember>> = _pendingInvites


    fun fetchGroupMembers(groupId: String) {
        db.collection("groups").document(groupId)
            .collection("members")
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener

                val members = snapshot?.documents?.mapNotNull { it.toObject(GroupMember::class.java) } ?: emptyList()

                members.forEach { member ->
                    getUserEmailFromUid(member.uid) { email ->
                        member.email = email
                        _groupMembers.value = members.filter { it.accepted == true }
                        _pendingInvites.value = members.filter { it.accepted != true }
                    }
                }
            }
    }


    fun createGroup(groupName: String, groupType: String) {
        val currentUser = auth.currentUser ?: return
        val creatorId = currentUser.uid

        val groupData = hashMapOf(
            "name" to groupName,
            "type" to groupType,
            "createdBy" to creatorId,
            "createdAt" to FieldValue.serverTimestamp()
        )

        db.collection("groups")
            .add(groupData)
            .addOnSuccessListener { groupRef ->
                val groupId = groupRef.id
                _createdGroupId.value = groupId

                val memberRef = groupRef.collection("members").document(creatorId)
                memberRef.set(
                    mapOf(
                        "uid" to creatorId,
                        "role" to "admin",
                        "accepted" to true,
                        "joinedAt" to FieldValue.serverTimestamp()
                    )
                ).addOnSuccessListener {
                    _message.value = "Group created successfully!"
                    fetchGroupMembers(groupId)
                    fetchMyGroups()
                }.addOnFailureListener { e ->
                    _message.value = "Failed to add creator as member: ${e.message}"
                }
            }
    }


    /**
     * Fetch groups created by me and map UID → Email
     */
    fun fetchMyGroups() {
        val currentUser = auth.currentUser ?: return
        db.collection("groups")
            .whereEqualTo("createdBy", currentUser.uid)
            .get()
            .addOnSuccessListener { result ->
                val groups = mutableListOf<Group>()

                result.documents.forEach { doc ->
                    val creatorUid = doc.getString("createdBy") ?: ""
                    getUserEmailFromUid(creatorUid) { email ->
                        groups.add(
                            Group(
                                id = doc.id,
                                name = doc.getString("name") ?: "",
                                type = doc.getString("type") ?: "",
                                createdBy = email ?: creatorUid // fallback UID if email not found
                            )
                        )
                        _myGroups.value = groups
                    }
                }
            }
            .addOnFailureListener { e ->
                _message.value = "Failed to fetch groups: ${e.message}"
            }
    }

    /**
     * Helper: Fetch user email by UID from "users" collection
     */
    private fun getUserEmailFromUid(uid: String, onResult: (String?) -> Unit) {
        db.collection("users").document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val email = document.getString("email")
                    onResult(email)
                } else {
                    onResult(null)
                }
            }
            .addOnFailureListener {
                onResult(null)
            }
    }

    fun fetchGroupDetails(groupId: String, onComplete: (Group?) -> Unit) {
        db.collection("groups").document(groupId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val createdByUid = doc.getString("createdBy") ?: ""

                    // Fetch user email from users collection
                    db.collection("users").document(createdByUid)
                        .get()
                        .addOnSuccessListener { userDoc ->
                            val createdByEmail = userDoc.getString("email") ?: createdByUid // fallback to uid

                            val group = Group(
                                id = doc.id,
                                name = doc.getString("name") ?: "",
                                type = doc.getString("type") ?: "",
                                createdBy = createdByEmail,  // 👈 set email instead of uid
                            )
                            onComplete(group)
                        }
                        .addOnFailureListener {
                            val group = Group(
                                id = doc.id,
                                name = doc.getString("name") ?: "",
                                type = doc.getString("type") ?: "",
                                createdBy = createdByUid, // fallback
                            )
                            onComplete(group)
                        }
                } else {
                    _message.value = "Group not found"
                    onComplete(null)
                }
            }
            .addOnFailureListener { e ->
                _message.value = "Failed to fetch group: ${e.message}"
                onComplete(null)
            }
    }

    fun deleteGroup(groupId: String) {
        val currentUser = auth.currentUser ?: return

        db.collection("groups").document(groupId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val createdBy = doc.getString("createdBy")
                    if (createdBy == currentUser.uid) {
                        // First delete all members
                        db.collection("groups").document(groupId)
                            .collection("members")
                            .get()
                            .addOnSuccessListener { members ->
                                val batch = db.batch()
                                for (member in members) {
                                    batch.delete(member.reference)
                                }
                                // Delete group doc after members
                                batch.delete(doc.reference)

                                batch.commit()
                                    .addOnSuccessListener {
                                        _message.value = "Group deleted successfully"
                                        fetchMyGroups()
                                    }
                                    .addOnFailureListener { e ->
                                        _message.value = "Failed to delete group: ${e.message}"
                                    }
                            }
                    } else {
                        _message.value = "Only the creator can delete the group"
                    }
                }
            }
    }

    fun leaveGroup(groupId: String) {
        val currentUser = auth.currentUser ?: return

        val memberRef = db.collection("groups")
            .document(groupId)
            .collection("members")
            .document(currentUser.uid)

        memberRef.delete()
            .addOnSuccessListener {
                _message.value = "You left the group"
                fetchMyGroups()
            }
            .addOnFailureListener { e ->
                _message.value = "Failed to leave group: ${e.message}"
            }
    }


}

@Composable
fun TestGroupScreen(
    navController: NavHostController,
    viewModel: GroupViewModel = viewModel()
) {

    var groupName by remember { mutableStateOf("") }
    var groupType by remember { mutableStateOf("") }

    val createdGroupId by viewModel.createdGroupId.observeAsState()
    val message by viewModel.message.observeAsState("")
    val myGroups by viewModel.myGroups.observeAsState(emptyList())

    // Fetch my groups initially
    LaunchedEffect(Unit) {
        viewModel.fetchMyGroups()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text("Firestore Group Test", style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
            value = groupName,
            onValueChange = { groupName = it },
            label = { Text("Group Name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = groupType,
            onValueChange = { groupType = it },
            label = { Text("Group Type") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                if (groupName.isNotBlank() && groupType.isNotBlank()) {
                    viewModel.createGroup(groupName, groupType)
                    groupName = ""
                    groupType = ""
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create Group")
        }

        if (message.isNotEmpty()) {
            Text(message, color = Color.Gray)
        }

        Divider()
        Text("My Groups:", style = MaterialTheme.typography.titleMedium)

        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            myGroups.forEach { group ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    // Placeholder group icon
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = "Group Icon",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column {
                        Text("Name: ${group.name}", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("Type: ${group.type}", fontSize = 12.sp, color = Color.Gray)
                        Text("Created By: ${group.createdBy}", fontSize = 12.sp, color = Color.DarkGray)
                    }
                }
                Divider()
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewTestGroupScreen() {
    TestGroupScreen(navController = rememberNavController())
}
