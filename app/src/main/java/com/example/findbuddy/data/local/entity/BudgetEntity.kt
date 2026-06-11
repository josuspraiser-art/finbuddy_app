package com.example.findbuddy.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "budgets",
    indices = [
        Index(value = ["userId", "categoryId", "month", "year"], unique = true)
    ]
)
data class BudgetEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val categoryId: String,
    val month: Int,
    val year: Int,
    val budgetAmount: Double,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
