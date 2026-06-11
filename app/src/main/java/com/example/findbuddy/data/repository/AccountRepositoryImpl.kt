package com.example.findbuddy.data.repository

import com.example.findbuddy.data.api.AccountApi
import com.example.findbuddy.data.local.dao.AccountDao
import com.example.findbuddy.data.local.entity.AccountEntity
import com.example.findbuddy.data.model.AccountCreateRequest
import com.example.findbuddy.data.model.AccountUpdateRequest
import com.example.findbuddy.domain.model.Account
import com.example.findbuddy.domain.repository.AccountRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepositoryImpl @Inject constructor(
    private val accountDao: AccountDao,
    private val accountApi: AccountApi
) : AccountRepository {

    override fun getAccounts(userId: String): Flow<List<Account>> {
        return accountDao.getAccounts(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun createAccount(
        userId: String,
        name: String,
        type: String,
        balance: Double
    ): Result<Account> = withContext(Dispatchers.IO) {
        try {
            // Enforce account name uniqueness per user locally
            val existing = accountDao.getAccountByName(userId, name)
            if (existing != null) {
                return@withContext Result.failure(Exception("Account name must be unique"))
            }

            val response = accountApi.createAccount(
                AccountCreateRequest(
                    accountName = name,
                    accountType = type,
                    openingBalance = balance
                )
            )

            val entity = AccountEntity(
                id = response.id,
                userId = userId,
                accountName = response.accountName,
                accountType = response.accountType,
                openingBalance = response.openingBalance,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            accountDao.insertAccount(entity)
            Result.success(entity.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateAccount(
        id: String,
        userId: String,
        name: String,
        type: String,
        balance: Double
    ): Result<Account> = withContext(Dispatchers.IO) {
        try {
            // Check if name is being changed and if new name is already taken
            val existing = accountDao.getAccountByName(userId, name)
            if (existing != null && existing.id != id) {
                return@withContext Result.failure(Exception("Account name must be unique"))
            }

            val response = accountApi.updateAccount(
                id = id,
                request = AccountUpdateRequest(
                    accountName = name,
                    accountType = type,
                    openingBalance = balance
                )
            )

            val entity = AccountEntity(
                id = response.id,
                userId = userId,
                accountName = response.accountName,
                accountType = response.accountType,
                openingBalance = response.openingBalance,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            accountDao.insertAccount(entity)
            Result.success(entity.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteAccount(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Call API first. If there are transactions, the API will fail with a 400 Bad Request.
            accountApi.deleteAccount(id)
            // If API deletion succeeds, delete locally as well
            accountDao.deleteAccount(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncAccounts(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val remoteAccounts = accountApi.getAccounts()
            val entities = remoteAccounts.map { response ->
                AccountEntity(
                    id = response.id,
                    userId = userId,
                    accountName = response.accountName,
                    accountType = response.accountType,
                    openingBalance = response.openingBalance,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            }
            // Overwrite local records with remote truth
            accountDao.clearAll()
            accountDao.insertAccounts(entities)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun AccountEntity.toDomain(): Account {
        return Account(
            id = id,
            userId = userId,
            accountName = accountName,
            accountType = accountType,
            openingBalance = openingBalance,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}
