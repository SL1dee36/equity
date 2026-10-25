package com.equity.app.domain.model

data class Transaction(
    val id: Long = 0,
    val amount: Double,
    val type: TransactionType,
    val category: TransactionCategory,
    val date: Long,
    val note: String? = null,
    val isRecurring: Boolean = false
)
