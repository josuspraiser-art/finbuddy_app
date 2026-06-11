package com.example.findbuddy.ui.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.findbuddy.data.local.JwtDecoder
import com.example.findbuddy.data.local.TokenManager
import com.example.findbuddy.domain.repository.AccountRepository
import com.example.findbuddy.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _state = MutableStateFlow(TransactionState())
    val state: StateFlow<TransactionState> = _state.asStateFlow()

    private val userId: String
        get() {
            val token = tokenManager.getToken() ?: ""
            return JwtDecoder.getUserIdFromToken(token)
        }

    init {
        handleIntent(TransactionIntent.LoadInitialData)
    }

    fun handleIntent(intent: TransactionIntent) {
        when (intent) {
            is TransactionIntent.LoadInitialData -> {
                loadInitialData()
            }
            is TransactionIntent.LoadDetails -> {
                loadDetails(intent.id)
            }
            is TransactionIntent.ChangeType -> {
                _state.value = _state.value.copy(type = intent.type, errorMsg = null)
            }
            is TransactionIntent.ChangeAmount -> {
                _state.value = _state.value.copy(amount = intent.amount, errorMsg = null)
            }
            is TransactionIntent.ChangeDate -> {
                _state.value = _state.value.copy(date = intent.date, errorMsg = null)
            }
            is TransactionIntent.ChangeDescription -> {
                _state.value = _state.value.copy(description = intent.description, errorMsg = null)
            }
            is TransactionIntent.SelectAccount -> {
                _state.value = _state.value.copy(selectedAccountId = intent.id, errorMsg = null)
            }
            is TransactionIntent.SelectDestinationAccount -> {
                _state.value = _state.value.copy(selectedDestinationAccountId = intent.id, errorMsg = null)
            }
            is TransactionIntent.SelectCategory -> {
                _state.value = _state.value.copy(selectedCategoryId = intent.id, errorMsg = null)
            }
            is TransactionIntent.Save -> {
                saveTransaction()
            }
            is TransactionIntent.Delete -> {
                deleteTransaction()
            }
            is TransactionIntent.ClearError -> {
                _state.value = _state.value.copy(errorMsg = null)
            }
        }
    }

    private fun loadInitialData() {
        // Default the date to today (YYYY-MM-DD format)
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
        _state.value = _state.value.copy(date = today, isLoading = true)

        viewModelScope.launch {
            // Load accounts
            accountRepository.getAccounts(userId).collect { list ->
                _state.value = _state.value.copy(
                    accountsList = list,
                    // Auto-select first account if none selected
                    selectedAccountId = _state.value.selectedAccountId.ifEmpty {
                        list.firstOrNull()?.id ?: ""
                    },
                    selectedDestinationAccountId = _state.value.selectedDestinationAccountId.ifEmpty {
                        if (list.size > 1) list[1].id else ""
                    }
                )
            }
        }

        viewModelScope.launch {
            // Load categories from API
            val categoriesResult = transactionRepository.getCategories()
            _state.value = _state.value.copy(isLoading = false)
            categoriesResult.onSuccess { categories ->
                _state.value = _state.value.copy(
                    categoriesList = categories,
                    selectedCategoryId = _state.value.selectedCategoryId.ifEmpty {
                        categories.firstOrNull()?.id ?: ""
                    }
                )
            }.onFailure { error ->
                _state.value = _state.value.copy(errorMsg = error.message ?: "Failed to load categories")
            }
        }
    }

    private fun loadDetails(transactionId: String) {
        _state.value = _state.value.copy(
            isLoading = true,
            isEditMode = true,
            editingTransactionId = transactionId
        )

        viewModelScope.launch {
            // Retrieve list from local DB flow and find target transaction
            transactionRepository.getTransactions(userId).collect { transactions ->
                val target = transactions.find { it.id == transactionId }
                if (target != null) {
                    _state.value = _state.value.copy(
                        amount = target.amount.toString(),
                        date = target.date,
                        description = target.description,
                        type = target.type,
                        selectedAccountId = target.accountId,
                        selectedDestinationAccountId = target.destinationAccountId ?: "",
                        selectedCategoryId = target.categoryId ?: "",
                        isLoading = false
                    )
                } else {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMsg = "Transaction not found"
                    )
                }
            }
        }
    }

    private fun saveTransaction() {
        val currentState = _state.value
        val amountVal = currentState.amount.toDoubleOrNull()
        if (amountVal == null || amountVal <= 0) {
            _state.value = _state.value.copy(errorMsg = "Amount must be greater than zero")
            return
        }
        if (currentState.description.isBlank()) {
            _state.value = _state.value.copy(errorMsg = "Description cannot be empty")
            return
        }
        if (currentState.date.isBlank()) {
            _state.value = _state.value.copy(errorMsg = "Date cannot be empty")
            return
        }

        _state.value = _state.value.copy(isLoading = true)

        viewModelScope.launch {
            val result = if (currentState.type == "TRANSFER") {
                if (currentState.selectedAccountId.isEmpty() || currentState.selectedDestinationAccountId.isEmpty()) {
                    _state.value = _state.value.copy(isLoading = false, errorMsg = "Please select both accounts")
                    return@launch
                }
                if (currentState.selectedAccountId == currentState.selectedDestinationAccountId) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMsg = "Source and destination accounts must not be identical"
                    )
                    return@launch
                }
                transactionRepository.createTransfer(
                    userId = userId,
                    sourceAccountId = currentState.selectedAccountId,
                    destinationAccountId = currentState.selectedDestinationAccountId,
                    amount = amountVal,
                    description = currentState.description,
                    date = currentState.date
                )
            } else {
                if (currentState.selectedAccountId.isEmpty()) {
                    _state.value = _state.value.copy(isLoading = false, errorMsg = "Please select an account")
                    return@launch
                }
                if (currentState.selectedCategoryId.isEmpty()) {
                    _state.value = _state.value.copy(isLoading = false, errorMsg = "Please select a category")
                    return@launch
                }

                if (currentState.isEditMode && currentState.editingTransactionId != null) {
                    transactionRepository.updateTransaction(
                        id = currentState.editingTransactionId,
                        userId = userId,
                        type = currentState.type,
                        amount = amountVal,
                        accountId = currentState.selectedAccountId,
                        categoryId = currentState.selectedCategoryId,
                        description = currentState.description,
                        date = currentState.date
                    )
                } else {
                    transactionRepository.createTransaction(
                        userId = userId,
                        type = currentState.type,
                        amount = amountVal,
                        accountId = currentState.selectedAccountId,
                        categoryId = currentState.selectedCategoryId,
                        description = currentState.description,
                        date = currentState.date
                    )
                }
            }

            _state.value = _state.value.copy(isLoading = false)
            result.onSuccess {
                _state.value = _state.value.copy(isSuccess = true, errorMsg = null)
            }.onFailure { error ->
                _state.value = _state.value.copy(errorMsg = error.message ?: "Failed to save transaction")
            }
        }
    }

    private fun deleteTransaction() {
        val currentState = _state.value
        val txId = currentState.editingTransactionId ?: return

        _state.value = _state.value.copy(isLoading = true)
        viewModelScope.launch {
            val result = transactionRepository.deleteTransaction(txId)
            _state.value = _state.value.copy(isLoading = false)
            result.onSuccess {
                _state.value = _state.value.copy(isSuccess = true, errorMsg = null)
            }.onFailure { error ->
                _state.value = _state.value.copy(errorMsg = error.message ?: "Failed to delete transaction")
            }
        }
    }
}
