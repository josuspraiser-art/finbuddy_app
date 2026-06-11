package com.example.findbuddy.domain.repository

import com.example.findbuddy.domain.model.Budget
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {
    fun getBudgets(userId: String): Flow<List<Budget>>
    fun getBudgetsForPeriod(userId: String, month: Int, year: Int): Flow<List<Budget>>
    suspend fun createOrUpdateBudget(
        userId: String,
        categoryId: String,
        month: Int,
        year: Int,
        amount: Double
    ): Result<Budget>
    suspend fun syncBudgets(userId: String): Result<Unit>
}
