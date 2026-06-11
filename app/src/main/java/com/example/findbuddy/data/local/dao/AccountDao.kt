package com.example.findbuddy.data.local.dao

import androidx.room.*
import com.example.findbuddy.data.local.entity.AccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {

    @Query("SELECT * FROM accounts WHERE userId = :userId ORDER BY createdAt DESC")
    fun getAccounts(userId: String): Flow<List<AccountEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAccount(account: AccountEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAccounts(accounts: List<AccountEntity>): List<Long>

    @Update
    fun updateAccount(account: AccountEntity): Int

    @Query("DELETE FROM accounts WHERE id = :accountId")
    fun deleteAccount(accountId: String): Int

    @Query("SELECT * FROM accounts WHERE id = :accountId LIMIT 1")
    fun getAccountById(accountId: String): AccountEntity?

    @Query("SELECT * FROM accounts WHERE userId = :userId AND accountName = :name LIMIT 1")
    fun getAccountByName(userId: String, name: String): AccountEntity?

    @Query("DELETE FROM accounts")
    fun clearAll(): Int
}
