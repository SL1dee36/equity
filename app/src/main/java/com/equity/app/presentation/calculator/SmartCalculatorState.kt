package com.equity.app.presentation.calculator

import com.equity.app.domain.model.EquityFormulaResult
import com.equity.app.domain.model.SavingsGoal

data class SmartCalculatorState(
    val activeGoal: SavingsGoal? = null,
    val targetAmountInput: String = "10000",
    val goalTitleInput: String = "Alpha Wealth Vault",
    val targetDaysInput: Int = 90,
    val currentSavedInput: String = "4250",
    val simulationDepositAmount: Double = 1000.0,
    val useAutoFinancials: Boolean = true,
    val customMonthlyIncome: String = "",
    val customEssentialExpense: String = "",
    val formulaResult: EquityFormulaResult? = null,
    val isAddFundsDialogOpen: Boolean = false,
    val addFundsAmount: String = "",
    val isSavingSuccess: Boolean = false,
    val isLoading: Boolean = true
)
