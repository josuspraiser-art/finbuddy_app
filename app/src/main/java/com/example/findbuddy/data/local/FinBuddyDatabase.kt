package com.example.findbuddy.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.findbuddy.data.local.dao.AccountDao
import com.example.findbuddy.data.local.dao.TransactionDao
import com.example.findbuddy.data.local.entity.AccountEntity
import com.example.findbuddy.data.local.entity.TransactionEntity
import com.example.findbuddy.data.local.entity.TransferEntity

@Database(
    entities = [
        AccountEntity::class,
        TransactionEntity::class,
        TransferEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class FinBuddyDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun transactionDao(): TransactionDao
}

