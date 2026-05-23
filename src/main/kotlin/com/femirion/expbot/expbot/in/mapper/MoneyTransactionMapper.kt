package com.femirion.expbot.expbot.`in`.mapper

import com.femirion.expbot.expbot.domain.entity.MoneyTransaction
import com.femirion.expbot.expbot.`in`.repository.MoneyTransactionEntity
import org.springframework.stereotype.Service

@Service
class MoneyTransactionMapper(
    private val categoryMapper: CategoryMapper,
) {

    fun toTransaction(entity: MoneyTransactionEntity): MoneyTransaction {
        return MoneyTransaction(
            id = entity.id,
            telegramMessageId = entity.telegramMessageId,
            telegramUserId = entity.telegramUserId,
            chatId = entity.chatId,
            category = categoryMapper.toCategory(entity.category),
            type = entity.type,
            amount = entity.amount,
            note = entity.note,
            occurredAt = entity.occurredAt,
            createdAt = entity.createdAt,
        )
    }
}
