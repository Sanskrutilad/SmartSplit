package com.example.smartsplit.Viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.math.BigDecimal
import java.math.RoundingMode

data class Expense(
    val id: String = "",
    val description: String = "",
    val amount: Double = 0.0,
    val paidBy: String = "",
    val splitBy: String = "",
    val createdAt: Long = 0L,
    val splits: Map<String, Double> = emptyMap()
)
data class Settlement(val from: String, val to: String, val amount: Double)
class ExpenseViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _groupExpenses = MutableLiveData<List<Expense>>(emptyList())
    val groupExpenses: LiveData<List<Expense>> = _groupExpenses

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    fun addExpense(
        description: String,
        amount: Double,
        paidBy: String?,
        splitBy: String,
        members: List<GroupMember>,
        groupId: String? = null,
        friendId: String? = null,
        splitInputs: Map<String, String> = emptyMap()
    ) {
        val currentUserId = auth.currentUser?.uid ?: return
        val payerId = if (paidBy == "You") currentUserId else paidBy ?: currentUserId

        // 1. Calculate splits using BigDecimal
        val splits = mutableMapOf<String, Double>()
        when (splitBy) {
            "Equally" -> {
                val share = BigDecimal(amount)
                    .divide(BigDecimal(members.size), 2, RoundingMode.HALF_UP)
                members.forEach { splits[it.uid] = share.toDouble() }
            }
            "By shares" -> {
                val totalShares = splitInputs.values.sumOf { it.toDoubleOrNull() ?: 0.0 }
                if (totalShares > 0) {
                    members.forEach { member ->
                        val share = BigDecimal(splitInputs[member.uid]?.toDoubleOrNull() ?: 0.0)
                        val totalSharesBD = BigDecimal(totalShares)
                        val bdAmount = BigDecimal(amount)
                        val fraction = if (totalSharesBD.compareTo(BigDecimal.ZERO) != 0) {
                            share.divide(totalSharesBD, 10, RoundingMode.HALF_UP)
                        } else BigDecimal.ZERO
                        val memberAmount = fraction.multiply(bdAmount).setScale(2, RoundingMode.HALF_UP)
                        splits[member.uid] = memberAmount.toDouble()
                    }
                }
            }
            "By percentage" -> {
                members.forEach { member ->
                    val percent = BigDecimal(splitInputs[member.uid]?.toDoubleOrNull() ?: 0.0)
                    val bdAmount = BigDecimal(amount)
                    val memberAmount = percent.divide(BigDecimal(100), 10, RoundingMode.HALF_UP)
                        .multiply(bdAmount)
                        .setScale(2, RoundingMode.HALF_UP)
                    splits[member.uid] = memberAmount.toDouble()
                }
            }
        }

        // 2. Firestore object
        val expense = hashMapOf(
            "description" to description,
            "amount" to amount,
            "paidBy" to payerId,
            "splitBy" to splitBy,
            "createdAt" to System.currentTimeMillis(),
            "splits" to splits
        )

        // 3. Save in Firestore
        when {
            groupId != null -> {
                db.collection("groups")
                    .document(groupId)
                    .collection("expenses")
                    .add(expense)
                    .addOnSuccessListener { _message.value = "Expense added successfully!" }
                    .addOnFailureListener { e -> _message.value = "Error adding group expense: ${e.message}" }
            }
            friendId != null -> {
                db.collection("users")
                    .document(currentUserId)
                    .collection("friends")
                    .document(friendId)
                    .collection("expenses")
                    .add(expense)
                    .addOnSuccessListener { _message.value = "Expense added with friend" }
                    .addOnFailureListener { e -> _message.value = "Error adding friend expense: ${e.message}" }
            }
            else -> _message.value = "No friendId or groupId selected"
        }
    }

    fun fetchGroupExpenses(groupId: String) {
        db.collection("groups")
            .document(groupId)
            .collection("expenses")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    _message.value = "Error fetching expenses: ${e.message}"
                    return@addSnapshotListener
                }

                val expenses = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Expense::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                _groupExpenses.value = expenses
            }
    }
    private fun calculateBalances(expenses: List<Expense>): Map<String, Double> {
        val balances = mutableMapOf<String, Double>()

        for (expense in expenses) {
            val payer = expense.paidBy
            val amount = expense.amount
            val splits = expense.splits

            // make sure payer is initialized
            balances[payer] = balances.getOrDefault(payer, 0.0)

            // subtract each member's share
            for ((memberId, share) in splits) {
                balances[memberId] = balances.getOrDefault(memberId, 0.0) - share
            }

            // add full amount back to payer
            balances[payer] = balances.getOrDefault(payer, 0.0) + amount
        }

        return balances
    }

    fun calculateSettlements(expenses: List<Expense>): List<Settlement> {
        val balances = calculateBalances(expenses).toMutableMap()
        val creditors = ArrayDeque(balances.filter { it.value > 0 }.toList())
        val debtors = ArrayDeque(balances.filter { it.value < 0 }.toList())
        val settlements = mutableListOf<Settlement>()

        while (creditors.isNotEmpty() && debtors.isNotEmpty()) {
            val (creditorId, credit) = creditors.removeFirst()
            val (debtorId, debt) = debtors.removeFirst()

            val amount = minOf(credit, -debt)

            settlements.add(Settlement(from = debtorId, to = creditorId, amount = amount))

            val newCredit = credit - amount
            val newDebt = debt + amount

            if (newCredit > 0.01) creditors.addFirst(creditorId to newCredit)
            if (newDebt < -0.01) debtors.addFirst(debtorId to newDebt)
        }

        return settlements
    }


}

// --- elsewhere, GroupMember remains the same as before ---
