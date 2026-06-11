package com.example.findbuddy.data.repository

import com.example.findbuddy.data.api.CategoryApi
import com.example.findbuddy.data.local.dao.CategoryDao
import com.example.findbuddy.data.local.entity.CategoryEntity
import com.example.findbuddy.data.model.CategoryCreateRequest
import com.example.findbuddy.data.model.CategoryResponse
import com.example.findbuddy.domain.model.Category
import com.example.findbuddy.domain.repository.CategoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao,
    private val categoryApi: CategoryApi
) : CategoryRepository {

    override fun getCategories(userId: String): Flow<List<Category>> {
        return categoryDao.getCategories(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun createCategory(
        userId: String,
        name: String,
        type: String
    ): Result<Category> = withContext(Dispatchers.IO) {
        try {
            if (name.isBlank()) {
                return@withContext Result.failure(Exception("Category name cannot be empty"))
            }

            // Enforce name uniqueness locally
            val existing = categoryDao.getCategoryByName(userId, name)
            if (existing != null) {
                return@withContext Result.failure(Exception("Category name must be unique"))
            }

            val response = categoryApi.createCategory(
                CategoryCreateRequest(
                    categoryName = name,
                    categoryType = type
                )
            )

            val entity = CategoryEntity(
                id = response.id,
                userId = userId,
                categoryName = response.categoryName,
                categoryType = response.categoryType,
                isSystem = response.isSystem
            )

            categoryDao.insertCategory(entity)
            Result.success(entity.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncCategories(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val remoteCategories = categoryApi.getCategories()
            val entities = remoteCategories.map { response ->
                CategoryEntity(
                    id = response.id,
                    userId = userId,
                    categoryName = response.categoryName,
                    categoryType = response.categoryType,
                    isSystem = response.isSystem
                )
            }

            // Sync from remote source of truth by overwriting local DB
            categoryDao.clearAll()
            categoryDao.insertCategories(entities)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun CategoryEntity.toDomain(): Category {
        return Category(
            id = id,
            userId = userId,
            categoryName = categoryName,
            categoryType = categoryType,
            isSystem = isSystem
        )
    }
}
