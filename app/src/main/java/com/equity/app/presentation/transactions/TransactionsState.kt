package com.equity.app.presentation.transactions

import com.equity.app.domain.model.Transaction
import com.equity.app.domain.model.TransactionType

data class TransactionsState(
    val transactions: List<Transaction> = emptyList(),
    val filteredTransactions: List<Transaction> = emptyList(),
    val filterType: TransactionType? = null,
    val searchQuery: String = "",
    val editingTransaction: Transaction? = null,
    val isAddEditSheetOpen: Boolean = false,
    val isLoading: Boolean = true
)
