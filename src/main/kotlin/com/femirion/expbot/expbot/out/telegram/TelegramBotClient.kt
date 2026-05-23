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
        sendMessage(chatId, text, replyMarkup = null)
    }

    fun sendMessageWithCategoryButtons(chatId: Long, text: String, categories: List<TelegramButtonCategory>) {
        val buttons = categories.map { category ->
            listOf(
                mapOf(
                    "text" to category.name,
                    "callback_data" to "expense:${category.code}",
                )
            )
        }
        sendMessage(
            chatId = chatId,
            text = text,
            replyMarkup = mapOf("inline_keyboard" to buttons),
        )
    }

    fun answerCallbackQuery(callbackQueryId: String) {
        if (botToken.isBlank()) {
            log.info { "Telegram bot token is not configured; outgoing message skipped" }
            return
        }

        restClient.post()
            .uri("https://api.telegram.org/bot{token}/answerCallbackQuery", botToken.trim())
            .body(mapOf("callback_query_id" to callbackQueryId))
            .retrieve()
            .toBodilessEntity()
    }

    private fun sendMessage(chatId: Long, text: String, replyMarkup: Map<String, Any>?) {
        if (botToken.isBlank()) {
            log.info { "Telegram bot token is not configured; outgoing message skipped" }
            return
        }

        val body = mutableMapOf<String, Any>("chat_id" to chatId, "text" to text)
        if (replyMarkup != null) {
            body["reply_markup"] = replyMarkup
        }

        restClient.post()
            .uri("https://api.telegram.org/bot{token}/sendMessage", botToken.trim())
            .body(body)
            .retrieve()
            .toBodilessEntity()
    }

    companion object {
        private val log: Logger = Logger.getLogger(TelegramBotClient::class.java.name)
    }
}

data class TelegramButtonCategory(
    val code: String,
    val name: String,
)
