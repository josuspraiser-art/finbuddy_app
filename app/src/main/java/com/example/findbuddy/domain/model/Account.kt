package com.example.findbuddy.domain.model

data class Account(
    val id: String,
    val userId: String,
    val accountName: String,
    val accountType: String,
    val openingBalance: Double,
    val createdAt: Long,
    val updatedAt: Long
)
