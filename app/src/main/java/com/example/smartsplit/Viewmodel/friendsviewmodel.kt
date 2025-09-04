package com.example.smartsplit.Viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class FriendRequest(
    val fromUserId: String = "",
    val toUserId: String = "",
    val status: String = "pending", // pending, accepted, rejected
    val timestamp: Long = System.currentTimeMillis()
)

data class Friend(
    val uid: String = "",
    val email: String = "",
    val name: String = ""
)


class FriendsViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _friends = MutableStateFlow<List<Friend>>(emptyList())
    val friends: StateFlow<List<Friend>> = _friends
    private val _friendRequests = MutableStateFlow<List<Pair<String, FriendRequest>>>(emptyList())
    val friendRequests: StateFlow<List<Pair<String, FriendRequest>>> = _friendRequests
    private val currentUserId = auth.currentUser?.uid

    init {
        currentUserId?.let { fetchFriends(it) }
    }


    fun sendFriendRequest(email: String, onResult: (Boolean, String) -> Unit) {
        if (currentUserId == null) {
            onResult(false, "User not logged in")
            return
        }

        db.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { query ->
                if (query.isEmpty) {
                    onResult(false, "Email not registered")
                } else {
                    val toUser = query.documents.first()
                    val toUserId = toUser.id

                    val request = FriendRequest(
                        fromUserId = currentUserId,
                        toUserId = toUserId
                    )

                    db.collection("friendRequests")
                        .add(request)
                        .addOnSuccessListener {
                            onResult(true, "Request sent successfully")
                        }
                        .addOnFailureListener {
                            onResult(false, "Failed to send request")
                        }
                }
            }
            .addOnFailureListener {
                onResult(false, "Error checking email")
            }
    }

    private fun fetchFriends(userId: String) {
        db.collection("friends")
            .whereArrayContains("members", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    Log.e("FriendsViewModel", "Error fetching friends", error)
                    _friends.value = emptyList()
                    return@addSnapshotListener
                }

                Log.d("FriendsViewModel", "Friends snapshot size: ${snapshot.size()}")

                val tempFriends = mutableListOf<Friend>()

                snapshot.documents.forEach { doc ->
                    val members = doc.get("members") as? List<*>
                    Log.d("FriendsViewModel", "Friend doc: ${doc.id}, members=$members")

                    val friendId = members?.firstOrNull { it != userId } as? String
                    Log.d("FriendsViewModel", "Other friendId: $friendId")

                    if (friendId != null) {
                        db.collection("users").document(friendId).get()
                            .addOnSuccessListener { userDoc ->
                                val email = userDoc.getString("email") ?: "Unknown"
                                val name = userDoc.getString("name") ?: "Unknown"  // 👈 fetch name
                                Log.d("FriendsViewModel", "Fetched email: $email, name: $name")

                                // add friend to list
                                tempFriends.add(Friend(uid = friendId, email = email, name = name))

                                // update StateFlow with latest list
                                _friends.value = tempFriends.toList()
                            }
                            .addOnFailureListener {
                                Log.e("FriendsViewModel", "Failed to fetch user: $friendId", it)
                            }
                    }

                }
            }
    }

    fun fetchFriendRequests(userId: String) {
        db.collection("friendRequests")
            .whereEqualTo("toUserId", userId)
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    _friendRequests.value = emptyList()
                    return@addSnapshotListener
                }

                val list = snapshot.documents.mapNotNull { doc ->
                    val request = doc.toObject(FriendRequest::class.java)
                    request?.let { doc.id to it }
                }
                _friendRequests.value = list
            }
    }

    // ✅ Accept request
    fun acceptRequest(requestId: String, request: FriendRequest) {
        val friendsRef = db.collection("friends").document()

        db.runBatch { batch ->
            // mark request as accepted
            batch.update(
                db.collection("friendRequests").document(requestId),
                "status", "accepted"
            )

            // ✅ add to friends collection
            batch.set(
                friendsRef,
                mapOf(
                    "members" to listOf(request.fromUserId, request.toUserId),
                    "timestamp" to System.currentTimeMillis()
                )
            )
        }
    }



    fun rejectRequest(requestId: String) {
        db.collection("friendRequests").document(requestId)
            .update("status", "rejected")
    }
}
