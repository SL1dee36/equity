package com.equity.app.domain.usecase

import com.equity.app.domain.model.CategoryExpenseSummary
import com.equity.app.domain.model.MonthlyCashFlow
import com.equity.app.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

class GetAnalyticsUseCase(
    private val repository: TransactionRepository
) {
    fun getCategoryExpenseBreakdown(): Flow<List<CategoryExpenseSummary>> {
        return repository.getCategoryExpenseBreakdown()
    }

    fun getMonthlyCashFlowTrends(): Flow<List<MonthlyCashFlow>> {
        return repository.getMonthlyCashFlowTrends()
    }
}
