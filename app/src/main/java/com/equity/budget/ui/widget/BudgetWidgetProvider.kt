package com.equity.budget.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.equity.budget.MainActivity
import com.equity.budget.R
import com.equity.budget.data.local.AppDatabase
import com.equity.budget.data.model.Currency
import com.equity.budget.data.model.TransactionType
import com.equity.budget.data.preferences.PreferencesManager
import com.equity.budget.domain.CurrencyConverter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

class BudgetWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                for (appWidgetId in appWidgetIds) {
                    updateAppWidgetSync(context, appWidgetManager, appWidgetId)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context) ?: return
            val thisWidget = ComponentName(context, BudgetWidgetProvider::class.java)
            val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget) ?: return
            if (allWidgetIds.isEmpty()) return

            CoroutineScope(Dispatchers.IO).launch {
                for (widgetId in allWidgetIds) {
                    updateAppWidgetSync(context, appWidgetManager, widgetId)
                }
            }
        }

        private suspend fun updateAppWidgetSync(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            try {
                val views = RemoteViews(context.packageName, R.layout.widget_budget)

                // Open app click intent
                val openAppIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val openAppPendingIntent = PendingIntent.getActivity(
                    context, 0, openAppIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_root, openAppPendingIntent)

                // Add transaction click intent
                val addTxIntent = Intent(context, MainActivity::class.java).apply {
                    action = "ACTION_ADD_TRANSACTION"
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val addTxPendingIntent = PendingIntent.getActivity(
                    context, 1, addTxIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_btn_add, addTxPendingIntent)

                val db = AppDatabase.getDatabase(context)
                val prefs = PreferencesManager(context)
                val settings = prefs.settings.value

                val allTx = db.transactionDao().getAllTransactions().first()

                var totalBalance = 0.0
                for (tx in allTx) {
                    val norm = if (settings.autoConvert) {
                        CurrencyConverter.convert(
                            tx.amount,
                            Currency.fromCode(tx.currency),
                            settings.primaryCurrency,
                            settings.usdToRub,
                            settings.eurToRub
                        )
                    } else tx.amount

                    if (tx.type == TransactionType.INCOME.id) {
                        totalBalance += norm
                    } else {
                        totalBalance -= norm
                    }
                }

                // Month expense
                val cal = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val monthStart = cal.timeInMillis

                var monthExpense = 0.0
                for (tx in allTx.filter { it.timestamp >= monthStart && it.type == TransactionType.EXPENSE.id }) {
                    val norm = if (settings.autoConvert) {
                        CurrencyConverter.convert(
                            tx.amount,
                            Currency.fromCode(tx.currency),
                            settings.primaryCurrency,
                            settings.usdToRub,
                            settings.eurToRub
                        )
                    } else tx.amount
                    monthExpense += norm
                }

                views.setTextViewText(
                    R.id.widget_balance_value,
                    CurrencyConverter.format(totalBalance, settings.primaryCurrency)
                )
                views.setTextViewText(
                    R.id.widget_expense_value,
                    CurrencyConverter.format(monthExpense, settings.primaryCurrency)
                )

                appWidgetManager.updateAppWidget(appWidgetId, views)
            } catch (_: Exception) {}
        }
    }
}
