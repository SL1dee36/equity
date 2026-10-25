package com.equity.app.domain.repository

import com.equity.app.domain.model.SavingsGoal
import kotlinx.coroutines.flow.Flow

interface SavingsGoalRepository {
    fun getAllGoals(): Flow<List<SavingsGoal>>
    fun getActiveGoal(): Flow<SavingsGoal?>
    suspend fun getGoalById(id: Long): SavingsGoal?
    suspend fun saveGoal(goal: SavingsGoal): Long
    suspend fun updateGoal(goal: SavingsGoal)
    suspend fun deleteGoal(goal: SavingsGoal)
    suspend fun addFundsToGoal(id: Long, amount: Double)
}
