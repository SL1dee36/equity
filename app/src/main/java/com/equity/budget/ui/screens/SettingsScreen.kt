package com.equity.budget.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.equity.budget.BuildConfig
import com.equity.budget.data.model.AppLanguage
import com.equity.budget.data.model.Currency
import com.equity.budget.data.model.ThemeMode
import com.equity.budget.data.preferences.AppSettings
import com.equity.budget.domain.UpdateInfo
import com.equity.budget.ui.components.ExpressivePill
import com.equity.budget.ui.components.FlatCard
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsScreen(
    settings: AppSettings,
    isFetchingRates: Boolean,
    isCheckingUpdates: Boolean = false,
    availableUpdate: UpdateInfo? = null,
    onLanguageChange: (AppLanguage) -> Unit,
    onCurrencyChange: (Currency) -> Unit,
    onAutoConvertChange: (Boolean) -> Unit,
    onAutoFetchRatesChange: (Boolean) -> Unit,
    onRefreshRatesNow: () -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    onDynamicColorsChange: (Boolean) -> Unit,
    onRatesChange: (usdToRub: Double, eurToRub: Double) -> Unit,
    onCheckUpdates: (currentVersion: String) -> Unit = {},
    onDismissUpdateDialog: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isRu = settings.language == AppLanguage.RU
    val context = LocalContext.current
    var showRatesDialog by remember { mutableStateOf(false) }

    val versionName = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: BuildConfig.VERSION_NAME
        } catch (_: Exception) {
            BuildConfig.VERSION_NAME
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = if (isRu) "Настройки" else "Settings",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 1. Language Section
        SettingsCard(
            icon = Icons.Default.Language,
            title = if (isRu) "Язык интерфейса" else "App Language"
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AppLanguage.entries.forEach { lang ->
                    ExpressivePill(
                        text = lang.displayName,
                        isSelected = lang == settings.language,
                        onClick = { onLanguageChange(lang) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Primary Currency Section
        SettingsCard(
            icon = Icons.Default.Payments,
            title = if (isRu) "Основная валюта" else "Primary Currency"
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val currencies = listOf(
                    Currency.RUB to if (isRu) "Рубль (₽)" else "Ruble (₽)",
                    Currency.USD to if (isRu) "Доллар ($)" else "Dollar ($)",
                    Currency.EUR to if (isRu) "Евро (€)" else "Euro (€)"
                )
                currencies.forEach { (cur, label) ->
                    ExpressivePill(
                        text = label,
                        isSelected = cur == settings.primaryCurrency,
                        onClick = { onCurrencyChange(cur) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 3. Auto-conversion & Real-time Rate Fetching Section
        SettingsCard(
            icon = Icons.Default.CurrencyExchange,
            title = if (isRu) "Конвертация и курсы валют" else "Conversion & Exchange Rates"
        ) {
            // Auto convert toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isRu) "Автоконвертация сумм" else "Auto-convert amounts",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isRu) "Баланс и отчёты пересчитываются в основную валюту"
                        else "Balances and reports convert to selected primary currency",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = settings.autoConvert,
                    onCheckedChange = onAutoConvertChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Realtime Auto Fetch rates switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isRu) "Парсинг курсов в реальном времени" else "Real-time rates auto-sync",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isRu) "Автоматически запрашивать актуальные курсы с ЦБ РФ"
                        else "Automatically fetch latest rates from central exchange feed",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = settings.autoFetchRates,
                    onCheckedChange = onAutoFetchRatesChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Current Rates Display & Refresh Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isRu) "$1 = ${String.format(Locale.US, "%.2f", settings.usdToRub)} ₽,  €1 = ${String.format(Locale.US, "%.2f", settings.eurToRub)} ₽"
                        else "$1 = ${String.format(Locale.US, "%.2f", settings.usdToRub)} ₽,  €1 = ${String.format(Locale.US, "%.2f", settings.eurToRub)} ₽",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (settings.lastRatesUpdateTimestamp > 0) {
                        val sdf = SimpleDateFormat("dd.MM HH:mm", Locale.getDefault())
                        Text(
                            text = if (isRu) "Обновлено: ${sdf.format(Date(settings.lastRatesUpdateTimestamp))}"
                            else "Updated: ${sdf.format(Date(settings.lastRatesUpdateTimestamp))}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { showRatesDialog = true }) {
                        Text(if (isRu) "Вручную" else "Edit", fontSize = 12.sp)
                    }

                    if (isFetchingRates) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        IconButton(onClick = onRefreshRatesNow) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh rates",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 4. Appearance & Themes Section
        SettingsCard(
            icon = Icons.Default.DarkMode,
            title = if (isRu) "Оформление и тема" else "Appearance & Themes"
        ) {
            val themeOptions = listOf(
                ThemeMode.SYSTEM to if (isRu) "Система" else "System",
                ThemeMode.LIGHT to if (isRu) "Светлая" else "Light",
                ThemeMode.DARK to if (isRu) "Тёмная" else "Dark",
                ThemeMode.AMOLED to if (isRu) "AMOLED" else "AMOLED"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                themeOptions.forEach { (mode, label) ->
                    ExpressivePill(
                        text = label,
                        isSelected = mode == settings.themeMode,
                        onClick = { onThemeModeChange(mode) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && settings.themeMode != ThemeMode.AMOLED) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isRu) "Цвета обоев устройства" else "Wallpaper Dynamic Colors",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isRu) "Material You палитра под цвет рабочего стола" else "Material You palette matching device wallpaper",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = settings.dynamicColors,
                        onCheckedChange = onDynamicColorsChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Dynamic App Version and Check for Updates Button at the very bottom
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Equity v$versionName",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(enabled = !isCheckingUpdates) { onCheckUpdates(versionName) }
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isCheckingUpdates) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(
                    text = if (isRu) "Проверить обновления" else "Check for updates",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    if (showRatesDialog) {
        ExchangeRatesDialog(
            currentUsd = settings.usdToRub,
            currentEur = settings.eurToRub,
            isRu = isRu,
            onDismiss = { showRatesDialog = false },
            onSave = { u, e ->
                onRatesChange(u, e)
                showRatesDialog = false
            }
        )
    }

    // Update Available Dialog
    if (availableUpdate != null) {
        AlertDialog(
            onDismissRequest = onDismissUpdateDialog,
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    text = if (isRu) "Доступна версия ${availableUpdate.latestVersion}" else "Update ${availableUpdate.latestVersion} Available",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = if (isRu) "Вышла новая версия приложения на GitHub. Хотите перейти к загрузке?"
                        else "A newer version of Equity is available on GitHub. Would you like to download it?",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (availableUpdate.body.isNotBlank()) {
                        Text(
                            text = availableUpdate.body,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(availableUpdate.downloadUrl))
                        context.startActivity(intent)
                        onDismissUpdateDialog()
                    },
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Text(if (isRu) "Обновить" else "Update")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissUpdateDialog) {
                    Text(if (isRu) "Позже" else "Later")
                }
            }
        )
    }
}

@Composable
fun SettingsCard(
    icon: ImageVector,
    title: String,
    content: @Composable () -> Unit
) {
    FlatCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun ExchangeRatesDialog(
    currentUsd: Double,
    currentEur: Double,
    isRu: Boolean,
    onDismiss: () -> Unit,
    onSave: (usdToRub: Double, eurToRub: Double) -> Unit
) {
    var usdText by remember { mutableStateOf(currentUsd.toString()) }
    var eurText by remember { mutableStateOf(currentEur.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = if (isRu) "Курсы к рублю (₽)" else "Exchange Rates to RUB",
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = usdText,
                    onValueChange = { usdText = it },
                    label = { Text("1 USD ($) = ? RUB (₽)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = eurText,
                    onValueChange = { eurText = it },
                    label = { Text("1 EUR (€) = ? RUB (₽)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val u = usdText.toDoubleOrNull() ?: currentUsd
                    val e = eurText.toDoubleOrNull() ?: currentEur
                    onSave(u, e)
                },
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text(if (isRu) "Сохранить" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isRu) "Отмена" else "Cancel")
            }
        }
    )
}
