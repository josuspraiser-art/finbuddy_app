package com.example.findbuddy.domain.model

data class Transaction(
    val id: String,
    val userId: String,
    val type: String, // INCOME, EXPENSE, TRANSFER
    val accountId: String, // source account for transfers
    val accountName: String,
    val destinationAccountId: String? = null,
    val destinationAccountName: String? = null,
    val categoryId: String? = null,
    val categoryName: String? = null,
    val amount: Double,
    val date: String, // YYYY-MM-DD
    val description: String,
    val createdAt: Long,
    val updatedAt: Long
)
