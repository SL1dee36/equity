package com.equity.budget.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.equity.budget.data.model.AppLanguage
import com.equity.budget.data.model.Currency
import com.equity.budget.data.model.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AppSettings(
    val language: AppLanguage = AppLanguage.RU,
    val primaryCurrency: Currency = Currency.RUB,
    val autoConvert: Boolean = true,
    val autoFetchRates: Boolean = true,
    val lastRatesUpdateTimestamp: Long = 0L,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColors: Boolean = true,
    // Rates relative to RUB
    val usdToRub: Double = 96.5,
    val eurToRub: Double = 104.5,
    // Savings target & daily allowance calculator
    val savingsGoalAmount: Double = 0.0,
    val savingsTargetDays: Int = 30
)

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("equity_prefs", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private fun loadSettings(): AppSettings {
        val langStr = prefs.getString(KEY_LANGUAGE, AppLanguage.RU.code) ?: AppLanguage.RU.code
        val lang = if (langStr == AppLanguage.EN.code) AppLanguage.EN else AppLanguage.RU

        val currStr = prefs.getString(KEY_CURRENCY, Currency.RUB.code) ?: Currency.RUB.code
        val currency = Currency.fromCode(currStr)

        val autoConvert = prefs.getBoolean(KEY_AUTO_CONVERT, true)
        val autoFetchRates = prefs.getBoolean(KEY_AUTO_FETCH_RATES, true)
        val lastRatesUpdateTimestamp = prefs.getLong(KEY_LAST_RATES_UPDATE, 0L)

        val themeModeStr = prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
        val themeMode = try {
            ThemeMode.valueOf(themeModeStr)
        } catch (_: Exception) {
            ThemeMode.SYSTEM
        }

        val dynamicColors = prefs.getBoolean(KEY_DYNAMIC_COLORS, true)
        val usdToRub = prefs.getFloat(KEY_USD_RUB, 96.5f).toDouble()
        val eurToRub = prefs.getFloat(KEY_EUR_RUB, 104.5f).toDouble()

        val savingsGoalAmount = prefs.getFloat(KEY_SAVINGS_GOAL, 0f).toDouble()
        val savingsTargetDays = prefs.getInt(KEY_SAVINGS_DAYS, 30)

        return AppSettings(
            language = lang,
            primaryCurrency = currency,
            autoConvert = autoConvert,
            autoFetchRates = autoFetchRates,
            lastRatesUpdateTimestamp = lastRatesUpdateTimestamp,
            themeMode = themeMode,
            dynamicColors = dynamicColors,
            usdToRub = usdToRub,
            eurToRub = eurToRub,
            savingsGoalAmount = savingsGoalAmount,
            savingsTargetDays = savingsTargetDays
        )
    }

    fun updateLanguage(lang: AppLanguage) {
        prefs.edit().putString(KEY_LANGUAGE, lang.code).apply()
        _settings.value = _settings.value.copy(language = lang)
    }

    fun updatePrimaryCurrency(currency: Currency) {
        prefs.edit().putString(KEY_CURRENCY, currency.code).apply()
        _settings.value = _settings.value.copy(primaryCurrency = currency)
    }

    fun updateAutoConvert(autoConvert: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_CONVERT, autoConvert).apply()
        _settings.value = _settings.value.copy(autoConvert = autoConvert)
    }

    fun updateAutoFetchRates(autoFetch: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_FETCH_RATES, autoFetch).apply()
        _settings.value = _settings.value.copy(autoFetchRates = autoFetch)
    }

    fun updateThemeMode(themeMode: ThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, themeMode.name).apply()
        _settings.value = _settings.value.copy(themeMode = themeMode)
    }

    fun updateDynamicColors(dynamicColors: Boolean) {
        prefs.edit().putBoolean(KEY_DYNAMIC_COLORS, dynamicColors).apply()
        _settings.value = _settings.value.copy(dynamicColors = dynamicColors)
    }

    fun updateRates(usdToRub: Double, eurToRub: Double, timestamp: Long = System.currentTimeMillis()) {
        prefs.edit()
            .putFloat(KEY_USD_RUB, usdToRub.toFloat())
            .putFloat(KEY_EUR_RUB, eurToRub.toFloat())
            .putLong(KEY_LAST_RATES_UPDATE, timestamp)
            .apply()
        _settings.value = _settings.value.copy(
            usdToRub = usdToRub,
            eurToRub = eurToRub,
            lastRatesUpdateTimestamp = timestamp
        )
    }

    fun updateSavingsGoal(amount: Double, days: Int) {
        prefs.edit()
            .putFloat(KEY_SAVINGS_GOAL, amount.toFloat())
            .putInt(KEY_SAVINGS_DAYS, days)
            .apply()
        _settings.value = _settings.value.copy(
            savingsGoalAmount = amount,
            savingsTargetDays = days
        )
    }

    companion object {
        private const val KEY_LANGUAGE = "key_language"
        private const val KEY_CURRENCY = "key_currency"
        private const val KEY_AUTO_CONVERT = "key_auto_convert"
        private const val KEY_AUTO_FETCH_RATES = "key_auto_fetch_rates"
        private const val KEY_LAST_RATES_UPDATE = "key_last_rates_update"
        private const val KEY_THEME_MODE = "key_theme_mode"
        private const val KEY_DYNAMIC_COLORS = "key_dynamic_colors"
        private const val KEY_USD_RUB = "key_usd_rub"
        private const val KEY_EUR_RUB = "key_eur_rub"
        private const val KEY_SAVINGS_GOAL = "key_savings_goal"
        private const val KEY_SAVINGS_DAYS = "key_savings_days"
    }
}
