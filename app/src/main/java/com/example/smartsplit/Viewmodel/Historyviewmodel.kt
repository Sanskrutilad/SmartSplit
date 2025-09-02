package com.example.smartsplit.Viewmodel

import androidx.lifecycle.ViewModel
import com.example.smartsplit.screens.history.ActionType
import com.example.smartsplit.screens.history.HistoryItem
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID
import java.util.logging.Logger

data class ActivityLog(
    val id: String = UUID.randomUUID().toString(),  // Unique ID
    val type: String,    // "GROUP_CREATED", "GROUP_DELETED", "EXPENSE_ADDED", etc.
    val description: String, // User-friendly message
    val timestamp: Long = System.currentTimeMillis(),
    val relatedGroupId: String? = null,
    val userId: String? = null
)

fun logActivity(
    type: String,
    description: String,
    relatedGroupId: String? = null,
    userId: String? = null
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
}

class HistoryViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val _history = MutableStateFlow<List<HistoryItem>>(emptyList())
    val history: StateFlow<List<HistoryItem>> = _history

    init {
        fetchHistory()
    }

    private fun fetchHistory() {
        db.collection("historyLogs")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    Logger.getGlobal().warning("Error fetching history: ${error?.message}")
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
                            title = typeStr.replace("_", " ").capitalize(),
                            description = doc.getString("description") ?: "",
                            timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                            type = type
                        )
                    } catch (e: Exception) {
                        Logger.getGlobal().warning("Failed to parse doc ${doc.id}: ${e.message}")
                        null
                    }
                }

                _history.value = items
                Logger.getGlobal().info("Fetched ${items.size} history items")
            }
    }
}