package com.example.findbuddy.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transfers")
data class TransferEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val sourceAccountId: String,
    val destinationAccountId: String,
    val amount: Double,
    val date: String, // YYYY-MM-DD
    val description: String,
    val createdAt: Long
)
