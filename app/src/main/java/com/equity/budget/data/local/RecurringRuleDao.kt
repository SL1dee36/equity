package com.equity.budget.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringRuleDao {
    @Query("SELECT * FROM recurring_rules ORDER BY id DESC")
    fun getAllRules(): Flow<List<RecurringRuleEntity>>

    @Query("SELECT * FROM recurring_rules WHERE isActive = 1")
    suspend fun getActiveRules(): List<RecurringRuleEntity>

    @Query("SELECT * FROM recurring_rules WHERE id = :id")
    suspend fun getRuleById(id: Long): RecurringRuleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: RecurringRuleEntity): Long

    @Update
    suspend fun updateRule(rule: RecurringRuleEntity)

    @Delete
    suspend fun deleteRule(rule: RecurringRuleEntity)

    @Query("DELETE FROM recurring_rules WHERE id = :id")
    suspend fun deleteRuleById(id: Long)

    @Query("UPDATE recurring_rules SET lastProcessedDate = :lastDate WHERE id = :id")
    suspend fun updateLastProcessedDate(id: Long, lastDate: Long)

    @Query("UPDATE recurring_rules SET isActive = :isActive WHERE id = :id")
    suspend fun setRuleActive(id: Long, isActive: Boolean)
}
