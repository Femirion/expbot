package com.femirion.expbot.expbot.`in`.mapper

import com.femirion.expbot.expbot.domain.entity.Category
import com.femirion.expbot.expbot.domain.entity.CategoryType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class CategoryMapperTest {
    private val mapper = CategoryMapper()

    @Test
    fun `maps category to entity with default created at`() {
        val entity = mapper.toEntity(
            Category(
                code = "FOOD",
                name = "Food",
                type = CategoryType.EXPENSE,
            )
        )

        assertEquals("FOOD", entity.code)
        assertEquals("Food", entity.name)
        assertEquals(CategoryType.EXPENSE, entity.type)
        assertNotNull(entity.createdAt)
    }

    @Test
    fun `maps entity to category`() {
        val createdAt = LocalDateTime.parse("2026-05-14T10:15:30")
        val category = mapper.toCategory(
            com.femirion.expbot.expbot.`in`.repository.CategoryEntity(
                id = 1,
                code = "SALARY",
                name = "Salary",
                type = CategoryType.INCOME,
                createdAt = createdAt,
            )
        )

        assertEquals(1, category.id)
        assertEquals("SALARY", category.code)
        assertEquals("Salary", category.name)
        assertEquals(CategoryType.INCOME, category.type)
        assertEquals(createdAt, category.createdAt)
    }
}
