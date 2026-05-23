package com.femirion.expbot.expbot.domain.service

import com.femirion.expbot.expbot.domain.entity.Category
import com.femirion.expbot.expbot.domain.entity.CategoryExpenseSummary
import com.femirion.expbot.expbot.domain.entity.MoneyTransaction
import com.femirion.expbot.expbot.`in`.provider.MoneyTransactionProvider
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.OffsetDateTime

@Service
class MoneyTransactionService(
    private val transactionProvider: MoneyTransactionProvider,
    private val transactionReporter: MoneyTransactionReporter,
) {

    @Transactional
    fun create(transaction: MoneyTransaction): MoneyTransaction {
        if (transactionProvider.exists(transaction.telegramMessageId, transaction.chatId)) {
            throw DuplicateTransactionException()
        }
        val saved = transactionProvider.save(transaction)
        transactionReporter.report(saved)
        return saved
    }

    @Transactional(readOnly = true)
    fun sumExpensesByCategory(chatId: Long, from: OffsetDateTime, to: OffsetDateTime): List<CategoryExpenseSummary> {
        return transactionProvider.sumExpensesByCategory(chatId, from, to)
    }

    @Transactional(readOnly = true)
    fun balance(chatId: Long): BigDecimal {
        return transactionProvider.balance(chatId)
    }

    @Transactional
    fun correctBalance(
        telegramMessageId: Long,
        telegramUserId: Long,
        chatId: Long,
        targetBalance: BigDecimal,
        occurredAt: OffsetDateTime,
    ): BigDecimal {
        if (transactionProvider.correctionExists(telegramMessageId, chatId)) {
            throw DuplicateBalanceCorrectionException()
        }
        val currentBalance = transactionProvider.balance(chatId)
        val correctionAmount = targetBalance - currentBalance
        transactionProvider.saveBalanceCorrection(
            telegramMessageId = telegramMessageId,
            telegramUserId = telegramUserId,
            chatId = chatId,
            amount = correctionAmount,
            targetBalance = targetBalance,
            previousBalance = currentBalance,
            occurredAt = occurredAt,
        )
        return targetBalance
    }
}

class DuplicateTransactionException : RuntimeException("Telegram message was already processed as transaction")
class DuplicateBalanceCorrectionException : RuntimeException("Telegram message was already processed as balance correction")

interface MoneyTransactionReporter {
    fun report(transaction: MoneyTransaction)
}
