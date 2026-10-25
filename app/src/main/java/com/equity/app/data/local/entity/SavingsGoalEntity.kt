package com.equity.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.equity.app.domain.model.SavingsGoal

@Entity(tableName = "savings_goals")
data class SavingsGoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val targetDate: Long,
    val createdAt: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false,
    val priority: Int = 1
) {
    fun toDomain(): SavingsGoal {
        return SavingsGoal(
            id = id,
            title = title,
            targetAmount = targetAmount,
            currentAmount = currentAmount,
            targetDate = targetDate,
            createdAt = createdAt,
            isCompleted = isCompleted,
            priority = priority
        )
    }

    companion object {
        fun fromDomain(goal: SavingsGoal): SavingsGoalEntity {
            return SavingsGoalEntity(
                id = goal.id,
                title = goal.title,
                targetAmount = goal.targetAmount,
                currentAmount = goal.currentAmount,
                targetDate = goal.targetDate,
                createdAt = goal.createdAt,
                isCompleted = goal.isCompleted,
                priority = goal.priority
            )
        }
    }
}
