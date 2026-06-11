package com.example.findbuddy.ui.transactions

sealed class TransactionIntent {
    object LoadInitialData : TransactionIntent()
    data class LoadDetails(val id: String) : TransactionIntent()
    data class ChangeType(val type: String) : TransactionIntent()
    data class ChangeAmount(val amount: String) : TransactionIntent()
    data class ChangeDate(val date: String) : TransactionIntent()
    data class ChangeDescription(val description: String) : TransactionIntent()
    data class SelectAccount(val id: String) : TransactionIntent()
    data class SelectDestinationAccount(val id: String) : TransactionIntent()
    data class SelectCategory(val id: String) : TransactionIntent()
    object Save : TransactionIntent()
    object Delete : TransactionIntent()
    object ClearError : TransactionIntent()
}
