package com.equity.budget.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.equity.budget.data.local.TransactionEntity
import com.equity.budget.data.model.AppLanguage
import com.equity.budget.data.model.TimePeriod
import com.equity.budget.data.model.TransactionType
import com.equity.budget.data.preferences.AppSettings
import com.equity.budget.domain.CurrencyConverter
import com.equity.budget.ui.components.ExpressivePill
import com.equity.budget.ui.components.FlatCard
import com.equity.budget.ui.components.TransactionItem
import com.equity.budget.ui.theme.ExpenseRed
import com.equity.budget.ui.theme.IncomeGreen
import com.equity.budget.ui.viewmodel.BudgetSummary

@Composable
fun HomeScreen(
    summary: BudgetSummary,
    settings: AppSettings,
    selectedPeriod: TimePeriod,
    selectedTypeFilter: TransactionType?,
    onPeriodSelected: (TimePeriod) -> Unit,
    onTypeFilterSelected: (TransactionType?) -> Unit,
    onAddTransaction: () -> Unit,
    onEditTransaction: (TransactionEntity) -> Unit,
    onUpdateSavingsGoal: (Double, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val isRu = settings.language == AppLanguage.RU
    var showGoalDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTransaction,
                modifier = Modifier.offset(y = 6.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                elevation = androidx.compose.material3.FloatingActionButtonDefaults.elevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp
                )
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Transaction")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Main Balance Card (Flat, no shadows)
            FlatCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = if (isRu) "Общий баланс" else "Total Balance",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = CurrencyConverter.format(summary.totalBalance, settings.primaryCurrency),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Income & Expense summary pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Income
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(IncomeGreen.copy(alpha = 0.12f))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(IncomeGreen.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDownward,
                                    contentDescription = null,
                                    tint = IncomeGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (isRu) "Доходы" else "Income",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = CurrencyConverter.format(summary.totalIncome, settings.primaryCurrency),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = IncomeGreen
                                )
                            }
                        }

                        // Expenses
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(ExpenseRed.copy(alpha = 0.12f))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(ExpenseRed.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowUpward,
                                    contentDescription = null,
                                    tint = ExpenseRed,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (isRu) "Расходы" else "Expenses",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = CurrencyConverter.format(summary.totalExpense, settings.primaryCurrency),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ExpenseRed
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Savings Goal & Safe Daily Limit Card
            val goal = summary.savingsGoal
            FlatCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { showGoalDialog = true },
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Savings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    if (goal.hasGoal) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isRu) "Лимит на день: " else "Daily Limit: ",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = CurrencyConverter.format(goal.safeDailyLimit, settings.primaryCurrency),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(
                                text = if (isRu) {
                                    "Осталось сегодня: ${CurrencyConverter.format(goal.remainingToday, settings.primaryCurrency)} (цель: ${CurrencyConverter.format(goal.goalAmount, settings.primaryCurrency)})"
                                } else {
                                    "Left today: ${CurrencyConverter.format(goal.remainingToday, settings.primaryCurrency)} (target: ${CurrencyConverter.format(goal.goalAmount, settings.primaryCurrency)})"
                                },
                                fontSize = 11.sp,
                                color = if (goal.remainingToday < 0) ExpenseRed else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isRu) "Цель накоплений и лимит на день" else "Savings Goal & Daily Allowance",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isRu) "Нажмите, чтобы задать цель и рассчитать расход" else "Tap to set goal and calculate safe spend",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                            .clickable { showGoalDialog = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (isRu) "Настроить" else "Edit",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Time Period Selector Pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val periods = listOf(
                    TimePeriod.TODAY to if (isRu) "Сегодня" else "Today",
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

            Spacer(modifier = Modifier.height(8.dp))

            // Type filter: All / Expense / Income
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ExpressivePill(
                    text = if (isRu) "Все" else "All",
                    isSelected = selectedTypeFilter == null,
                    onClick = { onTypeFilterSelected(null) }
                )
                ExpressivePill(
                    text = if (isRu) "Расходы" else "Expenses",
                    isSelected = selectedTypeFilter == TransactionType.EXPENSE,
                    onClick = { onTypeFilterSelected(TransactionType.EXPENSE) }
                )
                ExpressivePill(
                    text = if (isRu) "Доходы" else "Income",
                    isSelected = selectedTypeFilter == TransactionType.INCOME,
                    onClick = { onTypeFilterSelected(TransactionType.INCOME) }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Transaction List
            if (summary.filteredTransactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isRu) "Операций пока нет" else "No transactions yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isRu) "Нажмите +, чтобы добавить первую запись" else "Tap + to add your first transaction",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                FlatCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                    borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            start = 8.dp,
                            end = 8.dp,
                            top = 6.dp,
                            bottom = 88.dp // Adequate space so last item is never hidden behind FAB
                        )
                    ) {
                        items(
                            items = summary.filteredTransactions,
                            key = { it.id }
                        ) { transaction ->
                            TransactionItem(
                                transaction = transaction,
                                settings = settings,
                                onClick = { onEditTransaction(transaction) }
                            )
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f),
                                thickness = 0.5.dp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    if (showGoalDialog) {
        SavingsGoalDialog(
            currentGoal = settings.savingsGoalAmount,
            currentDays = settings.savingsTargetDays,
            currencySymbol = settings.primaryCurrency.symbol,
            isRu = isRu,
            onDismiss = { showGoalDialog = false },
            onSave = { amt, days ->
                onUpdateSavingsGoal(amt, days)
                showGoalDialog = false
            }
        )
    }
}

@Composable
fun SavingsGoalDialog(
    currentGoal: Double,
    currentDays: Int,
    currencySymbol: String,
    isRu: Boolean,
    onDismiss: () -> Unit,
    onSave: (Double, Int) -> Unit
) {
    var goalText by remember {
        mutableStateOf(if (currentGoal > 0) currentGoal.toLong().toString() else "")
    }
    var daysText by remember {
        mutableStateOf(currentDays.toString())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = if (isRu) "Цель накоплений и лимит" else "Savings Goal & Daily Limit",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = if (isRu) "Укажите неприкосновенную сумму накоплений и срок (в днях), чтобы приложение рассчитало сколько можно безопасно тратить в день."
                    else "Set target amount you want to keep and remaining days to calculate your safe daily budget.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = goalText,
                    onValueChange = { goalText = it },
                    label = { Text(if (isRu) "Цель накоплений ($currencySymbol)" else "Savings Goal ($currencySymbol)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                OutlinedTextField(
                    value = daysText,
                    onValueChange = { daysText = it },
                    label = { Text(if (isRu) "Срок (дней, например 30)" else "Period (days, e.g. 30)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val g = goalText.toDoubleOrNull() ?: 0.0
                    val d = daysText.toIntOrNull() ?: 30
                    onSave(g, d)
                },
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text(if (isRu) "Применить" else "Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isRu) "Отмена" else "Cancel")
            }
        }
    )
}
