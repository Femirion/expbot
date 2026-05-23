package com.femirion.expbot.expbot.`in`.controller.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class TelegramUpdate(
    @JsonProperty("update_id")
    val updateId: Long,
    val message: TelegramMessage? = null,
)