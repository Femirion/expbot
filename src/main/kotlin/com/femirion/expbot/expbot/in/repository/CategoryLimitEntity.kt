package com.femirion.expbot.expbot.`in`.repository

import com.femirion.expbot.expbot.domain.entity.LimitPeriod
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.math.BigDecimal
import java.time.OffsetDateTime

@Entity
@Table(
    name = "category_limits",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_category_limits_category_period", columnNames = ["category_id", "period"]),
    ],
)
class CategoryLimitEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    val category: CategoryEntity,

    @Enumerated(EnumType.STRING)
    @Column(name = "period", nullable = false, length = 32)
    val period: LimitPeriod,

    @Column(name = "amount", nullable = false, precision = 14, scale = 2)
    var amount: BigDecimal,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now(),
)
