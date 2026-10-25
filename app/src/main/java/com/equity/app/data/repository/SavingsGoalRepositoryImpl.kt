package com.equity.app.data.repository

import com.equity.app.data.local.dao.SavingsGoalDao
import com.equity.app.data.local.entity.SavingsGoalEntity
import com.equity.app.domain.model.SavingsGoal
import com.equity.app.domain.repository.SavingsGoalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SavingsGoalRepositoryImpl(
    private val savingsGoalDao: SavingsGoalDao
) : SavingsGoalRepository {

    override fun getAllGoals(): Flow<List<SavingsGoal>> {
        return savingsGoalDao.getAllGoals().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getActiveGoal(): Flow<SavingsGoal?> {
        return savingsGoalDao.getActiveGoalFlow().map { it?.toDomain() }
    }

    override suspend fun getGoalById(id: Long): SavingsGoal? {
        return savingsGoalDao.getGoalById(id)?.toDomain()
    }

    override suspend fun saveGoal(goal: SavingsGoal): Long {
        return savingsGoalDao.insertGoal(SavingsGoalEntity.fromDomain(goal))
    }

    override suspend fun updateGoal(goal: SavingsGoal) {
        savingsGoalDao.updateGoal(SavingsGoalEntity.fromDomain(goal))
    }

    override suspend fun deleteGoal(goal: SavingsGoal) {
        savingsGoalDao.deleteGoal(SavingsGoalEntity.fromDomain(goal))
    }

    override suspend fun addFundsToGoal(id: Long, amount: Double) {
        savingsGoalDao.addFundsToGoal(id, amount)
    }
}
