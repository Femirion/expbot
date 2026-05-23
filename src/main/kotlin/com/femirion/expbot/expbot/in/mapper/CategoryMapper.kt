package com.femirion.expbot.expbot.`in`.mapper

import com.femirion.expbot.expbot.domain.entity.Category
import com.femirion.expbot.expbot.`in`.repository.CategoryEntity
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class CategoryMapper {

    fun toEntity(category: Category): CategoryEntity {
        return CategoryEntity(
            id = category.id,
            code = category.code,
            name = category.name,
            type = category.type,
            isActive = category.isActive,
            createdAt = category.createdAt ?: LocalDateTime.now(),
        )
    }

    fun toCategory(categoryEntity: CategoryEntity): Category {
        return Category(
            id = categoryEntity.id,
            code = categoryEntity.code,
            name = categoryEntity.name,
            type = categoryEntity.type,
            isActive = categoryEntity.isActive,
            createdAt = categoryEntity.createdAt,
        )
    }

}
