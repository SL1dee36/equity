package com.equity.budget.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val currency: String, // RUB, USD, EUR
    val type: String,     // expense, income
    val categoryId: String,
    val timestamp: Long,
    val isAutoAdded: Boolean = false,
    val recurringRuleId: Long? = null,
    val note: String = ""
)
