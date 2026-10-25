package com.equity.app.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Dashboard : Screen(
        route = "dashboard",
        title = "Dashboard",
        icon = Icons.Rounded.Dashboard
    )

    data object Transactions : Screen(
        route = "transactions",
        title = "Transactions",
        icon = Icons.Rounded.AccountBalanceWallet
    )

    data object SmartCalculator : Screen(
        route = "calculator",
        title = "Smart Formula",
        icon = Icons.Rounded.Calculate
    )

    companion object {
        val bottomNavItems = listOf(
            Dashboard,
            Transactions,
            SmartCalculator
        )
    }
}
