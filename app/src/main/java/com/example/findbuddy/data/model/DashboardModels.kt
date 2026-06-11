package com.example.findbuddy.data.model

import com.google.gson.annotations.SerializedName

data class DashboardResponse(
    @SerializedName("totalBalance") val totalBalance: Double,
    @SerializedName("monthlyIncome") val monthlyIncome: Double,
    @SerializedName("monthlyExpense") val monthlyExpense: Double,
    @SerializedName("netWorth") val netWorth: Double,
    @SerializedName("budgets") val budgets: List<BudgetResponse>
)
