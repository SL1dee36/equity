package com.equity.app.data.repository

import com.equity.app.data.local.dao.TransactionDao
import com.equity.app.data.local.entity.TransactionEntity
import com.equity.app.domain.model.CategoryExpenseSummary
import com.equity.app.domain.model.FinancialSummary
import com.equity.app.domain.model.MonthlyCashFlow
import com.equity.app.domain.model.Transaction
import com.equity.app.domain.model.TransactionCategory
import com.equity.app.domain.model.TransactionType
import com.equity.app.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TransactionRepositoryImpl(
    private val transactionDao: TransactionDao
) : TransactionRepository {

    override fun getAllTransactions(): Flow<List<Transaction>> {
        return transactionDao.getAllTransactions().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getTransactionsByType(type: TransactionType): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByType(type.name).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByDateRange(startDate, endDate).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getFinancialSummary(): Flow<FinancialSummary> {
        val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)

        return combine(
            transactionDao.getTotalIncomeFlow(),
            transactionDao.getTotalExpenseFlow(),
            transactionDao.getSumByTypeSince(TransactionType.INCOME.name, thirtyDaysAgo),
            transactionDao.getSumByTypeSince(TransactionType.EXPENSE.name, thirtyDaysAgo)
        ) { totalIncome, totalExpense, monthlyIncome, monthlyExpense ->
            val currentBalance = totalIncome - totalExpense
            val savingsRate = if (totalIncome > 0) {
                (((totalIncome - totalExpense) / totalIncome) * 100).coerceIn(-100.0, 100.0).toFloat()
            } else 0f

            FinancialSummary(
                totalIncome = totalIncome,
                totalExpense = totalExpense,
                currentBalance = currentBalance,
                savingsRatePercentage = savingsRate,
                monthlyIncome = if (monthlyIncome > 0) monthlyIncome else totalIncome,
                monthlyExpense = if (monthlyExpense > 0) monthlyExpense else totalExpense
            )
        }
    }

    override fun getCategoryExpenseBreakdown(): Flow<List<CategoryExpenseSummary>> {
        return transactionDao.getExpenseSumsByCategory().map { rows ->
            val totalExpense = rows.sumOf { it.total }
            if (totalExpense <= 0) {
                emptyList()
            } else {
                rows.map { row ->
                    val category = TransactionCategory.fromDisplayName(row.category)
                    val percentage = ((row.total / totalExpense) * 100).toFloat()
                    CategoryExpenseSummary(
                        category = category,
                        totalAmount = row.total,
                        percentage = percentage
                    )
                }.sortedByDescending { it.totalAmount }
            }
        }
    }

    override fun getMonthlyCashFlowTrends(): Flow<List<MonthlyCashFlow>> {
        return transactionDao.getAllTransactions().map { entities ->
            val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())
            val calendar = Calendar.getInstance()

            // Group into last 5 months
            val monthlyData = mutableMapOf<String, Pair<Double, Double>>() // Month -> (Income, Expense)

            // Seed last 4 months in order
            for (i in 3 downTo 0) {
                val cal = Calendar.getInstance().apply {
                    add(Calendar.MONTH, -i)
                }
                val label = monthFormat.format(cal.time)
                monthlyData[label] = Pair(0.0, 0.0)
            }

            for (entity in entities) {
                calendar.timeInMillis = entity.date
                val label = monthFormat.format(calendar.time)
                if (monthlyData.containsKey(label)) {
                    val current = monthlyData[label] ?: Pair(0.0, 0.0)
                    if (entity.type == TransactionType.INCOME.name) {
                        monthlyData[label] = Pair(current.first + entity.amount, current.second)
                    } else {
                        monthlyData[label] = Pair(current.first, current.second + entity.amount)
                    }
                }
            }

            monthlyData.map { (month, flow) ->
                MonthlyCashFlow(
                    monthLabel = month,
                    income = flow.first,
                    expense = flow.second,
                    netSavings = flow.first - flow.second
                )
            }
        }
    }

    override suspend fun getTransactionById(id: Long): Transaction? {
        return transactionDao.getTransactionById(id)?.toDomain()
    }

    override suspend fun addTransaction(transaction: Transaction): Long {
        return transactionDao.insertTransaction(TransactionEntity.fromDomain(transaction))
    }

    override suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.updateTransaction(TransactionEntity.fromDomain(transaction))
    }

    override suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.deleteTransaction(TransactionEntity.fromDomain(transaction))
    }

    override suspend fun deleteTransactionById(id: Long) {
        transactionDao.deleteTransactionById(id)
    }
}
