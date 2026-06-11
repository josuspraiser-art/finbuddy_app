package com.example.findbuddy.domain.repository

import com.example.findbuddy.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getCategories(userId: String): Flow<List<Category>>
    suspend fun createCategory(userId: String, name: String, type: String): Result<Category>
    suspend fun syncCategories(userId: String): Result<Unit>
}
