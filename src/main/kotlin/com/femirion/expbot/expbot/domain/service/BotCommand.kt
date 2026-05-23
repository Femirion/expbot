package com.femirion.expbot.expbot.domain.service

import com.femirion.expbot.expbot.domain.entity.CategoryType
import java.math.BigDecimal

sealed interface BotCommand {
    data object Help : BotCommand
    data object ListCategories : BotCommand
    data object StartExpense : BotCommand
    data object TodayExpenses : BotCommand
    data object MonthExpenses : BotCommand

    data class CreateCategory(
        val type: CategoryType,
        val code: String,
        val name: String,
    ) : BotCommand

    data class CreateTransaction(
        val categoryCode: String,
        val amount: BigDecimal,
        val note: String?,
        val expectedType: CategoryType,
    ) : BotCommand
}
