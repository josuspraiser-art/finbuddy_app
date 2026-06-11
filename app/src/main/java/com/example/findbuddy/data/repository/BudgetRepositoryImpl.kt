package com.example.findbuddy.data.repository

import com.example.findbuddy.data.api.BudgetApi
import com.example.findbuddy.data.local.dao.BudgetDao
import com.example.findbuddy.data.local.dao.TransactionDao
import com.example.findbuddy.data.local.entity.BudgetEntity
import com.example.findbuddy.data.model.BudgetCreateRequest
import com.example.findbuddy.domain.model.Budget
import com.example.findbuddy.domain.repository.BudgetRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetRepositoryImpl @Inject constructor(
    private val budgetDao: BudgetDao,
    private val transactionDao: TransactionDao,
    private val budgetApi: BudgetApi
) : BudgetRepository {

    override fun getBudgets(userId: String): Flow<List<Budget>> {
        val budgetsFlow = budgetDao.getBudgets(userId)
        val transactionsFlow = transactionDao.getTransactions(userId)

        return combine(budgetsFlow, transactionsFlow) { budgetEntities, transactionEntities ->
            budgetEntities.map { budgetEntity ->
                val datePrefix = String.format("%04d-%02d-", budgetEntity.year, budgetEntity.month)
                val spent = transactionEntities.filter {
                    it.categoryId == budgetEntity.categoryId &&
                    it.type == "EXPENSE" &&
                    it.date.startsWith(datePrefix)
                }.sumOf { it.amount }

                val remaining = budgetEntity.budgetAmount - spent
                val percent = if (budgetEntity.budgetAmount > 0) (spent / budgetEntity.budgetAmount) * 100 else 0.0

                val status = when {
                    percent > 100 -> "EXCEEDED"
                    percent >= 80 -> "WARNING"
                    else -> "NORMAL"
                }

                Budget(
                    id = budgetEntity.id,
                    userId = userId,
                    categoryId = budgetEntity.categoryId,
                    month = budgetEntity.month,
                    year = budgetEntity.year,
                    budgetAmount = budgetEntity.budgetAmount,
                    spentAmount = spent,
                    remainingAmount = remaining,
                    usagePercentage = percent,
                    status = status
                )
            }
        }
    }

    override fun getBudgetsForPeriod(userId: String, month: Int, year: Int): Flow<List<Budget>> {
        val budgetsFlow = budgetDao.getBudgetsForPeriod(userId, month, year)
        val transactionsFlow = transactionDao.getTransactions(userId)

        return combine(budgetsFlow, transactionsFlow) { budgetEntities, transactionEntities ->
            budgetEntities.map { budgetEntity ->
                val datePrefix = String.format("%04d-%02d-", year, month)
                val spent = transactionEntities.filter {
                    it.categoryId == budgetEntity.categoryId &&
                    it.type == "EXPENSE" &&
                    it.date.startsWith(datePrefix)
                }.sumOf { it.amount }

                val remaining = budgetEntity.budgetAmount - spent
                val percent = if (budgetEntity.budgetAmount > 0) (spent / budgetEntity.budgetAmount) * 100 else 0.0

                val status = when {
                    percent > 100 -> "EXCEEDED"
                    percent >= 80 -> "WARNING"
                    else -> "NORMAL"
                }

                Budget(
                    id = budgetEntity.id,
                    userId = userId,
                    categoryId = budgetEntity.categoryId,
                    month = month,
                    year = year,
                    budgetAmount = budgetEntity.budgetAmount,
                    spentAmount = spent,
                    remainingAmount = remaining,
                    usagePercentage = percent,
                    status = status
                )
            }
        }
    }

    override suspend fun createOrUpdateBudget(
        userId: String,
        categoryId: String,
        month: Int,
        year: Int,
        amount: Double
    ): Result<Budget> = withContext(Dispatchers.IO) {
        try {
            if (month !in 1..12) {
                return@withContext Result.failure(Exception("Month must be between 1 and 12"))
            }
            if (year < 2000) {
                return@withContext Result.failure(Exception("Year must be >= 2000"))
            }
            if (amount < 0) {
                return@withContext Result.failure(Exception("Budget amount must be zero or positive"))
            }

            val request = BudgetCreateRequest(categoryId, month, year, amount)
            val response = budgetApi.createBudget(request)

            val entity = BudgetEntity(
                id = response.id,
                userId = userId,
                categoryId = response.categoryId,
                month = response.month,
                year = response.year,
                budgetAmount = response.budgetAmount,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            budgetDao.insertBudget(entity)

            val datePrefix = String.format("%04d-%02d-", response.year, response.month)
            val spent = transactionDao.getSpentForCategoryAndPeriod(userId, response.categoryId, datePrefix + "%") ?: 0.0

            val remaining = response.budgetAmount - spent
            val percent = if (response.budgetAmount > 0) (spent / response.budgetAmount) * 100 else 0.0
            val status = when {
                percent > 100 -> "EXCEEDED"
                percent >= 80 -> "WARNING"
                else -> "NORMAL"
            }

            Result.success(
                Budget(
                    id = response.id,
                    userId = userId,
                    categoryId = response.categoryId,
                    month = response.month,
                    year = response.year,
                    budgetAmount = response.budgetAmount,
                    spentAmount = spent,
                    remainingAmount = remaining,
                    usagePercentage = percent,
                    status = status
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncBudgets(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val remoteBudgets = budgetApi.getBudgets()
            val entities = remoteBudgets.map { response ->
                BudgetEntity(
                    id = response.id,
                    userId = userId,
                    categoryId = response.categoryId,
                    month = response.month,
                    year = response.year,
                    budgetAmount = response.budgetAmount,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            }
            budgetDao.clearAll()
            budgetDao.insertBudgets(entities)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
