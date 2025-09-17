package com.example.smartsplit.Viewmodel

import androidx.lifecycle.ViewModel
import com.example.smartsplit.screens.history.ActionType
import com.example.smartsplit.screens.history.HistoryItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID
import java.util.logging.Logger

data class ActivityLog(
    val id: String = UUID.randomUUID().toString(),  // Unique ID
    val type: String,
    val description: String,
    val timestamp: Long = System.currentTimeMillis(),
    val relatedGroupId: String? = null,
    val userId: String? = null
)
enum class ActionType {
    CREATE,
    GROUP_DELETED,
    GROUP_LEFT,
    EXPENSE_ADDED
}

// Add these constants for activity types
object ActivityTypes {
    const val GROUP_DELETED = "GROUP_DELETED"
    const val GROUP_LEFT = "GROUP_LEFT"
    const val EXPENSE_ADDED = "EXPENSE_ADDED"
    const val GROUP_CREATED = "GROUP_CREATED"
}

fun logActivity(
    type: String,
    description: String,
    relatedGroupId: String? = null,
    userId: String? = FirebaseAuth.getInstance().currentUser?.uid
) {
    val log = ActivityLog(
        type = type,
        description = description,
        relatedGroupId = relatedGroupId,
        userId = userId
    )

    FirebaseFirestore.getInstance()
        .collection("historyLogs")
        .document(log.id)
        .set(log)
        .addOnSuccessListener {
            Logger.getGlobal().info("✅ Log added for userId=$userId, type=$type")
        }
        .addOnFailureListener { e ->
            Logger.getGlobal().warning("❌ Failed to add log: ${e.message}")
        }
}
class HistoryViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _history = MutableStateFlow<List<HistoryItem>>(emptyList())
    val history: StateFlow<List<HistoryItem>> = _history
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    init {
        currentUserId?.let { fetchHistory(it) }
    }

    private fun fetchHistory(currentUserId: String) {
        db.collection("historyLogs")
            .whereEqualTo("userId", currentUserId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    _history.value = emptyList()
                    return@addSnapshotListener
                }

                val items = snapshot.documents.mapNotNull { doc ->
                    try {
                        val typeStr = doc.getString("type") ?: "CREATE"
                        val type = runCatching { ActionType.valueOf(typeStr) }
                            .getOrDefault(ActionType.CREATE)

                        HistoryItem(
                            id = doc.id,
                            title = typeStr.replace("_", " ").uppercase(),
                            description = doc.getString("description") ?: "",
                            timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                            type = type
                        )
                    } catch (_: Exception) {
                        null
                    }
                }
                _history.value = items
            }
    }

    // ✅ Create a new list
    fun createNewList(listName: String, onComplete: (Boolean) -> Unit) {
        val uid = currentUserId ?: return
        val newList = hashMapOf(
            "userId" to uid,
            "title" to listName,
            "createdAt" to System.currentTimeMillis(),
            "items" to emptyList<String>() // initially empty
        )

        db.collection("userLists")
            .add(newList)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    // ✅ Add an item to a specific list
    fun addItemToList(listId: String, item: String, onComplete: (Boolean) -> Unit) {
        val listRef = db.collection("userLists").document(listId)

        listRef.update("items", FieldValue.arrayUnion(item))
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    // ✅ Remove an item from a list
    fun removeItemFromList(listId: String, item: String, onComplete: (Boolean) -> Unit) {
        val listRef = db.collection("userLists").document(listId)

        listRef.update("items", FieldValue.arrayRemove(item))
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }
}
