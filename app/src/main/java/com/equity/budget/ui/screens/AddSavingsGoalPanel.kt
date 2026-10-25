package com.equity.budget.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.equity.budget.data.model.AppLanguage
import com.equity.budget.data.model.Currency
import com.equity.budget.data.preferences.AppSettings
import com.equity.budget.domain.CurrencyConverter
import com.equity.budget.ui.components.FlatCard
import com.equity.budget.ui.theme.IncomeGreen
import kotlin.math.max

@Composable
fun AddSavingsGoalPanel(
    settings: AppSettings,
    onSaveGoal: (title: String, targetAmount: Double, currentAmount: Double, targetMonths: Int, currency: Currency) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isRu = settings.language == AppLanguage.RU

    var title by remember { mutableStateOf("") }
    var targetAmountText by remember { mutableStateOf("") }
    var currentAmountText by remember { mutableStateOf("") }
    var monthsText by remember { mutableStateOf("12") }
    var selectedCurrency by remember { mutableStateOf(settings.primaryCurrency) }

    val targetAmount = targetAmountText.toDoubleOrNull() ?: 0.0
    val currentAmount = currentAmountText.toDoubleOrNull() ?: 0.0
    val months = (monthsText.toIntOrNull() ?: 12).coerceAtLeast(1)

    // Calculation: (Target - Current) / Months
    val remainingToSave = max(0.0, targetAmount - currentAmount)
    val monthlyPayment = if (months > 0) remainingToSave / months else 0.0
    val dailyPayment = monthlyPayment / 30.0

    val presets = remember(isRu) {
        if (isRu) listOf("Машина", "Отпуск", "Подушка безопасности", "Ноутбук", "Квартира", "Ремонт", "Подарок")
        else listOf("Car", "Vacation", "Emergency Fund", "Laptop", "Apartment", "Renovation")
    }

    val monthOptions = listOf(3, 6, 12, 18, 24, 36)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        // Goal Name Input
        Text(
            text = if (isRu) "Название цели" else "Goal Title",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(if (isRu) "Например: Отпуск на море" else "e.g. Vacation in Italy") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        )

        // Presets row
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            presets.forEach { preset ->
                val isPicked = title.equals(preset, ignoreCase = true)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isPicked) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        )
                        .clickable { title = preset }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = preset,
                        fontSize = 12.sp,
                        fontWeight = if (isPicked) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isPicked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Target Amount & Currency selector
        Text(
            text = if (isRu) "Сколько хотите накопить" else "Target Amount",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = targetAmountText,
                onValueChange = { input ->
                    val sanitized = input.replace(',', '.')
                    if (sanitized.count { it == '.' } <= 1 && sanitized.all { it.isDigit() || it == '.' }) {
                        targetAmountText = sanitized
                    }
                },
                modifier = Modifier.weight(1f),
                placeholder = { Text("0.00", fontSize = 22.sp, fontWeight = FontWeight.Bold) },
                textStyle = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            )

            Spacer(modifier = Modifier.width(10.dp))

            // Currency pills
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Currency.entries.forEach { cur ->
                    val selected = cur == selectedCurrency
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                            .clickable { selectedCurrency = cur }
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = cur.symbol,
                            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Initial Saved Amount (Already have)
        Text(
            text = if (isRu) "Уже накоплено сейчас (необязательно)" else "Already Saved (Optional)",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = currentAmountText,
            onValueChange = { input ->
                val sanitized = input.replace(',', '.')
                if (sanitized.count { it == '.' } <= 1 && sanitized.all { it.isDigit() || it == '.' }) {
                    currentAmountText = sanitized
                }
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("0.00") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Target Duration / Period in Months
        Text(
            text = if (isRu) "Срок накопления (в месяцах)" else "Period (months)",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = monthsText,
            onValueChange = { input ->
                if (input.all { it.isDigit() } && input.length <= 3) {
                    monthsText = input
                }
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("12") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        )

        // Month quick selection chips
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            monthOptions.forEach { m ->
                val isPicked = monthsText == m.toString()
                val label = if (isRu) {
                    when (m) {
                        12 -> "12 мес (1 год)"
                        24 -> "24 мес (2 года)"
                        36 -> "36 мес (3 года)"
                        else -> "$m мес"
                    }
                } else {
                    when (m) {
                        12 -> "12 mo (1 yr)"
                        24 -> "24 mo (2 yrs)"
                        else -> "$m mo"
                    }
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isPicked) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        )
                        .clickable { monthsText = m.toString() }
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (isPicked) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isPicked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Live Calculation Result Card
        FlatCard(
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
            borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
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
                            text = "${CurrencyConverter.format(monthlyPayment, selectedCurrency)} / мес",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (isRu) "В день:" else "Per day:",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "≈ ${CurrencyConverter.format(dailyPayment, selectedCurrency)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                if (targetAmount > 0) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isRu) {
                            "Осталось накопить: ${CurrencyConverter.format(remainingToSave, selectedCurrency)} за $months мес."
                        } else {
                            "Remaining to save: ${CurrencyConverter.format(remainingToSave, selectedCurrency)} over $months months."
                        },
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Submit Button
        Button(
            onClick = {
                if (targetAmount > 0) {
                    onSaveGoal(title, targetAmount, currentAmount, months, selectedCurrency)
                    onDismiss()
                }
            },
            enabled = targetAmount > 0,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Icon(
                imageVector = Icons.Default.Savings,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isRu) "Создать цель накопления" else "Create Savings Goal",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
