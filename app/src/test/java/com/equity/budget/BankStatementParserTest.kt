package com.equity.budget

import com.equity.budget.data.model.TransactionType
import com.equity.budget.domain.BankStatementParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Locale

class BankStatementParserTest {

    @Test
    fun testParseTBankSampleText() {
        val sampleStatement = """
            Исх. № 00000000
            АКЦИОНЕРНОЕ ОБЩЕСТВО «ТБАНК»
            РОССИЯ, МОСКВА, УЛ. ПРИМЕРНАЯ, Д. 1
            ТЕЛ.: +7 800 000-00-00, TBANK.RU
            Справка о движении средств
            26.10.2026
            Иванов Иван Иванович
            Адрес места жительства: 100000, г. Москва, ул. Центральная, д. 1
            О продукте
            Дата заключения договора: 01.01.2022
            Номер договора: 1111111111
            Номер лицевого счета: 40817810000000000000
            Движение средств за период с 26.10.2025 по 26.10.2026
            Дата и время операции Дата списания Сумма в валюте операции Сумма операции в валюте карты Описание операции Номер карты
            25.10.2026 21:24 25.10.2026 21:39 -348.48 ₽ -348.48 ₽ Оплата в PEREKRESTOK MOSCOW RUS 0000
            25.10.2026 21:24 25.10.2026 21:25 -51.52 ₽ -51.52 ₽ Внутренний перевод на договор 9999999999 0000
            25.10.2026 20:32 25.10.2026 20:33 -604.00 ₽ -604.00 ₽ Оплата в Vkusnoitochka_00000__SB P 0000
            25.10.2026 17:57 25.10.2026 17:57 +10 821.92 ₽ +10 821.92 ₽ Перевод себе 0000
            20.10.2026 22:31 20.10.2026 22:31 +247.00 ₽ +247.00 ₽ Кэшбэк за обычные покупки —
            30.08.2026 07:59 30.08.2026 08:14 -8 600.00 Դ -2 060.99 ₽ Оплата в LG Shop ARM 0000
            АО «ТБанк» универсальная лицензия Банка России № 2673
            1
        """.trimIndent()

        val result = BankStatementParser.parseText(sampleStatement)

        assertEquals("Should parse 6 transactions", 6, result.totalCount)

        // Verify first transaction: Perekrestok
        val tx0 = result.transactions.find { it.description.contains("PEREKRESTOK") }
        assertNotNull(tx0)
        assertEquals(348.48, tx0!!.amount, 0.001)
        assertEquals(TransactionType.EXPENSE, tx0.type)
        assertEquals("groceries", tx0.categoryId)
        assertEquals("0000", tx0.cardLast4)

        // Verify piggy bank: internal transfer to agreement
        val tx1 = result.transactions.find { it.description.contains("договор 9999999999") }
        assertNotNull(tx1)
        assertEquals(51.52, tx1!!.amount, 0.001)
        assertEquals("investments", tx1.categoryId)

        // Verify food: Vkusno i Tochka
        val tx2 = result.transactions.find { it.description.contains("Vkusnoitochka") }
        assertNotNull(tx2)
        assertEquals(604.00, tx2!!.amount, 0.001)
        assertEquals("dining", tx2.categoryId)

        // Verify income: Transfer to self
        val tx3 = result.transactions.find { it.description.contains("Перевод себе") }
        assertNotNull(tx3)
        assertEquals(10821.92, tx3!!.amount, 0.001)
        assertEquals(TransactionType.INCOME, tx3.type)

        // Verify cashback with dash card
        val tx4 = result.transactions.find { it.description.contains("Кэшбэк") }
        assertNotNull(tx4)
        assertEquals(247.00, tx4!!.amount, 0.001)
        assertEquals(TransactionType.INCOME, tx4.type)
        assertEquals("—", tx4.cardLast4)

        // Verify foreign currency transaction
        val tx5 = result.transactions.find { it.description.contains("LG Shop") }
        assertNotNull(tx5)
        assertEquals(2060.99, tx5!!.amount, 0.001)
        assertEquals(TransactionType.EXPENSE, tx5.type)
        assertEquals("shopping", tx5.categoryId)

        // Check timestamp accuracy (25.10.2026 21:24)
        val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        val expectedTime = sdf.parse("25.10.2026 21:24")!!.time
        assertEquals(expectedTime, tx0.timestamp)

        // Transactions should be sorted descending by timestamp
        assertTrue(result.transactions[0].timestamp >= result.transactions.last().timestamp)
    }

    @Test
    fun testParseMultilineTextBlocks() {
        val sample = """
            24.10.2026
            17:48
            24.10.2026
            17:49
            -867.00 ₽ -867.00 ₽ Оплата в
            Vkusnoitochka_00000__SB
            P
            0000
            24.10.2026
            17:43
            24.10.2026
            17:58
            -3 980.00 ₽ -3 980.00 ₽ Оплата в PHO BO TOM
            YUM MOSCOW RUS
            0000
        """.trimIndent()

        val result = BankStatementParser.parseText(sample)
        assertEquals(2, result.totalCount)
        assertEquals(867.0, result.transactions[0].amount, 0.001)
        assertEquals(3980.0, result.transactions[1].amount, 0.001)
        assertEquals("dining", result.transactions[0].categoryId)
        assertEquals("dining", result.transactions[1].categoryId)
    }

    @Test
    fun testDuplicateDetection() {
        val sample = "25.10.2026 21:24 25.10.2026 21:39 -348.48 ₽ -348.48 ₽ Оплата в PEREKRESTOK MOSCOW RUS 0000"

        val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        val txTime = sdf.parse("25.10.2026 21:24")!!.time

        val existing = listOf(
            com.equity.budget.data.local.TransactionEntity(
                id = 1,
                title = "PEREKRESTOK",
                amount = 348.48,
                currency = "RUB",
                type = "expense",
                categoryId = "groceries",
                timestamp = txTime
            )
        )

        val result = BankStatementParser.parseText(sample, existing)
        assertEquals(1, result.totalCount)
        assertTrue("Transaction should be detected as duplicate", result.transactions[0].isDuplicate)
        assertTrue("Duplicate transaction should be unselected by default", !result.transactions[0].isSelected)
        assertEquals(1, result.duplicateCount)
        assertEquals(0, result.newCount)
    }

    @Test
    fun testParseCsv() {
        val csvData = """
            Дата операции;Дата платежа;Номер карты;Статус;Сумма операции;Валюта операции;Сумма платежа;Валюта платежа;Кэшбэк;Категория;MCC;Описание
            25.10.2026 21:24;25.10.2026 21:39;*0000;OK;-348.48;RUB;-348.48;RUB;3.48;Супермаркеты;5411;Оплата в PEREKRESTOK
            25.10.2026 17:57;25.10.2026 17:57;*0000;OK;10821.92;RUB;10821.92;RUB;0.00;Переводы;6536;Перевод себе
        """.trimIndent()

        val stream = java.io.ByteArrayInputStream(csvData.toByteArray(Charsets.UTF_8))
        val result = BankStatementParser.parseCsv(stream)

        assertEquals(2, result.totalCount)
        assertEquals(348.48, result.transactions[0].amount, 0.001)
        assertEquals("groceries", result.transactions[0].categoryId)
        assertEquals(10821.92, result.transactions[1].amount, 0.001)
        assertEquals(TransactionType.INCOME, result.transactions[1].type)
    }
}
