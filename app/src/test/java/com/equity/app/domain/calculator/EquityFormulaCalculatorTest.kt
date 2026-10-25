package com.equity.app.domain.calculator

import com.equity.app.domain.model.GoalFeasibility
import com.equity.app.domain.model.SavingsGoal
import com.equity.app.domain.model.Transaction
import com.equity.app.domain.model.TransactionCategory
import com.equity.app.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class EquityFormulaCalculatorTest {

    private val calculator = EquityFormulaCalculator()

    @Test
    fun `calculate returns healthy feasibility when savings target is balanced`() {
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 90)
        }

        val goal = SavingsGoal(
            title = "Alpha Reserve",
            targetAmount = 6000.0,
            currentAmount = 1500.0, // 4500 remaining over ~3 months => ~1500/mo
            targetDate = cal.timeInMillis
        )

        val transactions = listOf(
            Transaction(
                amount = 7000.0,
                type = TransactionType.INCOME,
                category = TransactionCategory.SALARY,
                date = now - 1000
            ),
            Transaction(
                amount = 2500.0,
                type = TransactionType.EXPENSE,
                category = TransactionCategory.RENT,
                date = now - 2000,
                isRecurring = true
            )
        )

        val result = calculator.calculate(transactions, goal)

        assertEquals(7000.0, result.averageMonthlyIncome, 0.01)
        assertEquals(2500.0, result.monthlyEssentialExpenses, 0.01)
        assertEquals(4500.0, result.discretionaryIncome, 0.01)
        assertEquals(4500.0, result.remainingAmountToSave, 0.01)

        // Required monthly savings is ~1500/mo out of 4500 discretionary (~33% load) -> HEALTHY
        assertEquals(GoalFeasibility.HEALTHY, result.feasibility)
        assertTrue(result.safeToSpendMonthly > 2500.0)
        assertTrue(result.mustSaveRatio > 0f)
        assertTrue(result.safeToSpendDaily > 0.0)
    }

    @Test
    fun `calculate auto-lock deduction calculates correct locked amount for incoming deposit`() {
        val cal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 180)
        }

        val goal = SavingsGoal(
            title = "Tech Growth",
            targetAmount = 10000.0,
            currentAmount = 2000.0,
            targetDate = cal.timeInMillis
        )

        val result = calculator.calculate(
            transactions = emptyList(),
            goal = goal,
            manualMonthlyIncome = 8000.0,
            manualEssentialExpense = 3000.0
        )

        // Monthly savings needed: 8000 / ~5.91 mos = ~1353/mo
        // Must save ratio: ~1353 / 8000 = ~16.9%
        val incomingDeposit = 2000.0
        val lockedAmount = result.calculateAutoLockAmount(incomingDeposit)

        assertTrue(lockedAmount > 300.0 && lockedAmount < 400.0)
        assertEquals(result.mustSaveRatio, result.incomingLockPercentage, 0.01f)
    }

    @Test
    fun `calculate flags unfeasible deficit when target exceeds discretionary capacity`() {
        val cal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 30) // Only 1 month to save 10,000!
        }

        val goal = SavingsGoal(
            title = "Impossible Dream",
            targetAmount = 12000.0,
            currentAmount = 2000.0,
            targetDate = cal.timeInMillis
        )

        val result = calculator.calculate(
            transactions = emptyList(),
            goal = goal,
            manualMonthlyIncome = 5000.0,
            manualEssentialExpense = 3500.0 // Only 1500 discretionary income
        )

        assertEquals(GoalFeasibility.UNFEASIBLE, result.feasibility)
        assertNotNull(result.suggestedTargetDateDays)
        assertTrue(result.suggestedTargetDateDays!! > 30L)
    }

    @Test
    fun `calculate detects reached goal with zero remaining amount`() {
        val cal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 45)
        }

        val goal = SavingsGoal(
            title = "Completed Target",
            targetAmount = 5000.0,
            currentAmount = 5500.0,
            targetDate = cal.timeInMillis
        )

        val result = calculator.calculate(
            transactions = emptyList(),
            goal = goal,
            manualMonthlyIncome = 6000.0,
            manualEssentialExpense = 2000.0
        )

        assertEquals(GoalFeasibility.OPTIMAL, result.feasibility)
        assertEquals(0.0, result.remainingAmountToSave, 0.01)
    }
}
