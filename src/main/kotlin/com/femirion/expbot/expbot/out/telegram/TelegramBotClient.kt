package com.femirion.expbot.expbot.out.telegram

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import java.util.logging.Logger

@Service
class TelegramBotClient(
    @Value("\${expbot.telegram.bot-token:}")
    private val botToken: String,
) {
    private val restClient = RestClient.create()

    fun sendMessage(chatId: Long, text: String) {
        if (botToken.isBlank()) {
            log.info { "Telegram bot token is not configured; outgoing message skipped" }
            return
        }

        restClient.post()
            .uri("https://api.telegram.org/bot{token}/sendMessage", botToken)
            .body(mapOf("chat_id" to chatId, "text" to text))
            .retrieve()
            .toBodilessEntity()
    }

    companion object {
        private val log: Logger = Logger.getLogger(TelegramBotClient::class.java.name)
    }
}
