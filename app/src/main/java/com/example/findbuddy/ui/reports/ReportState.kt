package com.example.findbuddy.ui.reports

import com.example.findbuddy.data.model.AccountActivityResponse
import com.example.findbuddy.data.model.CategorySpendResponse
import com.example.findbuddy.data.model.IncomeExpenseBreakdown
import com.example.findbuddy.domain.model.Category
import com.example.findbuddy.domain.model.Account

data class ReportState(
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val savings: Double = 0.0,
    val breakdownList: List<IncomeExpenseBreakdown> = emptyList(),
    val categoryDistribution: List<CategorySpendResponse> = emptyList(),
    val accountActivities: List<AccountActivityResponse> = emptyList(),
    val categoriesList: List<Category> = emptyList(),
    val accountsList: List<Account> = emptyList(),
    val selectedPeriod: String = "monthly", // weekly or monthly
    val selectedCategoryId: String? = null,
    val selectedAccountId: String? = null,
    val anchorDate: String = "",
    val isLoading: Boolean = false,
    val errorMsg: String? = null,
    val largestSingleExpense: Double = 0.0,
    val largestSingleExpenseDesc: String = "-",
    val averageDailySpend: Double = 0.0,
    val incomeGrowthPercentage: Double = 0.0,
    val budgetHealthStatus: String = "Optimal",
    val budgetHealthDesc: String = "No budgets set"
)
