package com.equity.budget.data.model

enum class Currency(val code: String, val symbol: String, val displayNameEn: String, val displayNameRu: String) {
    RUB("RUB", "₽", "Russian Ruble (RUB)", "Российский рубль (₽)"),
    USD("USD", "$", "US Dollar (USD)", "Доллар США ($)"),
    EUR("EUR", "€", "Euro (EUR)", "Евро (€)");

    companion object {
        fun fromCode(code: String): Currency {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: RUB
        }
    }
}

enum class TransactionType(val id: String) {
    EXPENSE("expense"),
    INCOME("income")
}

enum class RecurrenceFrequency(val id: String) {
    MONTHLY("monthly"),
    WEEKLY("weekly"),
    BIWEEKLY("biweekly")
}

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
    AMOLED
}

enum class AppLanguage(val code: String, val displayName: String) {
    RU("ru", "Русский"),
    EN("en", "English")
}

enum class TimePeriod {
    TODAY,
    WEEK,
    MONTH,
    YEAR,
    ALL
}
