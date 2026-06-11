package com.example.findbuddy.data.local.dao

import androidx.room.*
import com.example.findbuddy.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories WHERE userId = :userId ORDER BY isSystem DESC, categoryName ASC")
    fun getCategories(userId: String): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCategory(category: CategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCategories(categories: List<CategoryEntity>): List<Long>

    @Query("SELECT * FROM categories WHERE userId = :userId AND categoryName = :name LIMIT 1")
    fun getCategoryByName(userId: String, name: String): CategoryEntity?

    @Query("DELETE FROM categories WHERE id = :id")
    fun deleteCategory(id: String): Int

    @Query("DELETE FROM categories")
    fun clearAll(): Int
}
