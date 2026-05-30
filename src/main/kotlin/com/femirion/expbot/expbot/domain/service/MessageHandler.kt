package com.femirion.expbot.expbot.domain.service

import com.femirion.expbot.expbot.domain.entity.Category
import com.femirion.expbot.expbot.domain.entity.CategoryType
import com.femirion.expbot.expbot.domain.entity.LimitPeriod
import com.femirion.expbot.expbot.domain.entity.Message
import com.femirion.expbot.expbot.domain.entity.MoneyTransaction
import com.femirion.expbot.expbot.out.telegram.TelegramBotClient
import com.femirion.expbot.expbot.out.telegram.TelegramButtonCategory
import com.femirion.expbot.expbot.service.CategoryService
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Instant
import java.time.OffsetDateTime
import java.time.YearMonth
import java.time.ZoneOffset
import java.time.temporal.TemporalAdjusters
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Logger

@Service
class MessageHandler(
    private val commandParser: BotCommandParser,
    private val categoryService: CategoryService,
    private val categoryLimitService: CategoryLimitService,
    private val transactionService: MoneyTransactionService,
    private val telegramBotClient: TelegramBotClient,
) {
    private val pendingTransactions = ConcurrentHashMap<PendingTransactionKey, PendingTransaction>()
    private val pendingLimits = ConcurrentHashMap<PendingTransactionKey, PendingLimit>()

    fun handle(update: Message) {
        val pendingKey = PendingTransactionKey(update.chatId, update.userId)
        val pendingTransaction = pendingTransactions[pendingKey]
        if (pendingTransaction != null && !update.text.orEmpty().trim().startsWith("/")) {
            val response = runCatching { createPendingTransaction(update, pendingTransaction) }
                .getOrElse { error ->
                    log.warning { "Failed to handle pending transaction message ${update.messageId}: ${error.message}" }
                    "Could not save it: ${error.message ?: "unknown error"}"
                }
            telegramBotClient.sendMessage(update.chatId, response)
            return
        }

        val pendingLimit = pendingLimits[pendingKey]
        if (pendingLimit != null && !update.text.orEmpty().trim().startsWith("/")) {
            val response = runCatching { createPendingLimit(update, pendingLimit) }
                .getOrElse { error ->
                    log.warning { "Failed to handle pending limit message ${update.messageId}: ${error.message}" }
                    "Could not save limit: ${error.message ?: "unknown error"}"
                }
            telegramBotClient.sendMessage(update.chatId, response)
            return
        }

        val command = commandParser.parse(update.text)
        if (command == null) {
            telegramBotClient.sendMessage(update.chatId, helpText())
            return
        }
        if (command == BotCommand.StartExpense) {
            startCategorySelection(update, CategoryType.EXPENSE)
            return
        }
        if (command == BotCommand.StartIncome) {
            startCategorySelection(update, CategoryType.INCOME)
            return
        }
        if (command == BotCommand.StartLimit) {
            startLimitCategorySelection(update)
            return
        }

        val response = runCatching { handleCommand(update, command) }
            .getOrElse { error ->
                log.warning { "Failed to handle message ${update.messageId}: ${error.message}" }
                "Could not save it: ${error.message ?: "unknown error"}"
            }
        if (response.isNotBlank()) {
            telegramBotClient.sendMessage(update.chatId, response)
        }
    }

    fun handleCallbackQuery(callbackQueryId: String, userId: Long, chatId: Long?, data: String?) {
        telegramBotClient.answerCallbackQuery(callbackQueryId)
        if (chatId == null) {
            return
        }

        val limitCategoryCode = data
            ?.takeIf { it.startsWith("$LIMIT_CALLBACK_PREFIX:") }
            ?.substringAfter(":", missingDelimiterValue = "")
            ?.trim()
            ?.uppercase()
            ?.takeIf { it.isNotBlank() }
        if (limitCategoryCode != null) {
            val category = categoryService.getCategoryByCode(limitCategoryCode)
            if (category == null || !category.isActive || category.type != CategoryType.EXPENSE) {
                telegramBotClient.sendMessage(chatId, "Expense category $limitCategoryCode is not available")
                return
            }
            pendingTransactions.remove(PendingTransactionKey(chatId, userId))
            pendingLimits[PendingTransactionKey(chatId, userId)] = PendingLimit(category.code)
            telegramBotClient.sendMessage(chatId, "Send limit amount and period, for example: 1000 D or 30000 M")
            return
        }

        val categoryType = data?.callbackCategoryType()
        val categoryCode = data
            ?.substringAfter(":", missingDelimiterValue = "")
            ?.trim()
            ?.uppercase()
            ?.takeIf { it.isNotBlank() }

        if (categoryType == null || categoryCode == null) {
            telegramBotClient.sendMessage(chatId, helpText())
            return
        }

        val category = categoryService.getCategoryByCode(categoryCode)
        if (category == null || !category.isActive || category.type != categoryType) {
            telegramBotClient.sendMessage(chatId, "${categoryType.label()} category $categoryCode is not available")
            return
        }

        pendingTransactions[PendingTransactionKey(chatId, userId)] = PendingTransaction(category.code, categoryType)
        pendingLimits.remove(PendingTransactionKey(chatId, userId))
        telegramBotClient.sendMessage(chatId, "Send amount and description, for example: 12.50 groceries")
    }

    private fun handleCommand(message: Message, command: BotCommand): String {
        return when (command) {
            BotCommand.Help -> helpText()
            BotCommand.ListCategories -> categoriesText()
            BotCommand.TodayExpenses -> todayExpensesText()
            BotCommand.WeekExpenses -> weekExpensesText()
            BotCommand.MonthExpenses -> monthExpensesText()
            BotCommand.Balance -> balanceText(message.chatId)
            BotCommand.StartLimit -> {
                startLimitCategorySelection(message)
                ""
            }
            is BotCommand.CorrectBalance -> correctBalance(message, command)
            is BotCommand.CreateExchangeWithdrawal -> createExchangeWithdrawal(message, command)
            BotCommand.StartExpense -> {
                startCategorySelection(message, CategoryType.EXPENSE)
                ""
            }
            BotCommand.StartIncome -> {
                startCategorySelection(message, CategoryType.INCOME)
                ""
            }
            is BotCommand.CreateCategory -> createCategory(command)
            is BotCommand.CreateTransaction -> createTransaction(message, command)
        }
    }

    private fun startCategorySelection(message: Message, type: CategoryType) {
        pendingLimits.remove(PendingTransactionKey(message.chatId, message.userId))
        val categories = categoryService.getAll()
            .filter { it.isActive && it.type == type }
            .sortedBy { it.code }
        if (categories.isEmpty()) {
            telegramBotClient.sendMessage(message.chatId, "No ${type.name.lowercase()} categories yet. Add one with /category ${type.name.lowercase()} code Name")
            return
        }

        telegramBotClient.sendMessageWithCategoryButtons(
            chatId = message.chatId,
            text = "Choose ${type.name.lowercase()} category",
            callbackPrefix = type.callbackPrefix(),
            categories = categories.map { TelegramButtonCategory(code = it.code, name = it.name) },
        )
    }

    private fun startLimitCategorySelection(message: Message) {
        pendingTransactions.remove(PendingTransactionKey(message.chatId, message.userId))
        val categories = categoryService.getAll()
            .filter { it.isActive && it.type == CategoryType.EXPENSE }
            .sortedBy { it.code }
        if (categories.isEmpty()) {
            telegramBotClient.sendMessage(message.chatId, "No expense categories yet. Add one with /category expense code Name")
            return
        }

        telegramBotClient.sendMessageWithCategoryButtons(
            chatId = message.chatId,
            text = "Choose expense category for limit",
            callbackPrefix = LIMIT_CALLBACK_PREFIX,
            categories = categories.map { TelegramButtonCategory(code = it.code, name = it.name) },
        )
    }

    private fun createPendingTransaction(message: Message, pendingTransaction: PendingTransaction): String {
        val parts = message.text.orEmpty().trim().split(Regex("\\s+"), limit = 2)
        val amount = parts.firstOrNull()
            ?.replace(',', '.')
            ?.toBigDecimalOrNull()
            ?: return "Send amount and description, for example: 12.50 groceries"
        if (amount <= BigDecimal.ZERO) {
            return "Amount must be greater than zero"
        }

        val command = BotCommand.CreateTransaction(
            categoryCode = pendingTransaction.categoryCode,
            amount = amount,
            note = parts.getOrNull(1)?.trim()?.takeIf { it.isNotBlank() },
            expectedType = pendingTransaction.type,
        )
        val response = createTransaction(message, command)
        pendingTransactions.remove(PendingTransactionKey(message.chatId, message.userId))
        return response
    }

    private fun createCategory(command: BotCommand.CreateCategory): String {
        val category = categoryService.createCategory(
            Category(
                code = command.code,
                name = command.name,
                type = command.type,
            )
        )
        return "Category saved: ${category.code} (${category.type})"
    }

    private fun createPendingLimit(message: Message, pendingLimit: PendingLimit): String {
        val parts = message.text.orEmpty().trim().split(Regex("\\s+"))
        if (parts.size != 2) {
            return "Send limit amount and period, for example: 1000 D or 30000 M"
        }
        val amount = parts[0].replace(',', '.').toBigDecimalOrNull()
            ?: return "Limit amount must be a number"
        if (amount <= BigDecimal.ZERO) {
            return "Limit amount must be greater than zero"
        }
        val period = parts[1].limitPeriod()
            ?: return "Limit period must be D for day or M for month"
        val category = categoryService.getCategoryByCode(pendingLimit.categoryCode)
            ?: return "Category ${pendingLimit.categoryCode} does not exist"
        if (!category.isActive || category.type != CategoryType.EXPENSE) {
            return "Expense category ${category.code} is not available"
        }

        val limit = categoryLimitService.saveLimit(category, amount, period)
        pendingLimits.remove(PendingTransactionKey(message.chatId, message.userId))
        return "${period.label()} limit saved for ${limit.categoryCode}: ${limit.amount.toPlainString()}"
    }

    private fun createTransaction(message: Message, command: BotCommand.CreateTransaction): String {
        val category = categoryService.getCategoryByCode(command.categoryCode)
            ?: return "Category ${command.categoryCode} does not exist. Add it with /category ${command.expectedType.name.lowercase()} ${command.categoryCode} Name"
        if (!category.isActive) {
            return "Category ${category.code} is inactive"
        }
        if (category.type != command.expectedType) {
            return "Category ${category.code} is ${category.type.name.lowercase()}, not ${command.expectedType.name.lowercase()}"
        }

        val transaction = transactionService.create(
            MoneyTransaction(
                telegramMessageId = message.messageId,
                telegramUserId = message.userId,
                chatId = message.chatId,
                category = category,
                type = category.type,
                amount = command.amount,
                note = command.note,
                occurredAt = OffsetDateTime.ofInstant(Instant.ofEpochSecond(message.date), ZoneOffset.UTC),
            )
        )
        val direction = if (transaction.type == CategoryType.EXPENSE) "Expense" else "Income"
        val transactionCategory = transaction.category ?: return "$direction saved: ${transaction.amount.toPlainString()}"
        val response = "$direction saved: ${transaction.amount.toPlainString()} ${transactionCategory.code}"
        if (transaction.type != CategoryType.EXPENSE) {
            return response
        }
        val limitMessages = categoryLimitService.exceededLimits(transactionCategory, transaction.occurredAt)
            .map { limit ->
                "${limit.period.label()} limit exceeded for ${limit.categoryCode}: ${limit.total.toPlainString()} / ${limit.amount.toPlainString()}"
            }
        return (listOf(response) + limitMessages).joinToString("\n")
    }

    private fun createExchangeWithdrawal(message: Message, command: BotCommand.CreateExchangeWithdrawal): String {
        val transaction = transactionService.createExchangeWithdrawal(
            telegramMessageId = message.messageId,
            telegramUserId = message.userId,
            chatId = message.chatId,
            amount = command.amount,
            note = command.note,
            occurredAt = OffsetDateTime.ofInstant(Instant.ofEpochSecond(message.date), ZoneOffset.UTC),
        )
        return "Exchange saved: ${transaction.amount.toPlainString()}"
    }

    private fun categoriesText(): String {
        val categories = categoryService.getAll()
            .filter { it.isActive }
            .sortedWith(compareBy<Category> { it.type.name }.thenBy { it.code })
        if (categories.isEmpty()) {
            return "No categories yet"
        }
        return categories.joinToString(separator = "\n") { "${it.code} - ${it.name} (${it.type.name.lowercase()})" }
    }

    private fun todayExpensesText(): String {
        val today = LocalDate.now(ZoneOffset.UTC)
        val from = today.atStartOfDay().atOffset(ZoneOffset.UTC)
        val to = today.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC)
        return expensesSummaryText("Today expenses", from, to)
    }

    private fun weekExpensesText(): String {
        val today = LocalDate.now(ZoneOffset.UTC)
        val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val from = weekStart.atStartOfDay().atOffset(ZoneOffset.UTC)
        val to = weekStart.plusWeeks(1).atStartOfDay().atOffset(ZoneOffset.UTC)
        return expensesSummaryText("Week expenses", from, to)
    }

    private fun monthExpensesText(): String {
        val month = YearMonth.now(ZoneOffset.UTC)
        val from = month.atDay(1).atStartOfDay().atOffset(ZoneOffset.UTC)
        val to = month.plusMonths(1).atDay(1).atStartOfDay().atOffset(ZoneOffset.UTC)
        return expensesSummaryText("Month expenses", from, to)
    }

    private fun expensesSummaryText(
        title: String,
        from: OffsetDateTime,
        to: OffsetDateTime,
    ): String {
        val summaries = transactionService.sumExpensesByCategory(from, to)
        if (summaries.isEmpty()) {
            return "$title:\nNo expenses"
        }

        val total = summaries.fold(BigDecimal.ZERO) { sum, summary -> sum + summary.total }
        val lines = summaries.map { "${it.categoryCode} (${it.categoryName}): ${it.total.toPlainString()}" }
        return (listOf("$title:") + lines + "Total: ${total.toPlainString()}").joinToString("\n")
    }

    private fun balanceText(chatId: Long): String {
        return "Balance: ${transactionService.balance(chatId).toPlainString()}"
    }

    private fun correctBalance(message: Message, command: BotCommand.CorrectBalance): String {
        val balance = transactionService.correctBalance(
            telegramMessageId = message.messageId,
            telegramUserId = message.userId,
            chatId = message.chatId,
            targetBalance = command.targetBalance,
            occurredAt = OffsetDateTime.ofInstant(Instant.ofEpochSecond(message.date), ZoneOffset.UTC),
        )
        return "Balance corrected: ${balance.toPlainString()}"
    }

    private fun helpText(): String {
        return """
            Commands:
            /categories
            /today
            /week
            /month
            /b
            /corr 800
            /ex 20000
            /l
            /category expense food Food
            /category income salary Salary
            /e
            /i
            /expense food 12.50 groceries
            /income salary 1000 May salary
        """.trimIndent()
    }

    companion object {
        val log: Logger = Logger.getLogger(MessageHandler::class.java.name)
    }
}

private data class PendingTransactionKey(
    val chatId: Long,
    val userId: Long,
)

private data class PendingTransaction(
    val categoryCode: String,
    val type: CategoryType,
)

private data class PendingLimit(
    val categoryCode: String,
)

private fun CategoryType.callbackPrefix(): String {
    return name.lowercase()
}

private fun LimitPeriod.label(): String {
    return when (this) {
        LimitPeriod.DAY -> "Day"
        LimitPeriod.MONTH -> "Month"
    }
}

private fun String.limitPeriod(): LimitPeriod? {
    return when (trim().uppercase()) {
        "D" -> LimitPeriod.DAY
        "M" -> LimitPeriod.MONTH
        else -> null
    }
}

private fun CategoryType.label(): String {
    return name.lowercase().replaceFirstChar { it.uppercase() }
}

private fun String.callbackCategoryType(): CategoryType? {
    return when {
        startsWith("${CategoryType.EXPENSE.callbackPrefix()}:") -> CategoryType.EXPENSE
        startsWith("${CategoryType.INCOME.callbackPrefix()}:") -> CategoryType.INCOME
        else -> null
    }
}

private const val LIMIT_CALLBACK_PREFIX = "limit"
