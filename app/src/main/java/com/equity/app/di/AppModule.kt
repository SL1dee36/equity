package com.equity.app.di

import com.equity.app.data.local.EquityDatabase
import com.equity.app.data.local.EquityDatabaseSeeder
import com.equity.app.data.repository.SavingsGoalRepositoryImpl
import com.equity.app.data.repository.TransactionRepositoryImpl
import com.equity.app.domain.calculator.EquityFormulaCalculator
import com.equity.app.domain.repository.SavingsGoalRepository
import com.equity.app.domain.repository.TransactionRepository
import com.equity.app.domain.usecase.AddFundsToGoalUseCase
import com.equity.app.domain.usecase.AddTransactionUseCase
import com.equity.app.domain.usecase.CalculateEquityFormulaUseCase
import com.equity.app.domain.usecase.DeleteTransactionUseCase
import com.equity.app.domain.usecase.GetActiveSavingsGoalUseCase
import com.equity.app.domain.usecase.GetAllSavingsGoalsUseCase
import com.equity.app.domain.usecase.GetAnalyticsUseCase
import com.equity.app.domain.usecase.GetFinancialSummaryUseCase
import com.equity.app.domain.usecase.GetTransactionsUseCase
import com.equity.app.domain.usecase.SaveSavingsGoalUseCase
import com.equity.app.domain.usecase.UpdateTransactionUseCase
import com.equity.app.presentation.calculator.SmartCalculatorViewModel
import com.equity.app.presentation.dashboard.DashboardViewModel
import com.equity.app.presentation.transactions.TransactionsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // Database
    single {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        EquityDatabase.buildDatabase(androidContext(), scope).also { db ->
            // Seed sample data if database is empty on app startup
            scope.launch {
                EquityDatabaseSeeder.seedIfEmpty(db.transactionDao(), db.savingsGoalDao())
            }
        }
    }

    // DAOs
    single { get<EquityDatabase>().transactionDao() }
    single { get<EquityDatabase>().savingsGoalDao() }

    // Repositories
    single<TransactionRepository> { TransactionRepositoryImpl(get()) }
    single<SavingsGoalRepository> { SavingsGoalRepositoryImpl(get()) }

    // Domain Calculator
    single { EquityFormulaCalculator() }

    // UseCases
    factory { GetTransactionsUseCase(get()) }
    factory { AddTransactionUseCase(get()) }
    factory { UpdateTransactionUseCase(get()) }
    factory { DeleteTransactionUseCase(get()) }
    factory { GetFinancialSummaryUseCase(get()) }
    factory { GetActiveSavingsGoalUseCase(get()) }
    factory { GetAllSavingsGoalsUseCase(get()) }
    factory { SaveSavingsGoalUseCase(get()) }
    factory { AddFundsToGoalUseCase(get()) }
    factory { CalculateEquityFormulaUseCase(get(), get(), get()) }
    factory { GetAnalyticsUseCase(get()) }

    // ViewModels
    viewModel {
        DashboardViewModel(
            getFinancialSummaryUseCase = get(),
            getActiveSavingsGoalUseCase = get(),
            getTransactionsUseCase = get(),
            getAnalyticsUseCase = get(),
            calculateEquityFormulaUseCase = get(),
            addTransactionUseCase = get()
        )
    }

    viewModel {
        TransactionsViewModel(
            getTransactionsUseCase = get(),
            addTransactionUseCase = get(),
            updateTransactionUseCase = get(),
            deleteTransactionUseCase = get()
        )
    }

    viewModel {
        SmartCalculatorViewModel(
            getActiveSavingsGoalUseCase = get(),
            saveSavingsGoalUseCase = get(),
            addFundsToGoalUseCase = get(),
            calculateEquityFormulaUseCase = get(),
            getFinancialSummaryUseCase = get()
        )
    }
}
