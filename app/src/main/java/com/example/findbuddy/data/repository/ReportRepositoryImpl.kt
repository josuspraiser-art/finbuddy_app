package com.example.findbuddy.data.repository

import com.example.findbuddy.data.api.ReportApi
import com.example.findbuddy.data.local.dao.AccountDao
import com.example.findbuddy.data.local.dao.CategoryDao
import com.example.findbuddy.data.local.dao.TransactionDao
import com.example.findbuddy.data.model.*
import com.example.findbuddy.domain.repository.AccountRepository
import com.example.findbuddy.domain.repository.BudgetRepository
import com.example.findbuddy.domain.repository.ReportRepository
import com.example.findbuddy.domain.repository.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportRepositoryImpl @Inject constructor(
    private val accountDao: AccountDao,
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val reportApi: ReportApi,
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val budgetRepository: BudgetRepository
) : ReportRepository {

    private fun getWeeklyBounds(anchor: LocalDate): Pair<LocalDate, LocalDate> {
        val dayOfWeek = anchor.dayOfWeek.value
        val monday = anchor.minusDays((dayOfWeek - 1).toLong())
        val sunday = monday.plusDays(6)
        return Pair(monday, sunday)
    }

    private fun getMonthlyBounds(anchor: LocalDate): Pair<LocalDate, LocalDate> {
        val startOfMonth = anchor.withDayOfMonth(1)
        val endOfMonth = anchor.withDayOfMonth(anchor.lengthOfMonth())
        return Pair(startOfMonth, endOfMonth)
    }

    override fun getIncomeExpenseReport(
        userId: String,
        period: String,
        date: String
    ): Flow<IncomeExpenseReportResponse> {
        val transactionsFlow = transactionDao.getTransactions(userId)

        return transactionsFlow.map { transactions ->
            val anchor = try {
                LocalDate.parse(date)
            } catch (e: Exception) {
                LocalDate.now()
            }

            if (period.equals("weekly", ignoreCase = true)) {
                val (monday, sunday) = getWeeklyBounds(anchor)
                val breakdown = ArrayList<IncomeExpenseBreakdown>()
                var totalInc = 0.0
                var totalExp = 0.0

                for (i in 0..6) {
                    val day = monday.plusDays(i.toLong())
                    val dayStr = day.toString()
                    val dayTxList = transactions.filter { it.date == dayStr }
                    val inc = dayTxList.filter { it.type == "INCOME" }.sumOf { it.amount }
                    val exp = dayTxList.filter { it.type == "EXPENSE" }.sumOf { it.amount }

                    breakdown.add(
                        IncomeExpenseBreakdown(
                            label = dayStr,
                            income = inc,
                            expense = exp
                        )
                    )
                    totalInc += inc
                    totalExp += exp
                }

                IncomeExpenseReportResponse(
                    totalIncome = totalInc,
                    totalExpense = totalExp,
                    savings = totalInc - totalExp,
                    breakdown = breakdown
                )
            } else {
                // Monthly: Show last 6 months ending at anchor month
                val breakdown = ArrayList<IncomeExpenseBreakdown>()
                var totalInc = 0.0
                var totalExp = 0.0

                for (i in 5 downTo 0) {
                    val monthDate = anchor.minusMonths(i.toLong())
                    val (start, end) = getMonthlyBounds(monthDate)

                    val startStr = start.toString()
                    val endStr = end.toString()

                    val monthTxList = transactions.filter { it.date in startStr..endStr }
                    val inc = monthTxList.filter { it.type == "INCOME" }.sumOf { it.amount }
                    val exp = monthTxList.filter { it.type == "EXPENSE" }.sumOf { it.amount }

                    val monthLabel = monthDate.month.getDisplayName(TextStyle.SHORT, Locale.US)
                    breakdown.add(
                        IncomeExpenseBreakdown(
                            label = monthLabel,
                            income = inc,
                            expense = exp
                        )
                    )

                    if (i == 0) {
                        // Current anchor month totals
                        totalInc = inc
                        totalExp = exp
                    }
                }

                IncomeExpenseReportResponse(
                    totalIncome = totalInc,
                    totalExpense = totalExp,
                    savings = totalInc - totalExp,
                    breakdown = breakdown
                )
            }
        }.flowOn(Dispatchers.IO)
    }

    override fun getCategoryReport(
        userId: String,
        period: String,
        date: String
    ): Flow<CategoryReportResponse> {
        val transactionsFlow = transactionDao.getTransactions(userId)
        val categoriesFlow = categoryDao.getCategories(userId)

        return combine(transactionsFlow, categoriesFlow) { transactions, categories ->
            val anchor = try {
                LocalDate.parse(date)
            } catch (e: Exception) {
                LocalDate.now()
            }

            val (start, end) = if (period.equals("weekly", ignoreCase = true)) {
                getWeeklyBounds(anchor)
            } else {
                getMonthlyBounds(anchor)
            }

            val startStr = start.toString()
            val endStr = end.toString()

            val periodTx = transactions.filter { it.date in startStr..endStr }
            val expenses = periodTx.filter { it.type == "EXPENSE" }
            val totalExp = expenses.sumOf { it.amount }

            val categoryMap = expenses.groupBy { it.categoryId }
            val categorySpends = categories.map { cat ->
                val spend = categoryMap[cat.id]?.sumOf { it.amount } ?: 0.0
                val percentage = if (totalExp > 0) (spend / totalExp) * 100 else 0.0
                CategorySpendResponse(
                    categoryId = cat.id,
                    categoryName = cat.categoryName,
                    spend = spend,
                    percentage = percentage
                )
            }.filter { it.spend > 0.0 }
             .sortedByDescending { it.spend }

            CategoryReportResponse(
                totalExpense = totalExp,
                categories = categorySpends
            )
        }.flowOn(Dispatchers.IO)
    }

    override fun getAccountReport(
        userId: String,
        period: String,
        date: String
    ): Flow<AccountReportResponse> {
        val transactionsFlow = transactionDao.getTransactions(userId)
        val accountsFlow = accountDao.getAccounts(userId)

        return combine(transactionsFlow, accountsFlow) { transactions, accounts ->
            val anchor = try {
                LocalDate.parse(date)
            } catch (e: Exception) {
                LocalDate.now()
            }

            val (start, end) = if (period.equals("weekly", ignoreCase = true)) {
                getWeeklyBounds(anchor)
            } else {
                getMonthlyBounds(anchor)
            }

            val startStr = start.toString()
            val endStr = end.toString()

            val periodTx = transactions.filter { it.date in startStr..endStr }

            val accountActivities = accounts.map { acc ->
                val accTx = periodTx.filter { it.accountId == acc.id }
                val txCount = accTx.size
                val spending = accTx.filter { it.type == "EXPENSE" }.sumOf { it.amount }

                AccountActivityResponse(
                    accountId = acc.id,
                    accountName = acc.accountName,
                    transactionCount = txCount,
                    totalSpending = spending,
                    currentBalance = acc.openingBalance
                )
            }

            AccountReportResponse(accounts = accountActivities)
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun syncReports(
        userId: String,
        period: String,
        date: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Sync all dependency tables
            accountRepository.syncAccounts(userId)
            transactionRepository.syncAll(userId)
            budgetRepository.syncBudgets(userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
