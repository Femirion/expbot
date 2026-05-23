package com.femirion.expbot.expbot.domain.service

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TelegramUserAccessServiceTest {

    @Test
    fun `allows users from comma separated allow-list`() {
        val service = TelegramUserAccessService("1234, 2345")

        assertTrue(service.isAllowed(1234))
        assertTrue(service.isAllowed(2345))
        assertFalse(service.isAllowed(3456))
    }

    @Test
    fun `rejects all users when allow-list is blank`() {
        val service = TelegramUserAccessService("")

        assertFalse(service.isAllowed(1234))
        assertFalse(service.isAllowed(null))
    }
}
