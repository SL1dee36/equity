package com.equity.budget.domain

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.equity.budget.data.local.AppDatabase

class RecurringWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val database = AppDatabase.getDatabase(applicationContext)
            val scheduler = RecurringScheduler(database)
            scheduler.processDueRules()
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }
}
