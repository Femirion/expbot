package com.femirion.expbot.expbot.domain.entity

data class Message(
    val messageId: Long,
    val date: Long,
    val text: String? = null,
    val userId: Long,
    val chatId: Long,
)