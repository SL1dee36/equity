package com.equity.budget.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class Category(
    val id: String,
    val type: TransactionType,
    val nameEn: String,
    val nameRu: String,
    val icon: ImageVector,
    val color: Color,
    val presetsRu: List<String> = emptyList(),
    val presetsEn: List<String> = emptyList()
) {
    fun localizedName(language: AppLanguage): String = if (language == AppLanguage.RU) nameRu else nameEn
    fun localizedPresets(language: AppLanguage): List<String> = if (language == AppLanguage.RU) presetsRu else presetsEn

    companion object {
        val ALL_CATEGORIES = listOf(
            // Income
            Category(
                id = "salary",
                type = TransactionType.INCOME,
                nameEn = "Salary",
                nameRu = "Зарплата",
                icon = Icons.Default.Work,
                color = Color(0xFF10B981),
                presetsRu = listOf("Зарплата", "Аванс", "Премия", "Бонус"),
                presetsEn = listOf("Main Salary", "Advance", "Bonus", "Overtime")
            ),
            Category(
                id = "freelance",
                type = TransactionType.INCOME,
                nameEn = "Freelance",
                nameRu = "Фриланс",
                icon = Icons.Default.Laptop,
                color = Color(0xFF06B6D4),
                presetsRu = listOf("Проект", "Консультация", "Гонорар", "Заказ"),
                presetsEn = listOf("Client Project", "Consulting", "Contract", "Gig")
            ),
            Category(
                id = "investments",
                type = TransactionType.INCOME,
                nameEn = "Investments",
                nameRu = "Инвестиции",
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                color = Color(0xFF6366F1),
                presetsRu = listOf("Дивиденды", "Проценты", "Купоны", "Крипта"),
                presetsEn = listOf("Dividends", "Interest", "Coupons", "Crypto")
            ),
            Category(
                id = "gift",
                type = TransactionType.INCOME,
                nameEn = "Gifts",
                nameRu = "Подарки",
                icon = Icons.Default.CardGiftcard,
                color = Color(0xFFEC4899),
                presetsRu = listOf("Подарок", "Перевод", "Возврат долга"),
                presetsEn = listOf("Gift", "Transfer", "Debt Repayment")
            ),
            Category(
                id = "other_income",
                type = TransactionType.INCOME,
                nameEn = "Other Income",
                nameRu = "Другой доход",
                icon = Icons.Default.AccountBalance,
                color = Color(0xFF8B5CF6),
                presetsRu = listOf("Кэшбэк", "Продажа вещей", "Возврат налога"),
                presetsEn = listOf("Cashback", "Sold Items", "Tax Refund")
            ),

            // Expense
            Category(
                id = "groceries",
                type = TransactionType.EXPENSE,
                nameEn = "Groceries",
                nameRu = "Продукты",
                icon = Icons.Default.ShoppingCart,
                color = Color(0xFFF97316),
                presetsRu = listOf("Супермаркет", "Пятёрочка", "Перекрёсток", "ВкусВилл", "Рынок"),
                presetsEn = listOf("Supermarket", "Grocery Store", "Bakery", "Market")
            ),
            Category(
                id = "housing",
                type = TransactionType.EXPENSE,
                nameEn = "Housing & Rent",
                nameRu = "Жильё и ЖКУ",
                icon = Icons.Default.Home,
                color = Color(0xFF3B82F6),
                presetsRu = listOf("Аренда", "Коммуналка", "Интернет", "Электричество"),
                presetsEn = listOf("Rent", "Utilities", "Internet", "Electricity")
            ),
            Category(
                id = "transport",
                type = TransactionType.EXPENSE,
                nameEn = "Transport",
                nameRu = "Транспорт",
                icon = Icons.Default.DirectionsCar,
                color = Color(0xFFEAB308),
                presetsRu = listOf("Такси", "Бензин", "Метро", "Каршеринг", "Парковка"),
                presetsEn = listOf("Taxi / Uber", "Gas / Fuel", "Metro", "Car sharing", "Parking")
            ),
            Category(
                id = "dining",
                type = TransactionType.EXPENSE,
                nameEn = "Dining Out",
                nameRu = "Кафе и еда",
                icon = Icons.Default.Restaurant,
                color = Color(0xFFF43F5E),
                presetsRu = listOf("Кофе", "Обед", "Бизнес-ланч", "Ужин", "Доставка еды"),
                presetsEn = listOf("Coffee", "Lunch", "Dinner", "Food Delivery")
            ),
            Category(
                id = "shopping",
                type = TransactionType.EXPENSE,
                nameEn = "Shopping",
                nameRu = "Покупки",
                icon = Icons.Default.ShoppingBag,
                color = Color(0xFFA855F7),
                presetsRu = listOf("Одежда", "Обувь", "Ozon", "Wildberries", "Электроника"),
                presetsEn = listOf("Clothes", "Shoes", "Amazon", "Electronics", "Hardware")
            ),
            Category(
                id = "entertainment",
                type = TransactionType.EXPENSE,
                nameEn = "Entertainment",
                nameRu = "Развлечения",
                icon = Icons.Default.Movie,
                color = Color(0xFF14B8A6),
                presetsRu = listOf("Кино", "Подписка", "Игры", "Концерт", "Книги"),
                presetsEn = listOf("Cinema", "Subscription", "Games", "Concert", "Books")
            ),
            Category(
                id = "health",
                type = TransactionType.EXPENSE,
                nameEn = "Health & Pharmacy",
                nameRu = "Здоровье",
                icon = Icons.Default.LocalHospital,
                color = Color(0xFFEF4444),
                presetsRu = listOf("Аптека", "Врач", "Стоматолог", "Анализы"),
                presetsEn = listOf("Pharmacy", "Doctor", "Dentist", "Medicine")
            ),
            Category(
                id = "sport",
                type = TransactionType.EXPENSE,
                nameEn = "Sport & Hobby",
                nameRu = "Спорт и хобби",
                icon = Icons.Default.FitnessCenter,
                color = Color(0xFF84CC16),
                presetsRu = listOf("Фитнес", "Бассейн", "Спорттовары", "Секция"),
                presetsEn = listOf("Gym", "Pool", "Sport gear", "Training")
            ),
            Category(
                id = "other_expense",
                type = TransactionType.EXPENSE,
                nameEn = "Other Expense",
                nameRu = "Прочее",
                icon = Icons.Default.MoreHoriz,
                color = Color(0xFF64748B),
                presetsRu = listOf("Комиссия", "Связь", "Штраф", "Услуги"),
                presetsEn = listOf("Bank fee", "Mobile plan", "Fine", "Services")
            )
        )

        fun getById(id: String): Category {
            return ALL_CATEGORIES.firstOrNull { it.id == id }
                ?: Category(id, TransactionType.EXPENSE, id, id, Icons.Default.MoreHoriz, Color(0xFF64748B))
        }

        fun getCategoriesForType(type: TransactionType): List<Category> {
            return ALL_CATEGORIES.filter { it.type == type }
        }
    }
}
