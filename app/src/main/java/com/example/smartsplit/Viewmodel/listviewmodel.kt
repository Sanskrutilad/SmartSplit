package com.example.smartsplit.Viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ListViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _lists = MutableStateFlow<List<UserList>>(emptyList())
    val lists: StateFlow<List<UserList>> = _lists

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    init {
        Log.d("ListViewModel", "Init: currentUserId=$currentUserId")
        currentUserId?.let { observeLists(it) }
    }

    private fun observeLists(userId: String) {
        Log.d("ListViewModel", "observeLists() called for userId=$userId")

        db.collection("userLists")
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ListViewModel", "SnapshotListener error", error)
                    _lists.value = emptyList()
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    Log.w("ListViewModel", "Snapshot is null")
                    _lists.value = emptyList()
                    return@addSnapshotListener
                }

                Log.d("ListViewModel", "Snapshot received: size=${snapshot.size()}")

                val userLists = snapshot.documents.mapNotNull { doc ->
                    try {
                        val items = (doc.get("items") as? List<Map<String, Any?>>)?.map {
                            ListItem(
                                name = it["name"] as? String ?: "",
                                price = (it["price"] as? Number)?.toDouble(),
                                quantity = (it["quantity"] as? Number)?.toInt() ?: 1  // ✅ read quantity
                            )
                        } ?: emptyList()


                        val list = UserList(
                            id = doc.id,
                            title = doc.getString("title") ?: "Untitled",
                            createdAt = doc.getLong("createdAt") ?: 0L,
                            items = items
                        )
                        Log.d("ListViewModel", "Parsed document: $list")
                        list
                    } catch (e: Exception) {
                        Log.e("ListViewModel", "Error parsing document: ${doc.id}", e)
                        null
                    }
                }

                Log.d("ListViewModel", "Final list count=${userLists.size}")
                _lists.value = userLists
            }
    }

    fun createNewList(title: String, onComplete: (Boolean) -> Unit = {}) {
        val uid = currentUserId ?: run {
            Log.e("ListViewModel", "createNewList() failed: userId is null")
            return
        }

        val newList = hashMapOf(
            "userId" to uid,
            "title" to title,
            "createdAt" to System.currentTimeMillis(),
            "items" to emptyList<Map<String, Any>>() // ✅ empty ListItem list
        )

        Log.d("ListViewModel", "Creating new list: $newList")

        db.collection("userLists")
            .add(newList)
            .addOnSuccessListener { docRef ->
                Log.d("ListViewModel", "List created successfully with id=${docRef.id}")
                onComplete(true)
            }
            .addOnFailureListener { e ->
                Log.e("ListViewModel", "Failed to create list", e)
                onComplete(false)
            }
    }

    fun addItem(listId: String, item: String, price: Double? = null, onComplete: (Boolean) -> Unit = {}) {
        val newItem = mapOf(
            "name" to item,
            "price" to price,
            "quantity" to 1
        )
        db.collection("userLists")
            .document(listId)
            .update("items", FieldValue.arrayUnion(newItem))
            .addOnSuccessListener {
                Log.d("ListViewModel", "Item added successfully to $listId")
                onComplete(true)
            }
            .addOnFailureListener { e ->
                Log.e("ListViewModel", "Failed to add item", e)
                onComplete(false)
            }
    }

    fun removeItem(listId: String, item: ListItem, onComplete: (Boolean) -> Unit = {}) {
        val itemMap = mapOf(
            "name" to item.name,
            "price" to item.price
        )

        db.collection("userLists")
            .document(listId)
            .update("items", FieldValue.arrayRemove(itemMap))
            .addOnSuccessListener {
                Log.d("ListViewModel", "Item removed successfully from $listId")
                onComplete(true)
            }
            .addOnFailureListener { e ->
                Log.e("ListViewModel", "Failed to remove item", e)
                onComplete(false)
            }
    }

    fun increaseQuantity(listId: String, item: ListItem) {
        val oldItem = mapOf(
            "name" to item.name,
            "price" to item.price,
            "quantity" to item.quantity
        )
        val newItem = mapOf(
            "name" to item.name,
            "price" to item.price,
            "quantity" to item.quantity + 1
        )
        db.collection("userLists").document(listId)
            .update("items", FieldValue.arrayRemove(oldItem))
            .addOnSuccessListener {
                db.collection("userLists").document(listId)
                    .update("items", FieldValue.arrayUnion(newItem))
            }
    }


}

data class ListItem(
    val name: String = "",
    val price: Double? = null,
    val quantity: Int = 1
)


data class UserList(
    val id: String,
    val title: String,
    val createdAt: Long,
    val items: List<ListItem>
)
