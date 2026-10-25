package com.equity.app.domain.model

enum class GoalFeasibility {
    OPTIMAL,      // Saving < 30% of discretionary income; comfortable
    HEALTHY,      // Saving 30-60% of discretionary income; balanced
    AGGRESSIVE,   // Saving 60-85% of discretionary income; strict budgeting required
    AT_RISK,      // Saving 85-100% of discretionary income; high chance of shortfall
    UNFEASIBLE    // Required saving exceeds total discretionary income!
}

data class EquityFormulaResult(
    val averageMonthlyIncome: Double,
    val monthlyEssentialExpenses: Double,
    val discretionaryIncome: Double,
    val targetAmount: Double,
    val currentSavedAmount: Double,
    val remainingAmountToSave: Double,
    val daysRemaining: Long,
    val monthsRemaining: Double,
    val requiredMonthlySavings: Double,
    val requiredDailySavings: Double,
    val mustSaveRatio: Float,          // e.g. 0.28 (28% of total income)
    val safeToSpendRatio: Float,       // e.g. 0.42 (42% of total income)
    val safeToSpendMonthly: Double,
    val safeToSpendDaily: Double,
    val incomingLockPercentage: Float, // percentage to lock from each incoming transaction
    val feasibility: GoalFeasibility,
    val recommendationMessage: String,
    val suggestedTargetDateDays: Long? = null // Recommended timeline extension if at risk
) {
    fun calculateAutoLockAmount(incomingAmount: Double): Double {
        return (incomingAmount * (incomingLockPercentage / 100.0)).coerceAtLeast(0.0)
    }
}
