package com.equity.budget.domain

import com.equity.budget.data.local.TransactionEntity
import com.equity.budget.data.model.Category
import com.equity.budget.data.model.Currency
import com.equity.budget.data.model.TransactionType
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs

data class ParsedTransaction(
    val title: String,
    val amount: Double,
    val currency: String = "RUB",
    val type: TransactionType,
    val categoryId: String,
    val timestamp: Long,
    val rawDateString: String,
    val description: String,
    val cardLast4: String = "",
    val isDuplicate: Boolean = false,
    val isSelected: Boolean = true
) {
    fun toEntity(): TransactionEntity {
        return TransactionEntity(
            title = title,
            amount = amount,
            currency = currency,
            type = type.id,
            categoryId = categoryId,
            timestamp = timestamp,
            note = if (cardLast4.isNotBlank() && cardLast4 != "—") "Карта *$cardLast4 • $description" else description
        )
    }
}

data class StatementParseResult(
    val transactions: List<ParsedTransaction>,
    val totalCount: Int,
    val newCount: Int,
    val duplicateCount: Int,
    val totalIncome: Double,
    val totalExpense: Double,
    val startDate: Long?,
    val endDate: Long?,
    val bankName: String = "Т-Банк"
)

object BankStatementParser {

    private val dateTimeFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    private val dateFormatOnly = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    // Regex for date: DD.MM.YYYY
    private val dateRegex = Regex("""\b(\d{2}\.\d{2}\.\d{4})\b""")
    // Regex for time: HH:MM
    private val timeRegex = Regex("""\b(\d{2}:\d{2})\b""")
    // Regex for amount with sign and mandatory currency symbol (₽, Դ, $, €, RUB, etc.)
    // Note: in bank statements, amounts always contain the currency mark (e.g. -348.48 ₽, -8 600.00 Դ)
    // Requiring currency prevents false matches on phone numbers (+7 495 648-10-00) or contract numbers.
    private val amountRegex = Regex("""([+-])\s*(\d{1,3}(?:[\s\u00A0]\d{3})*(?:[.,]\d{1,2})?)\s*([₽\u20BD\u058F$€]|RUB|USD|EUR)""", RegexOption.IGNORE_CASE)

    fun parsePdf(
        inputStream: InputStream,
        existingTransactions: List<TransactionEntity> = emptyList()
    ): StatementParseResult {
        val document = PDDocument.load(inputStream)
        val fullText = try {
            val stripper = PDFTextStripper()
            stripper.sortByPosition = true
            stripper.getText(document)
        } finally {
            document.close()
        }
        return parseText(fullText, existingTransactions)
    }

    fun parseCsv(
        inputStream: InputStream,
        existingTransactions: List<TransactionEntity> = emptyList()
    ): StatementParseResult {
        val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
        val lines = reader.readLines()
        val parsedList = mutableListOf<ParsedTransaction>()

        if (lines.isEmpty()) {
            return StatementParseResult(emptyList(), 0, 0, 0, 0.0, 0.0, null, null)
        }

        // Detect delimiter: semicolon or comma
        val header = lines.first()
        val delimiter = if (header.count { it == ';' } > header.count { it == ',' }) ';' else ','

        // Look for standard T-Bank CSV column indexes:
        // Дата операции;Дата платежа;Номер карты;Статус;Сумма операции;Валюта операции;Сумма платежа;Валюта платежа;Кэшбэк;Категория;MCC;Описание
        val headerCols = header.split(delimiter).map { it.trim().trim('"', '\'') }
        val dateIdx = headerCols.indexOfFirst { it.contains("Дата операции", ignoreCase = true) || it.contains("Дата", ignoreCase = true) }
        val amountIdx = headerCols.indexOfFirst { it.contains("Сумма платежа", ignoreCase = true) || it.contains("Сумма операции", ignoreCase = true) || it.contains("Сумма", ignoreCase = true) }
        val descIdx = headerCols.indexOfFirst { it.contains("Описание", ignoreCase = true) }
        val catIdx = headerCols.indexOfFirst { it.contains("Категория", ignoreCase = true) }
        val cardIdx = headerCols.indexOfFirst { it.contains("Номер карты", ignoreCase = true) }

        for (i in 1 until lines.size) {
            val line = lines[i].trim()
            if (line.isBlank()) continue
            val parts = line.split(delimiter).map { it.trim().trim('"', '\'') }
            if (parts.size <= maxOf(dateIdx, amountIdx)) continue

            val rawDate = if (dateIdx >= 0 && dateIdx < parts.size) parts[dateIdx] else ""
            val rawAmount = if (amountIdx >= 0 && amountIdx < parts.size) parts[amountIdx] else ""
            val desc = if (descIdx >= 0 && descIdx < parts.size) parts[descIdx] else ""
            val card = if (cardIdx >= 0 && cardIdx < parts.size) parts[cardIdx] else ""
            val csvCat = if (catIdx >= 0 && catIdx < parts.size) parts[catIdx] else ""

            val cleanAmountStr = rawAmount.replace(" ", "").replace("\u00A0", "").replace(',', '.')
            val amountVal = cleanAmountStr.toDoubleOrNull() ?: continue
            val isIncome = amountVal > 0
            val absAmount = abs(amountVal)

            val timestamp = parseTimestamp(rawDate)
            val categoryId = if (csvCat.isNotBlank()) mapCsvCategory(csvCat, isIncome) else CategoryGuesser.guess(desc, isIncome)
            val title = cleanTitle(desc)

            val isDup = isDuplicate(timestamp, absAmount, isIncome, title, existingTransactions)

            parsedList.add(
                ParsedTransaction(
                    title = title,
                    amount = absAmount,
                    currency = "RUB",
                    type = if (isIncome) TransactionType.INCOME else TransactionType.EXPENSE,
                    categoryId = categoryId,
                    timestamp = timestamp,
                    rawDateString = rawDate,
                    description = desc,
                    cardLast4 = card,
                    isDuplicate = isDup,
                    isSelected = !isDup
                )
            )
        }

        return buildResult(parsedList)
    }

    fun parseText(
        rawText: String,
        existingTransactions: List<TransactionEntity> = emptyList()
    ): StatementParseResult {
        val parsedList = mutableListOf<ParsedTransaction>()
        val allFilteredLines = rawText.lines()
            .map { it.trim() }
            .filter { it.isNotBlank() && !isHeaderOrFooterLine(it) }

        // In official bank PDFs, find where the transaction table starts (skip client info and contract header)
        val headerIdx = allFilteredLines.indexOfFirst { line ->
            line.contains("Дата и время операции", ignoreCase = true) ||
            line.contains("Движение средств за период", ignoreCase = true)
        }
        val filteredLines = if (headerIdx >= 0 && headerIdx + 1 < allFilteredLines.size) {
            allFilteredLines.subList(headerIdx + 1, allFilteredLines.size)
        } else {
            allFilteredLines
        }

        // Group into logical transaction blocks
        val blocks = mutableListOf<String>()
        var currentBlock = StringBuilder()

        for (i in filteredLines.indices) {
            val line = filteredLines[i]

            // Check if line starts with a date
            val isDateStart = dateRegex.find(line)?.range?.first == 0
            if (isDateStart) {
                // Verify that this date is accompanied by a time (either on the same line or the immediately next line)
                val hasTimeOnLine = timeRegex.containsMatchIn(line)
                val hasTimeOnNextLine = (i + 1 < filteredLines.size) && timeRegex.containsMatchIn(filteredLines[i + 1])
                val isValidTxDate = hasTimeOnLine || hasTimeOnNextLine

                if (isValidTxDate) {
                    val hasAmount = amountRegex.containsMatchIn(currentBlock)
                    // If current block already has an amount, this valid date starts the NEXT transaction block
                    if (hasAmount && currentBlock.isNotBlank()) {
                        blocks.add(currentBlock.toString())
                        currentBlock = StringBuilder()
                    }
                } else if (!amountRegex.containsMatchIn(currentBlock)) {
                    // Date without time and no transaction in progress -> header date, skip
                    continue
                }
            }

            if (currentBlock.isNotEmpty()) {
                currentBlock.append(" ")
            }
            currentBlock.append(line)
        }
        if (currentBlock.isNotBlank()) {
            blocks.add(currentBlock.toString())
        }

        for (block in blocks) {
            val tx = parseSingleBlock(block, existingTransactions)
            if (tx != null) {
                parsedList.add(tx)
            }
        }

        // Sort by timestamp descending
        parsedList.sortByDescending { it.timestamp }

        return buildResult(parsedList)
    }

    private fun parseSingleBlock(
        block: String,
        existingTransactions: List<TransactionEntity>
    ): ParsedTransaction? {
        // Find operation date & time
        val dateMatches = dateRegex.findAll(block).toList()
        if (dateMatches.isEmpty()) return null

        val firstDateStr = dateMatches[0].value
        // Find time near the date
        val timeMatches = timeRegex.findAll(block).toList()
        val timeStr = if (timeMatches.isNotEmpty()) timeMatches[0].value else "12:00"

        val rawDateStr = "$firstDateStr $timeStr"
        val timestamp = parseTimestamp(rawDateStr)

        // Find amounts
        val amountMatches = amountRegex.findAll(block).toList()
        if (amountMatches.isEmpty()) return null

        // In T-Bank, there are typically 2 amounts: [0] amount in op currency, [1] amount in card currency (RUB)
        // If there's 1, we use it; if there are 2, we use the second (which is in card currency).
        val targetAmountMatch = if (amountMatches.size >= 2) amountMatches[1] else amountMatches[0]

        val sign = targetAmountMatch.groupValues[1]
        val numStr = targetAmountMatch.groupValues[2].replace(" ", "").replace("\u00A0", "").replace(',', '.')
        val amountVal = numStr.toDoubleOrNull() ?: return null
        if (amountVal <= 0.0) return null

        val isIncome = sign == "+"
        val type = if (isIncome) TransactionType.INCOME else TransactionType.EXPENSE

        // Find currency
        val currencyStr = targetAmountMatch.groupValues[3].uppercase().trim()
        val currency = when {
            currencyStr.contains("USD") || currencyStr.contains("$") -> "USD"
            currencyStr.contains("EUR") || currencyStr.contains("€") -> "EUR"
            else -> "RUB"
        }

        // Description is between the last amount and the card number
        val endOfAmounts = targetAmountMatch.range.last + 1
        val afterAmounts = if (endOfAmounts < block.length) block.substring(endOfAmounts).trim() else ""

        // Find card number at the end: 4 digits or dash
        val cardMatch = Regex("""(\d{4}|—|-)\s*$""").find(afterAmounts)
        val cardLast4 = cardMatch?.groupValues?.get(1) ?: ""
        val rawDesc = if (cardMatch != null) {
            afterAmounts.substring(0, cardMatch.range.first).trim()
        } else {
            afterAmounts.trim()
        }

        val description = rawDesc.replace(Regex("""\s+"""), " ")
        if (description.isBlank() && !isIncome) return null

        val categoryId = CategoryGuesser.guess(description, isIncome)
        val title = cleanTitle(description)

        val isDup = isDuplicate(timestamp, amountVal, isIncome, title, existingTransactions)

        return ParsedTransaction(
            title = title,
            amount = amountVal,
            currency = currency,
            type = type,
            categoryId = categoryId,
            timestamp = timestamp,
            rawDateString = rawDateStr,
            description = description,
            cardLast4 = cardLast4,
            isDuplicate = isDup,
            isSelected = !isDup
        )
    }

    private fun cleanTitle(rawDesc: String): String {
        var clean = rawDesc.trim()
        // Strip common prefixes
        val prefixes = listOf("Оплата в ", "Оплата ", "Перевод с ", "Перевод в ")
        for (p in prefixes) {
            if (clean.startsWith(p, ignoreCase = true)) {
                clean = clean.substring(p.length).trim()
                break
            }
        }
        if (clean.contains("__")) {
            clean = clean.replace(Regex("""__.*"""), "")
        }
        return clean.ifBlank { "Операция" }
    }

    private fun parseTimestamp(rawDateStr: String): Long {
        val clean = rawDateStr.trim().replace("\u00A0", " ")
        val parts = clean.split(" ")
        val datePart = parts.getOrNull(0) ?: ""
        val timePart = parts.getOrNull(1) ?: "12:00"

        // Format 1: DD.MM.YYYY
        if (datePart.contains(".")) {
            val dmy = datePart.split(".")
            if (dmy.size == 3) {
                val d = dmy[0].toIntOrNull()
                val m = dmy[1].toIntOrNull()
                val y = dmy[2].toIntOrNull()
                if (d != null && m != null && y != null) {
                    val hm = timePart.split(":")
                    val h = hm.getOrNull(0)?.toIntOrNull() ?: 12
                    val min = hm.getOrNull(1)?.toIntOrNull() ?: 0
                    val cal = Calendar.getInstance().apply {
                        set(Calendar.YEAR, y)
                        set(Calendar.MONTH, m - 1)
                        set(Calendar.DAY_OF_MONTH, d)
                        set(Calendar.HOUR_OF_DAY, h)
                        set(Calendar.MINUTE, min)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    return cal.timeInMillis
                }
            }
        }

        // Format 2: YYYY-MM-DD
        if (datePart.contains("-")) {
            val ymd = datePart.split("-")
            if (ymd.size == 3) {
                val y = ymd[0].toIntOrNull()
                val m = ymd[1].toIntOrNull()
                val d = ymd[2].toIntOrNull()
                if (d != null && m != null && y != null) {
                    val hm = timePart.split(":")
                    val h = hm.getOrNull(0)?.toIntOrNull() ?: 12
                    val min = hm.getOrNull(1)?.toIntOrNull() ?: 0
                    val cal = Calendar.getInstance().apply {
                        set(Calendar.YEAR, y)
                        set(Calendar.MONTH, m - 1)
                        set(Calendar.DAY_OF_MONTH, d)
                        set(Calendar.HOUR_OF_DAY, h)
                        set(Calendar.MINUTE, min)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    return cal.timeInMillis
                }
            }
        }

        return System.currentTimeMillis()
    }

    private fun isDuplicate(
        timestamp: Long,
        amount: Double,
        isIncome: Boolean,
        title: String,
        existing: List<TransactionEntity>
    ): Boolean {
        val typeId = if (isIncome) TransactionType.INCOME.id else TransactionType.EXPENSE.id
        // Check if within 2 minutes with matching amount and type
        return existing.any { tx ->
            tx.type == typeId &&
                abs(tx.amount - amount) < 0.01 &&
                abs(tx.timestamp - timestamp) <= 120_000L
        }
    }

    private fun isHeaderOrFooterLine(line: String): Boolean {
        val l = line.lowercase()
        return l.contains("акционерное общество") ||
            l.contains("справка о движении средств") ||
            l.contains("движение средств за период") ||
            l.contains("дата и время операции") ||
            l.contains("дата списания") ||
            l.contains("сумма в валюте операции") ||
            l.contains("сумма операции в валюте карты") ||
            l.contains("описание операции") ||
            l.contains("номер карты") ||
            l.contains("лицензия банка россии") ||
            l.contains("бик 044525974") ||
            l.contains("инн 7710140679") ||
            l.contains("кпп 771301001") ||
            l.contains("к/с 30101810145250000974") ||
            l.contains("о продукте") ||
            l.contains("номер договора") ||
            l.contains("номер лицевого") ||
            l.contains("адрес места жительства") ||
            l.contains("исх. №") ||
            l.contains("исх. no") ||
            l.contains("тел.:") ||
            l.contains("тел:") ||
            l.contains("tbank.ru") ||
            l.contains("дата заключения договора") ||
            l.contains("руководитель управления") ||
            l.contains("пополнения:") ||
            l.contains("расходы:") ||
            l.contains("с уважением") ||
            (line.length <= 4 && line.all { it.isDigit() }) // standalone page numbers
    }

    private fun mapCsvCategory(csvCategory: String, isIncome: Boolean): String {
        val catLower = csvCategory.lowercase()
        return when {
            isIncome -> "salary"
            catLower.contains("супермаркет") || catLower.contains("продукт") -> "groceries"
            catLower.contains("ресторан") || catLower.contains("фастфуд") || catLower.contains("кафе") -> "dining"
            catLower.contains("транспорт") || catLower.contains("такси") || catLower.contains("авто") -> "transport"
            catLower.contains("здоров") || catLower.contains("аптек") || catLower.contains("медицин") -> "health"
            catLower.contains("развлечен") || catLower.contains("кино") || catLower.contains("игр") -> "entertainment"
            catLower.contains("одежд") || catLower.contains("покупк") || catLower.contains("дом") -> "shopping"
            catLower.contains("жкх") || catLower.contains("связь") -> "housing"
            catLower.contains("спорт") || catLower.contains("фитнес") -> "sport"
            catLower.contains("перевод") -> "investments"
            else -> "other_expense"
        }
    }

    private fun buildResult(list: List<ParsedTransaction>): StatementParseResult {
        val totalCount = list.size
        val duplicateCount = list.count { it.isDuplicate }
        val newCount = totalCount - duplicateCount
        val totalIncome = list.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val totalExpense = list.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val startDate = list.minOfOrNull { it.timestamp }
        val endDate = list.maxOfOrNull { it.timestamp }

        return StatementParseResult(
            transactions = list,
            totalCount = totalCount,
            newCount = newCount,
            duplicateCount = duplicateCount,
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            startDate = startDate,
            endDate = endDate
        )
    }
}

object CategoryGuesser {

    fun guess(description: String, isIncome: Boolean): String {
        val d = description.uppercase()

        if (isIncome) {
            return when {
                d.contains("ЗАРПЛАТ") || d.contains("АВАНС") || d.contains("ПРЕМИ") -> "salary"
                d.contains("КЭШБЭК") || d.contains("ВОЗВРАТ") -> "other_income"
                d.contains("ИНВЕСТ") || d.contains("ДИВИДЕНД") || d.contains("ПРОЦЕНТ") -> "investments"
                d.contains("ПОПОЛНЕНИЕ") || d.contains("ПЕРЕВОД СЕБЕ") || d.contains("ВНУТРИБАНКОВСКИЙ") -> "salary"
                else -> "other_income"
            }
        }

        return when {
            // Groceries / Супермаркеты
            listOf(
                "PEREKRESTOK", "ПЕРЕКРЕСТОК", "PYATEROCHKA", "ПЯТЕРОЧКА", "DIXY", "ДИКСИ",
                "ATAK", "АТАК", "AUCHAN", "АШАН", "LENTA", "ЛЕНТА", "VKUSVILL", "ВКУСВИЛЛ",
                "VERNYJ", "ВЕРНЫЙ", "MAGNIT", "МАГНИТ", "SPAR", "СПАР", "BAKALEYA", "БАКАЛЕЯ",
                "SVETOFOR", "СВЕТОФОР", "MARKET", "PRODUKTY", "ПРОДУКТЫ", "KCO"
            ).any { d.contains(it) } -> "groceries"

            // Dining / Кафе, рестораны, фастфуд
            listOf(
                "VKUSNO", "ВКУСНО", "DODO", "ДОДО", "ROSTICS", "РОСТИКС", "ТЕРЕМОК", "TEREMOK",
                "БУРГЕР", "BURGER", "ZA OBE SHHEKI", "ЗА ОБЕ ЩЕКИ", "KHALYAL", "ХАЛЯЛЬ",
                "SUSHI", "СУШИ", "PIZZA", "ПИЦЦА", "PEKARNYA", "ПЕКАРНЯ", "COFFEE", "КОФЕ",
                "APPETIT", "АППЕТИТ", "QSR", "TAKO", "ТАКО", "HLEB", "ХЛЕБ", "YAKITORIYA",
                "ЯКИТОРИЯ", "RESTAURANT", "РЕСТОРАН", "CAFE", "КАФЕ", "SHOKO", "ШОКО",
                "SHAURMA", "ШАУРМА", "CHIBO", "FOOD", "EDA", "ЕДА", "DELUXE", "PHO", "TOM YUM", "YUM"
            ).any { d.contains(it) } -> "dining"

            // Transport / Транспорт, такси, метро, билеты
            listOf(
                "MOS.TRANSPORT", "ТРАНСПОРТ", "STRELKA", "СТРЕЛКА", "ТРОЙКА", "TROIKA",
                "YANDEX*GO", "YANDEX*4121*GO", "UBER", "ТАКСИ", "TAXI", "METRO", "МЕТРО",
                "ПАРКОВКА", "PARKING", "AZS", "АЗС", "БЕНЗИН", "LUKOIL", "GAZPROM",
                "SHIRAK AVIA", "АВИАСЕЙЛС", "AVIASALES", "ЮТЭЙР", "UTAIR", "РЖД", "RZHD",
                "VENDING METRO", "БИЛЕТ ЧЕРЕЗ СБП"
            ).any { d.contains(it) } -> "transport"

            // Health / Аптеки, медицина
            listOf(
                "APTEKA", "АПТЕКА", "36,6", "36.6", "GORZDRAV", "ГОРЗДРАВ", "TRIKA", "ТРИКА",
                "DENT", "СТОМАТОЛОГ", "ВРАЧ", "DOCTOR", "HOSPITAL", "БОЛЬНИЦА", "MEDICINE",
                "OPTIC", "ОПТИКА"
            ).any { d.contains(it) } -> "health"

            // Gaming & Online Entertainment / Развлечения
            listOf(
                "LISSKINS", "SLVX", "SBGG", "GGS", "CASEHUNT", "UPGRADER", "STEAM", "FUNPAY",
                "PLUMID", "GGSEL", "GGFORT", "PLAYPASS", "FLICKERDROP", "SKINOUT", "IGROMAGAZ",
                "CUCUMBERCHIK", "Z2S", "DARKAWE", "ONLIPAY", "MINECRAFT", "TIMEWEB",
                "КИНО", "CINEMA", "KINO", "THEATRE", "ТЕАТР", "CONCERT", "КОНЦЕРТ",
                "KASSIR", "КАССИР", "INTIKETS", "TICKETS", "VKMUSIC", "YANDEX*PLUS"
            ).any { d.contains(it) } -> "entertainment"

            // Shopping / Покупки, одежда, маркетплейсы
            listOf(
                "COLINS", "NEW YORKER", "ZARINA", "SPORTMASTER", "СПОРТМАСТЕР", "FIXPRICE",
                "ФИКС ПРАЙС", "GALAMART", "ГАЛАМАРТ", "SUNLIGHT", "САНЛАЙТ", "OZON", "ОЗОН",
                "WILDBERRIES", "WB", "ЦВЕТОЧНЫЙ", "FLOWERS", "ЦВЕТЫ", "SHOPPING", "ZARA",
                "H&M", "CLOTHES", "DUTY FREE", "KOSINO PARK", "LG", "SHOP"
            ).any { d.contains(it) } -> "shopping"

            // Housing / Жилье, ЖКУ, связь
            listOf(
                "ЖКХ", "ЖКУ", "КОММУНАЛ", "RENT", "АРЕНДА", "INTERNET", "ИНТЕРНЕТ", "MTS", "МТС",
                "BEELINE", "БИЛАЙН", "MEGAFON", "МЕГАФОН", "TELE2", "ТЕЛЕ2", "T-MOBI",
                "РОСТЕЛЕКОМ", "ROSTELECOM"
            ).any { d.contains(it) } -> "housing"

            // Investments / Копилка (Т-Банк Инвесткопилка: Внутренний перевод на договор)
            listOf(
                "ВНУТРЕННИЙ ПЕРЕВОД НА ДОГОВОР", "ИНВЕСТКОПИЛК", "ИНВЕСТИЦИИ", "БРОКЕР"
            ).any { d.contains(it) } -> "investments"

            // Transfers / Gifts
            listOf("ВНЕШНИЙ ПЕРЕВОД", "ПЕРЕВОД ПО НОМЕРУ").any { d.contains(it) } -> "gift"

            else -> "other_expense"
        }
    }
}
