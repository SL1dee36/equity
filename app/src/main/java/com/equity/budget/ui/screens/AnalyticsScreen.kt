package com.equity.budget.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.equity.budget.data.model.AppLanguage
import com.equity.budget.data.model.TimePeriod
import com.equity.budget.data.preferences.AppSettings
import com.equity.budget.domain.CurrencyConverter
import com.equity.budget.ui.components.CategoryDonutChart
import com.equity.budget.ui.components.CumulativeBalanceChart
import com.equity.budget.ui.components.ExpressivePill
import com.equity.budget.ui.components.FlatCard
import com.equity.budget.ui.components.TrendChart
import com.equity.budget.ui.components.WeekdaySpendingChart
import com.equity.budget.ui.theme.ExpenseRed
import com.equity.budget.ui.theme.IncomeGreen
import com.equity.budget.ui.viewmodel.BudgetSummary

@Composable
fun AnalyticsScreen(
    summary: BudgetSummary,
    settings: AppSettings,
    selectedPeriod: TimePeriod,
    onPeriodSelected: (TimePeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    val isRu = settings.language == AppLanguage.RU
    var activeChartTab by remember { mutableIntStateOf(0) } // 0: Trend, 1: Cumulative, 2: Weekday, 3: Categories

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = if (isRu) "Аналитика и графики" else "Analytics & Charts",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Period filter pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val periods = listOf(
                TimePeriod.WEEK to if (isRu) "Неделя" else "Week",
                TimePeriod.MONTH to if (isRu) "Месяц" else "Month",
                TimePeriod.YEAR to if (isRu) "Год" else "Year",
                TimePeriod.ALL to if (isRu) "Всё время" else "All"
            )
            periods.forEach { (p, label) ->
                ExpressivePill(
                    text = label,
                    isSelected = p == selectedPeriod,
                    onClick = { onPeriodSelected(p) }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Key metrics cards (Flat, 3 cards in row)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Net Cash Flow
            FlatCard(
                modifier = Modifier.weight(1f),
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = if (isRu) "Чистый итог" else "Net Cash Flow",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val net = summary.totalIncome - summary.totalExpense
                    Text(
                        text = CurrencyConverter.format(net, settings.primaryCurrency, showSign = true),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (net >= 0) IncomeGreen else ExpenseRed
                    )
                }
            }

            // Savings rate
            FlatCard(
                modifier = Modifier.weight(1f),
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = if (isRu) "Сбережения" else "Savings Rate",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = String.format("%.1f%%", summary.savingsRate),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (summary.savingsRate >= 0) MaterialTheme.colorScheme.primary else ExpenseRed
                    )
                }
            }

            // Daily average expense
            FlatCard(
                modifier = Modifier.weight(1f),
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = if (isRu) "В день" else "Avg / Day",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = CurrencyConverter.format(summary.avgDailyExpense, settings.primaryCurrency),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Chart Type Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val chartTabs = listOf(
                0 to if (isRu) "Тенденция" else "Trend",
                1 to if (isRu) "Динамика баланса" else "Balance Flow",
                2 to if (isRu) "По дням недели" else "Weekdays",
                3 to if (isRu) "Категории" else "Categories"
            )
            chartTabs.forEach { (tabIdx, label) ->
                ExpressivePill(
                    text = label,
                    isSelected = tabIdx == activeChartTab,
                    onClick = { activeChartTab = tabIdx }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Selected Chart Card
        FlatCard(
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                when (activeChartTab) {
                    0 -> {
                        Text(
                            text = if (isRu) "Тенденция доходов и расходов" else "Income & Expense Trend",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        TrendChart(
                            points = summary.trendPoints,
                            language = settings.language
                        )
                    }
                    1 -> {
                        Text(
                            text = if (isRu) "Динамика накоплений и баланса" else "Cumulative Balance Curve",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        CumulativeBalanceChart(
                            points = summary.cumulativePoints,
                            currency = settings.primaryCurrency,
                            language = settings.language
                        )
                    }
                    2 -> {
                        Text(
                            text = if (isRu) "Распределение трат по дням недели" else "Spending by Day of Week",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        WeekdaySpendingChart(
                            days = summary.weekdaySpends,
                            language = settings.language
                        )
                    }
                    3 -> {
                        Text(
                            text = if (isRu) "Расходы по категориям" else "Expenses by Category",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        CategoryDonutChart(
                            shares = summary.categoryBreakdown,
                            totalAmount = summary.totalExpense,
                            currency = settings.primaryCurrency,
                            language = settings.language
                        )
                    }
                }
            }
        }

        // Also show Category breakdown below if not already on the Categories tab
        if (activeChartTab != 3 && summary.categoryBreakdown.isNotEmpty()) {
            Spacer(modifier = Modifier.height(14.dp))
            FlatCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isRu) "Доли категорий расходов" else "Category Breakdown",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    CategoryDonutChart(
                        shares = summary.categoryBreakdown,
                        totalAmount = summary.totalExpense,
                        currency = settings.primaryCurrency,
                        language = settings.language
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
