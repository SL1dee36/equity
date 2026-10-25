package com.equity.budget.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.equity.budget.data.model.Category
import com.equity.budget.data.model.Currency
import com.equity.budget.data.model.TransactionType
import com.equity.budget.data.preferences.AppSettings
import com.equity.budget.ui.components.ExpressivePill
import com.equity.budget.ui.theme.ExpenseRed
import com.equity.budget.ui.theme.IncomeGreen

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditTransactionSheet(
    settings: AppSettings,
    recentCategoryTitles: List<String> = emptyList(),
    existingTransaction: TransactionEntity? = null,
    onCategoryChanged: (String) -> Unit = {},
    onDismiss: () -> Unit,
    onSave: (title: String, amount: Double, currency: Currency, type: TransactionType, categoryId: String) -> Unit,
    onDelete: ((Long) -> Unit)? = null
) {
    val isRu = settings.language == AppLanguage.RU
    val isEditing = existingTransaction != null

    var type by remember {
        mutableStateOf(
            if (existingTransaction != null) {
                if (existingTransaction.type == TransactionType.INCOME.id) TransactionType.INCOME else TransactionType.EXPENSE
            } else TransactionType.EXPENSE
        )
    }

    var amountText by remember {
        mutableStateOf(if (existingTransaction != null) {
            if (existingTransaction.amount % 1.0 == 0.0) existingTransaction.amount.toLong().toString()
            else existingTransaction.amount.toString()
        } else "")
    }

    var title by remember {
        mutableStateOf(existingTransaction?.title ?: "")
    }

    var selectedCurrency by remember {
        mutableStateOf(
            if (existingTransaction != null) Currency.fromCode(existingTransaction.currency)
            else settings.primaryCurrency
        )
    }

    val availableCategories = remember(type) { Category.getCategoriesForType(type) }
    var selectedCategoryId by remember(type) {
        mutableStateOf(
            existingTransaction?.categoryId ?: availableCategories.firstOrNull()?.id ?: ""
        )
    }

    val currentCategory = remember(selectedCategoryId) { Category.getById(selectedCategoryId) }
    val suggestionList = remember(selectedCategoryId, recentCategoryTitles, settings.language) {
        (recentCategoryTitles + currentCategory.localizedPresets(settings.language)).distinct()
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isEditing) {
                        if (isRu) "Редактирование" else "Edit Transaction"
                    } else {
                        if (isRu) "Новая операция" else "New Transaction"
                    },
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (isEditing && onDelete != null) {
                    IconButton(onClick = {
                        onDelete(existingTransaction!!.id)
                        onDismiss()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = ExpenseRed
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Income / Expense Pill Switcher
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(CircleShape)
                        .background(if (type == TransactionType.EXPENSE) ExpenseRed else Color.Transparent)
                        .clickable {
                            type = TransactionType.EXPENSE
                            val newCat = Category.getCategoriesForType(TransactionType.EXPENSE).firstOrNull()?.id ?: ""
                            selectedCategoryId = newCat
                            onCategoryChanged(newCat)
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isRu) "Расход" else "Expense",
                        color = if (type == TransactionType.EXPENSE) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(CircleShape)
                        .background(if (type == TransactionType.INCOME) IncomeGreen else Color.Transparent)
                        .clickable {
                            type = TransactionType.INCOME
                            val newCat = Category.getCategoriesForType(TransactionType.INCOME).firstOrNull()?.id ?: ""
                            selectedCategoryId = newCat
                            onCategoryChanged(newCat)
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isRu) "Доход" else "Income",
                        color = if (type == TransactionType.INCOME) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Amount Input & Currency selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { input ->
                        val sanitized = input.replace(',', '.')
                        if (sanitized.count { it == '.' } <= 1 && sanitized.all { it.isDigit() || it == '.' }) {
                            amountText = sanitized
                        }
                    },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("0.00", fontSize = 24.sp, fontWeight = FontWeight.Bold) },
                    textStyle = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 26.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                )

                Spacer(modifier = Modifier.width(10.dp))

                // Currency selector pills
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Currency.entries.forEach { cur ->
                        val selected = cur == selectedCurrency
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (selected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                                .clickable { selectedCurrency = cur }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cur.symbol,
                                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Categories Grid
            Text(
                text = if (isRu) "Категория" else "Category",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                availableCategories.forEach { category ->
                    val isCatSelected = category.id == selectedCategoryId
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isCatSelected) category.color.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                            )
                            .clickable {
                                selectedCategoryId = category.id
                                onCategoryChanged(category.id)
                            }
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = category.icon,
                            contentDescription = null,
                            tint = if (isCatSelected) category.color else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = category.localizedName(settings.language),
                            fontSize = 12.sp,
                            fontWeight = if (isCatSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isCatSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Title / Description Field
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        if (isRu) "Описание (выберите шаблон ниже или введите)" else "Description (tap preset below or type)",
                        fontSize = 13.sp
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            )

            // Quick Preset Suggestions ("протыкать описание сразу")
            if (suggestionList.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    suggestionList.forEach { preset ->
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
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save Button
            val amountVal = amountText.toDoubleOrNull() ?: 0.0
            Button(
                onClick = {
                    if (amountVal > 0) {
                        onSave(title, amountVal, selectedCurrency, type, selectedCategoryId)
                        onDismiss()
                    }
                },
                enabled = amountVal > 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = if (isRu) "Сохранить" else "Save",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
