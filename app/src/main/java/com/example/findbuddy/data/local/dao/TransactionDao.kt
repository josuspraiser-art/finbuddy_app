package com.example.findbuddy.data.local.dao

import androidx.room.*
import com.example.findbuddy.data.local.entity.TransactionEntity
import com.example.findbuddy.data.local.entity.TransferEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY date DESC, createdAt DESC")
    fun getTransactions(userId: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transfers WHERE userId = :userId ORDER BY date DESC, createdAt DESC")
    fun getTransfers(userId: String): Flow<List<TransferEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTransaction(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTransactions(transactions: List<TransactionEntity>): List<Long>

    @Query("DELETE FROM transactions WHERE id = :id")
    fun deleteTransaction(id: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTransfer(transfer: TransferEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTransfers(transfers: List<TransferEntity>): List<Long>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    fun getTransactionById(id: String): TransactionEntity?

    @Query("SELECT * FROM transfers WHERE id = :id LIMIT 1")
    fun getTransferById(id: String): TransferEntity?

    @Query("SELECT SUM(amount) FROM transactions WHERE userId = :userId AND categoryId = :categoryId AND type = 'EXPENSE' AND date LIKE :datePattern")
    fun getSpentForCategoryAndPeriod(userId: String, categoryId: String, datePattern: String): Double?

    @Query("DELETE FROM transactions")
    fun clearTransactions(): Int

    @Query("DELETE FROM transfers")
    fun clearTransfers(): Int
}
