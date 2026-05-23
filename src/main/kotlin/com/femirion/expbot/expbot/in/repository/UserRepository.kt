package com.femirion.expbot.expbot.`in`.repository

import com.femirion.expbot.expbot.domain.entity.UserStatus
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<UserEntity, Long> {

    fun findByTelegramUserId(telegramUserId: Long): UserEntity?

    fun existsByTelegramUserId(telegramUserId: Long): Boolean

    fun findAllByStatus(status: UserStatus): List<UserEntity>
}