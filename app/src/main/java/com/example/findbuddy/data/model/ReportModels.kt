package com.example.findbuddy.data.model

import com.google.gson.annotations.SerializedName

data class IncomeExpenseBreakdown(
    @SerializedName("label") val label: String,
    @SerializedName("income") val income: Double,
    @SerializedName("expense") val expense: Double
)

data class IncomeExpenseReportResponse(
    @SerializedName("totalIncome") val totalIncome: Double,
    @SerializedName("totalExpense") val totalExpense: Double,
    @SerializedName("savings") val savings: Double,
    @SerializedName("breakdown") val breakdown: List<IncomeExpenseBreakdown>
)

data class CategorySpendResponse(
    @SerializedName("categoryId") val categoryId: String,
    @SerializedName("categoryName") val categoryName: String,
    @SerializedName("spend") val spend: Double,
    @SerializedName("percentage") val percentage: Double
)

data class CategoryReportResponse(
    @SerializedName("totalExpense") val totalExpense: Double,
    @SerializedName("categories") val categories: List<CategorySpendResponse>
)

data class AccountActivityResponse(
    @SerializedName("accountId") val accountId: String,
    @SerializedName("accountName") val accountName: String,
    @SerializedName("transactionCount") val transactionCount: Int,
    @SerializedName("totalSpending") val totalSpending: Double,
    @SerializedName("currentBalance") val currentBalance: Double
)

data class AccountReportResponse(
    @SerializedName("accounts") val accounts: List<AccountActivityResponse>
)
