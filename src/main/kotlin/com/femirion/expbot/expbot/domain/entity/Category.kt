package com.femirion.expbot.expbot.domain.entity

import java.time.LocalDateTime

data class Category(
    val id: Long? = null,
    val code: String,
    val name: String,
    val type: CategoryType,
    val isActive: Boolean = true,
    val createdAt: LocalDateTime? = null,
)