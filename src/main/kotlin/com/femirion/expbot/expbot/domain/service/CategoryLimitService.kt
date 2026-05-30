package com.femirion.expbot.expbot.domain.service

import com.femirion.expbot.expbot.domain.entity.Category
import com.femirion.expbot.expbot.domain.entity.CategoryType
import com.femirion.expbot.expbot.domain.entity.LimitPeriod
import com.femirion.expbot.expbot.`in`.repository.CategoryLimitEntity
import com.femirion.expbot.expbot.`in`.repository.CategoryLimitRepository
import com.femirion.expbot.expbot.`in`.repository.CategoryRepository
import com.femirion.expbot.expbot.`in`.repository.MoneyTransactionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.time.YearMonth
import java.time.ZoneOffset

@Service
class CategoryLimitService(
    private val categoryLimitRepository: CategoryLimitRepository,
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: MoneyTransactionRepository,
) {

    @Transactional
    fun saveLimit(category: Category, amount: BigDecimal, period: LimitPeriod): CategoryLimit {
        require(category.type == CategoryType.EXPENSE) { "Limits can be created only for expense categories" }
        require(amount > BigDecimal.ZERO) { "Limit amount must be greater than zero" }

        val categoryId = requireNotNull(category.id) { "Category id is required" }
        val categoryEntity = categoryRepository.getReferenceById(categoryId)
        val limit = categoryLimitRepository.findByCategoryIdAndPeriod(categoryId, period)
            .map { existing ->
                existing.amount = amount
                existing.updatedAt = OffsetDateTime.now(ZoneOffset.UTC)
                existing
            }
            .orElseGet {
                CategoryLimitEntity(
                    category = categoryEntity,
                    period = period,
                    amount = amount,
                    createdAt = OffsetDateTime.now(ZoneOffset.UTC),
                    updatedAt = OffsetDateTime.now(ZoneOffset.UTC),
                )
            }
        return categoryLimitRepository.save(limit).toDomain()
    }

    @Transactional(readOnly = true)
    fun exceededLimits(category: Category, occurredAt: OffsetDateTime): List<CategoryLimitStatus> {
        val categoryId = category.id ?: return emptyList()
        return categoryLimitRepository.findAllByCategoryId(categoryId)
            .map { limit ->
                val range = limit.period.rangeFor(occurredAt)
                val total = transactionRepository.expenseTotalByCategory(categoryId, range.from, range.to)
                CategoryLimitStatus(
                    categoryCode = category.code,
                    categoryName = category.name,
                    period = limit.period,
                    amount = limit.amount,
                    total = total,
                )
            }
            .filter { it.total > it.amount }
            .sortedBy { it.period.ordinal }
    }
}

data class CategoryLimit(
    val categoryCode: String,
    val period: LimitPeriod,
    val amount: BigDecimal,
)

data class CategoryLimitStatus(
    val categoryCode: String,
    val categoryName: String,
    val period: LimitPeriod,
    val amount: BigDecimal,
    val total: BigDecimal,
)

private data class DateTimeRange(
    val from: OffsetDateTime,
    val to: OffsetDateTime,
)

private fun CategoryLimitEntity.toDomain(): CategoryLimit {
    return CategoryLimit(
        categoryCode = category.code,
        period = period,
        amount = amount,
    )
}

private fun LimitPeriod.rangeFor(occurredAt: OffsetDateTime): DateTimeRange {
    val utcDateTime = occurredAt.withOffsetSameInstant(ZoneOffset.UTC)
    return when (this) {
        LimitPeriod.DAY -> {
            val date = utcDateTime.toLocalDate()
            DateTimeRange(
                from = date.atStartOfDay().atOffset(ZoneOffset.UTC),
                to = date.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC),
            )
        }
        LimitPeriod.MONTH -> {
            val month = YearMonth.from(utcDateTime)
            DateTimeRange(
                from = month.atDay(1).atStartOfDay().atOffset(ZoneOffset.UTC),
                to = month.plusMonths(1).atDay(1).atStartOfDay().atOffset(ZoneOffset.UTC),
            )
        }
    }
}
