package com.equity.app.presentation.calculator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equity.app.domain.model.SavingsGoal
import com.equity.app.domain.usecase.AddFundsToGoalUseCase
import com.equity.app.domain.usecase.CalculateEquityFormulaUseCase
import com.equity.app.domain.usecase.GetActiveSavingsGoalUseCase
import com.equity.app.domain.usecase.GetFinancialSummaryUseCase
import com.equity.app.domain.usecase.SaveSavingsGoalUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

class SmartCalculatorViewModel(
    private val getActiveSavingsGoalUseCase: GetActiveSavingsGoalUseCase,
    private val saveSavingsGoalUseCase: SaveSavingsGoalUseCase,
    private val addFundsToGoalUseCase: AddFundsToGoalUseCase,
    private val calculateEquityFormulaUseCase: CalculateEquityFormulaUseCase,
    private val getFinancialSummaryUseCase: GetFinancialSummaryUseCase
) : ViewModel() {

    private val _targetAmount = MutableStateFlow("10000")
    private val _goalTitle = MutableStateFlow("Alpha Wealth Vault")
    private val _targetDays = MutableStateFlow(90)
    private val _currentSaved = MutableStateFlow("4250")
    private val _simulationDeposit = MutableStateFlow(1000.0)
    private val _useAutoFinancials = MutableStateFlow(true)
    private val _customIncome = MutableStateFlow("")
    private val _customExpense = MutableStateFlow("")
    private val _isAddFundsOpen = MutableStateFlow(false)
    private val _addFundsAmount = MutableStateFlow("")
    private val _isSavingSuccess = MutableStateFlow(false)
    private var initializedFromDb = false

    init {
        // Load initial values from DB if available
        viewModelScope.launch {
            val goal = getActiveSavingsGoalUseCase().firstOrNull()
            if (goal != null && !initializedFromDb) {
                _targetAmount.value = goal.targetAmount.toInt().toString()
                _goalTitle.value = goal.title
                _targetDays.value = goal.daysRemaining.toInt().coerceAtLeast(1)
                _currentSaved.value = goal.currentAmount.toInt().toString()
                initializedFromDb = true
            }
        }
    }

    val uiState: StateFlow<SmartCalculatorState> = combine(
        getActiveSavingsGoalUseCase(),
        _targetAmount,
        _goalTitle,
        _targetDays,
        _currentSaved,
        _simulationDeposit,
        _useAutoFinancials,
        _customIncome,
        _customExpense,
        _isAddFundsOpen,
        _addFundsAmount,
        _isSavingSuccess
    ) { params ->
        val activeGoal = params[0] as? SavingsGoal
        val targetAmountStr = params[1] as String
        val titleStr = params[2] as String
        val daysInt = params[3] as Int
        val currentSavedStr = params[4] as String
        val simulationDepositDbl = params[5] as Double
        val useAuto = params[6] as Boolean
        val customIncStr = params[7] as String
        val customExpStr = params[8] as String
        val isAddFunds = params[9] as Boolean
        val addFundsAmt = params[10] as String
        val isSaveSuccess = params[11] as Boolean

        val targetAmt = targetAmountStr.toDoubleOrNull() ?: 10000.0
        val currentAmt = currentSavedStr.toDoubleOrNull() ?: 0.0

        val cal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, daysInt)
        }

        val hypotheticalGoal = SavingsGoal(
            id = activeGoal?.id ?: 0L,
            title = titleStr.ifBlank { "My Savings Goal" },
            targetAmount = targetAmt,
            currentAmount = currentAmt,
            targetDate = cal.timeInMillis
        )

        val manualIncome = if (!useAuto) customIncStr.toDoubleOrNull() else null
        val manualExpense = if (!useAuto) customExpStr.toDoubleOrNull() else null

        val formulaResult = calculateEquityFormulaUseCase(
            overrideGoal = hypotheticalGoal,
            manualMonthlyIncome = manualIncome,
            manualEssentialExpense = manualExpense
        ).firstOrNull()

        SmartCalculatorState(
            activeGoal = activeGoal,
            targetAmountInput = targetAmountStr,
            goalTitleInput = titleStr,
            targetDaysInput = daysInt,
            currentSavedInput = currentSavedStr,
            simulationDepositAmount = simulationDepositDbl,
            useAutoFinancials = useAuto,
            customMonthlyIncome = customIncStr,
            customEssentialExpense = customExpStr,
            formulaResult = formulaResult,
            isAddFundsDialogOpen = isAddFunds,
            addFundsAmount = addFundsAmt,
            isSavingSuccess = isSaveSuccess,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SmartCalculatorState(isLoading = true)
    )

    fun onTargetAmountChange(newVal: String) {
        _targetAmount.update { newVal }
    }

    fun onGoalTitleChange(newVal: String) {
        _goalTitle.update { newVal }
    }

    fun onTargetDaysChange(days: Int) {
        _targetDays.update { days }
    }

    fun onCurrentSavedChange(newVal: String) {
        _currentSaved.update { newVal }
    }

    fun onSimulationDepositChange(amount: Double) {
        _simulationDeposit.update { amount }
    }

    fun onToggleAutoFinancials(useAuto: Boolean) {
        _useAutoFinancials.update { useAuto }
    }

    fun onCustomIncomeChange(newVal: String) {
        _customIncome.update { newVal }
    }

    fun onCustomExpenseChange(newVal: String) {
        _customExpense.update { newVal }
    }

    fun openAddFundsDialog() {
        _addFundsAmount.update { "" }
        _isAddFundsOpen.update { true }
    }

    fun closeAddFundsDialog() {
        _isAddFundsOpen.update { false }
    }

    fun onAddFundsAmountChange(newVal: String) {
        _addFundsAmount.update { newVal }
    }

    fun submitAddFunds() {
        val amount = _addFundsAmount.value.toDoubleOrNull() ?: return
        if (amount <= 0.0) return

        viewModelScope.launch {
            val goalId = uiState.value.activeGoal?.id
            if (goalId != null) {
                addFundsToGoalUseCase(goalId, amount)
                val current = _currentSaved.value.toDoubleOrNull() ?: 0.0
                _currentSaved.update { (current + amount).toString() }
            }
            closeAddFundsDialog()
        }
    }

    fun saveGoalToVault() {
        viewModelScope.launch {
            val targetAmt = _targetAmount.value.toDoubleOrNull() ?: 10000.0
            val currentAmt = _currentSaved.value.toDoubleOrNull() ?: 0.0
            val days = _targetDays.value

            val cal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, days)
            }

            val goalToSave = SavingsGoal(
                id = uiState.value.activeGoal?.id ?: 0L,
                title = _goalTitle.value.ifBlank { "Equity Target" },
                targetAmount = targetAmt,
                currentAmount = currentAmt,
                targetDate = cal.timeInMillis,
                isCompleted = false,
                priority = 1
            )

            saveSavingsGoalUseCase(goalToSave)
            _isSavingSuccess.update { true }
            kotlinx.coroutines.delay(2000)
            _isSavingSuccess.update { false }
        }
    }
}
