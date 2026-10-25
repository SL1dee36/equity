package com.equity.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.equity.app.data.local.entity.SavingsGoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavingsGoalDao {

    @Query("SELECT * FROM savings_goals ORDER BY isCompleted ASC, priority DESC, targetDate ASC")
    fun getAllGoals(): Flow<List<SavingsGoalEntity>>

    @Query("SELECT * FROM savings_goals WHERE isCompleted = 0 ORDER BY priority DESC, targetDate ASC LIMIT 1")
    fun getActiveGoalFlow(): Flow<SavingsGoalEntity?>

    @Query("SELECT * FROM savings_goals WHERE isCompleted = 0 ORDER BY priority DESC, targetDate ASC LIMIT 1")
    suspend fun getActiveGoal(): SavingsGoalEntity?

    @Query("SELECT * FROM savings_goals WHERE id = :id LIMIT 1")
    suspend fun getGoalById(id: Long): SavingsGoalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: SavingsGoalEntity): Long

    @Update
    suspend fun updateGoal(goal: SavingsGoalEntity)

    @Delete
    suspend fun deleteGoal(goal: SavingsGoalEntity)

    @Query("UPDATE savings_goals SET currentAmount = currentAmount + :additionalAmount WHERE id = :id")
    suspend fun addFundsToGoal(id: Long, additionalAmount: Double)

    @Query("SELECT COUNT(*) FROM savings_goals")
    suspend fun count(): Int
}
