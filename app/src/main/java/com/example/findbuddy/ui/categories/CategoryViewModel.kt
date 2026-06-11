package com.example.findbuddy.ui.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.findbuddy.data.local.JwtDecoder
import com.example.findbuddy.data.local.TokenManager
import com.example.findbuddy.domain.repository.CategoryRepository
import com.example.findbuddy.domain.repository.BudgetRepository
import com.example.findbuddy.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _state = MutableStateFlow(CategoryState())
    val state: StateFlow<CategoryState> = _state.asStateFlow()

    private val userId: String
        get() {
            val token = tokenManager.getToken() ?: ""
            return JwtDecoder.getUserIdFromToken(token)
        }

    private val currentMonth: Int
        get() = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1

    private val currentYear: Int
        get() = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)

    init {
        handleIntent(CategoryIntent.LoadCategories)
    }

    fun handleIntent(intent: CategoryIntent) {
        when (intent) {
            is CategoryIntent.LoadCategories -> {
                loadCategories()
            }
            is CategoryIntent.OpenCreateDialog -> {
                _state.value = _state.value.copy(
                    showCreateDialog = true,
                    newCategoryName = "",
                    newCategoryType = "EXPENSE",
                    errorMsg = null
                )
            }
            is CategoryIntent.DismissCreateDialog -> {
                _state.value = _state.value.copy(
                    showCreateDialog = false,
                    newCategoryName = "",
                    errorMsg = null
                )
            }
            is CategoryIntent.ChangeNewName -> {
                _state.value = _state.value.copy(newCategoryName = intent.name, errorMsg = null)
            }
            is CategoryIntent.ChangeNewType -> {
                _state.value = _state.value.copy(newCategoryType = intent.type, errorMsg = null)
            }
            is CategoryIntent.CreateCategory -> {
                createCategory()
            }
            is CategoryIntent.ClearError -> {
                _state.value = _state.value.copy(errorMsg = null)
            }
            is CategoryIntent.OpenLimitDialog -> {
                val budget = _state.value.budgetsList.find { it.categoryId == intent.category.id }
                val currentLimit = if (budget != null && budget.budgetAmount > 0) budget.budgetAmount.toInt().toString() else ""
                _state.value = _state.value.copy(
                    showLimitDialog = true,
                    selectedCategory = intent.category,
                    limitAmount = currentLimit,
                    errorMsg = null
                )
            }
            is CategoryIntent.DismissLimitDialog -> {
                _state.value = _state.value.copy(
                    showLimitDialog = false,
                    selectedCategory = null,
                    limitAmount = "",
                    errorMsg = null
                )
            }
            is CategoryIntent.ChangeLimitAmount -> {
                _state.value = _state.value.copy(limitAmount = intent.amount, errorMsg = null)
            }
            is CategoryIntent.SaveLimit -> {
                saveLimit()
            }
        }
    }

    private fun loadCategories() {
        _state.value = _state.value.copy(isLoading = true)

        viewModelScope.launch {
            categoryRepository.getCategories(userId).collect { list ->
                _state.value = _state.value.copy(categoriesList = list)
            }
        }

        viewModelScope.launch {
            budgetRepository.getBudgetsForPeriod(userId, currentMonth, currentYear).collect { budgets ->
                _state.value = _state.value.copy(
                    budgetsList = budgets,
                    isLoading = false
                )
            }
        }

        viewModelScope.launch {
            transactionRepository.getTransactions(userId).collect { transactions ->
                val datePrefix = String.format("%04d-%02d-", currentYear, currentMonth)
                val spentSum = transactions.filter {
                    it.type == "EXPENSE" &&
                    it.date.startsWith(datePrefix)
                }.sumOf { it.amount }
                _state.value = _state.value.copy(totalSpent = spentSum)
            }
        }

        viewModelScope.launch {
            categoryRepository.syncCategories(userId)
            budgetRepository.syncBudgets(userId)
        }
    }

    private fun createCategory() {
        val currentState = _state.value
        val name = currentState.newCategoryName.trim()
        val type = currentState.newCategoryType

        if (name.isBlank()) {
            _state.value = _state.value.copy(errorMsg = "Category name cannot be empty")
            return
        }

        _state.value = _state.value.copy(isLoading = true)
        viewModelScope.launch {
            val result = categoryRepository.createCategory(userId, name, type)
            _state.value = _state.value.copy(isLoading = false)
            result.onSuccess {
                _state.value = _state.value.copy(
                    showCreateDialog = false,
                    newCategoryName = "",
                    errorMsg = null
                )
            }.onFailure { error ->
                _state.value = _state.value.copy(
                    errorMsg = error.message ?: "Failed to create category"
                )
            }
        }
    }

    private fun saveLimit() {
        val currentState = _state.value
        val category = currentState.selectedCategory ?: return
        val amountStr = currentState.limitAmount.trim()

        if (amountStr.isEmpty()) {
            _state.value = _state.value.copy(errorMsg = "Limit amount cannot be empty")
            return
        }

        val amount = amountStr.toDoubleOrNull()
        if (amount == null || amount < 0) {
            _state.value = _state.value.copy(errorMsg = "Please enter a valid positive limit")
            return
        }

        _state.value = _state.value.copy(isLoading = true)
        viewModelScope.launch {
            val result = budgetRepository.createOrUpdateBudget(
                userId = userId,
                categoryId = category.id,
                month = currentMonth,
                year = currentYear,
                amount = amount
            )
            _state.value = _state.value.copy(isLoading = false)
            result.onSuccess {
                _state.value = _state.value.copy(
                    showLimitDialog = false,
                    selectedCategory = null,
                    limitAmount = "",
                    errorMsg = null
                )
            }.onFailure { error ->
                _state.value = _state.value.copy(
                    errorMsg = error.message ?: "Failed to save budget limit"
                )
            }
        }
    }
}
