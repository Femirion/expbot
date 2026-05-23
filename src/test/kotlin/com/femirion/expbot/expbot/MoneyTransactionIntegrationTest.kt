package com.femirion.expbot.expbot

import com.femirion.expbot.expbot.domain.entity.Category
import com.femirion.expbot.expbot.domain.entity.CategoryType
import com.femirion.expbot.expbot.domain.entity.MoneyTransaction
import com.femirion.expbot.expbot.domain.service.DuplicateTransactionException
import com.femirion.expbot.expbot.domain.service.MoneyTransactionService
import com.femirion.expbot.expbot.`in`.repository.MoneyTransactionRepository
import com.femirion.expbot.expbot.service.CategoryService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.OffsetDateTime

@SpringBootTest(
    properties = [
        "expbot.google-sheets.enabled=false",
        "expbot.telegram.bot-token=",
    ]
)
@Transactional
class MoneyTransactionIntegrationTest @Autowired constructor(
    private val categoryService: CategoryService,
    private val transactionService: MoneyTransactionService,
    private val transactionRepository: MoneyTransactionRepository,
) {

    @Test
    fun `creates expense transaction in postgres`() {
        val category = categoryService.createCategory(
            Category(
                code = "TEST_FOOD",
                name = "Test food",
                type = CategoryType.EXPENSE,
            )
        )

        val saved = transactionService.create(
            MoneyTransaction(
                telegramMessageId = 1001,
                telegramUserId = 2002,
                chatId = 3003,
                category = category,
                type = CategoryType.EXPENSE,
                amount = BigDecimal("42.50"),
                note = "lunch",
                occurredAt = OffsetDateTime.parse("2026-05-14T10:15:30Z"),
            )
        )

        assertNotNull(saved.id)
        val entity = transactionRepository.findById(saved.id!!).orElseThrow()
        assertEquals(0, BigDecimal("42.50").compareTo(entity.amount))
        assertEquals("lunch", entity.note)
        assertEquals(CategoryType.EXPENSE, entity.type)
        assertEquals("TEST_FOOD", entity.category.code)
    }

    @Test
    fun `rejects duplicate telegram message in same chat`() {
        val category = categoryService.createCategory(
            Category(
                code = "TEST_TAXI",
                name = "Test taxi",
                type = CategoryType.EXPENSE,
            )
        )
        val transaction = MoneyTransaction(
            telegramMessageId = 1101,
            telegramUserId = 2202,
            chatId = 3303,
            category = category,
            type = CategoryType.EXPENSE,
            amount = BigDecimal("15.00"),
            note = null,
            occurredAt = OffsetDateTime.parse("2026-05-14T10:15:30Z"),
        )

        transactionService.create(transaction)

        assertThrows(DuplicateTransactionException::class.java) {
            transactionService.create(transaction.copy(note = "duplicate"))
        }
    }

    companion object {
        @JvmStatic
        @DynamicPropertySource
        fun registerProperties(registry: DynamicPropertyRegistry) {
            IntegrationTestContainers.configurePostgres(registry)
        }
    }
}
