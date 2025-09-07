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
data class Settlement(val from: String, val to: String, val amount: Double)
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
    data class CombinedExpense(
        val id: String = "",
        val description: String = "",
        val amount: Double = 0.0,
        val paidBy: String = "",
        val splitBy: String = "",
        val createdAt: Long = 0L,
        val splits: Map<String, Double> = emptyMap(),
        val type: String = "", // "friend" or "group"
        val groupId: String? = null, // For group expenses
        val friendId: String? = null // For friend expenses
    )

    private val _combinedExpenses = MutableLiveData<List<CombinedExpense>>(emptyList())
    val combinedExpenses: LiveData<List<CombinedExpense>> = _combinedExpenses

    fun fetchAllExpensesForFriend(currentUserId: String, friendId: String) {
        val allExpenses = mutableListOf<CombinedExpense>()

        // 1. Fetch friend expenses
        db.collection("users")
            .document(currentUserId)
            .collection("friends")
            .document(friendId)
            .collection("expenses")
            .get()
            .addOnSuccessListener { expensesSnapshot ->
                expensesSnapshot.documents.forEach { doc ->
                    val expense = doc.toObject(Expense::class.java)
                    expense?.let {
                        allExpenses.add(
                            CombinedExpense(
                                id = doc.id,
                                description = it.description,
                                amount = it.amount,
                                paidBy = it.paidBy,
                                splitBy = it.splitBy,
                                createdAt = it.createdAt,
                                splits = it.splits,
                                type = "friend",
                                friendId = friendId
                            )
                        )
                    }
                }

                // 2. Fetch all groups current user is in
                db.collection("groups")
                    .get()
                    .addOnSuccessListener { groupsSnapshot ->
                        groupsSnapshot.documents.forEach { groupDoc ->
                            val groupId = groupDoc.id

                            // Check if friend is also in this group
                            db.collection("groups")
                                .document(groupId)
                                .collection("members")
                                .document(friendId)
                                .get()
                                .addOnSuccessListener { friendMemberDoc ->
                                    if (friendMemberDoc.exists()) {
                                        // Friend is in the group, fetch expenses
                                        db.collection("groups")
                                            .document(groupId)
                                            .collection("expenses")
                                            .get()
                                            .addOnSuccessListener { groupExpensesSnapshot ->
                                                groupExpensesSnapshot.documents.forEach { expenseDoc ->
                                                    val expense = expenseDoc.toObject(Expense::class.java)
                                                    expense?.let {
                                                        if (it.splits.containsKey(friendId) || it.paidBy == friendId) {
                                                            allExpenses.add(
                                                                CombinedExpense(
                                                                    id = expenseDoc.id,
                                                                    description = it.description,
                                                                    amount = it.amount,
                                                                    paidBy = it.paidBy,
                                                                    splitBy = it.splitBy,
                                                                    createdAt = it.createdAt,
                                                                    splits = it.splits,
                                                                    type = "group",
                                                                    groupId = groupId
                                                                )
                                                            )
                                                        }
                                                    }
                                                }
                                                _combinedExpenses.value = allExpenses
                                            }
                                    }
                                }
                        }
                    }
            }
    }
// In ExpenseViewModel.kt - Add these methods

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

    fun fetchFriendSettlements(currentUserId: String, friendId: String) {
        val settlementsRef = db.collection("users")
            .document(currentUserId)
            .collection("friends")
            .document(friendId)
            .collection("settlements")

        settlementsRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                _message.value = "Error fetching friend settlements: ${e.message}"
                return@addSnapshotListener
            }

            val settlements = snapshot?.documents?.mapNotNull { doc ->
                val from = doc.getString("from") ?: ""
                val to = doc.getString("to") ?: ""
                val amount = doc.getDouble("amount") ?: 0.0
                val createdAt = doc.getLong("createdAt") ?: 0L
                val expenseIds = doc.get("expenseIds") as? List<String> ?: emptyList()
                FriendSettlement(from, to, amount, createdAt, expenseIds)
            } ?: emptyList()

            _friendSettlements.value = settlements
        }
    }

    fun markFriendSettlementPaid(
        currentUserId: String,
        friendId: String,
        amount: Double,
        expenseIds: List<String>,
        onComplete: (Boolean) -> Unit = {}
    ) {
        val settlementData = hashMapOf(
            "from" to currentUserId,
            "to" to friendId,
            "amount" to amount,
            "createdAt" to System.currentTimeMillis(),
            "expenseIds" to expenseIds
        )

        db.collection("users")
            .document(currentUserId)
            .collection("friends")
            .document(friendId)
            .collection("settlements")
            .add(settlementData)
            .addOnSuccessListener {
                _message.value = "Settlement recorded!"
                onComplete(true)
            }
            .addOnFailureListener { e ->
                _message.value = "Error recording settlement: ${e.message}"
                onComplete(false)
            }
    }
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
    fun fetchAllFriendExpenses(currentUserId: String) {
        db.collection("users")
            .document(currentUserId)
            .collection("friends")
            .get()
            .addOnSuccessListener { friendsSnapshot ->
                val allExpenses = mutableListOf<FriendExpense>()
                val friendIds = friendsSnapshot.documents.map { it.id }

                friendIds.forEach { friendId ->
                    db.collection("users")
                        .document(currentUserId)
                        .collection("friends")
                        .document(friendId)
                        .collection("expenses")
                        .get()
                        .addOnSuccessListener { expensesSnapshot ->
                            expensesSnapshot.documents.forEach { doc ->
                                val expense = doc.toObject(Expense::class.java)
                                expense?.let {
                                    allExpenses.add(FriendExpense(
                                        id = doc.id,
                                        description = it.description,
                                        amount = it.amount,
                                        paidBy = it.paidBy,
                                        splitBy = it.splitBy,
                                        createdAt = it.createdAt,
                                        splits = it.splits,
                                        friendId = friendId
                                    ))
                                }
                            }
                            _friendExpenses.value = allExpenses
                        }
                }
            }
    }
    // Add this function to your ExpenseViewModel class
    fun fetchFriendExpenses(currentUserId: String, friendId: String) {
        db.collection("users")
            .document(currentUserId)
            .collection("friends")
            .document(friendId)
            .collection("expenses")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    _message.value = "Error fetching friend expenses: ${e.message}"
                    return@addSnapshotListener
                }

                val expenses = snapshot?.documents?.mapNotNull { doc ->
                    val expense = doc.toObject(Expense::class.java)
                    expense?.let {
                        FriendExpense(
                            id = doc.id,
                            description = it.description,
                            amount = it.amount,
                            paidBy = it.paidBy,
                            splitBy = it.splitBy,
                            createdAt = it.createdAt,
                            splits = it.splits,
                            friendId = friendId
                        )
                    }
                } ?: emptyList()

                // Update the friend expenses list
                val currentExpenses = _friendExpenses.value ?: emptyList()
                val filteredExpenses = currentExpenses.filter { it.friendId != friendId }
                _friendExpenses.value = filteredExpenses + expenses
            }
    }
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
                    val amount = doc.getDouble("amount") ?: 0.0
                    Settlement(from, to, amount)
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

        // Subtract already settled amounts
        for (settlement in existingSettlements) {
            balances[settlement.from] = balances.getOrDefault(settlement.from, 0.0) + settlement.amount
            balances[settlement.to] = balances.getOrDefault(settlement.to, 0.0) - settlement.amount
        }

        // Now calculate the net settlements needed
        val creditors = ArrayDeque(balances.filter { it.value > 0.01 }.toList())
        val debtors = ArrayDeque(balances.filter { it.value < -0.01 }.toList())
        val netSettlements = mutableListOf<Settlement>()

        while (creditors.isNotEmpty() && debtors.isNotEmpty()) {
            val (creditorId, credit) = creditors.removeFirst()
            val (debtorId, debt) = debtors.removeFirst()

            val amount = minOf(credit, -debt)
            netSettlements.add(Settlement(from = debtorId, to = creditorId, amount = amount))

            val newCredit = credit - amount
            val newDebt = debt + amount

            if (newCredit > 0.01) creditors.addFirst(creditorId to newCredit)
            if (newDebt < -0.01) debtors.addFirst(debtorId to newDebt)
        }

        return netSettlements
    }
    fun markSettlementPaid(settlement: Settlement, groupId: String, onComplete: (Boolean) -> Unit = {}) {
        val settlementsRef = db.collection("groups")
            .document(groupId)
            .collection("settlements")

        // Check if this settlement already exists to avoid duplicates
        settlementsRef
            .whereEqualTo("from", settlement.from)
            .whereEqualTo("to", settlement.to)
            .whereEqualTo("amount", settlement.amount)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    // Settlement doesn't exist, add it
                    val settlementData = hashMapOf(
                        "from" to settlement.from,
                        "to" to settlement.to,
                        "amount" to settlement.amount,
                        "paidAt" to System.currentTimeMillis()
                    )

                    settlementsRef.add(settlementData)
                        .addOnSuccessListener {
                            _message.value = "Settlement recorded!"
                            fetchSettlements(groupId) // Refresh settlements
                            onComplete(true)
                        }
                        .addOnFailureListener { e ->
                            _message.value = "Error settling payment: ${e.message}"
                            onComplete(false)
                        }
                } else {
                    // Settlement already exists
                    _message.value = "Settlement already recorded!"
                    fetchSettlements(groupId) // Refresh settlements
                    onComplete(true)
                }
            }
            .addOnFailureListener { e ->
                _message.value = "Error checking settlement: ${e.message}"
                onComplete(false)
            }
    }

}

// --- elsewhere, GroupMember remains the same as before ---
