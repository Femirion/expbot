package com.femirion.expbot.expbot.service

import com.femirion.expbot.expbot.domain.entity.Category
import com.femirion.expbot.expbot.`in`.provider.CategoryProvider
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class CategoryService(
    val categoryProvider: CategoryProvider,
) {

    fun getCategory(id: Long): Category? {
        return categoryProvider.getCategoryById(id)
    }

    fun getCategoryByCode(code: String): Category? {
        return categoryProvider.getCategoryByCode(code)
    }

    fun createCategory(category: Category): Category {
        val normalized = category.copy(
            code = category.code.trim().uppercase(),
            name = category.name.trim(),
            createdAt = category.createdAt ?: LocalDateTime.now(),
        )
        return categoryProvider.createCategory(normalized)
    }

    fun getAll(): List<Category> {
        return categoryProvider.getAll()
    }

    fun deleteById(id: Long) {
        categoryProvider.deleteById(id)
    }
}
