package com.example.findbuddy.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val type: String, // INCOME or EXPENSE
    val accountId: String,
    val categoryId: String,
    val amount: Double,
    val date: String, // YYYY-MM-DD
    val description: String,
    val createdAt: Long,
    val updatedAt: Long
)
