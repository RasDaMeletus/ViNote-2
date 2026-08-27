package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType {
    EXPENSE,
    INCOME
}

enum class TransactionSource {
    MANUAL,
    VOICE,
    SCAN,
    E_WALLET
}

@Entity(tableName = "transactions")
data class TransactionItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val amount: Long, // in IDR
    val category: String,
    val type: TransactionType = TransactionType.EXPENSE,
    val timestamp: Long = System.currentTimeMillis(),
    val timeLabel: String = "Today",
    val merchant: String = "",
    val source: TransactionSource = TransactionSource.MANUAL,
    val walletName: String? = null
)
