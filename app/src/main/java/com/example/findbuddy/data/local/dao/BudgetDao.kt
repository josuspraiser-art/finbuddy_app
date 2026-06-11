package com.example.findbuddy.data.local.dao

import androidx.room.*
import com.example.findbuddy.data.local.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {

    @Query("SELECT * FROM budgets WHERE userId = :userId")
    fun getBudgets(userId: String): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE userId = :userId AND month = :month AND year = :year")
    fun getBudgetsForPeriod(userId: String, month: Int, year: Int): Flow<List<BudgetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBudget(budget: BudgetEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBudgets(budgets: List<BudgetEntity>): List<Long>

    @Query("SELECT * FROM budgets WHERE userId = :userId AND categoryId = :categoryId AND month = :month AND year = :year LIMIT 1")
    fun getBudgetForCategoryAndPeriod(userId: String, categoryId: String, month: Int, year: Int): BudgetEntity?

    @Query("DELETE FROM budgets WHERE id = :id")
    fun deleteBudget(id: String): Int

    @Query("DELETE FROM budgets")
    fun clearAll(): Int
}
