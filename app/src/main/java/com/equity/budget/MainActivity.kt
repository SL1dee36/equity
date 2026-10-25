package com.equity.budget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.equity.budget.ui.screens.MainScreen
import com.equity.budget.ui.theme.EquityTheme
import com.equity.budget.ui.viewmodel.BudgetViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: BudgetViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val openAddTransaction = intent?.action == "ACTION_ADD_TRANSACTION"

        setContent {
            val settings by viewModel.settings.collectAsState()

            EquityTheme(
                themeMode = settings.themeMode,
                dynamicColor = settings.dynamicColors
            ) {
                MainScreen(
                    viewModel = viewModel,
                    initialOpenAddTransaction = openAddTransaction
                )
            }
        }
    }
}
