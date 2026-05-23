package com.femirion.expbot.expbot.domain.entity

import java.math.BigDecimal

data class CategoryExpenseSummary(
    val categoryCode: String,
    val categoryName: String,
    val total: BigDecimal,
)
