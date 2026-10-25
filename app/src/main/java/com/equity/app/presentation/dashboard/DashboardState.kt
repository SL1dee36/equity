package com.equity.app.presentation.dashboard

import com.equity.app.domain.model.CategoryExpenseSummary
import com.equity.app.domain.model.EquityFormulaResult
import com.equity.app.domain.model.FinancialSummary
import com.equity.app.domain.model.MonthlyCashFlow
import com.equity.app.domain.model.SavingsGoal
import com.equity.app.domain.model.Transaction

data class DashboardState(
    val financialSummary: FinancialSummary = FinancialSummary(),
    val activeSavingsGoal: SavingsGoal? = null,
    val equityFormulaResult: EquityFormulaResult? = null,
    val recentTransactions: List<Transaction> = emptyList(),
    val categoryBreakdown: List<CategoryExpenseSummary> = emptyList(),
    val monthlyTrends: List<MonthlyCashFlow> = emptyList(),
    val isLoading: Boolean = true,
    val isAddTransactionSheetOpen: Boolean = false
)
