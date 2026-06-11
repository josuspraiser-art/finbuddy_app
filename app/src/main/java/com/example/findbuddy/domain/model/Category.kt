package com.example.findbuddy.domain.model

data class Category(
    val id: String,
    val userId: String,
    val categoryName: String,
    val categoryType: String, // INCOME or EXPENSE
    val isSystem: Boolean
)
