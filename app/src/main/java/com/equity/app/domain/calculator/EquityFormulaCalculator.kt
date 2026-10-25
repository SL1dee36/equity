package com.equity.app.domain.calculator

import com.equity.app.domain.model.EquityFormulaResult
import com.equity.app.domain.model.GoalFeasibility
import com.equity.app.domain.model.SavingsGoal
import com.equity.app.domain.model.Transaction
import com.equity.app.domain.model.TransactionType
import kotlin.math.ceil
import kotlin.math.roundToInt

class EquityFormulaCalculator {

    fun calculate(
        transactions: List<Transaction>,
        goal: SavingsGoal,
        manualMonthlyIncome: Double? = null,
        manualEssentialExpense: Double? = null
    ): EquityFormulaResult {
        // Derive monthly income and essential expenses from transaction history if not manually provided
        val now = System.currentTimeMillis()
        val thirtyDaysAgo = now - (30L * 24 * 60 * 60 * 1000)

        val recentTransactions = transactions.filter { it.date >= thirtyDaysAgo }

        val computedMonthlyIncome = recentTransactions
            .filter { it.type == TransactionType.INCOME }
            .sumOf { it.amount }
            .let { if (it > 0) it else transactions.filter { t -> t.type == TransactionType.INCOME }.sumOf { t -> t.amount } }

        val computedEssentialExpense = recentTransactions
            .filter { it.type == TransactionType.EXPENSE && (it.category.isEssential || it.isRecurring) }
            .sumOf { it.amount }
            .let {
                if (it > 0) it else {
                    // Fallback to all essential expenses in history or a conservative 50%
                    val allEssential = transactions.filter { t -> t.type == TransactionType.EXPENSE && (t.category.isEssential || t.isRecurring) }.sumOf { t -> t.amount }
                    if (allEssential > 0) allEssential else (computedMonthlyIncome * 0.40)
                }
            }

        val monthlyIncome = manualMonthlyIncome ?: (if (computedMonthlyIncome > 0) computedMonthlyIncome else 5000.0)
        val essentialExpenses = manualEssentialExpense ?: computedEssentialExpense

        val discretionaryIncome = (monthlyIncome - essentialExpenses).coerceAtLeast(0.0)

        val targetAmount = goal.targetAmount.coerceAtLeast(100.0)
        val currentSaved = goal.currentAmount.coerceAtLeast(0.0)
        val remainingToSave = (targetAmount - currentSaved).coerceAtLeast(0.0)

        val diffMs = goal.targetDate - now
        val rawDays = if (diffMs <= 0) 1L else (diffMs / (1000L * 60 * 60 * 24))
        val daysRemaining = rawDays.coerceAtLeast(1L)
        val monthsRemaining = (daysRemaining / 30.4375).coerceAtLeast(0.05)

        val requiredMonthlySavings = (remainingToSave / monthsRemaining)
        val requiredDailySavings = (remainingToSave / daysRemaining)

        val safeToSpendMonthly = (discretionaryIncome - requiredMonthlySavings).coerceAtLeast(0.0)
        val safeToSpendDaily = safeToSpendMonthly / 30.4375

        // Must save ratio: percentage of monthly income
        val mustSaveRatio = ((requiredMonthlySavings / monthlyIncome) * 100).toFloat().coerceIn(0f, 100f)
        val safeToSpendRatio = ((safeToSpendMonthly / monthlyIncome) * 100).toFloat().coerceIn(0f, 100f)

        // Incoming lock percentage: percentage to lock from each incoming transaction
        val incomingLockPercentage = mustSaveRatio

        // Feasibility load
        val feasibilityLoad = if (discretionaryIncome > 0) {
            requiredMonthlySavings / discretionaryIncome
        } else {
            2.0
        }

        val (feasibility, recommendation, suggestedDays) = when {
            remainingToSave <= 0 -> {
                Triple(
                    GoalFeasibility.OPTIMAL,
                    "Target goal already reached! You've successfully built your intended equity reserve.",
                    null
                )
            }
            discretionaryIncome <= 0 || feasibilityLoad > 1.05 -> {
                // Goal exceeds discretionary capacity; calculate realistic timeline at 60% discretionary pace
                val safeMonthlyPace = if (discretionaryIncome > 0) discretionaryIncome * 0.60 else (monthlyIncome * 0.20)
                val neededMonths = remainingToSave / safeMonthlyPace.coerceAtLeast(50.0)
                val suggestedDaysRemaining = ceil(neededMonths * 30.4375).toLong()
                val deficit = (requiredMonthlySavings - discretionaryIncome).roundToInt()

                Triple(
                    GoalFeasibility.UNFEASIBLE,
                    "Goal deficit: Target requires saving \$${deficit}/mo more than your discretionary income. Extend target date to ${suggestedDaysRemaining} days or reduce expenses.",
                    suggestedDaysRemaining
                )
            }
            feasibilityLoad > 0.85 -> {
                Triple(
                    GoalFeasibility.AT_RISK,
                    "Strict budgeting needed: Goal consumes ${(feasibilityLoad * 100).roundToInt()}% of discretionary cash. Limit non-essential purchases.",
                    null
                )
            }
            feasibilityLoad > 0.55 -> {
                Triple(
                    GoalFeasibility.AGGRESSIVE,
                    "Aggressive acceleration: Requires disciplined focus. Locking ${incomingLockPercentage.roundToInt()}% of incoming deposits will keep you on track.",
                    null
                )
            }
            feasibilityLoad > 0.25 -> {
                Triple(
                    GoalFeasibility.HEALTHY,
                    "Balanced growth: Your equity trajectory is stable with \$${safeToSpendDaily.roundToInt()}/day safe to spend without derailing your goal.",
                    null
                )
            }
            else -> {
                Triple(
                    GoalFeasibility.OPTIMAL,
                    "Effortless accumulation: You are easily on schedule. Consider increasing target amount to accelerate your net worth growth.",
                    null
                )
            }
        }

        return EquityFormulaResult(
            averageMonthlyIncome = monthlyIncome,
            monthlyEssentialExpenses = essentialExpenses,
            discretionaryIncome = discretionaryIncome,
            targetAmount = targetAmount,
            currentSavedAmount = currentSaved,
            remainingAmountToSave = remainingToSave,
            daysRemaining = daysRemaining,
            monthsRemaining = monthsRemaining,
            requiredMonthlySavings = requiredMonthlySavings,
            requiredDailySavings = requiredDailySavings,
            mustSaveRatio = mustSaveRatio,
            safeToSpendRatio = safeToSpendRatio,
            safeToSpendMonthly = safeToSpendMonthly,
            safeToSpendDaily = safeToSpendDaily,
            incomingLockPercentage = incomingLockPercentage,
            feasibility = feasibility,
            recommendationMessage = recommendation,
            suggestedTargetDateDays = suggestedDays
        )
    }
}
