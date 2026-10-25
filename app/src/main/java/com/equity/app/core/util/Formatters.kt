package com.equity.app.core.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Formatters {

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US).apply {
        maximumFractionDigits = 2
        minimumFractionDigits = 2
    }

    private val compactCurrencyFormat = NumberFormat.getCurrencyInstance(Locale.US).apply {
        maximumFractionDigits = 0
        minimumFractionDigits = 0
    }

    fun formatCurrency(amount: Double): String {
        return currencyFormat.format(amount)
    }

    fun formatCompactCurrency(amount: Double): String {
        return compactCurrencyFormat.format(amount)
    }

    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatShortDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatRelativeDate(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diffDays = (now - timestamp) / (1000 * 60 * 60 * 24)
        return when {
            diffDays == 0L -> "Today"
            diffDays == 1L -> "Yesterday"
            diffDays < 7L -> "${diffDays}d ago"
            else -> formatDate(timestamp)
        }
    }

    fun formatPercentage(value: Float): String {
        return String.format(Locale.US, "%.1f%%", value)
    }
}
