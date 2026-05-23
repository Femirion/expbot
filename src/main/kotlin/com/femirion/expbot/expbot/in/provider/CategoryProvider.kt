package com.femirion.expbot.expbot.`in`.provider

import com.femirion.expbot.expbot.domain.entity.Category
import com.femirion.expbot.expbot.`in`.mapper.CategoryMapper
import com.femirion.expbot.expbot.`in`.repository.CategoryRepository
import org.springframework.stereotype.Service

@Service
class CategoryProvider(
    val categoryMapper: CategoryMapper,
    val categoryRepository: CategoryRepository,
) {

    fun getCategoryById(id: Long): Category? {
        return categoryRepository
            .findById(id)
            .map { entity -> categoryMapper.toCategory(entity) }
            .orElse(null)
    }

    fun getCategoryByCode(code: String): Category? {
        return categoryRepository
            .findByCodeIgnoreCase(code)
            .map { entity -> categoryMapper.toCategory(entity) }
            .orElse(null)
    }

    fun getAll(): List<Category> {
        return categoryRepository
            .findAll()
            .map { entity -> categoryMapper.toCategory(entity) }
    }

    fun createCategory(category: Category): Category {
        val savedEntity = categoryRepository.save(categoryMapper.toEntity(category))
        return categoryMapper.toCategory(savedEntity)
    }

    fun deleteById(id: Long) {
        categoryRepository.deleteById(id)
    }
}
