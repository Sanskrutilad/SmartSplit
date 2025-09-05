package com.example.smartsplit.Viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ExpenseViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun addExpense(
        description: String,
        amount: Double,
        paidBy: String?,
        splitBy: String,
        groupId: String? = null,
        friendId: String? = null
    ) {
        val currentUserId = auth.currentUser?.uid ?: return

        val expense = hashMapOf(
            "description" to description,
            "amount" to amount,
            "paidBy" to if (paidBy == "You") currentUserId else paidBy,
            "splitBy" to splitBy,
            "createdAt" to System.currentTimeMillis()
        )

        when {
            groupId != null -> {
                // Save under group expenses
                db.collection("groups")
                    .document(groupId)
                    .collection("expenses")
                    .add(expense)
                    .addOnSuccessListener {
                        Log.d("ExpenseViewModel", "Expense added to group $groupId")
                    }
                    .addOnFailureListener { e ->
                        Log.e("ExpenseViewModel", "Error adding group expense", e)
                    }
            }
            friendId != null -> {
                // Save under friend expenses
                db.collection("users")
                    .document(currentUserId)
                    .collection("friends")
                    .document(friendId)
                    .collection("expenses")
                    .add(expense)
                    .addOnSuccessListener {
                        Log.d("ExpenseViewModel", "Expense added with friend $friendId")
                    }
                    .addOnFailureListener { e ->
                        Log.e("ExpenseViewModel", "Error adding friend expense", e)
                    }
            }
            else -> {
                Log.w("ExpenseViewModel", "No friendId or groupId selected")
            }
        }
    }
}
