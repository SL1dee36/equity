package com.equity.app.presentation.calculator

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.equity.app.core.theme.DarkSilver
import com.equity.app.core.theme.ElectricMint
import com.equity.app.core.theme.ElectricMintDark
import com.equity.app.core.theme.ExpenseRed
import com.equity.app.core.theme.InfoCyan
import com.equity.app.core.theme.MutedSilver
import com.equity.app.core.theme.ObsidianBlack
import com.equity.app.core.theme.ObsidianBorder
import com.equity.app.core.theme.ObsidianCard
import com.equity.app.core.theme.ObsidianCardElevated
import com.equity.app.core.theme.PlatinumText
import com.equity.app.core.theme.PlatinumWhite
import com.equity.app.core.theme.WarningAmber
import com.equity.app.core.util.Formatters
import com.equity.app.domain.model.GoalFeasibility
import org.koin.androidx.compose.koinViewModel

@Composable
fun SmartCalculatorScreen(
    viewModel: SmartCalculatorViewModel = koinViewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ObsidianBlack
    ) { innerPadding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = ElectricMint)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "THE EQUITY FORMULA",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.5.sp
                                ),
                                color = ElectricMint
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Smart Savings Calculator",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = PlatinumWhite
                        )
                        Text(
                            text = "Calculates exact 'Safe to Spend' vs 'Must Save' ratios and deposit auto-locks.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = DarkSilver
                        )
                    }
                }

                // Feasibility & Advisory Banner
                state.formulaResult?.let { result ->
                    item {
                        val (bannerBg, bannerBorder, bannerIcon, statusTitle) = when (result.feasibility) {
                            GoalFeasibility.OPTIMAL -> Quad(
                                ElectricMint.copy(alpha = 0.12f),
                                ElectricMint.copy(alpha = 0.35f),
                                Icons.Rounded.Savings,
                                "OPTIMAL WEALTH TRAJECTORY"
                            )
                            GoalFeasibility.HEALTHY -> Quad(
                                ElectricMint.copy(alpha = 0.12f),
                                ElectricMint.copy(alpha = 0.35f),
                                Icons.Rounded.Savings,
                                "HEALTHY GROWTH PACE"
                            )
                            GoalFeasibility.AGGRESSIVE -> Quad(
                                InfoCyan.copy(alpha = 0.12f),
                                InfoCyan.copy(alpha = 0.35f),
                                Icons.Rounded.AccountBalance,
                                "AGGRESSIVE TARGET"
                            )
                            GoalFeasibility.AT_RISK -> Quad(
                                WarningAmber.copy(alpha = 0.15f),
                                WarningAmber.copy(alpha = 0.4f),
                                Icons.Rounded.Warning,
                                "TIGHT BUDGET WARNING"
                            )
                            GoalFeasibility.UNFEASIBLE -> Quad(
                                ExpenseRed.copy(alpha = 0.15f),
                                ExpenseRed.copy(alpha = 0.45f),
                                Icons.Rounded.Warning,
                                "UNFEASIBLE TIMELINE DEFICIT"
                            )
                        }

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .border(1.dp, bannerBorder, RoundedCornerShape(20.dp)),
                            color = ObsidianCardElevated
                        ) {
                            Row(
                                modifier = Modifier
                                    .background(bannerBg)
                                    .padding(16.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(bannerBorder, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = bannerIcon,
                                        contentDescription = null,
                                        tint = PlatinumWhite,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = statusTitle,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = PlatinumWhite
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = result.recommendationMessage,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                                        color = PlatinumText
                                    )
                                }
                            }
                        }
                    }
                }

                // Core Ratios Grid (Must Save vs Safe to Spend)
                state.formulaResult?.let { result ->
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Must Save Ratio Card
                            RatioCard(
                                title = "MUST SAVE",
                                percentage = result.mustSaveRatio,
                                primaryMetric = "${Formatters.formatCurrency(result.requiredMonthlySavings)}/mo",
                                secondaryMetric = "${Formatters.formatCurrency(result.requiredDailySavings)}/day",
                                accentColor = ElectricMint,
                                modifier = Modifier.weight(1f)
                            )

                            // Safe to Spend Ratio Card
                            RatioCard(
                                title = "SAFE TO SPEND",
                                percentage = result.safeToSpendRatio,
                                primaryMetric = "${Formatters.formatCurrency(result.safeToSpendMonthly)}/mo",
                                secondaryMetric = "${Formatters.formatCurrency(result.safeToSpendDaily)}/day",
                                accentColor = InfoCyan,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // The Killer Feature: Incoming Paycheck Auto-Lock Simulation Card
                state.formulaResult?.let { result ->
                    item {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(22.dp))
                                .border(1.dp, ElectricMintDark.copy(alpha = 0.4f), RoundedCornerShape(22.dp)),
                            color = ObsidianCardElevated
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Rounded.Lock,
                                            contentDescription = null,
                                            tint = ElectricMint,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "DYNAMIC INCOMING AUTO-LOCK",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = ElectricMint
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = ElectricMint.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "${result.incomingLockPercentage.toInt()}% Auto-Lock",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = ElectricMint,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "When you receive income, Equity automatically locks this exact ratio into your savings goal to guarantee target delivery.",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                                    color = MutedSilver
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                // Simulation Deposit Pills
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf(500.0, 1000.0, 2500.0, 5000.0).forEach { amt ->
                                        val isSel = state.simulationDepositAmount == amt
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (isSel) ElectricMint else ObsidianCard,
                                            border = androidx.compose.foundation.BorderStroke(
                                                1.dp,
                                                if (isSel) ElectricMint else ObsidianBorder
                                            ),
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { viewModel.onSimulationDepositChange(amt) }
                                        ) {
                                            Box(
                                                modifier = Modifier.padding(vertical = 8.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = Formatters.formatCompactCurrency(amt),
                                                    style = MaterialTheme.typography.titleMedium.copy(
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold
                                                    ),
                                                    color = if (isSel) ObsidianBlack else PlatinumWhite
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Simulation Result Breakdown Box
                                val autoLockAmt = result.calculateAutoLockAmount(state.simulationDepositAmount)
                                val safeSpendRemainder = (state.simulationDepositAmount - autoLockAmt).coerceAtLeast(0.0)

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(ObsidianCard, RoundedCornerShape(16.dp))
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Deposit: ${Formatters.formatCompactCurrency(state.simulationDepositAmount)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = DarkSilver
                                        )
                                        Text(
                                            text = "Lock to Equity: ${Formatters.formatCurrency(autoLockAmt)}",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = ElectricMint
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "Discretionary Remainder",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = DarkSilver
                                        )
                                        Text(
                                            text = Formatters.formatCurrency(safeSpendRemainder),
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = PlatinumWhite
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Goal Configuration Card
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(22.dp))
                            .border(1.dp, ObsidianBorder, RoundedCornerShape(22.dp)),
                        color = ObsidianCard
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "GOAL CONFIGURATION",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = DarkSilver
                                )

                                TextButton(onClick = { viewModel.openAddFundsDialog() }) {
                                    Icon(
                                        imageVector = Icons.Rounded.Add,
                                        contentDescription = null,
                                        tint = ElectricMint,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Add Funds",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = ElectricMint
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Goal Title Field
                            Text(
                                text = "Goal Title",
                                style = MaterialTheme.typography.labelMedium,
                                color = MutedSilver
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = state.goalTitleInput,
                                onValueChange = { viewModel.onGoalTitleChange(it) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = PlatinumWhite,
                                    unfocusedTextColor = PlatinumWhite,
                                    focusedBorderColor = ElectricMint,
                                    unfocusedBorderColor = ObsidianBorder,
                                    focusedContainerColor = ObsidianCardElevated,
                                    unfocusedContainerColor = ObsidianCardElevated
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Target Amount and Saved Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Target ($)",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MutedSilver
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    OutlinedTextField(
                                        value = state.targetAmountInput,
                                        onValueChange = { viewModel.onTargetAmountChange(it) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = PlatinumWhite,
                                            unfocusedTextColor = PlatinumWhite,
                                            focusedBorderColor = ElectricMint,
                                            unfocusedBorderColor = ObsidianBorder,
                                            focusedContainerColor = ObsidianCardElevated,
                                            unfocusedContainerColor = ObsidianCardElevated
                                        ),
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Current Saved ($)",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MutedSilver
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    OutlinedTextField(
                                        value = state.currentSavedInput,
                                        onValueChange = { viewModel.onCurrentSavedChange(it) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = PlatinumWhite,
                                            unfocusedTextColor = PlatinumWhite,
                                            focusedBorderColor = ElectricMint,
                                            unfocusedBorderColor = ObsidianBorder,
                                            focusedContainerColor = ObsidianCardElevated,
                                            unfocusedContainerColor = ObsidianCardElevated
                                        ),
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Target Horizon Days
                            Text(
                                text = "Target Horizon (${state.targetDaysInput} Days)",
                                style = MaterialTheme.typography.labelMedium,
                                color = MutedSilver
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(30, 60, 90, 180, 365).forEach { days ->
                                    val isSel = state.targetDaysInput == days
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSel) ElectricMint else ObsidianCardElevated,
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (isSel) ElectricMint else ObsidianBorder
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { viewModel.onTargetDaysChange(days) }
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "${days}d",
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = if (isSel) ObsidianBlack else MutedSilver
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Save Goal Button
                            Button(
                                onClick = { viewModel.saveGoalToVault() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ElectricMint,
                                    contentColor = ObsidianBlack
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (state.isSavingSuccess) "Vault Updated!" else "Set as Active Vault Goal",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Add Funds Dialog
        if (state.isAddFundsDialogOpen) {
            AlertDialog(
                onDismissRequest = { viewModel.closeAddFundsDialog() },
                containerColor = ObsidianCardElevated,
                title = {
                    Text(
                        text = "Add Funds to Vault",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = PlatinumWhite
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "Enter amount saved into this goal:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MutedSilver
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = state.addFundsAmount,
                            onValueChange = { viewModel.onAddFundsAmountChange(it) },
                            placeholder = { Text("e.g. 500", color = DarkSilver) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = PlatinumWhite,
                                unfocusedTextColor = PlatinumWhite,
                                focusedBorderColor = ElectricMint,
                                unfocusedBorderColor = ObsidianBorder,
                                focusedContainerColor = ObsidianCard,
                                unfocusedContainerColor = ObsidianCard
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.submitAddFunds() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricMint,
                            contentColor = ObsidianBlack
                        )
                    ) {
                        Text("Add Funds")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.closeAddFundsDialog() }) {
                        Text("Cancel", color = MutedSilver)
                    }
                }
            )
        }
    }
}

@Composable
fun RatioCard(
    title: String,
    percentage: Float,
    primaryMetric: String,
    secondaryMetric: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, ObsidianBorder, RoundedCornerShape(20.dp)),
        color = ObsidianCard
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                color = accentColor
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "${percentage.toInt()}%",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = PlatinumWhite
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = primaryMetric,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
                color = PlatinumText
            )
            Text(
                text = secondaryMetric,
                style = MaterialTheme.typography.labelSmall,
                color = DarkSilver
            )
        }
    }
}

data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
