package com.equity.app.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.equity.app.core.theme.DarkSilver
import com.equity.app.core.theme.ElectricMint
import com.equity.app.core.theme.ExpenseRed
import com.equity.app.core.theme.MutedSilver
import com.equity.app.core.theme.ObsidianBorder
import com.equity.app.domain.model.MonthlyCashFlow
import kotlin.math.max

@Composable
fun CustomBarChart(
    cashFlows: List<MonthlyCashFlow>,
    modifier: Modifier = Modifier
) {
    if (cashFlows.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(180.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No monthly cash flow data yet",
                style = MaterialTheme.typography.bodyMedium,
                color = DarkSilver
            )
        }
        return
    }

    val maxAmount = remember(cashFlows) {
        val peak = cashFlows.maxOfOrNull { max(it.income, it.expense) } ?: 1.0
        if (peak <= 0.0) 1.0 else peak
    }

    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(cashFlows) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 850, easing = FastOutSlowInEasing)
        )
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Legend at top
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(ElectricMint, CircleShape)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Income",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp),
                    color = MutedSilver
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(ExpenseRed, CircleShape)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Expense",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp),
                    color = MutedSilver
                )
            }
        }

        // Canvas Bars
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val chartHeight = size.height - 20.dp.toPx()
                val totalSlots = cashFlows.size
                val slotWidth = size.width / totalSlots
                val barWidth = 14.dp.toPx()
                val barSpacing = 4.dp.toPx()
                val cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())

                // Draw horizontal guide lines
                val gridSteps = 3
                for (i in 0..gridSteps) {
                    val y = (chartHeight / gridSteps) * i
                    drawLine(
                        color = ObsidianBorder,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                cashFlows.forEachIndexed { index, flow ->
                    val centerX = (index * slotWidth) + (slotWidth / 2f)

                    // Income Bar
                    val incomeFraction = (flow.income / maxAmount).toFloat().coerceIn(0f, 1f)
                    val incomeHeight = (chartHeight * incomeFraction * animationProgress.value).coerceAtLeast(3.dp.toPx())
                    val incomeLeft = centerX - barWidth - (barSpacing / 2f)
                    val incomeTop = chartHeight - incomeHeight

                    drawRoundRect(
                        color = ElectricMint,
                        topLeft = Offset(incomeLeft, incomeTop),
                        size = Size(barWidth, incomeHeight),
                        cornerRadius = cornerRadius
                    )

                    // Expense Bar
                    val expenseFraction = (flow.expense / maxAmount).toFloat().coerceIn(0f, 1f)
                    val expenseHeight = (chartHeight * expenseFraction * animationProgress.value).coerceAtLeast(3.dp.toPx())
                    val expenseLeft = centerX + (barSpacing / 2f)
                    val expenseTop = chartHeight - expenseHeight

                    drawRoundRect(
                        color = ExpenseRed,
                        topLeft = Offset(expenseLeft, expenseTop),
                        size = Size(barWidth, expenseHeight),
                        cornerRadius = cornerRadius
                    )
                }
            }
        }

        // Month Labels Row below canvas
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            cashFlows.forEach { flow ->
                Text(
                    text = flow.monthLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = DarkSilver
                )
            }
        }
    }
}
