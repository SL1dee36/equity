package com.equity.budget.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.equity.budget.data.local.SavingsGoalEntity
import com.equity.budget.data.model.AppLanguage
import com.equity.budget.data.model.Currency
import com.equity.budget.data.preferences.AppSettings
import com.equity.budget.domain.CurrencyConverter
import com.equity.budget.ui.theme.ExpenseRed
import com.equity.budget.ui.theme.IncomeGreen

@Composable
fun SavingsGoalsSection(
    goals: List<SavingsGoalEntity>,
    settings: AppSettings,
    onAddNewGoal: () -> Unit,
    onDepositToGoal: (goalId: Long, amount: Double) -> Unit,
    onDeleteGoal: (goalId: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val isRu = settings.language == AppLanguage.RU
    var isExpanded by remember { mutableStateOf(true) }
    var depositingGoal by remember { mutableStateOf<SavingsGoalEntity?>(null) }
    var deletingGoal by remember { mutableStateOf<SavingsGoalEntity?>(null) }

    val totalMonthlyNeeded = goals.filter { !it.isCompleted }.sumOf { it.monthlyPaymentNeeded }

    Column(modifier = modifier.fillMaxWidth()) {
        // Section Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { isExpanded = !isExpanded }
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Savings,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isRu) "Цели накоплений" else "Savings Goals",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (goals.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${goals.size}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }

            if (goals.isNotEmpty()) {
                Text(
                    text = buildString {
                        append(if (isRu) "План: " else "Plan: ")
                        append(CurrencyConverter.format(totalMonthlyNeeded, settings.primaryCurrency))
                        append(if (isRu) "/мес" else "/mo")
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        AnimatedVisibility(visible = isExpanded) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (goals.isEmpty()) {
                    FlatCard(
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                        onClick = onAddNewGoal
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isRu) "Поставьте первую цель накопления" else "Set your first savings goal",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isRu) "Укажите сумму и срок в меню +, и приложение рассчитает план отчислений в месяц"
                                    else "Specify amount and period in + menu to calculate your monthly savings plan",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 15.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isRu) "Создать" else "Add",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                } else {
                    goals.forEach { goal ->
                        SavingsGoalCard(
                            goal = goal,
                            settings = settings,
                            isRu = isRu,
                            onDepositClick = { depositingGoal = goal },
                            onDeleteClick = { deletingGoal = goal }
                        )
                    }
                }
            }
        }
    }

    // Deposit dialog
    depositingGoal?.let { goal ->
        var depositAmountText by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { depositingGoal = null },
            title = {
                Text(
                    text = if (isRu) "Пополнить «${goal.title}»" else "Deposit to «${goal.title}»",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (isRu) {
                            "Осталось накопить: ${CurrencyConverter.format(goal.remainingAmount, Currency.fromCode(goal.currency))}. Введите внесенную сумму:"
                        } else {
                            "Remaining to save: ${CurrencyConverter.format(goal.remainingAmount, Currency.fromCode(goal.currency))}. Enter amount:"
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = depositAmountText,
                        onValueChange = { input ->
                            val sanitized = input.replace(',', '.')
                            if (sanitized.count { it == '.' } <= 1 && sanitized.all { it.isDigit() || it == '.' }) {
                                depositAmountText = sanitized
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("0.00") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        )
                    )
                }
            },
            confirmButton = {
                val amt = depositAmountText.toDoubleOrNull() ?: 0.0
                Button(
                    onClick = {
                        if (amt > 0) {
                            onDepositToGoal(goal.id, amt)
                            depositingGoal = null
                        }
                    },
                    enabled = amt > 0,
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Text(if (isRu) "Внести" else "Deposit")
                }
            },
            dismissButton = {
                TextButton(onClick = { depositingGoal = null }) {
                    Text(if (isRu) "Отмена" else "Cancel")
                }
            }
        )
    }

    // Delete confirmation dialog
    deletingGoal?.let { goal ->
        AlertDialog(
            onDismissRequest = { deletingGoal = null },
            title = {
                Text(if (isRu) "Удалить цель?" else "Delete Goal?")
            },
            text = {
                Text(
                    text = if (isRu) "Вы уверены, что хотите удалить цель «${goal.title}»?"
                    else "Are you sure you want to delete «${goal.title}»?",
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteGoal(goal.id)
                        deletingGoal = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Text(if (isRu) "Удалить" else "Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingGoal = null }) {
                    Text(if (isRu) "Отмена" else "Cancel")
                }
            }
        )
    }
}

@Composable
fun SavingsGoalCard(
    goal: SavingsGoalEntity,
    settings: AppSettings,
    isRu: Boolean,
    onDepositClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val currency = Currency.fromCode(goal.currency)

    FlatCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Title, Target and Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Text(
                        text = goal.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    if (goal.isCompleted) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Completed",
                            tint = IncomeGreen,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                            .clickable(onClick = onDepositClick)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isRu) "+ Внести" else "+ Deposit",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { goal.progressPercent / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = if (goal.isCompleted) IncomeGreen else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                strokeCap = StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Amounts row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${CurrencyConverter.format(goal.currentAmount, currency)} из ${CurrencyConverter.format(goal.targetAmount, currency)}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${goal.progressPercent.toInt()}%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (goal.isCompleted) IncomeGreen else MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Monthly Savings Calculation Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (goal.isCompleted) IncomeGreen.copy(alpha = 0.1f)
                        else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                    )
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                if (goal.isCompleted) {
                    Text(
                        text = if (isRu) "🎉 Цель достигнута! Поздравляем!" else "🎉 Goal reached! Congratulations!",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = IncomeGreen
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isRu) "Нужно откладывать в месяц:" else "Monthly savings needed:",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${CurrencyConverter.format(goal.monthlyPaymentNeeded, currency)} / мес",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (isRu) "Срок:" else "Period:",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (isRu) "${goal.targetMonths} мес. (≈ ${CurrencyConverter.format(goal.dailyPaymentNeeded, currency)}/день)"
                                else "${goal.targetMonths} mo. (≈ ${CurrencyConverter.format(goal.dailyPaymentNeeded, currency)}/day)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}
