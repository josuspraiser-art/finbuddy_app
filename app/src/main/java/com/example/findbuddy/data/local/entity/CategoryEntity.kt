package com.example.findbuddy.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val categoryName: String,
    val categoryType: String, // INCOME or EXPENSE
    val isSystem: Boolean
)
