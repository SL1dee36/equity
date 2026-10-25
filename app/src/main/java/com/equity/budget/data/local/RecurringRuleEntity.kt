package com.equity.budget.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recurring_rules")
data class RecurringRuleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val currency: String,
    val type: String,     // expense, income
    val categoryId: String,
    val frequency: String, // monthly, weekly, biweekly
    val dayOfMonth: Int = 1, // 1..31
    val dayOfWeek: Int = 1,  // 1=Monday..7=Sunday
    val startDate: Long,
    val lastProcessedDate: Long = 0L,
    val isActive: Boolean = true
)
