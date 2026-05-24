package com.femirion.expbot.expbot.`in`.repository

import com.femirion.expbot.expbot.domain.entity.CategoryType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.OffsetDateTime

@Entity
@Table(name = "money_transactions")
class MoneyTransactionEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "telegram_message_id", nullable = false)
    val telegramMessageId: Long,

    @Column(name = "telegram_user_id", nullable = false)
    val telegramUserId: Long,

    @Column(name = "chat_id", nullable = false)
    val chatId: Long,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    val category: CategoryEntity?,

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 32)
    val type: CategoryType,

    @Column(name = "amount", nullable = false, precision = 14, scale = 2)
    val amount: BigDecimal,

    @Column(name = "note")
    val note: String? = null,

    @Column(name = "occurred_at", nullable = false)
    val occurredAt: OffsetDateTime,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),
)
