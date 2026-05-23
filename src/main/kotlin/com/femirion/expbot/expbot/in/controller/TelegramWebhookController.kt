package com.femirion.expbot.expbot.`in`.controller

import com.femirion.expbot.expbot.domain.service.MessageHandler
import com.femirion.expbot.expbot.domain.service.TelegramUserAccessService
import com.femirion.expbot.expbot.`in`.controller.dto.TelegramUpdate
import com.femirion.expbot.expbot.`in`.mapper.MessageMapper
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.logging.Logger

@RestController
@RequestMapping("/telegram")
class TelegramWebhookController(
    private val telegramUserAccessService: TelegramUserAccessService,
    private val telegramUpdateHandler: MessageHandler,
    private val messageMapper: MessageMapper,
) {
    @PostMapping("/webhook")
    fun handleWebhook(@RequestBody update: TelegramUpdate): ResponseEntity<Unit> {
        if (update.message != null) {
            log.info { "Received telegram update: updateId=${update.updateId}" }
            if (!telegramUserAccessService.isAllowed(update.message.from?.id)) {
                return ResponseEntity.ok().build()
            }
            telegramUpdateHandler.handle(messageMapper.toMessage(update.message))
        } else if (update.callbackQuery != null) {
            log.info { "Received telegram callback query: updateId=${update.updateId}" }
            if (!telegramUserAccessService.isAllowed(update.callbackQuery.from.id)) {
                return ResponseEntity.ok().build()
            }
            telegramUpdateHandler.handleCallbackQuery(
                callbackQueryId = update.callbackQuery.id,
                userId = update.callbackQuery.from.id,
                chatId = update.callbackQuery.message?.chat?.id,
                data = update.callbackQuery.data,
            )
        } else {
            log.warning { "Skipping update without message: updateId=${update.updateId}" }
        }
        return ResponseEntity.ok().build()
    }

    companion object {
        val log: Logger = Logger.getLogger(TelegramWebhookController::class.java.name)
    }
}
