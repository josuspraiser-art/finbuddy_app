package com.example.findbuddy.ui.accounts

import com.example.findbuddy.domain.model.Account

data class AccountState(
    val accountsList: List<Account> = emptyList(),
    val isLoading: Boolean = false,
    val errorMsg: String? = null,
    val showAddDialog: Boolean = false,
    val showEditDialog: Boolean = false,
    val selectedAccount: Account? = null
)
