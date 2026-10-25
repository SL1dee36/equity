package com.equity.budget.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.equity.budget.data.local.TransactionEntity
import com.equity.budget.data.model.AppLanguage
import com.equity.budget.data.model.Category
import com.equity.budget.data.model.TransactionType
import com.equity.budget.data.preferences.AppSettings
import com.equity.budget.domain.CurrencyConverter
import com.equity.budget.domain.ParsedTransaction
import com.equity.budget.domain.StatementParseResult
import com.equity.budget.ui.components.ExpressivePill
import com.equity.budget.ui.components.FlatCard
import com.equity.budget.ui.theme.ExpenseRed
import com.equity.budget.ui.theme.IncomeGreen
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StatementImportPanel(
    settings: AppSettings,
    onParseUri: (Uri, onResult: (StatementParseResult) -> Unit, onError: (String) -> Unit) -> Unit,
    onParseText: (String, onResult: (StatementParseResult) -> Unit, onError: (String) -> Unit) -> Unit,
    onImport: (List<TransactionEntity>) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isRu = settings.language == AppLanguage.RU

    var isParsing by remember { mutableStateOf(false) }
    var parseResult by remember { mutableStateOf<StatementParseResult?>(null) }
    var transactions by remember { mutableStateOf<List<ParsedTransaction>>(emptyList()) }
    var selectedTypeFilter by remember { mutableStateOf<TransactionType?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showPasteDialog by remember { mutableStateOf(false) }
    var editingCategoryTxIndex by remember { mutableStateOf<Int?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            isParsing = true
            errorMessage = null
            onParseUri(uri, { result ->
                isParsing = false
                parseResult = result
                transactions = result.transactions
                if (result.transactions.isEmpty()) {
                    errorMessage = if (isRu) {
                        "В файле не найдено строк с операциями. Убедитесь, что это выписка Т-Банка или поддерживаемый формат."
                    } else {
                        "No transaction records found in the file."
                    }
                }
            }, { err ->
                isParsing = false
                errorMessage = err
            })
        }
    }

    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val dayHeaderFormat = remember(settings.language) {
        SimpleDateFormat("d MMMM yyyy, EEEE", if (isRu) Locale("ru") else Locale.ENGLISH)
    }
    val periodFormat = remember(settings.language) {
        SimpleDateFormat("dd.MM.yyyy", if (isRu) Locale("ru") else Locale.ENGLISH)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        // Initial Upload State: No file parsed yet
        if (parseResult == null) {
            FlatCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.UploadFile,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (isRu) "Импорт выписки банка" else "Bank Statement Import",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (isRu) {
                            "Загрузите официальную выписку Т-Банка (.pdf) или .csv файл. Операции будут автоматически разложены по дням и категориям с сохранением точного времени."
                        } else {
                            "Upload your T-Bank statement (.pdf) or .csv file. Transactions will be placed onto their exact historical dates and categorized."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    if (isParsing) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(32.dp),
                                strokeWidth = 3.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = if (isRu) "Чтение и разбор выписки..." else "Parsing statement...",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Button(
                            onClick = {
                                filePickerLauncher.launch(
                                    arrayOf(
                                        "application/pdf",
                                        "text/comma-separated-values",
                                        "text/csv",
                                        "text/plain",
                                        "*/*"
                                    )
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isRu) "Выбрать PDF выписку" else "Choose PDF Statement",
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = { showPasteDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentPaste,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isRu) "Вставить текст выписки" else "Paste Statement Text",
                                fontSize = 13.sp
                            )
                        }
                    }

                    errorMessage?.let { err ->
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(ExpenseRed.copy(alpha = 0.1f))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = ExpenseRed,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = err,
                                fontSize = 11.sp,
                                color = ExpenseRed,
                                lineHeight = 15.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (isRu) {
                            "Где взять: в приложении Т-Банка → нажать на счёт карты → «Справки и выписки» → «Справка о движении средств» → скачать PDF."
                        } else {
                            "How to get: in T-Bank app → tap card account → Statements → Statement of funds movement → download PDF."
                        },
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 15.sp
                    )
                }
            }
        } else {
            // Parsed Result State: Show summary, date grouping, item selection and batch import
            val result = parseResult!!
            val selectedCount = transactions.count { it.isSelected }
            val selectedIncome = transactions.filter { it.isSelected && it.type == TransactionType.INCOME }.sumOf { it.amount }
            val selectedExpense = transactions.filter { it.isSelected && it.type == TransactionType.EXPENSE }.sumOf { it.amount }

            // Overview Summary Card
            FlatCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
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
                                text = if (isRu) "Выписка Т-Банка" else "T-Bank Statement",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (result.startDate != null && result.endDate != null) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${periodFormat.format(result.startDate)} — ${periodFormat.format(result.endDate)}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Close / choose other file button
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                .clickable {
                                    parseResult = null
                                    transactions = emptyList()
                                }
                                .padding(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Stats badges
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(IncomeGreen.copy(alpha = 0.12f))
                                .padding(horizontal = 10.dp, vertical = 7.dp)
                        ) {
                            Column {
                                Text(
                                    text = if (isRu) "Доходы" else "Income",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = CurrencyConverter.format(selectedIncome, settings.primaryCurrency),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = IncomeGreen
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(ExpenseRed.copy(alpha = 0.12f))
                                .padding(horizontal = 10.dp, vertical = 7.dp)
                        ) {
                            Column {
                                Text(
                                    text = if (isRu) "Расходы" else "Expense",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = CurrencyConverter.format(selectedExpense, settings.primaryCurrency),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ExpenseRed
                                )
                            }
                        }
                    }

                    // Duplicates info note
                    if (result.duplicateCount > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isRu) {
                                "ℹ️ ${result.duplicateCount} операций уже были в базе и пропущены по умолчанию, чтобы не задваивать баланс."
                            } else {
                                "ℹ️ ${result.duplicateCount} transactions were already in your budget and unchecked by default."
                            },
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Filter pills: All / Expenses / Income
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val expCount = transactions.count { it.type == TransactionType.EXPENSE }
                    val incCount = transactions.count { it.type == TransactionType.INCOME }

                    ExpressivePill(
                        text = if (isRu) "Все (${transactions.size})" else "All (${transactions.size})",
                        isSelected = selectedTypeFilter == null,
                        onClick = { selectedTypeFilter = null }
                    )
                    ExpressivePill(
                        text = if (isRu) "Расход ($expCount)" else "Expense ($expCount)",
                        isSelected = selectedTypeFilter == TransactionType.EXPENSE,
                        onClick = { selectedTypeFilter = TransactionType.EXPENSE }
                    )
                    ExpressivePill(
                        text = if (isRu) "Доход ($incCount)" else "Income ($incCount)",
                        isSelected = selectedTypeFilter == TransactionType.INCOME,
                        onClick = { selectedTypeFilter = TransactionType.INCOME }
                    )
                }

                // Quick toggle all
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = if (isRu) "Все" else "All",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable {
                                transactions = transactions.map { it.copy(isSelected = true) }
                            }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    )
                    Text(
                        text = if (isRu) "Снять" else "None",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable {
                                transactions = transactions.map { it.copy(isSelected = false) }
                            }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Filter transactions according to selected tab
            val displayTransactions = transactions.filter {
                selectedTypeFilter == null || it.type == selectedTypeFilter
            }

            // Group transactions by calendar day ("по датам должно правильно всё разложить")
            val groupedByDay = displayTransactions.groupBy { tx ->
                val cal = Calendar.getInstance().apply { timeInMillis = tx.timestamp }
                dayHeaderFormat.format(cal.time)
            }

            // Scrollable List of Grouped Transactions
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 180.dp, max = 360.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
            ) {
                LazyColumn(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    groupedByDay.forEach { (dayLabel, dayTxs) ->
                        // Day Header
                        item(key = "header_$dayLabel") {
                            val daySpend = dayTxs.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
                            val dayInc = dayTxs.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp, bottom = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = dayLabel.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = buildString {
                                        if (daySpend > 0) append("-${daySpend.toInt()} ₽ ")
                                        if (dayInc > 0) append("+${dayInc.toInt()} ₽")
                                    },
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }

                        // Day Transactions
                        itemsIndexed(
                            items = dayTxs,
                            key = { _, tx -> "${tx.timestamp}_${tx.amount}_${tx.title}_${tx.description}" }
                        ) { _, tx ->
                            val globalIndex = transactions.indexOf(tx)
                            val category = Category.getById(tx.categoryId)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        if (globalIndex >= 0) {
                                            transactions = transactions.toMutableList().also { list ->
                                                list[globalIndex] = tx.copy(isSelected = !tx.isSelected)
                                            }
                                        }
                                    }
                                    .padding(vertical = 5.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Checkbox
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(RoundedCornerShape(5.dp))
                                        .background(
                                            if (tx.isSelected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        )
                                        .border(
                                            1.dp,
                                            if (tx.isSelected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                            RoundedCornerShape(5.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (tx.isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // Time
                                Text(
                                    text = timeFormat.format(tx.timestamp),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    fontWeight = FontWeight.Medium
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                // Title and Category
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = tx.title,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (tx.isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                        maxLines = 1
                                    )

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(top = 2.dp)
                                    ) {
                                        // Category badge (clickable to change)
                                        Row(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(category.color.copy(alpha = 0.15f))
                                                .clickable { editingCategoryTxIndex = globalIndex }
                                                .padding(horizontal = 5.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = category.icon,
                                                contentDescription = null,
                                                tint = category.color,
                                                modifier = Modifier.size(10.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = category.localizedName(settings.language),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = category.color
                                            )
                                        }

                                        if (tx.isDuplicate) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (isRu) "Уже в базе" else "Duplicate",
                                                fontSize = 9.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // Amount
                                val isIncome = tx.type == TransactionType.INCOME
                                Text(
                                    text = "${if (isIncome) "+" else "-"}${CurrencyConverter.format(tx.amount, settings.primaryCurrency)}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (!tx.isSelected) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                    else if (isIncome) IncomeGreen else ExpenseRed
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        parseResult = null
                        transactions = emptyList()
                    },
                    modifier = Modifier.weight(0.4f),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )
                ) {
                    Text(if (isRu) "Заново" else "Reset", fontSize = 13.sp)
                }

                Button(
                    onClick = {
                        val selectedEntities = transactions
                            .filter { it.isSelected }
                            .map { it.toEntity() }

                        if (selectedEntities.isNotEmpty()) {
                            onImport(selectedEntities)
                            onDismiss()
                        }
                    },
                    enabled = selectedCount > 0,
                    modifier = Modifier.weight(0.6f),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Text(
                        text = if (isRu) "Импортировать ($selectedCount)" else "Import ($selectedCount)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }

    // Manual Text Paste Dialog
    if (showPasteDialog) {
        var pastedText by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showPasteDialog = false },
            title = {
                Text(
                    text = if (isRu) "Вставьте текст выписки" else "Paste Statement Text",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = if (isRu) {
                            "Скопируйте строки с операциями или CSV из интернет-банка и вставьте их сюда:"
                        } else {
                            "Copy operations or CSV lines from internet bank and paste here:"
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = pastedText,
                        onValueChange = { pastedText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        placeholder = { Text("25.10.2026 21:24 -348.48 ₽ Оплата в Перекресток...", fontSize = 12.sp) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pastedText.isNotBlank()) {
                            isParsing = true
                            errorMessage = null
                            showPasteDialog = false
                            onParseText(pastedText, { res ->
                                isParsing = false
                                parseResult = res
                                transactions = res.transactions
                            }, { err ->
                                isParsing = false
                                errorMessage = err
                            })
                        }
                    },
                    enabled = pastedText.isNotBlank(),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Text(if (isRu) "Распознать" else "Parse")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPasteDialog = false }) {
                    Text(if (isRu) "Отмена" else "Cancel")
                }
            }
        )
    }

    // Quick Category Changer Dialog for a transaction in the list
    editingCategoryTxIndex?.let { index ->
        val currentTx = transactions.getOrNull(index)
        if (currentTx != null) {
            val categoriesForType = Category.getCategoriesForType(currentTx.type)
            AlertDialog(
                onDismissRequest = { editingCategoryTxIndex = null },
                title = {
                    Text(
                        text = if (isRu) "Сменить категорию" else "Change Category",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = currentTx.title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            categoriesForType.forEach { cat ->
                                val isSelected = cat.id == currentTx.categoryId
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isSelected) cat.color.copy(alpha = 0.25f)
                                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                        )
                                        .clickable {
                                            transactions = transactions.toMutableList().also { list ->
                                                list[index] = currentTx.copy(categoryId = cat.id)
                                            }
                                            editingCategoryTxIndex = null
                                        }
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = cat.icon,
                                        contentDescription = null,
                                        tint = cat.color,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = cat.localizedName(settings.language),
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) cat.color else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { editingCategoryTxIndex = null }) {
                        Text(if (isRu) "Закрыть" else "Close")
                    }
                }
            )
        }
    }
}
