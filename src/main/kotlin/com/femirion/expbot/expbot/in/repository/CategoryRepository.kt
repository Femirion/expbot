package com.femirion.expbot.expbot.`in`.repository

import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface CategoryRepository : JpaRepository<CategoryEntity, Long> {

    fun findByName(name: String): Optional<CategoryEntity>

    fun findByCodeIgnoreCase(code: String): Optional<CategoryEntity>
}
