package com.femirion.expbot.expbot.out.sheets

import com.femirion.expbot.expbot.domain.entity.Category
import com.femirion.expbot.expbot.domain.entity.CategoryType
import com.femirion.expbot.expbot.domain.entity.MoneyTransaction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.time.ZoneOffset

class GoogleSheetsTransactionReporterTest {

    @Test
    fun `maps transaction to five sheet columns`() {
        val transaction = MoneyTransaction(
            telegramMessageId = 1001,
            telegramUserId = 2002,
            chatId = 3003,
            category = Category(
                id = 1,
                code = "FOOD",
                name = "Food and groceries",
                type = CategoryType.EXPENSE,
            ),
            type = CategoryType.EXPENSE,
            amount = BigDecimal("12.50"),
            note = "milk and bread",
            occurredAt = OffsetDateTime.of(2026, 5, 23, 14, 7, 33, 456_000_000, ZoneOffset.UTC),
        )

        assertEquals(
            listOf("2026-05-23", "14:07", "Food and groceries", "12.50", "milk and bread"),
            transaction.toSheetRow(),
        )
    }
}
