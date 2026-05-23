package com.femirion.expbot.expbot.domain.service

import com.femirion.expbot.expbot.domain.entity.Category
import com.femirion.expbot.expbot.domain.entity.CategoryType
import com.femirion.expbot.expbot.domain.entity.Message
import com.femirion.expbot.expbot.domain.entity.MoneyTransaction
import com.femirion.expbot.expbot.out.telegram.TelegramBotClient
import com.femirion.expbot.expbot.service.CategoryService
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.logging.Logger

@Service
class MessageHandler(
    private val commandParser: BotCommandParser,
    private val categoryService: CategoryService,
    private val transactionService: MoneyTransactionService,
    private val telegramBotClient: TelegramBotClient,
) {

    fun handle(update: Message) {
        val command = commandParser.parse(update.text)
        if (command == null) {
            telegramBotClient.sendMessage(update.chatId, helpText())
            return
        }

        val response = runCatching { handleCommand(update, command) }
            .getOrElse { error ->
                log.warning { "Failed to handle message ${update.messageId}: ${error.message}" }
                "Could not save it: ${error.message ?: "unknown error"}"
            }
        telegramBotClient.sendMessage(update.chatId, response)
    }

    private fun handleCommand(message: Message, command: BotCommand): String {
        return when (command) {
            BotCommand.Help -> helpText()
            BotCommand.ListCategories -> categoriesText()
            is BotCommand.CreateCategory -> createCategory(command)
            is BotCommand.CreateTransaction -> createTransaction(message, command)
        }
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
            /expense food 12.50 groceries
            /income salary 1000 May salary
        """.trimIndent()
    }

    companion object {
        val log: Logger = Logger.getLogger(MessageHandler::class.java.name)
    }
}
