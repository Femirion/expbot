package com.femirion.expbot.expbot.`in`.repository

import org.springframework.data.jpa.repository.JpaRepository

interface MoneyTransactionRepository : JpaRepository<MoneyTransactionEntity, Long> {

    fun existsByTelegramMessageIdAndChatId(telegramMessageId: Long, chatId: Long): Boolean
}
