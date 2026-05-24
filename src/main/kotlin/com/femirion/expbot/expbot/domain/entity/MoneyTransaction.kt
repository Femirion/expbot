package com.femirion.expbot.expbot.domain.entity

import java.math.BigDecimal
import java.time.OffsetDateTime

data class MoneyTransaction(
    val id: Long? = null,
    val telegramMessageId: Long,
    val telegramUserId: Long,
    val chatId: Long,
    val category: Category?,
    val type: CategoryType,
    val amount: BigDecimal,
    val note: String? = null,
    val occurredAt: OffsetDateTime,
    val createdAt: OffsetDateTime? = null,
)
