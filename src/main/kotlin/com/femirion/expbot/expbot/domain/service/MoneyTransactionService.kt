package com.femirion.expbot.expbot.domain.service

import com.femirion.expbot.expbot.domain.entity.Category
import com.femirion.expbot.expbot.domain.entity.MoneyTransaction
import com.femirion.expbot.expbot.`in`.provider.MoneyTransactionProvider
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

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
}

class DuplicateTransactionException : RuntimeException("Telegram message was already processed as transaction")

interface MoneyTransactionReporter {
    fun report(transaction: MoneyTransaction)
}
