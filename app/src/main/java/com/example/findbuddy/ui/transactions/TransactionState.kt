package com.example.findbuddy.ui.transactions

import com.example.findbuddy.domain.model.Account
import com.example.findbuddy.data.model.CategoryResponse

data class TransactionState(
    val amount: String = "",
    val date: String = "",
    val description: String = "",
    val type: String = "EXPENSE", // EXPENSE, INCOME, TRANSFER
    val selectedAccountId: String = "",
    val selectedDestinationAccountId: String = "",
    val selectedCategoryId: String = "",
    val accountsList: List<Account> = emptyList(),
    val categoriesList: List<CategoryResponse> = emptyList(),
    val isLoading: Boolean = false,
    val errorMsg: String? = null,
    val isSuccess: Boolean = false,
    val isEditMode: Boolean = false,
    val editingTransactionId: String? = null
)
