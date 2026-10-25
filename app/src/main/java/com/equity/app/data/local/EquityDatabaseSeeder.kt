package com.equity.app.data.local

import com.equity.app.data.local.dao.SavingsGoalDao
import com.equity.app.data.local.dao.TransactionDao
import com.equity.app.data.local.entity.SavingsGoalEntity
import com.equity.app.data.local.entity.TransactionEntity
import com.equity.app.domain.model.TransactionCategory
import com.equity.app.domain.model.TransactionType
import java.util.Calendar

object EquityDatabaseSeeder {

    suspend fun seedIfEmpty(transactionDao: TransactionDao, savingsGoalDao: SavingsGoalDao) {
        if (transactionDao.count() == 0) {
            val now = System.currentTimeMillis()
            val dayMs = 24L * 60 * 60 * 1000

            val sampleTransactions = listOf(
                TransactionEntity(
                    amount = 6500.0,
                    type = TransactionType.INCOME.name,
                    category = TransactionCategory.SALARY.displayName,
                    date = now - (2 * dayMs),
                    note = "Monthly Tech Lead Compensation",
                    isRecurring = true
                ),
                TransactionEntity(
                    amount = 1450.0,
                    type = TransactionType.INCOME.name,
                    category = TransactionCategory.FREELANCE.displayName,
                    date = now - (12 * dayMs),
                    note = "Mobile Architecture Consulting",
                    isRecurring = false
                ),
                TransactionEntity(
                    amount = 1850.0,
                    type = TransactionType.EXPENSE.name,
                    category = TransactionCategory.RENT.displayName,
                    date = now - (3 * dayMs),
                    note = "Apartment Rent & Parking",
                    isRecurring = true
                ),
                TransactionEntity(
                    amount = 640.0,
                    type = TransactionType.EXPENSE.name,
                    category = TransactionCategory.FOOD.displayName,
                    date = now - (1 * dayMs),
                    note = "Whole Foods & Dining",
                    isRecurring = false
                ),
                TransactionEntity(
                    amount = 480.0,
                    type = TransactionType.EXPENSE.name,
                    category = TransactionCategory.TECH.displayName,
                    date = now - (5 * dayMs),
                    note = "UltraWide 4K Monitor",
                    isRecurring = false
                ),
                TransactionEntity(
                    amount = 175.0,
                    type = TransactionType.EXPENSE.name,
                    category = TransactionCategory.UTILITIES.displayName,
                    date = now - (8 * dayMs),
                    note = "Fiber Internet & Electric",
                    isRecurring = true
                ),
                TransactionEntity(
                    amount = 195.0,
                    type = TransactionType.EXPENSE.name,
                    category = TransactionCategory.TRANSPORT.displayName,
                    date = now - (10 * dayMs),
                    note = "EV Charging & Tolls",
                    isRecurring = false
                ),
                TransactionEntity(
                    amount = 120.0,
                    type = TransactionType.EXPENSE.name,
                    category = TransactionCategory.HEALTH.displayName,
                    date = now - (15 * dayMs),
                    note = "Premium Gym & Wellness",
                    isRecurring = true
                ),
                TransactionEntity(
                    amount = 260.0,
                    type = TransactionType.EXPENSE.name,
                    category = TransactionCategory.ENTERTAINMENT.displayName,
                    date = now - (6 * dayMs),
                    note = "Concert Tickets & Lounge",
                    isRecurring = false
                )
            )

            transactionDao.insertTransactions(sampleTransactions)
        }

        if (savingsGoalDao.count() == 0) {
            val cal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, 90)
            }
            val sampleGoal = SavingsGoalEntity(
                title = "Emergency & Alpha Growth Fund",
                targetAmount = 10000.0,
                currentAmount = 4250.0,
                targetDate = cal.timeInMillis,
                isCompleted = false,
                priority = 1
            )
            savingsGoalDao.insertGoal(sampleGoal)
        }
    }
}
