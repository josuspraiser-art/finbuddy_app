package com.example.findbuddy.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.findbuddy.data.local.JwtDecoder
import com.example.findbuddy.data.local.TokenManager
import com.example.findbuddy.data.model.AccountActivityResponse
import com.example.findbuddy.data.model.CategorySpendResponse
import com.example.findbuddy.data.model.IncomeExpenseBreakdown
import com.example.findbuddy.domain.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ReportViewModel @Inject constructor(
    private val reportRepository: ReportRepository,
    private val categoryRepository: CategoryRepository,
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val budgetRepository: BudgetRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val userId: String
        get() {
            val token = tokenManager.getToken() ?: ""
            return JwtDecoder.getUserIdFromToken(token)
        }

    private val _state = MutableStateFlow(ReportState(anchorDate = LocalDate.now().toString()))
    val state: StateFlow<ReportState> = _state.asStateFlow()

    private val _period = MutableStateFlow("monthly")
    private val _categoryId = MutableStateFlow<String?>(null)
    private val _accountId = MutableStateFlow<String?>(null)
    private val _anchorDate = MutableStateFlow(LocalDate.now().toString())

    init {
        viewModelScope.launch {
            combine(
                _period,
                _categoryId,
                _accountId,
                _anchorDate
            ) { period, catId, accId, anchor ->
                CombinedParams(period, catId, accId, anchor)
            }.flatMapLatest { params ->
                val anchorLocalDate = try {
                    LocalDate.parse(params.anchor)
                } catch (e: Exception) {
                    LocalDate.now()
                }

                val m = anchorLocalDate.monthValue
                val y = anchorLocalDate.year

                val innerFlow = combine(
                    categoryRepository.getCategories(userId),
                    accountRepository.getAccounts(userId),
                    transactionRepository.getTransactions(userId),
                    budgetRepository.getBudgetsForPeriod(userId, m, y)
                ) { categories, accounts, transactions, budgets ->
                    InnerData(categories, accounts, transactions, budgets)
                }

                combine(
                    reportRepository.getIncomeExpenseReport(userId, params.period, params.anchor),
                    reportRepository.getCategoryReport(userId, params.period, params.anchor),
                    reportRepository.getAccountReport(userId, params.period, params.anchor),
                    innerFlow
                ) { ieRep, catRep, accRep, inner ->
                    val categories = inner.categories
                    val accounts = inner.accounts
                    val transactions = inner.transactions
                    val budgets = inner.budgets
                    
                    // Filter transactions and categories according to period & select options for secondary stats
                    val (start, end) = getPeriodBounds(anchorLocalDate, params.period)
                    val startStr = start.toString()
                    val endStr = end.toString()

                    val periodTx = transactions.filter { it.date in startStr..endStr }
                        .filter { params.catId == null || it.categoryId == params.catId }
                        .filter { params.accId == null || it.accountId == params.accId }

                    // 1. Largest Single Expense
                    val expensesList = periodTx.filter { it.type == "EXPENSE" }
                    val maxExpenseTx = expensesList.maxByOrNull { it.amount }
                    val largestSingleExpense = maxExpenseTx?.amount ?: 0.0
                    val largestSingleExpenseDesc = if (maxExpenseTx != null) {
                        val parsedDate = try {
                            LocalDate.parse(maxExpenseTx.date)
                        } catch (e: Exception) {
                            LocalDate.now()
                        }
                        val formattedDate = parsedDate.format(DateTimeFormatter.ofPattern("MMM dd", Locale.US))
                        "${maxExpenseTx.description} • $formattedDate"
                    } else {
                        "-"
                    }

                    // 2. Average Daily Spend
                    val totalExpenseAmount = expensesList.sumOf { it.amount }
                    val daysInPeriod = if (params.period.equals("weekly", ignoreCase = true)) 7.0 else anchorLocalDate.lengthOfMonth().toDouble()
                    val averageDailySpend = if (daysInPeriod > 0) totalExpenseAmount / daysInPeriod else 0.0

                    // 3. Income Growth (compare current period income vs previous period income)
                    val currentPeriodIncome = periodTx.filter { it.type == "INCOME" }.sumOf { it.amount }
                    val prevPeriodIncome = calculatePrevPeriodIncome(transactions, anchorLocalDate, params.period, params.catId, params.accId)
                    val incomeGrowth = if (prevPeriodIncome == 0.0) {
                        if (currentPeriodIncome > 0.0) 100.0 else 0.0
                    } else {
                        ((currentPeriodIncome - prevPeriodIncome) / prevPeriodIncome) * 100.0
                    }

                    // 4. Budget Health
                    // Check budgets for the categories we filter by, or all budgets in the period
                    val filteredBudgets = budgets.filter { params.catId == null || it.categoryId == params.catId }
                    val healthyCount = filteredBudgets.count { it.status.uppercase() == "NORMAL" || it.status.uppercase() == "WARNING" }
                    val budgetHealthPercentage = if (filteredBudgets.isNotEmpty()) {
                        (healthyCount.toDouble() / filteredBudgets.size) * 100.0
                    } else {
                        100.0
                    }
                    val budgetHealthStatus = when {
                        budgetHealthPercentage >= 90.0 -> "Optimal"
                        budgetHealthPercentage >= 50.0 -> "Warning"
                        else -> "Critical"
                    }
                    val budgetHealthDesc = if (filteredBudgets.isNotEmpty()) {
                        "${budgetHealthPercentage.toInt()}% of targets met"
                    } else {
                        "No budgets set"
                    }

                    // Apply filters to Category spending & Account report locally if needed
                    val filteredCategoryDistribution = catRep.categories
                        .filter { params.catId == null || it.categoryId == params.catId }
                    val filteredAccountActivities = accRep.accounts
                        .filter { params.accId == null || it.accountId == params.accId }

                    // Recalculate total income/expense/savings from filtered transactions
                    val finalTotalIncome = periodTx.filter { it.type == "INCOME" }.sumOf { it.amount }
                    val finalTotalExpense = periodTx.filter { it.type == "EXPENSE" }.sumOf { it.amount }

                    ReportState(
                        totalIncome = finalTotalIncome,
                        totalExpense = finalTotalExpense,
                        savings = finalTotalIncome - finalTotalExpense,
                        breakdownList = ieRep.breakdown, // Keep overall breakdown for chart
                        categoryDistribution = filteredCategoryDistribution,
                        accountActivities = filteredAccountActivities,
                        categoriesList = categories,
                        accountsList = accounts,
                        selectedPeriod = params.period,
                        selectedCategoryId = params.catId,
                        selectedAccountId = params.accId,
                        anchorDate = params.anchor,
                        largestSingleExpense = largestSingleExpense,
                        largestSingleExpenseDesc = largestSingleExpenseDesc,
                        averageDailySpend = averageDailySpend,
                        incomeGrowthPercentage = incomeGrowth,
                        budgetHealthStatus = budgetHealthStatus,
                        budgetHealthDesc = budgetHealthDesc,
                        errorMsg = null
                    )
                }
            }.catch { e ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMsg = e.message ?: "An unexpected error occurred"
                )
            }.collect { newState ->
                _state.value = newState
            }
        }

        // Trigger initial data load
        handleIntent(ReportIntent.LoadReport)
    }

    fun handleIntent(intent: ReportIntent) {
        when (intent) {
            is ReportIntent.LoadReport -> {
                loadReportData()
            }
            is ReportIntent.ChangePeriod -> {
                _period.value = intent.period
                loadReportData()
            }
            is ReportIntent.FilterByCategory -> {
                _categoryId.value = intent.categoryId
            }
            is ReportIntent.FilterByAccount -> {
                _accountId.value = intent.accountId
            }
            is ReportIntent.ChangeAnchorDate -> {
                _anchorDate.value = intent.date
                loadReportData()
            }
            is ReportIntent.ClearError -> {
                _state.value = _state.value.copy(errorMsg = null)
            }
        }
    }

    private fun loadReportData() {
        _state.value = _state.value.copy(isLoading = true)
        viewModelScope.launch {
            val result = reportRepository.syncReports(userId, _period.value, _anchorDate.value)
            _state.value = _state.value.copy(isLoading = false)
            result.onFailure { error ->
                _state.value = _state.value.copy(errorMsg = error.message ?: "Sync failed")
            }
        }
    }

    private fun getPeriodBounds(anchor: LocalDate, period: String): Pair<LocalDate, LocalDate> {
        return if (period.equals("weekly", ignoreCase = true)) {
            val dayOfWeek = anchor.dayOfWeek.value
            val monday = anchor.minusDays((dayOfWeek - 1).toLong())
            val sunday = monday.plusDays(6)
            Pair(monday, sunday)
        } else {
            val startOfMonth = anchor.withDayOfMonth(1)
            val endOfMonth = anchor.withDayOfMonth(anchor.lengthOfMonth())
            Pair(startOfMonth, endOfMonth)
        }
    }

    private fun calculatePrevPeriodIncome(
        transactions: List<com.example.findbuddy.domain.model.Transaction>,
        anchor: LocalDate,
        period: String,
        categoryId: String?,
        accountId: String?
    ): Double {
        val prevAnchor = if (period.equals("weekly", ignoreCase = true)) {
            anchor.minusWeeks(1)
        } else {
            anchor.minusMonths(1)
        }

        val (start, end) = getPeriodBounds(prevAnchor, period)
        val startStr = start.toString()
        val endStr = end.toString()

        return transactions.filter { it.date in startStr..endStr }
            .filter { it.type == "INCOME" }
            .filter { categoryId == null || it.categoryId == categoryId }
            .filter { accountId == null || it.accountId == accountId }
            .sumOf { it.amount }
    }

    private data class CombinedParams(
        val period: String,
        val catId: String?,
        val accId: String?,
        val anchor: String
    )

    private data class InnerData(
        val categories: List<com.example.findbuddy.domain.model.Category>,
        val accounts: List<com.example.findbuddy.domain.model.Account>,
        val transactions: List<com.example.findbuddy.domain.model.Transaction>,
        val budgets: List<com.example.findbuddy.domain.model.Budget>
    )
}
