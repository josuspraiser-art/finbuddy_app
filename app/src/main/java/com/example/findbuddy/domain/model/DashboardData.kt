package com.example.findbuddy.domain.model

data class DashboardData(
    val totalBalance: Double,
    val monthlyIncome: Double,
    val monthlyExpense: Double,
    val netWorth: Double,
    val budgets: List<Budget>
)
