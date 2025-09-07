package com.example.smartsplit.Viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Expense(
    val id: String = "",
    val description: String = "",
    val amount: Double = 0.0,
    val paidBy: String = "",
    val splitBy: String = "",
    val createdAt: Long = 0L,
    val splits: Map<String, Double> = emptyMap()
)

fun formatDate(timestamp: Long): String {
    if (timestamp == 0L) return "No date"
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
data class Settlement(
    val from: String,
    val to: String,
    val amount: Double,
    val paidAmount: Double = 0.0, // Track how much has been paid
    val id: String = "" // Add ID for tracking
) {
    val remainingAmount: Double get() = amount - paidAmount
    val isFullyPaid: Boolean get() = remainingAmount <= 0.01
}
class ExpenseViewModel : ViewModel() {
    private val _paidSettlements = MutableLiveData<List<Settlement>>(emptyList())
    val paidSettlements: LiveData<List<Settlement>> = _paidSettlements
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _groupExpenses = MutableLiveData<List<Expense>>(emptyList())
    val groupExpenses: LiveData<List<Expense>> = _groupExpenses

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message
    private val _settlements = MutableLiveData<List<Settlement>>(emptyList())
    val settlements: LiveData<List<Settlement>> = _settlements
    // Add to ExpenseViewModel
    // In ExpenseViewModel
    private val _friendExpenses = MutableLiveData<List<FriendExpense>>(emptyList())
    val friendExpenses: LiveData<List<FriendExpense>> = _friendExpenses
    // In ExpenseViewModel

    fun markSettlementPartiallyPaid(
        settlement: Settlement,
        groupId: String,
        amountPaid: Double,
        onComplete: (Boolean) -> Unit = {}
    ) {
        val currentUserId = auth.currentUser?.uid ?: return
        val settlementsRef = db.collection("groups")
            .document(groupId)
            .collection("settlements")

        if (amountPaid <= 0) {
            _message.value = "Payment amount must be greater than zero"
            onComplete(false)
            return
        }

        if (amountPaid > settlement.remainingAmount) {
            _message.value = "Payment amount cannot exceed remaining balance"
            onComplete(false)
            return
        }

        // Check if this settlement already exists
        settlementsRef
            .whereEqualTo("from", settlement.from)
            .whereEqualTo("to", settlement.to)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    // New settlement - create with partial payment
                    val newPaidAmount = amountPaid
                    val settlementData = hashMapOf(
                        "from" to settlement.from,
                        "to" to settlement.to,
                        "originalAmount" to settlement.amount, // Store original amount
                        "paidAmount" to newPaidAmount,
                        "remainingAmount" to (settlement.amount - newPaidAmount),
                        "lastUpdated" to System.currentTimeMillis(),
                        "createdAt" to System.currentTimeMillis()
                    )

                    settlementsRef.add(settlementData)
                        .addOnSuccessListener {
                            _message.value = "Partial payment recorded! Remaining: ₹${"%.2f".format(settlement.amount - newPaidAmount)}"
                            fetchSettlements(groupId)
                            onComplete(true)
                        }
                        .addOnFailureListener { e ->
                            _message.value = "Error recording partial payment: ${e.message}"
                            onComplete(false)
                        }
                } else {
                    // Existing settlement - update the paid amount
                    val doc = querySnapshot.documents.first()
                    val currentPaidAmount = doc.getDouble("paidAmount") ?: 0.0
                    val currentRemainingAmount = doc.getDouble("remainingAmount") ?: settlement.amount
                    val newPaidAmount = currentPaidAmount + amountPaid
                    val newRemainingAmount = currentRemainingAmount - amountPaid

                    val updateData = hashMapOf(
                        "paidAmount" to newPaidAmount,
                        "remainingAmount" to newRemainingAmount,
                        "lastUpdated" to System.currentTimeMillis()
                    )

                    doc.reference.update(updateData as Map<String, Any>)
                        .addOnSuccessListener {
                            _message.value = if (newRemainingAmount <= 0.01) {
                                "Payment completed! Fully settled."
                            } else {
                                "Partial payment recorded! Remaining: ₹${"%.2f".format(newRemainingAmount)}"
                            }
                            fetchSettlements(groupId)
                            onComplete(true)
                        }
                        .addOnFailureListener { e ->
                            _message.value = "Error updating payment: ${e.message}"
                            onComplete(false)
                        }
                }
            }
            .addOnFailureListener { e ->
                _message.value = "Error checking settlement: ${e.message}"
                onComplete(false)
            }
    }

    data class FriendSettlement(
        val from: String,
        val to: String,
        val amount: Double,
        val createdAt: Long = System.currentTimeMillis(),
        val expenseIds: List<String> = emptyList() // Track which expenses this settlement covers
    )

    // Add these to your ExpenseViewModel class
    private val _friendSettlements = MutableLiveData<List<FriendSettlement>>(emptyList())
    val friendSettlements: LiveData<List<FriendSettlement>> = _friendSettlements


    data class FriendExpense(
        val id: String = "",
        val description: String = "",
        val amount: Double = 0.0,
        val paidBy: String = "",
        val splitBy: String = "",
        val createdAt: Long = 0L,
        val splits: Map<String, Double> = emptyMap(),
        val friendId: String = "" // The friend's UID
    )

    fun fetchSettlements(groupId: String) {
        db.collection("groups")
            .document(groupId)
            .collection("settlements")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    _message.value = "Error fetching settlements: ${e.message}"
                    return@addSnapshotListener
                }

                val settlementsList = snapshot?.documents?.mapNotNull { doc ->
                    val from = doc.getString("from") ?: ""
                    val to = doc.getString("to") ?: ""
                    val originalAmount = doc.getDouble("originalAmount") ?: doc.getDouble("amount") ?: 0.0
                    val paidAmount = doc.getDouble("paidAmount") ?: 0.0
                    val id = doc.id

                    Settlement(
                        from = from,
                        to = to,
                        amount = originalAmount,
                        paidAmount = paidAmount,
                        id = id
                    )
                } ?: emptyList()

                _settlements.value = settlementsList
            }
    }

    fun fetchPaidSettlements(groupId: String) {
        db.collection("groups")
            .document(groupId)
            .collection("settlements")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    _message.value = "Error fetching settlements: ${e.message}"
                    return@addSnapshotListener
                }

                val paid = snapshot?.documents?.mapNotNull { doc ->
                    val from = doc.getString("from") ?: return@mapNotNull null
                    val to = doc.getString("to") ?: return@mapNotNull null
                    val amount = doc.getDouble("amount") ?: return@mapNotNull null
                    Settlement(from, to, amount)
                } ?: emptyList()

                _paidSettlements.value = paid
            }
    }

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

    fun calculateNetSettlements(expenses: List<Expense>, existingSettlements: List<Settlement>): List<Settlement> {
        // Calculate balances from expenses
        val balances = calculateBalances(expenses).toMutableMap()

        // Subtract already settled amounts (only the paid portions)
        for (settlement in existingSettlements) {
            balances[settlement.from] = balances.getOrDefault(settlement.from, 0.0) + settlement.paidAmount
            balances[settlement.to] = balances.getOrDefault(settlement.to, 0.0) - settlement.paidAmount
        }

        // Now calculate the net settlements needed
        val creditors = ArrayDeque(balances.filter { it.value > 0.01 }.toList())
        val debtors = ArrayDeque(balances.filter { it.value < -0.01 }.toList())
        val netSettlements = mutableListOf<Settlement>()

        while (creditors.isNotEmpty() && debtors.isNotEmpty()) {
            val (creditorId, credit) = creditors.removeFirst()
            val (debtorId, debt) = debtors.removeFirst()

            val amount = minOf(credit, -debt)
            netSettlements.add(Settlement(
                from = debtorId,
                to = creditorId,
                amount = amount,
                id = "${debtorId}_${creditorId}_${System.currentTimeMillis()}"
            ))

            val newCredit = credit - amount
            val newDebt = debt + amount

            if (newCredit > 0.01) creditors.addFirst(creditorId to newCredit)
            if (newDebt < -0.01) debtors.addFirst(debtorId to newDebt)
        }

        return netSettlements
    }


}

// --- elsewhere, GroupMember remains the same as before ---
