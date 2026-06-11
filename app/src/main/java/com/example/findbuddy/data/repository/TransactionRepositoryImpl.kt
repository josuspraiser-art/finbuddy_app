package com.example.findbuddy.data.repository

import com.example.findbuddy.data.api.TransactionApi
import com.example.findbuddy.data.local.dao.AccountDao
import com.example.findbuddy.data.local.dao.TransactionDao
import com.example.findbuddy.data.local.entity.TransactionEntity
import com.example.findbuddy.data.local.entity.TransferEntity
import com.example.findbuddy.data.model.*
import com.example.findbuddy.domain.model.Transaction
import com.example.findbuddy.domain.repository.AccountRepository
import com.example.findbuddy.domain.repository.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao,
    private val accountDao: AccountDao,
    private val transactionApi: TransactionApi,
    private val accountRepository: AccountRepository
) : TransactionRepository {

    private val categoryCache = ConcurrentHashMap<String, String>()

    private fun getStaticCategoryName(categoryId: String): String? {
        // Fallback or static mapping matching standard categories
        val defaultCategories = mapOf(
            "Grocery" to "Grocery",
            "Rent" to "Rent",
            "Petrol" to "Petrol",
            "Restaurant" to "Restaurant",
            "Clothing" to "Clothing",
            "Accessories" to "Accessories",
            "Home Appliances" to "Home Appliances",
            "Others" to "Others"
        )
        return defaultCategories[categoryId]
    }

    override fun getTransactions(userId: String): Flow<List<Transaction>> {
        val transactionsFlow = transactionDao.getTransactions(userId)
        val transfersFlow = transactionDao.getTransfers(userId)

        return combine(transactionsFlow, transfersFlow) { txList, tfList ->
            val domainTx = txList.map { tx ->
                val accName = accountDao.getAccountById(tx.accountId)?.accountName ?: "Unknown Account"
                val catName = categoryCache[tx.categoryId] ?: getStaticCategoryName(tx.categoryId) ?: "Grocery"
                Transaction(
                    id = tx.id,
                    userId = tx.userId,
                    type = tx.type,
                    accountId = tx.accountId,
                    accountName = accName,
                    categoryId = tx.categoryId,
                    categoryName = catName,
                    amount = tx.amount,
                    date = tx.date,
                    description = tx.description,
                    createdAt = tx.createdAt,
                    updatedAt = tx.updatedAt
                )
            }
            val domainTf = tfList.map { tf ->
                val srcAccName = accountDao.getAccountById(tf.sourceAccountId)?.accountName ?: "Unknown Account"
                val destAccName = accountDao.getAccountById(tf.destinationAccountId)?.accountName ?: "Unknown Account"
                Transaction(
                    id = tf.id,
                    userId = tf.userId,
                    type = "TRANSFER",
                    accountId = tf.sourceAccountId,
                    accountName = srcAccName,
                    destinationAccountId = tf.destinationAccountId,
                    destinationAccountName = destAccName,
                    amount = tf.amount,
                    date = tf.date,
                    description = tf.description,
                    createdAt = tf.createdAt,
                    updatedAt = tf.createdAt
                )
            }

            (domainTx + domainTf).sortedWith(
                compareByDescending<Transaction> { it.date }
                    .thenByDescending { it.createdAt }
            )
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun createTransaction(
        userId: String,
        type: String,
        amount: Double,
        accountId: String,
        categoryId: String,
        description: String,
        date: String
    ): Result<Transaction> = withContext(Dispatchers.IO) {
        try {
            if (amount <= 0) return@withContext Result.failure(Exception("Amount must be greater than zero"))
            if (description.isBlank()) return@withContext Result.failure(Exception("Description cannot be empty"))
            if (isFutureDate(date)) return@withContext Result.failure(Exception("Date cannot be in the future"))

            val response = transactionApi.createTransaction(
                TransactionCreateRequest(
                    type = type,
                    amount = amount,
                    accountId = accountId,
                    categoryId = categoryId,
                    description = description,
                    date = date
                )
            )

            val entity = TransactionEntity(
                id = response.id,
                userId = userId,
                type = response.type,
                accountId = response.accountId,
                categoryId = response.categoryId,
                amount = response.amount,
                date = response.date,
                description = response.description,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            transactionDao.insertTransaction(entity)

            // Force accounts to refresh updated balances
            accountRepository.syncAccounts(userId)

            val accName = accountDao.getAccountById(response.accountId)?.accountName ?: "Unknown Account"
            val catName = categoryCache[response.categoryId] ?: getStaticCategoryName(response.categoryId) ?: "Grocery"
            Result.success(
                Transaction(
                    id = response.id,
                    userId = userId,
                    type = response.type,
                    accountId = response.accountId,
                    accountName = accName,
                    categoryId = response.categoryId,
                    categoryName = catName,
                    amount = response.amount,
                    date = response.date,
                    description = response.description,
                    createdAt = entity.createdAt,
                    updatedAt = entity.updatedAt
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateTransaction(
        id: String,
        userId: String,
        type: String,
        amount: Double,
        accountId: String,
        categoryId: String,
        description: String,
        date: String
    ): Result<Transaction> = withContext(Dispatchers.IO) {
        try {
            if (amount <= 0) return@withContext Result.failure(Exception("Amount must be greater than zero"))
            if (description.isBlank()) return@withContext Result.failure(Exception("Description cannot be empty"))
            if (isFutureDate(date)) return@withContext Result.failure(Exception("Date cannot be in the future"))

            val response = transactionApi.updateTransaction(
                id = id,
                request = TransactionUpdateRequest(
                    type = type,
                    amount = amount,
                    accountId = accountId,
                    categoryId = categoryId,
                    description = description,
                    date = date
                )
            )

            val entity = TransactionEntity(
                id = response.id,
                userId = userId,
                type = response.type,
                accountId = response.accountId,
                categoryId = response.categoryId,
                amount = response.amount,
                date = response.date,
                description = response.description,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            transactionDao.insertTransaction(entity)

            // Force accounts to refresh updated balances
            accountRepository.syncAccounts(userId)

            val accName = accountDao.getAccountById(response.accountId)?.accountName ?: "Unknown Account"
            val catName = categoryCache[response.categoryId] ?: getStaticCategoryName(response.categoryId) ?: "Grocery"
            Result.success(
                Transaction(
                    id = response.id,
                    userId = userId,
                    type = response.type,
                    accountId = response.accountId,
                    accountName = accName,
                    categoryId = response.categoryId,
                    categoryName = catName,
                    amount = response.amount,
                    date = response.date,
                    description = response.description,
                    createdAt = entity.createdAt,
                    updatedAt = entity.updatedAt
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteTransaction(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val localTx = transactionDao.getTransactionById(id)
            val userId = localTx?.userId

            transactionApi.deleteTransaction(id)
            transactionDao.deleteTransaction(id)

            if (userId != null) {
                accountRepository.syncAccounts(userId)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createTransfer(
        userId: String,
        sourceAccountId: String,
        destinationAccountId: String,
        amount: Double,
        description: String,
        date: String
    ): Result<Transaction> = withContext(Dispatchers.IO) {
        try {
            if (sourceAccountId == destinationAccountId) {
                return@withContext Result.failure(Exception("Source and destination accounts must not be identical"))
            }
            if (amount <= 0) return@withContext Result.failure(Exception("Amount must be greater than zero"))
            if (isFutureDate(date)) return@withContext Result.failure(Exception("Date cannot be in the future"))

            val response = transactionApi.createTransfer(
                TransferCreateRequest(
                    sourceAccountId = sourceAccountId,
                    destinationAccountId = destinationAccountId,
                    amount = amount,
                    description = description,
                    date = date
                )
            )

            val entity = TransferEntity(
                id = response.id,
                userId = userId,
                sourceAccountId = response.sourceAccountId,
                destinationAccountId = response.destinationAccountId,
                amount = response.amount,
                date = response.date,
                description = response.description,
                createdAt = System.currentTimeMillis()
            )
            transactionDao.insertTransfer(entity)

            // Force accounts to refresh updated balances
            accountRepository.syncAccounts(userId)

            val srcAccName = accountDao.getAccountById(response.sourceAccountId)?.accountName ?: "Unknown Account"
            val destAccName = accountDao.getAccountById(response.destinationAccountId)?.accountName ?: "Unknown Account"
            Result.success(
                Transaction(
                    id = response.id,
                    userId = userId,
                    type = "TRANSFER",
                    accountId = response.sourceAccountId,
                    accountName = srcAccName,
                    destinationAccountId = response.destinationAccountId,
                    destinationAccountName = destAccName,
                    amount = response.amount,
                    date = response.date,
                    description = response.description,
                    createdAt = entity.createdAt,
                    updatedAt = entity.createdAt
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncAll(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Seed categories cache first
            val remoteCategories = transactionApi.getCategories()
            remoteCategories.forEach { category ->
                categoryCache[category.id] = category.categoryName
            }

            // Sync transactions from remote
            val remoteTx = transactionApi.getTransactions()
            val txEntities = remoteTx.map { response ->
                TransactionEntity(
                    id = response.id,
                    userId = userId,
                    type = response.type,
                    accountId = response.accountId,
                    categoryId = response.categoryId,
                    amount = response.amount,
                    date = response.date,
                    description = response.description,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            }

            transactionDao.clearTransactions()
            transactionDao.insertTransactions(txEntities)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCategories(): Result<List<CategoryResponse>> = withContext(Dispatchers.IO) {
        try {
            val response = transactionApi.getCategories()
            response.forEach { category ->
                categoryCache[category.id] = category.categoryName
            }
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun isFutureDate(dateString: String): Boolean {
        return try {
            val parts = dateString.split("-")
            val year = parts[0].toInt()
            val month = parts[1].toInt()
            val day = parts[2].toInt()

            val now = java.util.Calendar.getInstance()
            val currentYear = now.get(java.util.Calendar.YEAR)
            val currentMonth = now.get(java.util.Calendar.MONTH) + 1
            val currentDay = now.get(java.util.Calendar.DAY_OF_MONTH)

            if (year > currentYear) return true
            if (year == currentYear) {
                if (month > currentMonth) return true
                if (month == currentMonth) {
                    if (day > currentDay) return true
                }
            }
            false
        } catch (e: Exception) {
            false
        }
    }
}
