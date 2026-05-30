package com.femirion.expbot.expbot.domain.service

import com.femirion.expbot.expbot.domain.entity.CategoryType
import java.math.BigDecimal

sealed interface BotCommand {
    data object Help : BotCommand
    data object ListCategories : BotCommand
    data object StartExpense : BotCommand
    data object StartIncome : BotCommand
    data object StartLimit : BotCommand
    data object TodayExpenses : BotCommand
    data object WeekExpenses : BotCommand
    data object MonthExpenses : BotCommand
    data object Balance : BotCommand

    data class CorrectBalance(
        val targetBalance: BigDecimal,
    ) : BotCommand

    data class CreateExchangeWithdrawal(
        val amount: BigDecimal,
        val note: String?,
    ) : BotCommand

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
