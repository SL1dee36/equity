package com.equity.app.domain.model

data class CategoryExpenseSummary(
    val category: TransactionCategory,
    val totalAmount: Double,
    val percentage: Float
)

data class MonthlyCashFlow(
    val monthLabel: String,
    val income: Double,
    val expense: Double,
    val netSavings: Double = income - expense
)
