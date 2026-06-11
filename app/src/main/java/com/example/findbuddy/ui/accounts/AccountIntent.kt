package com.example.findbuddy.ui.accounts

import com.example.findbuddy.domain.model.Account

sealed class AccountIntent {
    object LoadAccounts : AccountIntent()
    data class CreateAccount(val name: String, val type: String, val balance: Double) : AccountIntent()
    data class UpdateAccount(val id: String, val name: String, val type: String, val balance: Double) : AccountIntent()
    data class DeleteAccount(val id: String) : AccountIntent()
    object OpenAddDialog : AccountIntent()
    data class OpenEditDialog(val account: Account) : AccountIntent()
    object DismissDialog : AccountIntent()
    object ClearError : AccountIntent()
}
