package com.example.findbuddy.domain.model

data class Budget(
    val id: String,
    val userId: String,
    val categoryId: String,
    val month: Int,
    val year: Int,
    val budgetAmount: Double,
    val spentAmount: Double,
    val remainingAmount: Double,
    val usagePercentage: Double,
    val status: String // NORMAL, WARNING, EXCEEDED
)
