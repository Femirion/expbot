package com.femirion.expbot.expbot.domain.service

import com.femirion.expbot.expbot.domain.entity.CategoryType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class BotCommandParserTest {
    private val parser = BotCommandParser()

    @Test
    fun `parses expense command with note`() {
        val command = parser.parse("/expense food 12.50 groceries") as BotCommand.CreateTransaction

        assertEquals("FOOD", command.categoryCode)
        assertEquals(0, BigDecimal("12.50").compareTo(command.amount))
        assertEquals("groceries", command.note)
        assertEquals(CategoryType.EXPENSE, command.expectedType)
    }

    @Test
    fun `parses bare expense command as category selection start`() {
        assertEquals(BotCommand.StartExpense, parser.parse("/e"))
    }

    @Test
    fun `parses expense summary commands`() {
        assertEquals(BotCommand.TodayExpenses, parser.parse("/today"))
        assertEquals(BotCommand.TodayExpenses, parser.parse("/t"))
        assertEquals(BotCommand.MonthExpenses, parser.parse("/month"))
        assertEquals(BotCommand.MonthExpenses, parser.parse("/m"))
        assertEquals(BotCommand.Balance, parser.parse("/balance"))
        assertEquals(BotCommand.Balance, parser.parse("/b"))
    }

    @Test
    fun `parses income command with comma decimal separator`() {
        val command = parser.parse("/i salary 1000,25 May salary") as BotCommand.CreateTransaction

        assertEquals("SALARY", command.categoryCode)
        assertEquals(0, BigDecimal("1000.25").compareTo(command.amount))
        assertEquals("May salary", command.note)
        assertEquals(CategoryType.INCOME, command.expectedType)
    }

    @Test
    fun `parses category command`() {
        val command = parser.parse("/c income bonus Bonus money") as BotCommand.CreateCategory

        assertEquals(CategoryType.INCOME, command.type)
        assertEquals("BONUS", command.code)
        assertEquals("Bonus money", command.name)
    }

    @Test
    fun `returns null for invalid transaction amount`() {
        assertNull(parser.parse("/expense food nope"))
        assertNull(parser.parse("/e food nope"))
        assertNull(parser.parse("/expense food -1"))
    }
}
