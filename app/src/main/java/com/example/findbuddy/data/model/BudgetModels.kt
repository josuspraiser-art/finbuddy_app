package com.example.findbuddy.data.model

data class BudgetCreateRequest(
    val categoryId: String,
    val month: Int,
    val year: Int,
    val budgetAmount: Double
)

data class BudgetResponse(
    val id: String,
    val categoryId: String,
    val month: Int,
    val year: Int,
    val budgetAmount: Double,
    val spentAmount: Double,
    val remainingAmount: Double,
    val usagePercentage: Double,
    val status: String
)
