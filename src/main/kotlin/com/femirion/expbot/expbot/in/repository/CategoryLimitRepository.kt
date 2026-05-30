package com.femirion.expbot.expbot.`in`.repository

import com.femirion.expbot.expbot.domain.entity.LimitPeriod
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface CategoryLimitRepository : JpaRepository<CategoryLimitEntity, Long> {

    fun findByCategoryIdAndPeriod(categoryId: Long, period: LimitPeriod): Optional<CategoryLimitEntity>

    fun findAllByCategoryId(categoryId: Long): List<CategoryLimitEntity>
}
