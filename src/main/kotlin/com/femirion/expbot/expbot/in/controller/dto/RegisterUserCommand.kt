package com.femirion.expbot.expbot.`in`.controller.dto

data class RegisterUserCommand(
    val telegramUserId: Long,
    val telegramUsername: String?,
    val displayName: String,
)