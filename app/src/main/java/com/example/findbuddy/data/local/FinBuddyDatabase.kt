package com.example.findbuddy.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.findbuddy.data.local.dao.AccountDao
import com.example.findbuddy.data.local.dao.TransactionDao
import com.example.findbuddy.data.local.dao.CategoryDao
import com.example.findbuddy.data.local.dao.BudgetDao
import com.example.findbuddy.data.local.entity.AccountEntity
import com.example.findbuddy.data.local.entity.TransactionEntity
import com.example.findbuddy.data.local.entity.TransferEntity
import com.example.findbuddy.data.local.entity.CategoryEntity
import com.example.findbuddy.data.local.entity.BudgetEntity

@Database(
    entities = [
        AccountEntity::class,
        TransactionEntity::class,
        TransferEntity::class,
        CategoryEntity::class,
        BudgetEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class FinBuddyDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao
}

