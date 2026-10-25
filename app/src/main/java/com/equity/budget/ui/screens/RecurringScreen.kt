package com.equity.budget.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.equity.budget.data.local.RecurringRuleEntity
import com.equity.budget.data.model.AppLanguage
import com.equity.budget.data.model.Category
import com.equity.budget.data.model.Currency
import com.equity.budget.data.model.RecurrenceFrequency
import com.equity.budget.data.model.TransactionType
import com.equity.budget.data.preferences.AppSettings
import com.equity.budget.domain.CurrencyConverter
import com.equity.budget.ui.components.ExpressivePill
import com.equity.budget.ui.components.FlatCard
import com.equity.budget.ui.theme.ExpenseRed
import com.equity.budget.ui.theme.IncomeGreen

@Composable
fun RecurringScreen(
    rules: List<RecurringRuleEntity>,
    settings: AppSettings,
    onAddRule: (title: String, amount: Double, currency: Currency, type: TransactionType, categoryId: String, freq: RecurrenceFrequency, dayOfMonth: Int, dayOfWeek: Int) -> Unit,
    onToggleRule: (id: Long, isActive: Boolean) -> Unit,
    onDeleteRule: (Long) -> Unit,
    onCheckDueNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isRu = settings.language == AppLanguage.RU
    var showAddSheet by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            Button(
                onClick = { showAddSheet = true },
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp),
                modifier = Modifier.height(48.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isRu) "Новое правило" else "New Auto-Rule",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            // Info Card
            FlatCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EventRepeat,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isRu) "Автодобавление" else "Scheduled Auto-Add",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isRu) "Автоматически начисляет зарплату или списывает платежи точно в срок"
                            else "Automatically creates transactions on scheduled dates like salary or rent",
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = onCheckDueNow) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Sync",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Rules List
            if (rules.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isRu) "Нет правил автодобавления" else "No auto-rules configured",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isRu) "Например: Зарплата каждого 10 числа или Аренда" else "e.g. Salary on 10th of every month or Rent",
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = rules,
                        key = { it.id }
                    ) { rule ->
                        RecurringRuleItem(
                            rule = rule,
                            settings = settings,
                            onToggle = { isActive -> onToggleRule(rule.id, isActive) },
                            onDelete = { onDeleteRule(rule.id) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    if (showAddSheet) {
        AddRecurringRuleSheet(
            settings = settings,
            onDismiss = { showAddSheet = false },
            onSave = { title, amount, currency, type, catId, freq, dom, dow ->
                onAddRule(title, amount, currency, type, catId, freq, dom, dow)
                showAddSheet = false
            }
        )
    }
}

@Composable
fun RecurringRuleItem(
    rule: RecurringRuleEntity,
    settings: AppSettings,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    val isRu = settings.language == AppLanguage.RU
    val category = Category.getById(rule.categoryId)
    val isIncome = rule.type == TransactionType.INCOME.id
    val currency = Currency.fromCode(rule.currency)

    val freqText = when (rule.frequency.lowercase()) {
        "monthly" -> if (isRu) "Каждое ${rule.dayOfMonth}-е число" else "Every ${rule.dayOfMonth}th of month"
        "weekly" -> {
            val dowName = when (rule.dayOfWeek) {
                1 -> if (isRu) "понедельникам" else "Mondays"
                2 -> if (isRu) "вторникам" else "Tuesdays"
                3 -> if (isRu) "средам" else "Wednesdays"
                4 -> if (isRu) "четвергам" else "Thursdays"
                5 -> if (isRu) "пятницам" else "Fridays"
                6 -> if (isRu) "субботам" else "Saturdays"
                else -> if (isRu) "воскресеньям" else "Sundays"
            }
            if (isRu) "По $dowName" else "Every $dowName"
        }
        "biweekly" -> if (isRu) "Каждые 2 недели" else "Every 2 weeks"
        else -> rule.frequency
    }

    FlatCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (rule.isActive) 0.4f else 0.15f),
        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = if (rule.isActive) 0.15f else 0.08f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(category.color.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = null,
                    tint = category.color,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = rule.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = freqText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = CurrencyConverter.format(rule.amount, currency, isIncome = isIncome),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isIncome) IncomeGreen else MaterialTheme.colorScheme.onSurface
                )
            }

            Switch(
                checked = rule.isActive,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddRecurringRuleSheet(
    settings: AppSettings,
    onDismiss: () -> Unit,
    onSave: (title: String, amount: Double, currency: Currency, type: TransactionType, categoryId: String, freq: RecurrenceFrequency, dayOfMonth: Int, dayOfWeek: Int) -> Unit
) {
    val isRu = settings.language == AppLanguage.RU
    var type by remember { mutableStateOf(TransactionType.INCOME) }
    var amountText by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var selectedCurrency by remember { mutableStateOf(settings.primaryCurrency) }
    var frequency by remember { mutableStateOf(RecurrenceFrequency.MONTHLY) }
    var dayOfMonth by remember { mutableIntStateOf(10) }
    var dayOfWeek by remember { mutableIntStateOf(1) } // 1=Mon

    val availableCategories = remember(type) { Category.getCategoriesForType(type) }
    var selectedCategoryId by remember(type) {
        mutableStateOf(availableCategories.firstOrNull()?.id ?: "")
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
            Text(
                text = if (isRu) "Автодобавление" else "Schedule Auto-Add",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Income / Expense
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
                        .background(if (type == TransactionType.INCOME) IncomeGreen else Color.Transparent)
                        .clickable { type = TransactionType.INCOME }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isRu) "Доход (зарплата)" else "Income (Salary)",
                        color = if (type == TransactionType.INCOME) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(CircleShape)
                        .background(if (type == TransactionType.EXPENSE) ExpenseRed else Color.Transparent)
                        .clickable { type = TransactionType.EXPENSE }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isRu) "Расход (платеж)" else "Expense (Payment)",
                        color = if (type == TransactionType.EXPENSE) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Amount & Currency
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
                    textStyle = MaterialTheme.typography.displayLarge.copy(fontSize = 26.sp),
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

            // Title
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(if (isRu) "Название (например, Зарплата)" else "Title (e.g. Salary)", fontSize = 14.sp)
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

            Spacer(modifier = Modifier.height(16.dp))

            // Frequency Selection
            Text(
                text = if (isRu) "Периодичность" else "Frequency",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ExpressivePill(
                    text = if (isRu) "Раз в месяц" else "Monthly",
                    isSelected = frequency == RecurrenceFrequency.MONTHLY,
                    onClick = { frequency = RecurrenceFrequency.MONTHLY }
                )
                ExpressivePill(
                    text = if (isRu) "Раз в 2 недели" else "Biweekly",
                    isSelected = frequency == RecurrenceFrequency.BIWEEKLY,
                    onClick = { frequency = RecurrenceFrequency.BIWEEKLY }
                )
                ExpressivePill(
                    text = if (isRu) "Еженедельно" else "Weekly",
                    isSelected = frequency == RecurrenceFrequency.WEEKLY,
                    onClick = { frequency = RecurrenceFrequency.WEEKLY }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Day Picker depending on frequency
            if (frequency == RecurrenceFrequency.MONTHLY) {
                Text(
                    text = if (isRu) "Число месяца: $dayOfMonth" else "Day of month: $dayOfMonth",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Quick day selectors: 1, 5, 10, 15, 20, 25, 30
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(1, 5, 10, 15, 20, 25, 30).forEach { day ->
                        ExpressivePill(
                            text = day.toString(),
                            isSelected = dayOfMonth == day,
                            onClick = { dayOfMonth = day }
                        )
                    }
                }
            } else if (frequency == RecurrenceFrequency.WEEKLY) {
                Text(
                    text = if (isRu) "День недели" else "Day of week",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                val days = if (isRu) listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
                else listOf("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su")

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    days.forEachIndexed { idx, label ->
                        val dow = idx + 1
                        ExpressivePill(
                            text = label,
                            isSelected = dayOfWeek == dow,
                            onClick = { dayOfWeek = dow }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Categories
            Text(
                text = if (isRu) "Категория" else "Category",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableCategories.forEach { category ->
                    val isCatSelected = category.id == selectedCategoryId
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isCatSelected) category.color.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            )
                            .clickable { selectedCategoryId = category.id }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = category.icon,
                            contentDescription = null,
                            tint = if (isCatSelected) category.color else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
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

            Spacer(modifier = Modifier.height(24.dp))

            val amountVal = amountText.toDoubleOrNull() ?: 0.0
            Button(
                onClick = {
                    if (amountVal > 0) {
                        onSave(title, amountVal, selectedCurrency, type, selectedCategoryId, frequency, dayOfMonth, dayOfWeek)
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
                    text = if (isRu) "Создать автодобавление" else "Create Auto-Rule",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
