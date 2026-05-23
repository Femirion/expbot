package com.femirion.expbot.expbot.`in`.repository

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.OffsetDateTime

@Entity
@Table(name = "balance_corrections")
class BalanceCorrectionEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "telegram_message_id", nullable = false)
    val telegramMessageId: Long,

    @Column(name = "telegram_user_id", nullable = false)
    val telegramUserId: Long,

    @Column(name = "chat_id", nullable = false)
    val chatId: Long,

    @Column(name = "amount", nullable = false, precision = 14, scale = 2)
    val amount: BigDecimal,

    @Column(name = "target_balance", nullable = false, precision = 14, scale = 2)
    val targetBalance: BigDecimal,

    @Column(name = "previous_balance", nullable = false, precision = 14, scale = 2)
    val previousBalance: BigDecimal,

    @Column(name = "occurred_at", nullable = false)
    val occurredAt: OffsetDateTime,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),
)
