package com.equity.budget

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.equity.budget.domain.RecurringWorker
import java.util.concurrent.TimeUnit

class EquityApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        scheduleRecurringWorker()
    }

    private fun scheduleRecurringWorker() {
        val periodicWork = PeriodicWorkRequestBuilder<RecurringWorker>(
            12, TimeUnit.HOURS
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "RecurringBudgetWorker",
            ExistingPeriodicWorkPolicy.KEEP,
            periodicWork
        )
    }
}
