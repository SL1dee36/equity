package com.equity.app.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equity.app.domain.model.CategoryExpenseSummary
import com.equity.app.domain.model.EquityFormulaResult
import com.equity.app.domain.model.MonthlyCashFlow
import com.equity.app.domain.model.Transaction
import com.equity.app.domain.usecase.AddTransactionUseCase
import com.equity.app.domain.usecase.CalculateEquityFormulaUseCase
import com.equity.app.domain.usecase.GetActiveSavingsGoalUseCase
import com.equity.app.domain.usecase.GetAnalyticsUseCase
import com.equity.app.domain.usecase.GetFinancialSummaryUseCase
import com.equity.app.domain.usecase.GetTransactionsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private data class AnalyticsBundle(
    val categoryBreakdown: List<CategoryExpenseSummary>,
    val monthlyTrends: List<MonthlyCashFlow>,
    val formulaResult: EquityFormulaResult?,
    val isAddSheetOpen: Boolean
)

class DashboardViewModel(
    getFinancialSummaryUseCase: GetFinancialSummaryUseCase,
    getActiveSavingsGoalUseCase: GetActiveSavingsGoalUseCase,
    getTransactionsUseCase: GetTransactionsUseCase,
    getAnalyticsUseCase: GetAnalyticsUseCase,
    calculateEquityFormulaUseCase: CalculateEquityFormulaUseCase,
    private val addTransactionUseCase: AddTransactionUseCase
) : ViewModel() {

    private val _isAddSheetOpen = MutableStateFlow(false)

    private val overviewFlow = combine(
        getFinancialSummaryUseCase(),
        getActiveSavingsGoalUseCase(),
        getTransactionsUseCase()
    ) { summary, goal, transactions ->
        Triple(summary, goal, transactions)
    }

    private val analyticsFlow = combine(
        getAnalyticsUseCase.getCategoryExpenseBreakdown(),
        getAnalyticsUseCase.getMonthlyCashFlowTrends(),
        calculateEquityFormulaUseCase(),
        _isAddSheetOpen
    ) { categoryBreakdown, monthlyTrends, formulaResult, isAddSheetOpen ->
        AnalyticsBundle(
            categoryBreakdown = categoryBreakdown,
            monthlyTrends = monthlyTrends,
            formulaResult = formulaResult,
            isAddSheetOpen = isAddSheetOpen
        )
    }

    val uiState: StateFlow<DashboardState> = combine(
        overviewFlow,
        analyticsFlow
    ) { (summary, goal, transactions), analytics ->
        DashboardState(
            financialSummary = summary,
            activeSavingsGoal = goal,
            equityFormulaResult = analytics.formulaResult,
            recentTransactions = transactions.take(5),
            categoryBreakdown = analytics.categoryBreakdown,
            monthlyTrends = analytics.monthlyTrends,
            isLoading = false,
            isAddTransactionSheetOpen = analytics.isAddSheetOpen
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardState(isLoading = true)
    )

    fun openAddTransactionSheet() {
        _isAddSheetOpen.update { true }
    }

    fun closeAddTransactionSheet() {
        _isAddSheetOpen.update { false }
    }

    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            addTransactionUseCase(transaction)
            closeAddTransactionSheet()
        }
    }
}
