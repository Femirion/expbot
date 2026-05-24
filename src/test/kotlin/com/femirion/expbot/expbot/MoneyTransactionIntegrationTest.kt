package com.femirion.expbot.expbot

import com.femirion.expbot.expbot.domain.entity.Category
import com.femirion.expbot.expbot.domain.entity.CategoryType
import com.femirion.expbot.expbot.domain.entity.MoneyTransaction
import com.femirion.expbot.expbot.domain.service.DuplicateBalanceCorrectionException
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
        assertEquals("TEST_FOOD", entity.category?.code)
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

    @Test
    fun `corrects balance without creating money transaction`() {
        val expenseCategory = categoryService.createCategory(
            Category(
                code = "TEST_BALANCE_EXP",
                name = "Test balance expense",
                type = CategoryType.EXPENSE,
            )
        )
        val incomeCategory = categoryService.createCategory(
            Category(
                code = "TEST_BALANCE_INC",
                name = "Test balance income",
                type = CategoryType.INCOME,
            )
        )
        transactionService.create(
            MoneyTransaction(
                telegramMessageId = 1201,
                telegramUserId = 2202,
                chatId = 3203,
                category = incomeCategory,
                type = CategoryType.INCOME,
                amount = BigDecimal("1000.00"),
                note = null,
                occurredAt = OffsetDateTime.parse("2026-05-14T10:15:30Z"),
            )
        )
        transactionService.create(
            MoneyTransaction(
                telegramMessageId = 1202,
                telegramUserId = 2202,
                chatId = 3203,
                category = expenseCategory,
                type = CategoryType.EXPENSE,
                amount = BigDecimal("100.00"),
                note = null,
                occurredAt = OffsetDateTime.parse("2026-05-14T11:15:30Z"),
            )
        )

        assertEquals(0, BigDecimal("900.00").compareTo(transactionService.balance(3203)))

        val correctedBalance = transactionService.correctBalance(
            telegramMessageId = 1203,
            telegramUserId = 2202,
            chatId = 3203,
            targetBalance = BigDecimal("800.00"),
            occurredAt = OffsetDateTime.parse("2026-05-14T12:15:30Z"),
        )

        assertEquals(0, BigDecimal("800.00").compareTo(correctedBalance))
        assertEquals(0, BigDecimal("800.00").compareTo(transactionService.balance(3203)))
        assertEquals(2, transactionRepository.count())
    }

    @Test
    fun `creates exchange withdrawal and decreases balance`() {
        val incomeCategory = categoryService.createCategory(
            Category(
                code = "TEST_EXCHANGE_INC",
                name = "Test exchange income",
                type = CategoryType.INCOME,
            )
        )
        transactionService.create(
            MoneyTransaction(
                telegramMessageId = 1401,
                telegramUserId = 2402,
                chatId = 3403,
                category = incomeCategory,
                type = CategoryType.INCOME,
                amount = BigDecimal("50000.00"),
                note = null,
                occurredAt = OffsetDateTime.parse("2026-05-14T10:15:30Z"),
            )
        )

        val saved = transactionService.createExchangeWithdrawal(
            telegramMessageId = 1402,
            telegramUserId = 2402,
            chatId = 3403,
            amount = BigDecimal("20000.00"),
            note = null,
            occurredAt = OffsetDateTime.parse("2026-05-14T11:15:30Z"),
        )

        assertNotNull(saved.id)
        assertEquals(CategoryType.EXCHANGE, saved.type)
        assertEquals(null, saved.category)
        assertEquals(0, BigDecimal("30000.00").compareTo(transactionService.balance(3403)))
    }

    @Test
    fun `rejects duplicate balance correction in same chat`() {
        transactionService.correctBalance(
            telegramMessageId = 1301,
            telegramUserId = 2302,
            chatId = 3303,
            targetBalance = BigDecimal("800.00"),
            occurredAt = OffsetDateTime.parse("2026-05-14T12:15:30Z"),
        )

        assertThrows(DuplicateBalanceCorrectionException::class.java) {
            transactionService.correctBalance(
                telegramMessageId = 1301,
                telegramUserId = 2302,
                chatId = 3303,
                targetBalance = BigDecimal("700.00"),
                occurredAt = OffsetDateTime.parse("2026-05-14T12:16:30Z"),
            )
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
