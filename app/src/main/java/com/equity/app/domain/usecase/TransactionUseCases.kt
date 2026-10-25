package com.equity.app.domain.usecase

import com.equity.app.domain.model.FinancialSummary
import com.equity.app.domain.model.Transaction
import com.equity.app.domain.model.TransactionType
import com.equity.app.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

class GetTransactionsUseCase(
    private val repository: TransactionRepository
) {
    operator fun invoke(type: TransactionType? = null): Flow<List<Transaction>> {
        return if (type == null) {
            repository.getAllTransactions()
        } else {
            repository.getTransactionsByType(type)
        }
    }
}

class AddTransactionUseCase(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(transaction: Transaction): Long {
        require(transaction.amount > 0) { "Amount must be greater than zero" }
        return repository.addTransaction(transaction)
    }
}

class UpdateTransactionUseCase(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(transaction: Transaction) {
        require(transaction.amount > 0) { "Amount must be greater than zero" }
        repository.updateTransaction(transaction)
    }
}

class DeleteTransactionUseCase(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(transaction: Transaction) {
        repository.deleteTransaction(transaction)
    }

    suspend operator fun invoke(id: Long) {
        repository.deleteTransactionById(id)
    }
}

class GetFinancialSummaryUseCase(
    private val repository: TransactionRepository
) {
    operator fun invoke(): Flow<FinancialSummary> {
        return repository.getFinancialSummary()
    }
}
