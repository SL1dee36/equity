package com.equity.budget.domain

import com.equity.budget.data.model.Currency
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object CurrencyConverter {

    /**
     * Converts [amount] from [from] currency into [to] currency using rates defined against RUB.
     */
    fun convert(
        amount: Double,
        from: Currency,
        to: Currency,
        usdToRub: Double = 93.5,
        eurToRub: Double = 101.5
    ): Double {
        if (from == to) return amount

        // First convert 'from' amount to RUB
        val amountInRub = when (from) {
            Currency.RUB -> amount
            Currency.USD -> amount * usdToRub
            Currency.EUR -> amount * eurToRub
        }

        // Then convert RUB to 'to' currency
        return when (to) {
            Currency.RUB -> amountInRub
            Currency.USD -> amountInRub / usdToRub
            Currency.EUR -> amountInRub / eurToRub
        }
    }

    fun format(
        amount: Double,
        currency: Currency,
        showSign: Boolean = false,
        isIncome: Boolean? = null
    ): String {
        val symbols = DecimalFormatSymbols(Locale.US).apply {
            groupingSeparator = ' '
            decimalSeparator = '.'
        }

        // If amount is integer-like, don't show trailing .00 for cleaner minimalist look,
        // or show two decimals if has cents
        val pattern = if (amount % 1.0 == 0.0) "#,##0" else "#,##0.00"
        val df = DecimalFormat(pattern, symbols)
        val formattedNumber = df.format(kotlin.math.abs(amount))

        val sign = when {
            isIncome == true -> "+"
            isIncome == false -> "-"
            showSign && amount > 0 -> "+"
            showSign && amount < 0 -> "-"
            else -> ""
        }

        return when (currency) {
            Currency.RUB -> "$sign$formattedNumber ₽"
            Currency.USD -> "$sign$$formattedNumber"
            Currency.EUR -> "$sign€$formattedNumber"
        }
    }
}
