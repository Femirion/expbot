package com.femirion.expbot.expbot.domain.service

import com.femirion.expbot.expbot.domain.entity.Category
import com.femirion.expbot.expbot.domain.entity.CategoryType
import com.femirion.expbot.expbot.domain.entity.Message
import com.femirion.expbot.expbot.domain.entity.MoneyTransaction
import com.femirion.expbot.expbot.out.telegram.TelegramBotClient
import com.femirion.expbot.expbot.out.telegram.TelegramButtonCategory
import com.femirion.expbot.expbot.service.CategoryService
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Logger

@Service
class MessageHandler(
    private val commandParser: BotCommandParser,
    private val categoryService: CategoryService,
    private val transactionService: MoneyTransactionService,
    private val telegramBotClient: TelegramBotClient,
) {
    private val pendingExpenses = ConcurrentHashMap<PendingExpenseKey, String>()

    fun handle(update: Message) {
        val pendingCategoryCode = pendingExpenses[PendingExpenseKey(update.chatId, update.userId)]
        if (pendingCategoryCode != null && !update.text.orEmpty().trim().startsWith("/")) {
            val response = runCatching { createPendingExpense(update, pendingCategoryCode) }
                .getOrElse { error ->
                    log.warning { "Failed to handle pending expense message ${update.messageId}: ${error.message}" }
                    "Could not save it: ${error.message ?: "unknown error"}"
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
            startExpense(update)
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

        val categoryCode = data
            ?.takeIf { it.startsWith(EXPENSE_CALLBACK_PREFIX) }
            ?.removePrefix(EXPENSE_CALLBACK_PREFIX)
            ?.trim()
            ?.uppercase()

        if (categoryCode == null) {
            telegramBotClient.sendMessage(chatId, helpText())
            return
        }

        val category = categoryService.getCategoryByCode(categoryCode)
        if (category == null || !category.isActive || category.type != CategoryType.EXPENSE) {
            telegramBotClient.sendMessage(chatId, "Expense category $categoryCode is not available")
            return
        }

        pendingExpenses[PendingExpenseKey(chatId, userId)] = category.code
        telegramBotClient.sendMessage(chatId, "Send amount and description, for example: 12.50 groceries")
    }

    private fun handleCommand(message: Message, command: BotCommand): String {
        return when (command) {
            BotCommand.Help -> helpText()
            BotCommand.ListCategories -> categoriesText()
            BotCommand.StartExpense -> {
                startExpense(message)
                ""
            }
            is BotCommand.CreateCategory -> createCategory(command)
            is BotCommand.CreateTransaction -> createTransaction(message, command)
        }
    }

    private fun startExpense(message: Message) {
        val categories = categoryService.getAll()
            .filter { it.isActive && it.type == CategoryType.EXPENSE }
            .sortedBy { it.code }
        if (categories.isEmpty()) {
            telegramBotClient.sendMessage(message.chatId, "No expense categories yet. Add one with /category expense food Food")
            return
        }

        telegramBotClient.sendMessageWithCategoryButtons(
            chatId = message.chatId,
            text = "Choose expense category",
            categories = categories.map { TelegramButtonCategory(code = it.code, name = it.name) },
        )
    }

    private fun createPendingExpense(message: Message, categoryCode: String): String {
        val parts = message.text.orEmpty().trim().split(Regex("\\s+"), limit = 2)
        val amount = parts.firstOrNull()
            ?.replace(',', '.')
            ?.toBigDecimalOrNull()
            ?: return "Send amount and description, for example: 12.50 groceries"
        if (amount <= BigDecimal.ZERO) {
            return "Amount must be greater than zero"
        }

        val command = BotCommand.CreateTransaction(
            categoryCode = categoryCode,
            amount = amount,
            note = parts.getOrNull(1)?.trim()?.takeIf { it.isNotBlank() },
            expectedType = CategoryType.EXPENSE,
        )
        val response = createTransaction(message, command)
        pendingExpenses.remove(PendingExpenseKey(message.chatId, message.userId))
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
        return "$direction saved: ${transaction.amount.toPlainString()} ${transaction.category.code}"
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

    private fun helpText(): String {
        return """
            Commands:
            /categories
            /category expense food Food
            /category income salary Salary
            /e
            /expense food 12.50 groceries
            /income salary 1000 May salary
        """.trimIndent()
    }

    companion object {
        val log: Logger = Logger.getLogger(MessageHandler::class.java.name)
        private const val EXPENSE_CALLBACK_PREFIX = "expense:"
    }
}

private data class PendingExpenseKey(
    val chatId: Long,
    val userId: Long,
)
