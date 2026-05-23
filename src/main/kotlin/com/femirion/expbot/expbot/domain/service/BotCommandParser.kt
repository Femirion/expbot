package com.femirion.expbot.expbot.domain.service

import com.femirion.expbot.expbot.domain.entity.CategoryType
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class BotCommandParser {

    fun parse(text: String?): BotCommand? {
        val normalized = text?.trim().orEmpty()
        if (normalized.isBlank()) {
            return null
        }

        val parts = normalized.split(Regex("\\s+"), limit = 4)
        return when (parts[0].lowercase().substringBefore("@")) {
            "/start", "/help" -> BotCommand.Help
            "/categories", "/cats", "/c" -> BotCommand.ListCategories
            "/today", "/t" -> BotCommand.TodayExpenses
            "/month", "/m" -> BotCommand.MonthExpenses
            "/category", "/cat" -> parseCategory(parts)
            "/expense", "/exp", "/e" -> {
                if (parts.size == 1) BotCommand.StartExpense else parseTransaction(parts, CategoryType.EXPENSE)
            }
            "/income", "/inc", "/i" -> parseTransaction(parts, CategoryType.INCOME)
            else -> null
        }
    }

    private fun parseCategory(parts: List<String>): BotCommand.CreateCategory? {
        if (parts.size < 4) {
            return null
        }
        val type = when (parts[1].lowercase()) {
            "expense", "exp" -> CategoryType.EXPENSE
            "income", "inc" -> CategoryType.INCOME
            else -> return null
        }
        return BotCommand.CreateCategory(
            type = type,
            code = parts[2].trim().uppercase(),
            name = parts[3].trim(),
        )
    }

    private fun parseTransaction(parts: List<String>, type: CategoryType): BotCommand.CreateTransaction? {
        if (parts.size < 3) {
            return null
        }
        val amount = parts[2].replace(',', '.').toBigDecimalOrNull() ?: return null
        if (amount <= BigDecimal.ZERO) {
            return null
        }
        return BotCommand.CreateTransaction(
            categoryCode = parts[1].trim().uppercase(),
            amount = amount,
            note = parts.getOrNull(3)?.trim()?.takeIf { it.isNotBlank() },
            expectedType = type,
        )
    }
}
