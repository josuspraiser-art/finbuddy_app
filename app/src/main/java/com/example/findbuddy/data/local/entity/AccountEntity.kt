package com.example.findbuddy.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val accountName: String,
    val accountType: String,
    val openingBalance: Double,
    val createdAt: Long,
    val updatedAt: Long
)
