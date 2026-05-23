package com.femirion.expbot.expbot.domain.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.logging.Logger

@Service
class TelegramUserAccessService(
    @Value("\${expbot.telegram.allowed-user-ids:}")
    allowedUserIds: String,
) {
    private val allowedUserIds: Set<Long> = allowedUserIds
        .split(",")
        .mapNotNull { it.trim().toLongOrNull() }
        .toSet()

    fun isAllowed(userId: Long?): Boolean {
        if (userId == null) {
            log.warning { "Rejected telegram update without user id" }
            return false
        }
        if (allowedUserIds.isEmpty()) {
            log.warning { "Rejected telegram user $userId: ALLOWED_TELEGRAM_USER_IDS is not configured" }
            return false
        }
        val allowed = userId in allowedUserIds
        if (!allowed) {
            log.warning { "Rejected telegram user $userId: user is not in allow-list" }
        }
        return allowed
    }

    companion object {
        private val log: Logger = Logger.getLogger(TelegramUserAccessService::class.java.name)
    }
}
