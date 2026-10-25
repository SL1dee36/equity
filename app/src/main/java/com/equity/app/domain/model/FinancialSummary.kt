package com.equity.app.domain.model

data class FinancialSummary(
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val currentBalance: Double = 0.0,
    val savingsRatePercentage: Float = 0f,
    val monthlyIncome: Double = 0.0,
    val monthlyExpense: Double = 0.0
)
