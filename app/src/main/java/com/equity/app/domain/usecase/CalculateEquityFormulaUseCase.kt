package com.equity.app.domain.usecase

import com.equity.app.domain.calculator.EquityFormulaCalculator
import com.equity.app.domain.model.EquityFormulaResult
import com.equity.app.domain.model.SavingsGoal
import com.equity.app.domain.repository.SavingsGoalRepository
import com.equity.app.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class CalculateEquityFormulaUseCase(
    private val transactionRepository: TransactionRepository,
    private val savingsGoalRepository: SavingsGoalRepository,
    private val calculator: EquityFormulaCalculator
) {
    operator fun invoke(
        overrideGoal: SavingsGoal? = null,
        manualMonthlyIncome: Double? = null,
        manualEssentialExpense: Double? = null
    ): Flow<EquityFormulaResult?> {
        return combine(
            transactionRepository.getAllTransactions(),
            savingsGoalRepository.getActiveGoal()
        ) { transactions, activeGoal ->
            val goalToUse = overrideGoal ?: activeGoal
            if (goalToUse == null) {
                null
            } else {
                calculator.calculate(
                    transactions = transactions,
                    goal = goalToUse,
                    manualMonthlyIncome = manualMonthlyIncome,
                    manualEssentialExpense = manualEssentialExpense
                )
            }
        }
    }

    fun calculateImmediate(
        goal: SavingsGoal,
        monthlyIncome: Double,
        essentialExpenses: Double
    ): EquityFormulaResult {
        return calculator.calculate(
            transactions = emptyList(),
            goal = goal,
            manualMonthlyIncome = monthlyIncome,
            manualEssentialExpense = essentialExpenses
        )
    }
}
