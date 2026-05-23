package com.femirion.expbot.expbot.domain.entity

import java.time.LocalDateTime

data class User(
    val id: Long?,
    val telegramUserId: Long,
    var telegramUsername: String,
    var displayName: String,
    var status: UserStatus,
    val createdAt: LocalDateTime,
    var updatedAt: LocalDateTime,
)