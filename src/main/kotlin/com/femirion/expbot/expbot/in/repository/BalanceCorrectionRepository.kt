package com.femirion.expbot.expbot.`in`.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.math.BigDecimal

interface BalanceCorrectionRepository : JpaRepository<BalanceCorrectionEntity, Long> {

    fun existsByTelegramMessageIdAndChatId(telegramMessageId: Long, chatId: Long): Boolean

    @Query(
        """
        select coalesce(sum(c.amount), 0)
        from BalanceCorrectionEntity c
        where c.chatId = :chatId
        """
    )
    fun balance(@Param("chatId") chatId: Long): BigDecimal
}
