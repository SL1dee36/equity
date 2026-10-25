package com.equity.app.domain.model

data class SavingsGoal(
    val id: Long = 0,
    val title: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val targetDate: Long,
    val createdAt: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false,
    val priority: Int = 1
) {
    val progress: Float
        get() = if (targetAmount <= 0) 0f else (currentAmount / targetAmount).coerceIn(0.0, 1.0).toFloat()

    val remainingAmount: Double
        get() = (targetAmount - currentAmount).coerceAtLeast(0.0)

    val daysRemaining: Long
        get() {
            val now = System.currentTimeMillis()
            val diff = targetDate - now
            return if (diff <= 0) 0L else (diff / (1000 * 60 * 60 * 24))
        }
}
