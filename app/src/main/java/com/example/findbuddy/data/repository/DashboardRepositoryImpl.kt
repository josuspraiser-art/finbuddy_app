package com.example.findbuddy.data.repository

import com.example.findbuddy.data.api.DashboardApi
import com.example.findbuddy.data.local.dao.AccountDao
import com.example.findbuddy.data.local.dao.TransactionDao
import com.example.findbuddy.domain.model.DashboardData
import com.example.findbuddy.domain.repository.AccountRepository
import com.example.findbuddy.domain.repository.BudgetRepository
import com.example.findbuddy.domain.repository.DashboardRepository
import com.example.findbuddy.domain.repository.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DashboardRepositoryImpl @Inject constructor(
    private val accountDao: AccountDao,
    private val transactionDao: TransactionDao,
    private val budgetRepository: BudgetRepository,
    private val dashboardApi: DashboardApi,
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository
) : DashboardRepository {

    override fun getDashboardData(userId: String, month: Int, year: Int): Flow<DashboardData> {
        val accountsFlow = accountDao.getAccounts(userId)
        val transactionsFlow = transactionDao.getTransactions(userId)
        val budgetsFlow = budgetRepository.getBudgetsForPeriod(userId, month, year)

        return combine(accountsFlow, transactionsFlow, budgetsFlow) { accounts, transactions, budgets ->
            val totalBalance = accounts.sumOf { it.openingBalance }
            
            val datePrefix = String.format("%04d-%02d-", year, month)
            val monthlyIncome = transactions.filter {
                it.type == "INCOME" && it.date.startsWith(datePrefix)
            }.sumOf { it.amount }

            val monthlyExpense = transactions.filter {
                it.type == "EXPENSE" && it.date.startsWith(datePrefix)
            }.sumOf { it.amount }

            val assets = accounts.filter {
                it.accountType.equals("BANK_ACCOUNT", ignoreCase = true) ||
                it.accountType.equals("CASH_WALLET", ignoreCase = true)
            }.sumOf { it.openingBalance }

            val liabilities = accounts.filter {
                it.accountType.equals("CREDIT_CARD", ignoreCase = true)
            }.sumOf { it.openingBalance }

            val netWorth = assets - liabilities

            DashboardData(
                totalBalance = totalBalance,
                monthlyIncome = monthlyIncome,
                monthlyExpense = monthlyExpense,
                netWorth = netWorth,
                budgets = budgets
            )
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun syncDashboard(userId: String, month: Int, year: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            accountRepository.syncAccounts(userId)
            transactionRepository.syncAll(userId)
            budgetRepository.syncBudgets(userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
