package com.example.findbuddy.ui.categories

import com.example.findbuddy.domain.model.Category
import com.example.findbuddy.domain.model.Budget

data class CategoryState(
    val categoriesList: List<Category> = emptyList(),
    val budgetsList: List<Budget> = emptyList(),
    val totalSpent: Double = 0.0,
    val isLoading: Boolean = false,
    val errorMsg: String? = null,
    val showCreateDialog: Boolean = false,
    val newCategoryName: String = "",
    val newCategoryType: String = "EXPENSE", // EXPENSE or INCOME
    val showLimitDialog: Boolean = false,
    val selectedCategory: Category? = null,
    val limitAmount: String = "",
    val isSuccess: Boolean = false
)
