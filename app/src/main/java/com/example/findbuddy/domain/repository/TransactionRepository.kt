package com.example.findbuddy.domain.repository

import com.example.findbuddy.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun getTransactions(userId: String): Flow<List<Transaction>>
    suspend fun createTransaction(
        userId: String,
        type: String,
        amount: Double,
        accountId: String,
        categoryId: String,
        description: String,
        date: String
    ): Result<Transaction>

    suspend fun updateTransaction(
        id: String,
        userId: String,
        type: String,
        amount: Double,
        accountId: String,
        categoryId: String,
        description: String,
        date: String
    ): Result<Transaction>

    suspend fun deleteTransaction(id: String): Result<Unit>

    suspend fun createTransfer(
        userId: String,
        sourceAccountId: String,
        destinationAccountId: String,
        amount: Double,
        description: String,
        date: String
    ): Result<Transaction>

    suspend fun getCategories(): Result<List<com.example.findbuddy.data.model.CategoryResponse>>

    suspend fun syncAll(userId: String): Result<Unit>
}
