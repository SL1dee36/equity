package com.equity.app.domain.model

enum class TransactionCategory(
    val displayName: String,
    val defaultType: TransactionType,
    val colorHex: Long,
    val isEssential: Boolean = false
) {
    // Income Categories
    SALARY("Salary", TransactionType.INCOME, 0xFF00E676),
    FREELANCE("Freelance & Side Gig", TransactionType.INCOME, 0xFF00B0FF),
    INVESTMENTS("Investments & Dividends", TransactionType.INCOME, 0xFF76FF03),
    BONUS("Bonus & Gifts", TransactionType.INCOME, 0xFFFFD700),
    OTHER_INCOME("Other Income", TransactionType.INCOME, 0xFF1DE9B6),

    // Essential Expenses
    RENT("Housing & Rent", TransactionType.EXPENSE, 0xFFFF5252, isEssential = true),
    UTILITIES("Utilities & Bills", TransactionType.EXPENSE, 0xFFFF7043, isEssential = true),
    FOOD("Food & Groceries", TransactionType.EXPENSE, 0xFFFFAB00, isEssential = true),
    TRANSPORT("Transportation", TransactionType.EXPENSE, 0xFF29B6F6, isEssential = true),
    HEALTH("Health & Medical", TransactionType.EXPENSE, 0xFFEC407A, isEssential = true),

    // Discretionary Expenses
    TECH("Tech & Software", TransactionType.EXPENSE, 0xFF7C4DFF),
    ENTERTAINMENT("Entertainment & Leisure", TransactionType.EXPENSE, 0xFFAB47BC),
    SHOPPING("Shopping & Lifestyle", TransactionType.EXPENSE, 0xFFFF4081),
    EDUCATION("Education & Growth", TransactionType.EXPENSE, 0xFF26A69A),
    OTHER_EXPENSE("Other Expense", TransactionType.EXPENSE, 0xFF78909C);

    companion object {
        fun fromDisplayName(name: String): TransactionCategory {
            return entries.firstOrNull { it.displayName.equals(name, ignoreCase = true) || it.name.equals(name, ignoreCase = true) }
                ?: OTHER_EXPENSE
        }

        fun categoriesFor(type: TransactionType): List<TransactionCategory> {
            return entries.filter { it.defaultType == type }
        }
    }
}
