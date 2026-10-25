package com.equity.budget.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.equity.budget.data.local.TransactionEntity
import com.equity.budget.data.model.AppLanguage
import com.equity.budget.data.model.Category
import com.equity.budget.data.model.Currency
import com.equity.budget.data.model.TransactionType
import com.equity.budget.data.preferences.AppSettings
import com.equity.budget.domain.CurrencyConverter
import com.equity.budget.ui.theme.ExpenseRed
import com.equity.budget.ui.theme.IncomeGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TransactionItem(
    transaction: TransactionEntity,
    settings: AppSettings,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val category = Category.getById(transaction.categoryId)
    val isIncome = transaction.type == TransactionType.INCOME.id
    val isRu = settings.language == AppLanguage.RU
    val sdf = SimpleDateFormat(if (isRu) "d MMMM, HH:mm" else "MMM d, HH:mm", if (isRu) Locale("ru") else Locale.ENGLISH)
    val formattedDate = sdf.format(Date(transaction.timestamp))

    val origCurrency = Currency.fromCode(transaction.currency)
    val formattedOrig = CurrencyConverter.format(
        amount = transaction.amount,
        currency = origCurrency,
        isIncome = isIncome
    )

    // Check if auto-converted display is different from original
    val showConverted = settings.autoConvert && origCurrency != settings.primaryCurrency
    val convertedAmount = if (showConverted) {
        CurrencyConverter.convert(
            amount = transaction.amount,
            from = origCurrency,
            to = settings.primaryCurrency,
            usdToRub = settings.usdToRub,
            eurToRub = settings.eurToRub
        )
    } else 0.0

    val formattedConverted = if (showConverted) {
        CurrencyConverter.format(
            amount = convertedAmount,
            currency = settings.primaryCurrency,
            isIncome = isIncome
        )
    } else ""

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Category Icon with pastel tint
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(category.color.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = null,
                tint = category.color,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Title and Date
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = transaction.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                if (transaction.isAutoAdded) {
                    Spacer(modifier = Modifier.width(6.dp))
                    BadgePill(
                        text = if (isRu) "Авто" else "Auto",
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Text(
                text = formattedDate,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Amount
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = if (showConverted) formattedConverted else formattedOrig,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = if (isIncome) IncomeGreen else MaterialTheme.colorScheme.onSurface
            )
            if (showConverted) {
                Text(
                    text = "($formattedOrig)",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
