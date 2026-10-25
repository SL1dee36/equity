package com.equity.budget.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.equity.budget.data.model.AppLanguage
import com.equity.budget.ui.theme.ExpenseRed
import com.equity.budget.ui.theme.IncomeGreen
import com.equity.budget.ui.viewmodel.TrendPoint
import kotlin.math.max

@Composable
fun TrendChart(
    points: List<TrendPoint>,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val isRu = language == AppLanguage.RU
    val maxVal = points.maxOfOrNull { max(it.income, it.expense) }?.coerceAtLeast(1.0) ?: 1.0

    Column(modifier = modifier.fillMaxWidth()) {
        // Legend
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(8.dp), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(8.dp)) {
                    drawCircle(color = IncomeGreen)
                }
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (isRu) "Доходы" else "Income",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.width(16.dp))

            Box(modifier = Modifier.size(8.dp), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(8.dp)) {
                    drawCircle(color = ExpenseRed)
                }
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (isRu) "Расходы" else "Expenses",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Minimalist Bar Chart Canvas
        val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val bottomPadding = 30f
            val chartHeight = canvasHeight - bottomPadding

            // Subtle horizontal guide lines (3 lines)
            for (i in 0..2) {
                val y = chartHeight * (i / 2f)
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(canvasWidth, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            if (points.isEmpty()) return@Canvas

            val groupWidth = canvasWidth / points.size
            val barWidth = (groupWidth * 0.28f).coerceAtMost(16.dp.toPx())
            val barSpacing = 4f

            points.forEachIndexed { index, point ->
                val groupCenter = index * groupWidth + (groupWidth / 2f)

                // Income bar
                val incomeHeight = ((point.income / maxVal) * chartHeight).toFloat().coerceAtLeast(3f)
                val incomeX = groupCenter - barWidth - (barSpacing / 2f)
                val incomeY = chartHeight - incomeHeight

                drawRoundRect(
                    color = IncomeGreen,
                    topLeft = Offset(incomeX, incomeY),
                    size = Size(barWidth, incomeHeight),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )

                // Expense bar
                val expenseHeight = ((point.expense / maxVal) * chartHeight).toFloat().coerceAtLeast(3f)
                val expenseX = groupCenter + (barSpacing / 2f)
                val expenseY = chartHeight - expenseHeight

                drawRoundRect(
                    color = ExpenseRed,
                    topLeft = Offset(expenseX, expenseY),
                    size = Size(barWidth, expenseHeight),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )
            }
        }

        // Labels row below canvas
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            points.forEach { point ->
                Text(
                    text = point.label,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
