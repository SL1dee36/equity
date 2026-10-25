package com.equity.budget.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.equity.budget.data.model.AppLanguage
import com.equity.budget.ui.theme.ExpenseRed
import kotlin.math.max

data class WeekdaySpend(
    val dayName: String,
    val amount: Double
)

@Composable
fun WeekdaySpendingChart(
    days: List<WeekdaySpend>,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val maxVal = days.maxOfOrNull { it.amount }?.coerceAtLeast(1.0) ?: 1.0
    val primaryColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
        ) {
            val w = size.width
            val h = size.height
            val bottomPadding = 20f
            val chartH = h - bottomPadding

            // Guide lines
            for (i in 0..1) {
                val y = chartH * (i / 1f)
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(w, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            if (days.isEmpty()) return@Canvas

            val colWidth = w / days.size
            val barWidth = (colWidth * 0.45f).coerceAtMost(22.dp.toPx())

            days.forEachIndexed { i, day ->
                val centerX = i * colWidth + (colWidth / 2f)
                val barHeight = ((day.amount / maxVal) * chartH).toFloat().coerceAtLeast(2f)
                val barX = centerX - (barWidth / 2f)
                val barY = chartH - barHeight

                val isPeak = day.amount == maxVal && day.amount > 0

                drawRoundRect(
                    color = if (isPeak) ExpenseRed else primaryColor.copy(alpha = 0.7f),
                    topLeft = Offset(barX, barY),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            days.forEach { day ->
                val isPeak = day.amount == maxVal && day.amount > 0
                Text(
                    text = day.dayName,
                    fontSize = 11.sp,
                    fontWeight = if (isPeak) FontWeight.Bold else FontWeight.Normal,
                    color = if (isPeak) ExpenseRed else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
