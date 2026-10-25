package com.equity.app.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.equity.app.core.theme.DarkSilver
import com.equity.app.core.theme.ElectricMint
import com.equity.app.core.theme.ElectricMintDark
import com.equity.app.core.theme.ElectricMintGlow
import com.equity.app.core.theme.ElectricMintSoft
import com.equity.app.core.theme.ExpenseRed
import com.equity.app.core.theme.MutedSilver
import com.equity.app.core.theme.ObsidianBorder
import com.equity.app.core.theme.ObsidianCard
import com.equity.app.core.theme.ObsidianCardElevated
import com.equity.app.core.theme.PlatinumText
import com.equity.app.core.theme.PlatinumWhite
import com.equity.app.core.util.Formatters
import com.equity.app.domain.model.SavingsGoal

@Composable
fun HeroNetWorthCard(
    currentBalance: Double,
    savingsRate: Float,
    monthlyIncome: Double,
    monthlyExpense: Double,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, Brush.verticalGradient(listOf(ElectricMintSoft.copy(alpha = 0.4f), ObsidianBorder)), RoundedCornerShape(24.dp)),
        color = ObsidianCardElevated,
        tonalElevation = 6.dp
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.radialGradient(
                        colors = listOf(ElectricMintDark.copy(alpha = 0.22f), Color(0xFF141716)),
                        radius = 800f
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(ElectricMintGlow, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.TrendingUp,
                                contentDescription = null,
                                tint = ElectricMint,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "TOTAL NET BALANCE",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp),
                            color = MutedSilver
                        )
                    }

                    // Savings rate badge
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (savingsRate >= 0) ElectricMint.copy(alpha = 0.15f) else ExpenseRed.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (savingsRate >= 0) ElectricMint.copy(alpha = 0.4f) else ExpenseRed.copy(alpha = 0.4f)
                        )
                    ) {
                        Text(
                            text = "${if (savingsRate >= 0) "+" else ""}${Formatters.formatPercentage(savingsRate)} saved",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = if (savingsRate >= 0) ElectricMint else ExpenseRed,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Big Net Balance
                Text(
                    text = Formatters.formatCurrency(currentBalance),
                    style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = PlatinumWhite
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Income / Expense dual pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IncomeExpenseChip(
                        isIncome = true,
                        label = "Income",
                        amount = monthlyIncome,
                        modifier = Modifier.weight(1f)
                    )
                    IncomeExpenseChip(
                        isIncome = false,
                        label = "Expense",
                        amount = monthlyExpense,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun IncomeExpenseChip(
    isIncome: Boolean,
    label: String,
    amount: Double,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = ObsidianCard,
        border = androidx.compose.foundation.BorderStroke(1.dp, ObsidianBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(
                        if (isIncome) ElectricMint.copy(alpha = 0.15f) else ExpenseRed.copy(alpha = 0.15f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isIncome) Icons.Rounded.ArrowUpward else Icons.Rounded.ArrowDownward,
                    contentDescription = null,
                    tint = if (isIncome) ElectricMint else ExpenseRed,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = DarkSilver
                )
                Text(
                    text = Formatters.formatCurrency(amount),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
                    color = PlatinumText
                )
            }
        }
    }
}

@Composable
fun SavingsGoalProgressCard(
    goal: SavingsGoal?,
    onNavigateToCalculator: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, ObsidianBorder, RoundedCornerShape(20.dp)),
        color = ObsidianCard,
        tonalElevation = 2.dp
    ) {
        if (goal == null) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "No Active Savings Goal",
                    style = MaterialTheme.typography.titleMedium,
                    color = PlatinumWhite
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Set a goal in the Smart Formula tab to calculate your wealth trajectory",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DarkSilver
                )
            }
        } else {
            val animatedProgress by animateFloatAsState(
                targetValue = goal.progress,
                animationSpec = tween(1000),
                label = "GoalProgress"
            )

            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "EQUITY SAVINGS GOAL",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = ElectricMint
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = goal.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = PlatinumWhite
                        )
                    }

                    Text(
                        text = "${(goal.progress * 100).toInt()}%",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = ElectricMint
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = ElectricMint,
                    trackColor = ObsidianBorder,
                    strokeCap = StrokeCap.Round
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Saved: ${Formatters.formatCurrency(goal.currentAmount)}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                        color = MutedSilver
                    )
                    Text(
                        text = "Target: ${Formatters.formatCurrency(goal.targetAmount)} (${goal.daysRemaining}d left)",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                        color = DarkSilver
                    )
                }
            }
        }
    }
}
