package com.equity.app.presentation.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equity.app.domain.model.Transaction
import com.equity.app.domain.model.TransactionType
import com.equity.app.domain.usecase.AddTransactionUseCase
import com.equity.app.domain.usecase.DeleteTransactionUseCase
import com.equity.app.domain.usecase.GetTransactionsUseCase
import com.equity.app.domain.usecase.UpdateTransactionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TransactionsViewModel(
    getTransactionsUseCase: GetTransactionsUseCase,
    private val addTransactionUseCase: AddTransactionUseCase,
    private val updateTransactionUseCase: UpdateTransactionUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase
) : ViewModel() {

    private val _filterType = MutableStateFlow<TransactionType?>(null)
    private val _searchQuery = MutableStateFlow("")
    private val _editingTransaction = MutableStateFlow<Transaction?>(null)
    private val _isAddEditSheetOpen = MutableStateFlow(false)

    val uiState: StateFlow<TransactionsState> = combine(
        getTransactionsUseCase(),
        _filterType,
        _searchQuery,
        _editingTransaction,
        _isAddEditSheetOpen
    ) { transactions, filterType, query, editing, isOpen ->
        val filtered = transactions.filter { item ->
            val matchesType = filterType == null || item.type == filterType
            val matchesQuery = query.isBlank() ||
                    item.category.displayName.contains(query, ignoreCase = true) ||
                    (item.note?.contains(query, ignoreCase = true) == true)
            matchesType && matchesQuery
        }

        TransactionsState(
            transactions = transactions,
            filteredTransactions = filtered,
            filterType = filterType,
            searchQuery = query,
            editingTransaction = editing,
            isAddEditSheetOpen = isOpen,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TransactionsState(isLoading = true)
    )

    fun setFilterType(type: TransactionType?) {
        _filterType.update { type }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.update { query }
    }

    fun openAddSheet() {
        _editingTransaction.update { null }
        _isAddEditSheetOpen.update { true }
    }

    fun openEditSheet(transaction: Transaction) {
        _editingTransaction.update { transaction }
        _isAddEditSheetOpen.update { true }
    }

    fun closeSheet() {
        _isAddEditSheetOpen.update { false }
        _editingTransaction.update { null }
    }

    fun saveTransaction(transaction: Transaction) {
        viewModelScope.launch {
            if (transaction.id == 0L) {
                addTransactionUseCase(transaction)
            } else {
                updateTransactionUseCase(transaction)
            }
            closeSheet()
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            deleteTransactionUseCase(transaction)
        }
    }
}
