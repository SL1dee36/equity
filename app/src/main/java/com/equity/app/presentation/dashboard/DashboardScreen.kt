package com.equity.app.presentation.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.equity.app.core.theme.DarkSilver
import com.equity.app.core.theme.ElectricMint
import com.equity.app.core.theme.ElectricMintDark
import com.equity.app.core.theme.MutedSilver
import com.equity.app.core.theme.ObsidianBlack
import com.equity.app.core.theme.ObsidianBorder
import com.equity.app.core.theme.ObsidianCard
import com.equity.app.core.theme.PlatinumText
import com.equity.app.core.theme.PlatinumWhite
import com.equity.app.core.util.Formatters
import com.equity.app.presentation.components.AddTransactionBottomSheet
import com.equity.app.presentation.components.CustomBarChart
import com.equity.app.presentation.components.CustomPieChart
import com.equity.app.presentation.components.HeroNetWorthCard
import com.equity.app.presentation.components.SavingsGoalProgressCard
import com.equity.app.presentation.components.TransactionItem
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToTransactions: () -> Unit,
    onNavigateToCalculator: () -> Unit,
    viewModel: DashboardViewModel = koinViewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ObsidianBlack,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openAddTransactionSheet() },
                containerColor = ElectricMint,
                contentColor = ObsidianBlack,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 80.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Add Transaction",
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    ) { innerPadding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = ElectricMint)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Top Brand Bar
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "EQUITY",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 2.sp
                                    ),
                                    color = PlatinumWhite
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(ElectricMint, CircleShape)
                                )
                            }
                            Text(
                                text = "Smart Balance & Wealth Calculator",
                                style = MaterialTheme.typography.labelSmall,
                                color = DarkSilver
                            )
                        }

                        // Status Chip
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = ObsidianCard,
                            border = androidx.compose.foundation.BorderStroke(1.dp, ObsidianBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AutoAwesome,
                                    contentDescription = null,
                                    tint = ElectricMint,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Offline Vault",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = MutedSilver
                                )
                            }
                        }
                    }
                }

                // Hero Card: Net Balance & Cash Flow
                item {
                    HeroNetWorthCard(
                        currentBalance = state.financialSummary.currentBalance,
                        savingsRate = state.financialSummary.savingsRatePercentage,
                        monthlyIncome = state.financialSummary.monthlyIncome,
                        monthlyExpense = state.financialSummary.monthlyExpense
                    )
                }

                // Equity Formula Quick Metric Card
                state.equityFormulaResult?.let { formula ->
                    item {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .border(1.dp, ObsidianBorder, RoundedCornerShape(18.dp)),
                            color = ObsidianCard
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(ElectricMint, CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "EQUITY FORMULA ENGINE",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = ElectricMint
                                        )
                                    }

                                    TextButton(onClick = onNavigateToCalculator) {
                                        Text(
                                            text = "Details",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = ElectricMint
                                        )
                                        Icon(
                                            imageVector = Icons.Rounded.ArrowForward,
                                            contentDescription = null,
                                            tint = ElectricMint,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = "Safe to Spend",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = DarkSilver
                                        )
                                        Text(
                                            text = "${Formatters.formatCurrency(formula.safeToSpendDaily)}/day",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = PlatinumWhite
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "Auto-Lock Incoming",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = DarkSilver
                                        )
                                        Text(
                                            text = "${formula.incomingLockPercentage.toInt()}% per deposit",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = ElectricMint
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Active Savings Goal Card
                item {
                    SavingsGoalProgressCard(
                        goal = state.activeSavingsGoal,
                        onNavigateToCalculator = onNavigateToCalculator
                    )
                }

                // Analytics: Expense Distribution Pie Chart
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .border(1.dp, ObsidianBorder, RoundedCornerShape(20.dp)),
                        color = ObsidianCard
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = "EXPENSE DISTRIBUTION",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = DarkSilver
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            CustomPieChart(items = state.categoryBreakdown)
                        }
                    }
                }

                // Analytics: Monthly Cash Flow Trends Bar Chart
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .border(1.dp, ObsidianBorder, RoundedCornerShape(20.dp)),
                        color = ObsidianCard
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = "MONTHLY CASH FLOW TRENDS",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = DarkSilver
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            CustomBarChart(cashFlows = state.monthlyTrends)
                        }
                    }
                }

                // Recent Transactions Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent Transactions",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = PlatinumWhite
                        )
                        TextButton(onClick = onNavigateToTransactions) {
                            Text(
                                text = "See All",
                                style = MaterialTheme.typography.labelMedium,
                                color = ElectricMint
                            )
                        }
                    }
                }

                // Recent Transactions Items
                if (state.recentTransactions.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No transactions yet. Tap '+' to create your first entry.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = DarkSilver
                            )
                        }
                    }
                } else {
                    items(state.recentTransactions, key = { it.id }) { transaction ->
                        TransactionItem(
                            transaction = transaction,
                            onItemClick = {},
                            onDeleteClick = {}
                        )
                    }
                }
            }
        }

        // Add Transaction Sheet
        if (state.isAddTransactionSheetOpen) {
            AddTransactionBottomSheet(
                sheetState = bottomSheetState,
                onDismiss = { viewModel.closeAddTransactionSheet() },
                onSaveTransaction = { newTx ->
                    viewModel.addTransaction(newTx)
                }
            )
        }
    }
}
