package com.equity.budget.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.equity.budget.data.local.TransactionEntity
import com.equity.budget.data.model.AppLanguage
import com.equity.budget.ui.viewmodel.BudgetViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun MainScreen(
    viewModel: BudgetViewModel,
    initialOpenAddTransaction: Boolean = false
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()
    val summary by viewModel.budgetSummary.collectAsState()
    val recurringRules by viewModel.recurringRules.collectAsState()
    val savingsGoals by viewModel.savingsGoals.collectAsState()
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()
    val selectedTypeFilter by viewModel.selectedTypeFilter.collectAsState()
    val isFetchingRates by viewModel.isFetchingRates.collectAsState()
    val isCheckingUpdates by viewModel.isCheckingUpdates.collectAsState()
    val availableUpdate by viewModel.availableUpdate.collectAsState()

    var currentTab by remember { mutableIntStateOf(0) }
    var showAddTransactionSheet by remember { mutableStateOf(initialOpenAddTransaction) }
    var initialAddSheetTab by remember { mutableIntStateOf(0) }
    var editingTransaction by remember { mutableStateOf<TransactionEntity?>(null) }
    var activeSheetCategoryId by remember { mutableStateOf("groceries") }

    val recentTitles by viewModel.getRecentTitlesForCategory(activeSheetCategoryId)
        .collectAsState(initial = emptyList())

    val isRu = settings.language == AppLanguage.RU

    LaunchedEffect(Unit) {
        viewModel.toastEvent.collectLatest { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            val navColors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            )

            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp
            ) {
                val navItems = listOf(
                    Triple(0, if (isRu) "Баланс" else "Balance", Icons.Default.AccountBalanceWallet),
                    Triple(1, if (isRu) "Графики" else "Charts", Icons.Default.BarChart),
                    Triple(2, if (isRu) "Авто" else "Auto-Add", Icons.Default.EventRepeat),
                    Triple(3, if (isRu) "Настройки" else "Settings", Icons.Default.Settings)
                )

                navItems.forEach { (index, title, icon) ->
                    val isSelected = currentTab == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = index },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = title,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = { Text(text = title, fontSize = 11.sp) },
                        colors = navColors
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                0 -> HomeScreen(
                    summary = summary,
                    settings = settings,
                    selectedPeriod = selectedPeriod,
                    selectedTypeFilter = selectedTypeFilter,
                    savingsGoals = savingsGoals,
                    onPeriodSelected = { viewModel.setPeriod(it) },
                    onTypeFilterSelected = { viewModel.setTypeFilter(it) },
                    onAddTransaction = {
                        activeSheetCategoryId = "groceries"
                        initialAddSheetTab = 0
                        showAddTransactionSheet = true
                    },
                    onAddNewGoal = {
                        initialAddSheetTab = 1
                        showAddTransactionSheet = true
                    },
                    onDepositToGoal = { id, amount ->
                        viewModel.depositToSavingsGoal(id, amount)
                    },
                    onDeleteGoal = { id ->
                        viewModel.deleteSavingsGoal(id)
                    },
                    onEditTransaction = { tx ->
                        activeSheetCategoryId = tx.categoryId
                        editingTransaction = tx
                    },
                    onUpdateSavingsGoal = { amt, days ->
                        viewModel.updateSavingsGoal(amt, days)
                    }
                )
                1 -> AnalyticsScreen(
                    summary = summary,
                    settings = settings,
                    selectedPeriod = selectedPeriod,
                    onPeriodSelected = { viewModel.setPeriod(it) }
                )
                2 -> RecurringScreen(
                    rules = recurringRules,
                    settings = settings,
                    onAddRule = { title, amt, cur, type, catId, freq, dom, dow ->
                        viewModel.addRecurringRule(
                            title = title,
                            amount = amt,
                            currency = cur,
                            type = type,
                            categoryId = catId,
                            frequency = freq,
                            dayOfMonth = dom,
                            dayOfWeek = dow,
                            startDate = System.currentTimeMillis()
                        )
                    },
                    onToggleRule = { id, active -> viewModel.toggleRecurringRule(id, active) },
                    onDeleteRule = { id -> viewModel.deleteRecurringRule(id) },
                    onCheckDueNow = { viewModel.checkAndApplyAutoAdditions(silent = false) }
                )
                3 -> SettingsScreen(
                    settings = settings,
                    isFetchingRates = isFetchingRates,
                    isCheckingUpdates = isCheckingUpdates,
                    availableUpdate = availableUpdate,
                    onLanguageChange = { viewModel.updateLanguage(it) },
                    onCurrencyChange = { viewModel.updatePrimaryCurrency(it) },
                    onAutoConvertChange = { viewModel.updateAutoConvert(it) },
                    onAutoFetchRatesChange = { viewModel.updateAutoFetchRates(it) },
                    onRefreshRatesNow = { viewModel.fetchRealtimeRates(silent = false) },
                    onThemeModeChange = { viewModel.updateThemeMode(it) },
                    onDynamicColorsChange = { viewModel.updateDynamicColors(it) },
                    onRatesChange = { usd, eur -> viewModel.updateExchangeRates(usd, eur) },
                    onCheckUpdates = { version -> viewModel.checkForUpdates(version) },
                    onDismissUpdateDialog = { viewModel.dismissUpdateDialog() }
                )
            }
        }
    }

    if (showAddTransactionSheet) {
        AddEditTransactionSheet(
            settings = settings,
            recentCategoryTitles = recentTitles,
            existingTransaction = null,
            initialTab = initialAddSheetTab,
            onCategoryChanged = { activeSheetCategoryId = it },
            onDismiss = { showAddTransactionSheet = false },
            onSave = { title, amount, cur, type, catId ->
                viewModel.addTransaction(
                    title = title,
                    amount = amount,
                    currency = cur,
                    type = type,
                    categoryId = catId
                )
            },
            onSaveGoal = { title, target, current, months, cur ->
                viewModel.addSavingsGoal(title, target, current, months, cur)
            },
            onParseUri = { uri, onResult, onError ->
                viewModel.parseStatementFromUri(uri, onResult, onError)
            },
            onParseText = { text, onResult, onError ->
                viewModel.parseStatementFromText(text, onResult, onError)
            },
            onImport = { list ->
                viewModel.importTransactions(list)
            }
        )
    }

    editingTransaction?.let { tx ->
        AddEditTransactionSheet(
            settings = settings,
            recentCategoryTitles = recentTitles,
            existingTransaction = tx,
            onCategoryChanged = { activeSheetCategoryId = it },
            onDismiss = { editingTransaction = null },
            onSave = { title, amount, cur, type, catId ->
                viewModel.updateTransaction(
                    tx.copy(
                        title = title,
                        amount = amount,
                        currency = cur.code,
                        type = type.id,
                        categoryId = catId
                    )
                )
                editingTransaction = null
            },
            onDelete = { id ->
                viewModel.deleteTransaction(id)
                editingTransaction = null
            }
        )
    }
}
