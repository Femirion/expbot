package com.femirion.expbot.expbot.`in`.provider

import com.femirion.expbot.expbot.domain.entity.Category
import com.femirion.expbot.expbot.domain.entity.CategoryExpenseSummary
import com.femirion.expbot.expbot.domain.entity.CategoryType
import com.femirion.expbot.expbot.domain.entity.MoneyTransaction
import com.femirion.expbot.expbot.`in`.mapper.MoneyTransactionMapper
import com.femirion.expbot.expbot.`in`.repository.BalanceCorrectionEntity
import com.femirion.expbot.expbot.`in`.repository.BalanceCorrectionRepository
import com.femirion.expbot.expbot.`in`.repository.CategoryRepository
import com.femirion.expbot.expbot.`in`.repository.MoneyTransactionEntity
import com.femirion.expbot.expbot.`in`.repository.MoneyTransactionRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.OffsetDateTime

@Service
class MoneyTransactionProvider(
    private val balanceCorrectionRepository: BalanceCorrectionRepository,
    private val categoryRepository: CategoryRepository,
    private val transactionMapper: MoneyTransactionMapper,
    private val transactionRepository: MoneyTransactionRepository,
) {

    fun exists(telegramMessageId: Long, chatId: Long): Boolean {
        return transactionRepository.existsByTelegramMessageIdAndChatId(telegramMessageId, chatId)
    }

    fun save(transaction: MoneyTransaction): MoneyTransaction {
        val categoryEntity = transaction.category
            ?.let { category ->
                val categoryId = requireNotNull(category.id) { "Category id is required" }
                categoryRepository.getReferenceById(categoryId)
            }
        val savedEntity = transactionRepository.save(
            MoneyTransactionEntity(
                id = transaction.id,
                telegramMessageId = transaction.telegramMessageId,
                telegramUserId = transaction.telegramUserId,
                chatId = transaction.chatId,
                category = categoryEntity,
                type = transaction.type,
                amount = transaction.amount,
                note = transaction.note,
                occurredAt = transaction.occurredAt,
                createdAt = transaction.createdAt ?: java.time.OffsetDateTime.now(),
            )
        )
        return transactionMapper.toTransaction(savedEntity)
    }

    fun sumExpensesByCategory(from: OffsetDateTime, to: OffsetDateTime): List<CategoryExpenseSummary> {
        return transactionRepository.sumByCategory(CategoryType.EXPENSE, from, to)
            .map {
                CategoryExpenseSummary(
                    categoryCode = it.categoryCode,
                    categoryName = it.categoryName,
                    total = it.total,
                )
            }
    }

    fun balance(chatId: Long): BigDecimal {
        return transactionRepository.balance(chatId) + balanceCorrectionRepository.balance(chatId)
    }

    fun correctionExists(telegramMessageId: Long, chatId: Long): Boolean {
        return balanceCorrectionRepository.existsByTelegramMessageIdAndChatId(telegramMessageId, chatId)
    }

    fun saveBalanceCorrection(
        telegramMessageId: Long,
        telegramUserId: Long,
        chatId: Long,
        amount: BigDecimal,
        targetBalance: BigDecimal,
        previousBalance: BigDecimal,
        occurredAt: OffsetDateTime,
    ) {
        balanceCorrectionRepository.save(
            BalanceCorrectionEntity(
                telegramMessageId = telegramMessageId,
                telegramUserId = telegramUserId,
                chatId = chatId,
                amount = amount,
                targetBalance = targetBalance,
                previousBalance = previousBalance,
                occurredAt = occurredAt,
            )
        )
    }
}
