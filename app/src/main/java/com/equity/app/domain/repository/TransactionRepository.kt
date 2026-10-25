package com.equity.app.domain.repository

import com.equity.app.domain.model.CategoryExpenseSummary
import com.equity.app.domain.model.FinancialSummary
import com.equity.app.domain.model.MonthlyCashFlow
import com.equity.app.domain.model.Transaction
import com.equity.app.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun getAllTransactions(): Flow<List<Transaction>>
    fun getTransactionsByType(type: TransactionType): Flow<List<Transaction>>
    fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<Transaction>>
    fun getFinancialSummary(): Flow<FinancialSummary>
    fun getCategoryExpenseBreakdown(): Flow<List<CategoryExpenseSummary>>
    fun getMonthlyCashFlowTrends(): Flow<List<MonthlyCashFlow>>
    suspend fun getTransactionById(id: Long): Transaction?
    suspend fun addTransaction(transaction: Transaction): Long
    suspend fun updateTransaction(transaction: Transaction)
    suspend fun deleteTransaction(transaction: Transaction)
    suspend fun deleteTransactionById(id: Long)
}
