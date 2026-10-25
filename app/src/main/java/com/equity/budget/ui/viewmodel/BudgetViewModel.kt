package com.equity.budget.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.equity.budget.data.local.AppDatabase
import com.equity.budget.data.local.RecurringRuleEntity
import com.equity.budget.data.local.TransactionEntity
import com.equity.budget.data.model.AppLanguage
import com.equity.budget.data.model.Category
import com.equity.budget.data.model.Currency
import com.equity.budget.data.model.RecurrenceFrequency
import com.equity.budget.data.model.ThemeMode
import com.equity.budget.data.model.TimePeriod
import com.equity.budget.data.model.TransactionType
import com.equity.budget.data.preferences.AppSettings
import com.equity.budget.data.preferences.PreferencesManager
import com.equity.budget.domain.CurrencyConverter
import com.equity.budget.domain.ExchangeRateFetcher
import com.equity.budget.domain.RecurringScheduler
import com.equity.budget.ui.components.CumulativePoint
import com.equity.budget.ui.components.WeekdaySpend
import com.equity.budget.ui.widget.BudgetWidgetProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.max

data class TrendPoint(
    val label: String,
    val income: Double,
    val expense: Double,
    val net: Double
)

data class CategoryShare(
    val category: Category,
    val amount: Double,
    val percentage: Float
)

data class SavingsGoalInfo(
    val goalAmount: Double = 0.0,
    val targetDays: Int = 30,
    val spendableTotal: Double = 0.0,
    val safeDailyLimit: Double = 0.0,
    val spentToday: Double = 0.0,
    val remainingToday: Double = 0.0,
    val hasGoal: Boolean = false
)

data class BudgetSummary(
    val totalBalance: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val savingsRate: Double = 0.0,
    val avgDailyExpense: Double = 0.0,
    val categoryBreakdown: List<CategoryShare> = emptyList(),
    val trendPoints: List<TrendPoint> = emptyList(),
    val cumulativePoints: List<CumulativePoint> = emptyList(),
    val weekdaySpends: List<WeekdaySpend> = emptyList(),
    val filteredTransactions: List<TransactionEntity> = emptyList(),
    val savingsGoal: SavingsGoalInfo = SavingsGoalInfo()
)

class BudgetViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val transactionDao = database.transactionDao()
    private val recurringRuleDao = database.recurringRuleDao()
    private val savingsGoalDao = database.savingsGoalDao()
    private val preferencesManager = PreferencesManager(application)
    private val scheduler = RecurringScheduler(database)

    val settings: StateFlow<AppSettings> = preferencesManager.settings

    val allTransactions: StateFlow<List<TransactionEntity>> = transactionDao.getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recurringRules: StateFlow<List<RecurringRuleEntity>> = recurringRuleDao.getAllRules()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savingsGoals: StateFlow<List<com.equity.budget.data.local.SavingsGoalEntity>> = savingsGoalDao.getAllGoals()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedPeriod = MutableStateFlow(TimePeriod.MONTH)
    val selectedPeriod: StateFlow<TimePeriod> = _selectedPeriod.asStateFlow()

    private val _selectedTypeFilter = MutableStateFlow<TransactionType?>(null)
    val selectedTypeFilter: StateFlow<TransactionType?> = _selectedTypeFilter.asStateFlow()

    private val _isFetchingRates = MutableStateFlow(false)
    val isFetchingRates: StateFlow<Boolean> = _isFetchingRates.asStateFlow()

    private val _isCheckingUpdates = MutableStateFlow(false)
    val isCheckingUpdates: StateFlow<Boolean> = _isCheckingUpdates.asStateFlow()

    private val _availableUpdate = MutableStateFlow<com.equity.budget.domain.UpdateInfo?>(null)
    val availableUpdate: StateFlow<com.equity.budget.domain.UpdateInfo?> = _availableUpdate.asStateFlow()

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    val budgetSummary: StateFlow<BudgetSummary> = combine(
        allTransactions,
        settings,
        selectedPeriod,
        selectedTypeFilter
    ) { txList, currentSettings, period, typeFilter ->
        calculateSummary(txList, currentSettings, period, typeFilter)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BudgetSummary())

    init {
        // Automatically check and add any due scheduled transactions on startup
        checkAndApplyAutoAdditions(silent = true)

        // If auto-fetch rates is enabled, fetch fresh rates on launch
        if (settings.value.autoFetchRates) {
            fetchRealtimeRates(silent = true)
        }
    }

    fun getRecentTitlesForCategory(categoryId: String): Flow<List<String>> {
        return transactionDao.getRecentTitlesForCategory(categoryId)
    }

    fun setPeriod(period: TimePeriod) {
        _selectedPeriod.value = period
    }

    fun setTypeFilter(type: TransactionType?) {
        _selectedTypeFilter.value = type
    }

    fun fetchRealtimeRates(silent: Boolean = false) {
        viewModelScope.launch {
            _isFetchingRates.value = true
            val res = ExchangeRateFetcher.fetchCurrentRates()
            _isFetchingRates.value = false

            res.onSuccess { rates ->
                preferencesManager.updateRates(rates.usdToRub, rates.eurToRub)
                if (!silent) {
                    val msg = if (settings.value.language == AppLanguage.RU) {
                        "Курсы обновлены: $1 = ${String.format(Locale.US, "%.2f", rates.usdToRub)}₽, €1 = ${String.format(Locale.US, "%.2f", rates.eurToRub)}₽ (${rates.source})"
                    } else {
                        "Rates updated: $1 = ${String.format(Locale.US, "%.2f", rates.usdToRub)}₽, €1 = ${String.format(Locale.US, "%.2f", rates.eurToRub)}₽"
                    }
                    _toastEvent.emit(msg)
                }
            }.onFailure { err ->
                if (!silent) {
                    val msg = if (settings.value.language == AppLanguage.RU) {
                        "Не удалось обновить курс: ${err.message ?: "ошибка сети"}"
                    } else {
                        "Failed to update rates: ${err.message ?: "network error"}"
                    }
                    _toastEvent.emit(msg)
                }
            }
        }
    }

    fun checkForUpdates(currentVersion: String) {
        viewModelScope.launch {
            _isCheckingUpdates.value = true
            val res = com.equity.budget.domain.UpdateChecker.checkForUpdates(currentVersion)
            _isCheckingUpdates.value = false

            res.onSuccess { info ->
                if (info.hasUpdate) {
                    _availableUpdate.value = info
                } else {
                    val msg = if (settings.value.language == AppLanguage.RU) {
                        "У вас установлена последняя версия (v$currentVersion)"
                    } else {
                        "You have the latest version (v$currentVersion)"
                    }
                    _toastEvent.emit(msg)
                }
            }.onFailure { err ->
                val msg = if (settings.value.language == AppLanguage.RU) {
                    "Не удалось проверить обновления: ${err.message ?: "ошибка сети"}"
                } else {
                    "Failed to check updates: ${err.message ?: "network error"}"
                }
                _toastEvent.emit(msg)
            }
        }
    }

    fun dismissUpdateDialog() {
        _availableUpdate.value = null
    }

    fun checkAndApplyAutoAdditions(silent: Boolean = false) {
        viewModelScope.launch {
            val count = scheduler.processDueRules()
            if (count > 0) {
                BudgetWidgetProvider.updateAllWidgets(getApplication())
            }
            if (!silent) {
                val msg = if (count > 0) {
                    if (settings.value.language == AppLanguage.RU) {
                        "Добавлено операций: $count"
                    } else {
                        "Auto-added $count transactions"
                    }
                } else {
                    if (settings.value.language == AppLanguage.RU) {
                        "Все автоплатежи актуальны"
                    } else {
                        "All auto-schedules are up to date"
                    }
                }
                _toastEvent.emit(msg)
            }
        }
    }

    fun addTransaction(
        title: String,
        amount: Double,
        currency: Currency,
        type: TransactionType,
        categoryId: String,
        timestamp: Long = System.currentTimeMillis(),
        note: String = ""
    ) {
        viewModelScope.launch {
            val entity = TransactionEntity(
                title = title.ifBlank { Category.getById(categoryId).localizedName(settings.value.language) },
                amount = amount,
                currency = currency.code,
                type = type.id,
                categoryId = categoryId,
                timestamp = timestamp,
                note = note
            )
            transactionDao.insertTransaction(entity)
            BudgetWidgetProvider.updateAllWidgets(getApplication())
        }
    }

    fun importTransactions(
        transactions: List<TransactionEntity>,
        onComplete: (Int) -> Unit = {}
    ) {
        viewModelScope.launch {
            if (transactions.isNotEmpty()) {
                transactionDao.insertTransactions(transactions)
                BudgetWidgetProvider.updateAllWidgets(getApplication())
                val isRu = settings.value.language == AppLanguage.RU
                val msg = if (isRu) {
                    "Импортировано операций: ${transactions.size}"
                } else {
                    "Successfully imported ${transactions.size} transactions"
                }
                _toastEvent.emit(msg)
                onComplete(transactions.size)
            }
        }
    }

    fun parseStatementFromUri(
        uri: android.net.Uri,
        onResult: (com.equity.budget.domain.StatementParseResult) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val context = getApplication<Application>()
                val contentResolver = context.contentResolver
                val mimeType = contentResolver.getType(uri) ?: ""
                val fileName = uri.lastPathSegment ?: ""
                val isPdf = mimeType.contains("pdf", ignoreCase = true) || fileName.endsWith(".pdf", ignoreCase = true)
                val isCsv = mimeType.contains("csv", ignoreCase = true) || fileName.endsWith(".csv", ignoreCase = true)

                contentResolver.openInputStream(uri)?.use { stream ->
                    val result = when {
                        isCsv -> com.equity.budget.domain.BankStatementParser.parseCsv(stream, allTransactions.value)
                        isPdf -> com.equity.budget.domain.BankStatementParser.parsePdf(stream, allTransactions.value)
                        else -> {
                            try {
                                com.equity.budget.domain.BankStatementParser.parsePdf(stream, allTransactions.value)
                            } catch (_: Exception) {
                                contentResolver.openInputStream(uri)?.use { s2 ->
                                    val text = s2.bufferedReader().readText()
                                    com.equity.budget.domain.BankStatementParser.parseText(text, allTransactions.value)
                                } ?: throw IllegalArgumentException("Не удалось прочитать файл")
                            }
                        }
                    }
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        onResult(result)
                    }
                } ?: run {
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        onError("Не удалось открыть файл")
                    }
                }
            } catch (e: Exception) {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    onError(e.message ?: "Ошибка парсинга файла выписки")
                }
            }
        }
    }

    fun parseStatementFromText(
        text: String,
        onResult: (com.equity.budget.domain.StatementParseResult) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val result = com.equity.budget.domain.BankStatementParser.parseText(text, allTransactions.value)
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    onResult(result)
                }
            } catch (e: Exception) {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    onError(e.message ?: "Ошибка парсинга текста")
                }
            }
        }
    }

    fun updateTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            transactionDao.updateTransaction(transaction)
            BudgetWidgetProvider.updateAllWidgets(getApplication())
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            transactionDao.deleteTransactionById(id)
            BudgetWidgetProvider.updateAllWidgets(getApplication())
        }
    }

    fun addRecurringRule(
        title: String,
        amount: Double,
        currency: Currency,
        type: TransactionType,
        categoryId: String,
        frequency: RecurrenceFrequency,
        dayOfMonth: Int,
        dayOfWeek: Int,
        startDate: Long
    ) {
        viewModelScope.launch {
            val rule = RecurringRuleEntity(
                title = title.ifBlank { Category.getById(categoryId).localizedName(settings.value.language) },
                amount = amount,
                currency = currency.code,
                type = type.id,
                categoryId = categoryId,
                frequency = frequency.id,
                dayOfMonth = dayOfMonth,
                dayOfWeek = dayOfWeek,
                startDate = startDate,
                isActive = true
            )
            recurringRuleDao.insertRule(rule)
            scheduler.processDueRules()
            BudgetWidgetProvider.updateAllWidgets(getApplication())
        }
    }

    fun updateRecurringRule(rule: RecurringRuleEntity) {
        viewModelScope.launch {
            recurringRuleDao.updateRule(rule)
            scheduler.processDueRules()
            BudgetWidgetProvider.updateAllWidgets(getApplication())
        }
    }

    fun toggleRecurringRule(id: Long, isActive: Boolean) {
        viewModelScope.launch {
            recurringRuleDao.setRuleActive(id, isActive)
            if (isActive) {
                scheduler.processDueRules()
                BudgetWidgetProvider.updateAllWidgets(getApplication())
            }
        }
    }

    fun deleteRecurringRule(id: Long) {
        viewModelScope.launch {
            recurringRuleDao.deleteRuleById(id)
        }
    }

    fun updateLanguage(lang: AppLanguage) {
        preferencesManager.updateLanguage(lang)
    }

    fun updatePrimaryCurrency(currency: Currency) {
        preferencesManager.updatePrimaryCurrency(currency)
        BudgetWidgetProvider.updateAllWidgets(getApplication())
    }

    fun updateAutoConvert(autoConvert: Boolean) {
        preferencesManager.updateAutoConvert(autoConvert)
        BudgetWidgetProvider.updateAllWidgets(getApplication())
    }

    fun updateAutoFetchRates(autoFetch: Boolean) {
        preferencesManager.updateAutoFetchRates(autoFetch)
        if (autoFetch) {
            fetchRealtimeRates()
        }
    }

    fun updateThemeMode(mode: ThemeMode) {
        preferencesManager.updateThemeMode(mode)
    }

    fun updateDynamicColors(dynamic: Boolean) {
        preferencesManager.updateDynamicColors(dynamic)
    }

    fun updateExchangeRates(usdToRub: Double, eurToRub: Double) {
        preferencesManager.updateRates(usdToRub, eurToRub)
        BudgetWidgetProvider.updateAllWidgets(getApplication())
    }

    fun updateSavingsGoal(amount: Double, days: Int) {
        preferencesManager.updateSavingsGoal(amount, days)
    }

    fun addSavingsGoal(
        title: String,
        targetAmount: Double,
        currentAmount: Double,
        targetMonths: Int,
        currency: Currency = settings.value.primaryCurrency
    ) {
        viewModelScope.launch {
            val goal = com.equity.budget.data.local.SavingsGoalEntity(
                title = title.ifBlank { if (settings.value.language == AppLanguage.RU) "Цель накопления" else "Savings Goal" },
                targetAmount = targetAmount,
                currentAmount = currentAmount,
                currency = currency.code,
                targetMonths = targetMonths.coerceAtLeast(1)
            )
            savingsGoalDao.insertGoal(goal)
            val isRu = settings.value.language == AppLanguage.RU
            val msg = if (isRu) "Цель «${goal.title}» создана!" else "Goal «${goal.title}» created!"
            _toastEvent.emit(msg)
        }
    }

    fun depositToSavingsGoal(goalId: Long, depositAmount: Double) {
        viewModelScope.launch {
            if (depositAmount > 0) {
                savingsGoalDao.addDeposit(goalId, depositAmount)
                val isRu = settings.value.language == AppLanguage.RU
                val msg = if (isRu) "Внесено: +${CurrencyConverter.format(depositAmount, settings.value.primaryCurrency)}"
                else "Deposited: +${CurrencyConverter.format(depositAmount, settings.value.primaryCurrency)}"
                _toastEvent.emit(msg)
            }
        }
    }

    fun updateSavingsGoalEntity(goal: com.equity.budget.data.local.SavingsGoalEntity) {
        viewModelScope.launch {
            savingsGoalDao.updateGoal(goal)
        }
    }

    fun deleteSavingsGoal(goalId: Long) {
        viewModelScope.launch {
            savingsGoalDao.deleteGoalById(goalId)
            val isRu = settings.value.language == AppLanguage.RU
            _toastEvent.emit(if (isRu) "Цель удалена" else "Goal deleted")
        }
    }

    fun getNormalizedAmount(amount: Double, currencyCode: String, currentSettings: AppSettings): Double {
        if (!currentSettings.autoConvert) return amount
        val fromCurrency = Currency.fromCode(currencyCode)
        return CurrencyConverter.convert(
            amount = amount,
            from = fromCurrency,
            to = currentSettings.primaryCurrency,
            usdToRub = currentSettings.usdToRub,
            eurToRub = currentSettings.eurToRub
        )
    }

    private fun calculateSummary(
        txList: List<TransactionEntity>,
        currentSettings: AppSettings,
        period: TimePeriod,
        typeFilter: TransactionType?
    ): BudgetSummary {
        val (periodStart, periodEnd) = getPeriodRange(period)
        val inPeriodList = txList.filter { it.timestamp in periodStart..periodEnd }

        var incomeTotal = 0.0
        var expenseTotal = 0.0

        for (tx in inPeriodList) {
            val normalized = getNormalizedAmount(tx.amount, tx.currency, currentSettings)
            if (tx.type == TransactionType.INCOME.id) {
                incomeTotal += normalized
            } else {
                expenseTotal += normalized
            }
        }

        // All time balance
        var totalBalance = 0.0
        for (tx in txList) {
            val normalized = getNormalizedAmount(tx.amount, tx.currency, currentSettings)
            if (tx.type == TransactionType.INCOME.id) {
                totalBalance += normalized
            } else {
                totalBalance -= normalized
            }
        }

        val filteredList = inPeriodList.filter { tx ->
            if (typeFilter == null) true else tx.type == typeFilter.id
        }

        val savingsRate = if (incomeTotal > 0) {
            ((incomeTotal - expenseTotal) / incomeTotal * 100.0).coerceIn(-100.0, 100.0)
        } else {
            if (expenseTotal > 0) -100.0 else 0.0
        }

        val daysInPeriod = ((periodEnd - periodStart) / 86_400_000L).coerceAtLeast(1L)
        val avgDailyExpense = expenseTotal / daysInPeriod

        // Category breakdown
        val expenseTransactions = inPeriodList.filter { it.type == TransactionType.EXPENSE.id }
        val categoryTotals = mutableMapOf<String, Double>()
        for (tx in expenseTransactions) {
            val norm = getNormalizedAmount(tx.amount, tx.currency, currentSettings)
            categoryTotals[tx.categoryId] = (categoryTotals[tx.categoryId] ?: 0.0) + norm
        }

        val categoryBreakdown = categoryTotals.map { (catId, sum) ->
            val percentage = if (expenseTotal > 0) ((sum / expenseTotal) * 100f).toFloat() else 0f
            CategoryShare(Category.getById(catId), sum, percentage)
        }.sortedByDescending { it.amount }

        // Trend points
        val trendPoints = generateTrendPoints(inPeriodList, currentSettings, period)

        // Cumulative points
        val cumulativePoints = generateCumulativePoints(inPeriodList, currentSettings)

        // Weekday spending
        val weekdaySpends = generateWeekdaySpends(expenseTransactions, currentSettings)

        // Savings goal & Safe Daily Spend calculation
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val spentToday = txList.filter { it.timestamp >= todayStart && it.type == TransactionType.EXPENSE.id }
            .sumOf { getNormalizedAmount(it.amount, it.currency, currentSettings) }

        val goalAmount = currentSettings.savingsGoalAmount
        val targetDays = max(1, currentSettings.savingsTargetDays)
        val spendableTotal = max(0.0, totalBalance - goalAmount)
        val safeDailyLimit = spendableTotal / targetDays
        val remainingToday = safeDailyLimit - spentToday

        val savingsGoalInfo = SavingsGoalInfo(
            goalAmount = goalAmount,
            targetDays = targetDays,
            spendableTotal = spendableTotal,
            safeDailyLimit = safeDailyLimit,
            spentToday = spentToday,
            remainingToday = remainingToday,
            hasGoal = goalAmount > 0
        )

        return BudgetSummary(
            totalBalance = totalBalance,
            totalIncome = incomeTotal,
            totalExpense = expenseTotal,
            savingsRate = savingsRate,
            avgDailyExpense = avgDailyExpense,
            categoryBreakdown = categoryBreakdown,
            trendPoints = trendPoints,
            cumulativePoints = cumulativePoints,
            weekdaySpends = weekdaySpends,
            filteredTransactions = filteredList,
            savingsGoal = savingsGoalInfo
        )
    }

    private fun generateCumulativePoints(
        transactions: List<TransactionEntity>,
        currentSettings: AppSettings
    ): List<CumulativePoint> {
        if (transactions.isEmpty()) return emptyList()

        val sorted = transactions.sortedBy { it.timestamp }
        val points = mutableListOf<CumulativePoint>()
        var runningBal = 0.0

        val sdf = SimpleDateFormat("dd.MM", Locale.getDefault())

        sorted.forEach { tx ->
            val norm = getNormalizedAmount(tx.amount, tx.currency, currentSettings)
            if (tx.type == TransactionType.INCOME.id) {
                runningBal += norm
            } else {
                runningBal -= norm
            }
            points.add(CumulativePoint(sdf.format(Date(tx.timestamp)), runningBal))
        }

        return points
    }

    private fun generateWeekdaySpends(
        expenseTransactions: List<TransactionEntity>,
        currentSettings: AppSettings
    ): List<WeekdaySpend> {
        val isRu = currentSettings.language == AppLanguage.RU
        val daysNames = if (isRu) listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
        else listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

        val dayTotals = DoubleArray(7) { 0.0 }
        val cal = Calendar.getInstance()

        for (tx in expenseTransactions) {
            cal.timeInMillis = tx.timestamp
            // Calendar.DAY_OF_WEEK: Sunday=1, Monday=2 ... Saturday=7
            val dow = cal.get(Calendar.DAY_OF_WEEK)
            val index = when (dow) {
                Calendar.MONDAY -> 0
                Calendar.TUESDAY -> 1
                Calendar.WEDNESDAY -> 2
                Calendar.THURSDAY -> 3
                Calendar.FRIDAY -> 4
                Calendar.SATURDAY -> 5
                else -> 6 // SUNDAY
            }
            dayTotals[index] += getNormalizedAmount(tx.amount, tx.currency, currentSettings)
        }

        return daysNames.mapIndexed { idx, name ->
            WeekdaySpend(name, dayTotals[idx])
        }
    }

    private fun generateTrendPoints(
        transactions: List<TransactionEntity>,
        currentSettings: AppSettings,
        period: TimePeriod
    ): List<TrendPoint> {
        val isRu = currentSettings.language == AppLanguage.RU
        val points = mutableListOf<TrendPoint>()

        when (period) {
            TimePeriod.TODAY -> {
                val cal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                for (i in 0 until 6) {
                    val start = cal.timeInMillis
                    cal.add(Calendar.HOUR_OF_DAY, 4)
                    val end = cal.timeInMillis
                    val label = String.format(Locale.getDefault(), "%02d:00", i * 4)

                    val inInterval = transactions.filter { it.timestamp in start until end }
                    val inc = inInterval.filter { it.type == TransactionType.INCOME.id }
                        .sumOf { getNormalizedAmount(it.amount, it.currency, currentSettings) }
                    val exp = inInterval.filter { it.type == TransactionType.EXPENSE.id }
                        .sumOf { getNormalizedAmount(it.amount, it.currency, currentSettings) }
                    points.add(TrendPoint(label, inc, exp, inc - exp))
                }
            }

            TimePeriod.WEEK -> {
                val cal = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, -6)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val sdf = SimpleDateFormat("EEE", if (isRu) Locale("ru") else Locale.ENGLISH)
                for (i in 0 until 7) {
                    val start = cal.timeInMillis
                    val label = sdf.format(cal.time).uppercase()
                    cal.add(Calendar.DAY_OF_YEAR, 1)
                    val end = cal.timeInMillis

                    val inDay = transactions.filter { it.timestamp in start until end }
                    val inc = inDay.filter { it.type == TransactionType.INCOME.id }
                        .sumOf { getNormalizedAmount(it.amount, it.currency, currentSettings) }
                    val exp = inDay.filter { it.type == TransactionType.EXPENSE.id }
                        .sumOf { getNormalizedAmount(it.amount, it.currency, currentSettings) }
                    points.add(TrendPoint(label, inc, exp, inc - exp))
                }
            }

            TimePeriod.MONTH -> {
                val cal = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                val intervals = listOf(
                    1 to 7,
                    8 to 15,
                    16 to 22,
                    23 to maxDay
                )
                for ((idx, interval) in intervals.withIndex()) {
                    cal.set(Calendar.DAY_OF_MONTH, interval.first)
                    val start = cal.timeInMillis
                    cal.set(Calendar.DAY_OF_MONTH, interval.second)
                    cal.set(Calendar.HOUR_OF_DAY, 23)
                    cal.set(Calendar.MINUTE, 59)
                    val end = cal.timeInMillis

                    val label = if (isRu) "${idx + 1}-я нед" else "Wk ${idx + 1}"
                    val inInterval = transactions.filter { it.timestamp in start..end }
                    val inc = inInterval.filter { it.type == TransactionType.INCOME.id }
                        .sumOf { getNormalizedAmount(it.amount, it.currency, currentSettings) }
                    val exp = inInterval.filter { it.type == TransactionType.EXPENSE.id }
                        .sumOf { getNormalizedAmount(it.amount, it.currency, currentSettings) }
                    points.add(TrendPoint(label, inc, exp, inc - exp))
                }
            }

            TimePeriod.YEAR, TimePeriod.ALL -> {
                val cal = Calendar.getInstance().apply {
                    set(Calendar.MONTH, 0)
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val sdf = SimpleDateFormat("MMM", if (isRu) Locale("ru") else Locale.ENGLISH)
                for (i in 0 until 12) {
                    val start = cal.timeInMillis
                    val label = sdf.format(cal.time)
                    cal.add(Calendar.MONTH, 1)
                    val end = cal.timeInMillis

                    val inMonth = transactions.filter { it.timestamp in start until end }
                    val inc = inMonth.filter { it.type == TransactionType.INCOME.id }
                        .sumOf { getNormalizedAmount(it.amount, it.currency, currentSettings) }
                    val exp = inMonth.filter { it.type == TransactionType.EXPENSE.id }
                        .sumOf { getNormalizedAmount(it.amount, it.currency, currentSettings) }
                    points.add(TrendPoint(label, inc, exp, inc - exp))
                }
            }
        }

        return points
    }

    private fun getPeriodRange(period: TimePeriod): Pair<Long, Long> {
        val start = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val end = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }

        when (period) {
            TimePeriod.TODAY -> {}
            TimePeriod.WEEK -> {
                // Rolling 7 days: from 6 days ago (00:00:00) to today (23:59:59.999)
                start.add(Calendar.DAY_OF_YEAR, -6)
            }
            TimePeriod.MONTH -> {
                start.set(Calendar.DAY_OF_MONTH, 1)
                end.set(Calendar.DAY_OF_MONTH, start.getActualMaximum(Calendar.DAY_OF_MONTH))
            }
            TimePeriod.YEAR -> {
                start.set(Calendar.DAY_OF_YEAR, 1)
                end.set(Calendar.DAY_OF_YEAR, start.getActualMaximum(Calendar.DAY_OF_YEAR))
            }
            TimePeriod.ALL -> {
                start.timeInMillis = 0L
                end.timeInMillis = Long.MAX_VALUE
            }
        }

        return Pair(start.timeInMillis, end.timeInMillis)
    }
}
