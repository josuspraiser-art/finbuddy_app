package com.example.findbuddy.domain.repository

import com.example.findbuddy.domain.model.Account
import kotlinx.coroutines.flow.Flow

interface AccountRepository {
    fun getAccounts(userId: String): Flow<List<Account>>
    suspend fun createAccount(userId: String, name: String, type: String, balance: Double): Result<Account>
    suspend fun updateAccount(id: String, userId: String, name: String, type: String, balance: Double): Result<Account>
    suspend fun deleteAccount(id: String): Result<Unit>
    suspend fun syncAccounts(userId: String): Result<Unit>
}
