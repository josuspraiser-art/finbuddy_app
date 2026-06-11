package com.example.findbuddy.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.findbuddy.data.local.JwtDecoder
import com.example.findbuddy.data.local.TokenManager
import com.example.findbuddy.domain.repository.DashboardRepository
import com.example.findbuddy.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val dashboardRepository: DashboardRepository,
    private val transactionRepository: TransactionRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val userId: String
        get() {
            val token = tokenManager.getToken() ?: ""
            return JwtDecoder.getUserIdFromToken(token)
        }

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    private val _month = MutableStateFlow(Calendar.getInstance().get(Calendar.MONTH) + 1)
    private val _year = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))

    init {
        // Collect reactive updates from the combined flows
        viewModelScope.launch {
            combine(_month, _year) { m, y -> Pair(m, y) }
                .flatMapLatest { (m, y) ->
                    combine(
                        dashboardRepository.getDashboardData(userId, m, y),
                        transactionRepository.getTransactions(userId)
                    ) { data, txs ->
                        // Take the last 3 recent transactions
                        val recent = txs.take(3)
                        DashboardState(
                            totalBalance = data.totalBalance,
                            netWorth = data.netWorth,
                            monthlyIncome = data.monthlyIncome,
                            monthlyExpense = data.monthlyExpense,
                            budgetsList = data.budgets,
                            recentTransactions = recent,
                            selectedMonth = m,
                            selectedYear = y,
                            isLoading = false,
                            errorMsg = null
                        )
                    }
                }
                .catch { e ->
                    _state.value = _state.value.copy(errorMsg = e.message ?: "An unexpected error occurred")
                }
                .collect { newState ->
                    _state.value = newState
                }
        }

        // Trigger initial data load
        handleIntent(DashboardIntent.LoadDashboard)
    }

    fun handleIntent(intent: DashboardIntent) {
        when (intent) {
            is DashboardIntent.LoadDashboard -> {
                loadDashboardData()
            }
            is DashboardIntent.ChangePeriod -> {
                _month.value = intent.month
                _year.value = intent.year
                loadDashboardData()
            }
            is DashboardIntent.ClearError -> {
                _state.value = _state.value.copy(errorMsg = null)
            }
        }
    }

    private fun loadDashboardData() {
        val m = _month.value
        val y = _year.value
        _state.value = _state.value.copy(isLoading = true)
        viewModelScope.launch {
            val result = dashboardRepository.syncDashboard(userId, m, y)
            _state.value = _state.value.copy(isLoading = false)
            result.onFailure { error ->
                _state.value = _state.value.copy(errorMsg = error.message ?: "Sync failed")
            }
        }
    }
}
