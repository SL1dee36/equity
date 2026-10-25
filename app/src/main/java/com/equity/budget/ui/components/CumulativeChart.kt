package com.equity.budget.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.equity.budget.data.model.AppLanguage
import com.equity.budget.data.model.Currency
import com.equity.budget.domain.CurrencyConverter
import com.equity.budget.ui.theme.IncomeGreen
import kotlin.math.max
import kotlin.math.min

data class CumulativePoint(
    val label: String,
    val balance: Double
)

@Composable
fun CumulativeBalanceChart(
    points: List<CumulativePoint>,
    currency: Currency,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val isRu = language == AppLanguage.RU
    if (points.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isRu) "Недостаточно данных для графика" else "Not enough data for chart",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )
        }
        return
    }

    val minBal = points.minOfOrNull { it.balance } ?: 0.0
    val maxBal = points.maxOfOrNull { it.balance } ?: 0.0
    val range = max(1.0, maxBal - minBal)

    val primaryColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) {
            val w = size.width
            val h = size.height
            val bottomPadding = 20f
            val topPadding = 10f
            val chartH = h - bottomPadding - topPadding

            // Horizontal guide lines
            for (i in 0..2) {
                val y = topPadding + chartH * (i / 2f)
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(w, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            if (points.size < 2) return@Canvas

            val stepX = w / (points.size - 1)
            val path = Path()
            val fillPath = Path()

            points.forEachIndexed { i, pt ->
                val x = i * stepX
                val normalizedY = ((pt.balance - minBal) / range).toFloat()
                val y = topPadding + chartH * (1f - normalizedY)

                if (i == 0) {
                    path.moveTo(x, y)
                    fillPath.moveTo(x, chartH + topPadding)
                    fillPath.lineTo(x, y)
                } else {
                    val prevX = (i - 1) * stepX
                    val prevNormY = ((points[i - 1].balance - minBal) / range).toFloat()
                    val prevY = topPadding + chartH * (1f - prevNormY)

                    // Smooth cubic bezier
                    val controlX1 = prevX + (x - prevX) / 2f
                    val controlY1 = prevY
                    val controlX2 = prevX + (x - prevX) / 2f
                    val controlY2 = y
                    path.cubicTo(controlX1, controlY1, controlX2, controlY2, x, y)
                    fillPath.cubicTo(controlX1, controlY1, controlX2, controlY2, x, y)
                }

                if (i == points.size - 1) {
                    fillPath.lineTo(x, chartH + topPadding)
                    fillPath.close()
                }
            }

            // Fill gradient under curve
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(primaryColor.copy(alpha = 0.25f), Color.Transparent),
                    startY = topPadding,
                    endY = chartH + topPadding
                )
            )

            // Line stroke
            drawPath(
                path = path,
                color = primaryColor,
                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
            )

            // Draw final dot
            val lastX = (points.size - 1) * stepX
            val lastNormY = ((points.last().balance - minBal) / range).toFloat()
            val lastY = topPadding + chartH * (1f - lastNormY)
            drawCircle(
                color = primaryColor,
                radius = 4.dp.toPx(),
                center = Offset(lastX, lastY)
            )
        }

        // Labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        ) {
            Text(
                text = points.firstOrNull()?.label ?: "",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = points.lastOrNull()?.label ?: "",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
