package com.equity.budget

import com.equity.budget.data.local.SavingsGoalEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SavingsGoalEntityTest {

    @Test
    fun testMonthlySavingsCalculation() {
        // User wants to save 120,000 in 12 months with 0 initial savings
        val goal = SavingsGoalEntity(
            title = "Отпуск",
            targetAmount = 120000.0,
            currentAmount = 0.0,
            targetMonths = 12,
            currency = "RUB"
        )

        assertEquals(120000.0, goal.remainingAmount, 0.001)
        assertEquals(10000.0, goal.monthlyPaymentNeeded, 0.001)
        assertEquals(333.33, goal.dailyPaymentNeeded, 0.01)
        assertEquals(0f, goal.progressPercent, 0.01f)
        assertFalse(goal.isCompleted)
    }

    @Test
    fun testPartialProgressSavingsCalculation() {
        // Target 300,000 in 6 months with 60,000 already saved
        val goal = SavingsGoalEntity(
            title = "Машина",
            targetAmount = 300000.0,
            currentAmount = 60000.0,
            targetMonths = 6,
            currency = "RUB"
        )

        assertEquals(240000.0, goal.remainingAmount, 0.001)
        // 240,000 / 6 = 40,000 per month
        assertEquals(40000.0, goal.monthlyPaymentNeeded, 0.001)
        assertEquals(1333.33, goal.dailyPaymentNeeded, 0.01)
        // 60,000 / 300,000 = 20%
        assertEquals(20.0f, goal.progressPercent, 0.01f)
        assertFalse(goal.isCompleted)
    }

    @Test
    fun testCompletedGoal() {
        val goal = SavingsGoalEntity(
            title = "Ноутбук",
            targetAmount = 150000.0,
            currentAmount = 155000.0,
            targetMonths = 3,
            currency = "RUB"
        )

        assertEquals(0.0, goal.remainingAmount, 0.001)
        assertEquals(0.0, goal.monthlyPaymentNeeded, 0.001)
        assertEquals(0.0, goal.dailyPaymentNeeded, 0.001)
        assertEquals(100.0f, goal.progressPercent, 0.01f)
        assertTrue(goal.isCompleted)
    }

    @Test
    fun testMultipleGoalsTotalMonthlySum() {
        val goal1 = SavingsGoalEntity(
            id = 1,
            title = "Отпуск",
            targetAmount = 120000.0,
            currentAmount = 0.0,
            targetMonths = 12,
            currency = "RUB"
        )
        val goal2 = SavingsGoalEntity(
            id = 2,
            title = "Подушка безопасности",
            targetAmount = 300000.0,
            currentAmount = 100000.0,
            targetMonths = 10,
            currency = "RUB"
        )

        val totalMonthlyNeeded = goal1.monthlyPaymentNeeded + goal2.monthlyPaymentNeeded
        // goal1: 10,000; goal2: (300,000 - 100,000) / 10 = 20,000 -> total = 30,000
        assertEquals(30000.0, totalMonthlyNeeded, 0.001)
    }
}
