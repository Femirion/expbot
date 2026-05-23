package com.femirion.expbot.expbot.`in`.controller.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant


@JsonIgnoreProperties(ignoreUnknown = true)
data class TelegramMessage(
    @JsonProperty("message_id")
    val messageId: Long,
    val date: Long,
    val text: String? = null,
    val from: TelegramUser? = null,
    val chat: TelegramChat,
) {
    fun messageInstant(): Instant = Instant.ofEpochSecond(date)
}