package com.femirion.expbot.expbot.`in`.mapper

import com.femirion.expbot.expbot.domain.entity.Message
import com.femirion.expbot.expbot.`in`.controller.dto.TelegramMessage
import org.springframework.stereotype.Service

@Service
class MessageMapper {
    
    fun toMessage(telegramMessage: TelegramMessage): Message {
        return Message(
            messageId = telegramMessage.messageId,
            date = telegramMessage.date,
            text = telegramMessage.text,
            userId = telegramMessage.from!!.id,
            chatId = telegramMessage.chat.id,
        )
    }
}