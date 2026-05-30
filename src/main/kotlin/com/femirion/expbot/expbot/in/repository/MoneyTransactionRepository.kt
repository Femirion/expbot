package com.femirion.expbot.expbot.`in`.repository

import com.femirion.expbot.expbot.domain.entity.CategoryType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.math.BigDecimal
import java.time.OffsetDateTime

interface MoneyTransactionRepository : JpaRepository<MoneyTransactionEntity, Long> {

    fun existsByTelegramMessageIdAndChatId(telegramMessageId: Long, chatId: Long): Boolean

    @Query(
        """
        select t.category.code as categoryCode,
               t.category.name as categoryName,
               sum(t.amount) as total
        from MoneyTransactionEntity t
        where t.type = :type
          and t.occurredAt >= :from
          and t.occurredAt < :to
        group by t.category.code, t.category.name
        order by t.category.code
        """
    )
    fun sumByCategory(
        @Param("type") type: CategoryType,
        @Param("from") from: OffsetDateTime,
        @Param("to") to: OffsetDateTime,
    ): List<CategoryExpenseTotal>

    @Query(
        """
        select coalesce(sum(t.amount), 0)
        from MoneyTransactionEntity t
        where t.category.id = :categoryId
          and t.type = com.femirion.expbot.expbot.domain.entity.CategoryType.EXPENSE
          and t.occurredAt >= :from
          and t.occurredAt < :to
        """
    )
    fun expenseTotalByCategory(
        @Param("categoryId") categoryId: Long,
        @Param("from") from: OffsetDateTime,
        @Param("to") to: OffsetDateTime,
    ): BigDecimal

    @Query(
        """
        select coalesce(sum(
            case
                when t.type = com.femirion.expbot.expbot.domain.entity.CategoryType.INCOME then t.amount
                when t.type = com.femirion.expbot.expbot.domain.entity.CategoryType.EXPENSE then -t.amount
                when t.type = com.femirion.expbot.expbot.domain.entity.CategoryType.EXCHANGE then -t.amount
                else 0
            end
        ), 0)
        from MoneyTransactionEntity t
        where t.chatId = :chatId
        """
    )
    fun balance(@Param("chatId") chatId: Long): BigDecimal
}

interface CategoryExpenseTotal {
    val categoryCode: String
    val categoryName: String
    val total: BigDecimal
}
