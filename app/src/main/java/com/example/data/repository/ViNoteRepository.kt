package com.example.data.repository

import com.example.data.local.GoalDao
import com.example.data.local.TransactionDao
import com.example.data.model.GoalItem
import com.example.data.model.TransactionItem
import com.example.data.model.TransactionSource
import com.example.data.model.TransactionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ViNoteRepository(
    private val transactionDao: TransactionDao,
    private val goalDao: GoalDao,
    val firestoreSyncRepository: FirestoreExpenseSyncRepository = FirestoreExpenseSyncRepository()
) {
    val allTransactions: Flow<List<TransactionItem>> = transactionDao.getAllTransactions()
    val allGoals: Flow<List<GoalItem>> = goalDao.getAllGoals()

    init {
        // Pre-populate with initial starter data if empty
        CoroutineScope(Dispatchers.IO).launch {
            val existingTx = transactionDao.getAllTransactions().first()
            if (existingTx.isEmpty()) {
                val now = System.currentTimeMillis()
                val hour = 3600 * 1000L
                val day = 24 * hour

                val initialTransactions = listOf(
                    TransactionItem(
                        title = "GrabFood",
                        amount = 25000L,
                        category = "Food",
                        type = TransactionType.EXPENSE,
                        timestamp = now - (5 * 60 * 1000L),
                        timeLabel = "Just now",
                        merchant = "GrabFood",
                        source = TransactionSource.E_WALLET,
                        walletName = "GoPay"
                    ),
                    TransactionItem(
                        title = "Kopi",
                        amount = 35000L,
                        category = "Food",
                        type = TransactionType.EXPENSE,
                        timestamp = now - (4 * hour),
                        timeLabel = "Today, 08:30 AM",
                        merchant = "Kopi Kenangan",
                        source = TransactionSource.MANUAL
                    ),
                    TransactionItem(
                        title = "GoRide",
                        amount = 25000L,
                        category = "Transport",
                        type = TransactionType.EXPENSE,
                        timestamp = now - (2 * hour),
                        timeLabel = "Today, 10:42 AM",
                        merchant = "GoJek",
                        source = TransactionSource.E_WALLET,
                        walletName = "GoPay"
                    ),
                    TransactionItem(
                        title = "Allowance",
                        amount = 500000L,
                        category = "Income",
                        type = TransactionType.INCOME,
                        timestamp = now - day,
                        timeLabel = "Yesterday, 02:15 PM",
                        merchant = "Transfer",
                        source = TransactionSource.MANUAL
                    ),
                    TransactionItem(
                        title = "Lunch Nasi Padang",
                        amount = 45000L,
                        category = "Food",
                        type = TransactionType.EXPENSE,
                        timestamp = now - (day + 3 * hour),
                        timeLabel = "Yesterday, 12:30 PM",
                        merchant = "Padang Sederhana",
                        source = TransactionSource.VOICE
                    )
                )
                transactionDao.insertAll(initialTransactions)
            }

            val existingGoals = goalDao.getAllGoals().first()
            if (existingGoals.isEmpty()) {
                val initialGoals = listOf(
                    GoalItem(
                        title = "New Headphones",
                        targetAmount = 1200000L,
                        currentAmount = 750000L,
                        targetDateDescription = "In 2 months",
                        category = "Gadget",
                        iconName = "headphones",
                        colorHex = "#0057C2"
                    ),
                    GoalItem(
                        title = "Holiday",
                        targetAmount = 2000000L,
                        currentAmount = 1200000L,
                        targetDateDescription = "In 4 months",
                        category = "Travel",
                        iconName = "flight_takeoff",
                        colorHex = "#B06000"
                    ),
                    GoalItem(
                        title = "School",
                        targetAmount = 500000L,
                        currentAmount = 450000L,
                        targetDateDescription = "In 1 month",
                        category = "Education",
                        iconName = "school",
                        colorHex = "#5CE1C6"
                    )
                )
                goalDao.insertAll(initialGoals)
            }
        }
    }

    suspend fun insertTransaction(transaction: TransactionItem): Long {
        val insertedId = transactionDao.insertTransaction(transaction)
        val itemToSync = if (transaction.id == 0L) transaction.copy(id = insertedId) else transaction
        // Sync to remote Firestore in background
        CoroutineScope(Dispatchers.IO).launch {
            firestoreSyncRepository.syncLocalToRemote(listOf(itemToSync))
        }
        return insertedId
    }

    suspend fun deleteTransaction(id: Long) {
        transactionDao.deleteById(id)
        // Also remove from Firestore remote
        CoroutineScope(Dispatchers.IO).launch {
            firestoreSyncRepository.deleteRemoteExpense(id)
        }
    }

    suspend fun clearAllTransactions() {
        transactionDao.clearAll()
        // Also wipe from Firestore remote
        CoroutineScope(Dispatchers.IO).launch {
            firestoreSyncRepository.clearAllRemoteExpenses()
        }
    }

    suspend fun syncExpensesWithFirestore(): SyncResult {
        return firestoreSyncRepository.performFullSync(transactionDao)
    }

    suspend fun insertGoal(goal: GoalItem): Long {
        return goalDao.insertGoal(goal)
    }

    suspend fun updateGoal(goal: GoalItem) {
        goalDao.updateGoal(goal)
    }

    suspend fun deleteGoal(id: Long) {
        goalDao.deleteById(id)
    }
}
