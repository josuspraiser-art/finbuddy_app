package com.example.findbuddy.ui.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.findbuddy.data.local.JwtDecoder
import com.example.findbuddy.data.local.TokenManager
import com.example.findbuddy.domain.repository.AccountRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _state = MutableStateFlow(AccountState())
    val state: StateFlow<AccountState> = _state.asStateFlow()

    private val userId: String
        get() {
            val token = tokenManager.getToken() ?: ""
            return JwtDecoder.getUserIdFromToken(token)
        }

    init {
        handleIntent(AccountIntent.LoadAccounts)
    }

    fun handleIntent(intent: AccountIntent) {
        when (intent) {
            is AccountIntent.LoadAccounts -> {
                loadAccounts()
            }
            is AccountIntent.CreateAccount -> {
                createAccount(intent.name, intent.type, intent.balance)
            }
            is AccountIntent.UpdateAccount -> {
                updateAccount(intent.id, intent.name, intent.type, intent.balance)
            }
            is AccountIntent.DeleteAccount -> {
                deleteAccount(intent.id)
            }
            is AccountIntent.OpenAddDialog -> {
                _state.value = _state.value.copy(showAddDialog = true, errorMsg = null)
            }
            is AccountIntent.OpenEditDialog -> {
                _state.value = _state.value.copy(
                    showEditDialog = true,
                    selectedAccount = intent.account,
                    errorMsg = null
                )
            }
            is AccountIntent.DismissDialog -> {
                _state.value = _state.value.copy(
                    showAddDialog = false,
                    showEditDialog = false,
                    selectedAccount = null,
                    errorMsg = null
                )
            }
            is AccountIntent.ClearError -> {
                _state.value = _state.value.copy(errorMsg = null)
            }
        }
    }

    private fun loadAccounts() {
        _state.value = _state.value.copy(isLoading = true)
        viewModelScope.launch {
            accountRepository.getAccounts(userId).collect { list ->
                _state.value = _state.value.copy(
                    accountsList = list,
                    isLoading = false
                )
            }
        }
        viewModelScope.launch {
            accountRepository.syncAccounts(userId)
        }
    }

    private fun createAccount(name: String, type: String, balance: Double) {
        if (name.isBlank()) {
            _state.value = _state.value.copy(errorMsg = "Account name cannot be empty")
            return
        }

        _state.value = _state.value.copy(isLoading = true)
        viewModelScope.launch {
            val result = accountRepository.createAccount(userId, name, type, balance)
            _state.value = _state.value.copy(isLoading = false)
            result.onSuccess {
                _state.value = _state.value.copy(showAddDialog = false, errorMsg = null)
            }.onFailure { error ->
                _state.value = _state.value.copy(errorMsg = error.message ?: "Failed to create account")
            }
        }
    }

    private fun updateAccount(id: String, name: String, type: String, balance: Double) {
        if (name.isBlank()) {
            _state.value = _state.value.copy(errorMsg = "Account name cannot be empty")
            return
        }

        _state.value = _state.value.copy(isLoading = true)
        viewModelScope.launch {
            val result = accountRepository.updateAccount(id, userId, name, type, balance)
            _state.value = _state.value.copy(isLoading = false)
            result.onSuccess {
                _state.value = _state.value.copy(
                    showEditDialog = false,
                    selectedAccount = null,
                    errorMsg = null
                )
            }.onFailure { error ->
                _state.value = _state.value.copy(errorMsg = error.message ?: "Failed to update account")
            }
        }
    }

    private fun deleteAccount(id: String) {
        _state.value = _state.value.copy(isLoading = true)
        viewModelScope.launch {
            val result = accountRepository.deleteAccount(id)
            _state.value = _state.value.copy(isLoading = false)
            result.onSuccess {
                _state.value = _state.value.copy(
                    showEditDialog = false,
                    selectedAccount = null,
                    errorMsg = null
                )
            }.onFailure { error ->
                _state.value = _state.value.copy(
                    errorMsg = error.message ?: "Cannot delete account. Verify if active transactions exist."
                )
            }
        }
    }
}
