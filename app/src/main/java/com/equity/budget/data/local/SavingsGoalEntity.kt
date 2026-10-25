package com.equity.budget.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "savings_goals")
data class SavingsGoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val currency: String = "RUB",
    val targetMonths: Int = 12,
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * Сколько осталось накопить
     */
    val remainingAmount: Double
        get() = (targetAmount - currentAmount).coerceAtLeast(0.0)

    /**
     * Сколько необходимо откладывать в месяц
     */
    val monthlyPaymentNeeded: Double
        get() {
            val months = targetMonths.coerceAtLeast(1)
            return remainingAmount / months
        }

    /**
     * Сколько откладывать в день (для ориентира)
     */
    val dailyPaymentNeeded: Double
        get() = monthlyPaymentNeeded / 30.0

    /**
     * Процент завершения от 0.0 до 100.0
     */
    val progressPercent: Float
        get() = if (targetAmount > 0) {
            ((currentAmount / targetAmount) * 100.0).coerceIn(0.0, 100.0).toFloat()
        } else 0f

    /**
     * Достигнута ли цель
     */
    val isCompleted: Boolean
        get() = currentAmount >= targetAmount && targetAmount > 0
}
