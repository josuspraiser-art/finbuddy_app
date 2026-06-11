package com.example.findbuddy.ui.dashboard

import com.example.findbuddy.domain.model.Budget
import com.example.findbuddy.domain.model.Transaction

data class DashboardState(
    val totalBalance: Double = 0.0,
    val netWorth: Double = 0.0,
    val monthlyIncome: Double = 0.0,
    val monthlyExpense: Double = 0.0,
    val budgetsList: List<Budget> = emptyList(),
    val recentTransactions: List<Transaction> = emptyList(),
    val selectedMonth: Int = 6,
    val selectedYear: Int = 2026,
    val isLoading: Boolean = false,
    val errorMsg: String? = null
)
