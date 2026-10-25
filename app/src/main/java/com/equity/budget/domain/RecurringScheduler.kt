package com.equity.budget.domain

import com.equity.budget.data.local.AppDatabase
import com.equity.budget.data.local.RecurringRuleEntity
import com.equity.budget.data.local.TransactionEntity
import com.equity.budget.data.model.RecurrenceFrequency
import java.util.Calendar

class RecurringScheduler(private val database: AppDatabase) {

    suspend fun processDueRules(): Int {
        val ruleDao = database.recurringRuleDao()
        val transactionDao = database.transactionDao()
        val activeRules = ruleDao.getActiveRules()
        var totalCreated = 0

        val now = System.currentTimeMillis()
        val nowCal = Calendar.getInstance().apply { timeInMillis = now }

        for (rule in activeRules) {
            val newlyCreated = processRule(rule, nowCal, transactionDao)
            if (newlyCreated > 0) {
                totalCreated += newlyCreated
                ruleDao.updateLastProcessedDate(rule.id, now)
            }
        }

        return totalCreated
    }

    private suspend fun processRule(
        rule: RecurringRuleEntity,
        currentCalendar: Calendar,
        transactionDao: com.equity.budget.data.local.TransactionDao
    ): Int {
        val frequency = try {
            RecurrenceFrequency.valueOf(rule.frequency.uppercase())
        } catch (_: Exception) {
            RecurrenceFrequency.MONTHLY
        }

        val startCal = Calendar.getInstance().apply {
            timeInMillis = if (rule.lastProcessedDate > 0) {
                // start from day after last processed
                rule.lastProcessedDate + 86_400_000L
            } else {
                rule.startDate
            }
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val todayLimit = Calendar.getInstance().apply {
            timeInMillis = currentCalendar.timeInMillis
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }

        if (startCal.after(todayLimit)) return 0

        var createdCount = 0
        val cursorCal = Calendar.getInstance().apply { timeInMillis = startCal.timeInMillis }

        when (frequency) {
            RecurrenceFrequency.MONTHLY -> {
                // Adjust to the desired day of month
                val targetDay = rule.dayOfMonth.coerceIn(1, 31)

                // If cursor is before the target day in current cursor month:
                val maxDayThisMonth = cursorCal.getActualMaximum(Calendar.DAY_OF_MONTH)
                val safeDay = targetDay.coerceAtMost(maxDayThisMonth)
                cursorCal.set(Calendar.DAY_OF_MONTH, safeDay)

                if (cursorCal.before(startCal)) {
                    // move to next month
                    cursorCal.add(Calendar.MONTH, 1)
                    val maxNext = cursorCal.getActualMaximum(Calendar.DAY_OF_MONTH)
                    cursorCal.set(Calendar.DAY_OF_MONTH, targetDay.coerceAtMost(maxNext))
                }

                while (!cursorCal.after(todayLimit)) {
                    val timestamp = cursorCal.timeInMillis
                    if (shouldCreateInstance(rule.id, timestamp, transactionDao)) {
                        transactionDao.insertTransaction(
                            TransactionEntity(
                                title = rule.title,
                                amount = rule.amount,
                                currency = rule.currency,
                                type = rule.type,
                                categoryId = rule.categoryId,
                                timestamp = timestamp,
                                isAutoAdded = true,
                                recurringRuleId = rule.id,
                                note = "Auto"
                            )
                        )
                        createdCount++
                    }

                    // Move to next month
                    cursorCal.add(Calendar.MONTH, 1)
                    val nextMax = cursorCal.getActualMaximum(Calendar.DAY_OF_MONTH)
                    cursorCal.set(Calendar.DAY_OF_MONTH, targetDay.coerceAtMost(nextMax))
                }
            }

            RecurrenceFrequency.WEEKLY -> {
                val targetDow = when (rule.dayOfWeek) {
                    1 -> Calendar.MONDAY
                    2 -> Calendar.TUESDAY
                    3 -> Calendar.WEDNESDAY
                    4 -> Calendar.THURSDAY
                    5 -> Calendar.FRIDAY
                    6 -> Calendar.SATURDAY
                    else -> Calendar.SUNDAY
                }

                while (cursorCal.get(Calendar.DAY_OF_WEEK) != targetDow) {
                    cursorCal.add(Calendar.DAY_OF_YEAR, 1)
                }

                while (!cursorCal.after(todayLimit)) {
                    val timestamp = cursorCal.timeInMillis
                    if (shouldCreateInstance(rule.id, timestamp, transactionDao)) {
                        transactionDao.insertTransaction(
                            TransactionEntity(
                                title = rule.title,
                                amount = rule.amount,
                                currency = rule.currency,
                                type = rule.type,
                                categoryId = rule.categoryId,
                                timestamp = timestamp,
                                isAutoAdded = true,
                                recurringRuleId = rule.id,
                                note = "Auto"
                            )
                        )
                        createdCount++
                    }
                    cursorCal.add(Calendar.WEEK_OF_YEAR, 1)
                }
            }

            RecurrenceFrequency.BIWEEKLY -> {
                while (!cursorCal.after(todayLimit)) {
                    val timestamp = cursorCal.timeInMillis
                    if (shouldCreateInstance(rule.id, timestamp, transactionDao)) {
                        transactionDao.insertTransaction(
                            TransactionEntity(
                                title = rule.title,
                                amount = rule.amount,
                                currency = rule.currency,
                                type = rule.type,
                                categoryId = rule.categoryId,
                                timestamp = timestamp,
                                isAutoAdded = true,
                                recurringRuleId = rule.id,
                                note = "Auto"
                            )
                        )
                        createdCount++
                    }
                    cursorCal.add(Calendar.DAY_OF_YEAR, 14)
                }
            }
        }

        return createdCount
    }

    private suspend fun shouldCreateInstance(
        ruleId: Long,
        timestamp: Long,
        transactionDao: com.equity.budget.data.local.TransactionDao
    ): Boolean {
        // Range of the day: +/- 12 hours from midnight or 00:00 to 23:59
        val dayStart = Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val dayEnd = Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        val count = transactionDao.countRecurringInstancesInPeriod(ruleId, dayStart, dayEnd)
        return count == 0
    }
}
