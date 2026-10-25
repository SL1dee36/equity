package com.equity.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.equity.app.domain.model.Transaction
import com.equity.app.domain.model.TransactionCategory
import com.equity.app.domain.model.TransactionType

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val type: String, // TransactionType name
    val category: String, // TransactionCategory name
    val date: Long,
    val note: String? = null,
    val isRecurring: Boolean = false
) {
    fun toDomain(): Transaction {
        val domainType = runCatching { TransactionType.valueOf(type) }.getOrDefault(TransactionType.EXPENSE)
        val domainCategory = TransactionCategory.fromDisplayName(category)
        return Transaction(
            id = id,
            amount = amount,
            type = domainType,
            category = domainCategory,
            date = date,
            note = note,
            isRecurring = isRecurring
        )
    }

    companion object {
        fun fromDomain(transaction: Transaction): TransactionEntity {
            return TransactionEntity(
                id = transaction.id,
                amount = transaction.amount,
                type = transaction.type.name,
                category = transaction.category.displayName,
                date = transaction.date,
                note = transaction.note,
                isRecurring = transaction.isRecurring
            )
        }
    }
}
