package com.equity.app.domain.usecase

import com.equity.app.domain.model.SavingsGoal
import com.equity.app.domain.repository.SavingsGoalRepository
import kotlinx.coroutines.flow.Flow

class GetActiveSavingsGoalUseCase(
    private val repository: SavingsGoalRepository
) {
    operator fun invoke(): Flow<SavingsGoal?> {
        return repository.getActiveGoal()
    }
}

class GetAllSavingsGoalsUseCase(
    private val repository: SavingsGoalRepository
) {
    operator fun invoke(): Flow<List<SavingsGoal>> {
        return repository.getAllGoals()
    }
}

class SaveSavingsGoalUseCase(
    private val repository: SavingsGoalRepository
) {
    suspend operator fun invoke(goal: SavingsGoal): Long {
        require(goal.targetAmount > 0) { "Target amount must be positive" }
        require(goal.title.isNotBlank()) { "Goal title cannot be blank" }
        return repository.saveGoal(goal)
    }
}

class AddFundsToGoalUseCase(
    private val repository: SavingsGoalRepository
) {
    suspend operator fun invoke(id: Long, amount: Double) {
        require(amount > 0) { "Amount to add must be positive" }
        repository.addFundsToGoal(id, amount)
    }
}
