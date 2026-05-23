package com.femirion.expbot.expbot.`in`.controller.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class TelegramCallbackQuery(
    val id: String,
    val from: TelegramUser,
    val message: TelegramMessage? = null,
    val data: String? = null,
)
