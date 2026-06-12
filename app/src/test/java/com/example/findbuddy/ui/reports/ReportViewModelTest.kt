package com.example.findbuddy.ui.reports

import android.content.SharedPreferences
import com.example.findbuddy.data.local.TokenManager
import com.example.findbuddy.data.model.*
import com.example.findbuddy.domain.model.*
import com.example.findbuddy.domain.repository.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.lang.reflect.Proxy
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class ReportViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var reportRepository: FakeReportRepository
    private lateinit var categoryRepository: FakeCategoryRepository
    private lateinit var accountRepository: FakeAccountRepository
    private lateinit var transactionRepository: FakeTransactionRepository
    private lateinit var budgetRepository: FakeBudgetRepository
    private lateinit var tokenManager: TokenManager

    private lateinit var viewModel: ReportViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        reportRepository = FakeReportRepository()
        categoryRepository = FakeCategoryRepository()
        accountRepository = FakeAccountRepository()
        transactionRepository = FakeTransactionRepository()
        budgetRepository = FakeBudgetRepository()
        tokenManager = createMockTokenManager()
        tokenManager.saveToken("header.eyJzdWIiOiJzdG9pY191c2VyIn0.signature")

        // Seed some base data
        categoryRepository.setCategories(
            listOf(Category("cat_1", "stoic_user", "Grocery", "EXPENSE", true))
        )
        accountRepository.setAccounts(
            listOf(Account("acc_1", "stoic_user", "Checking", "BANK_ACCOUNT", 1500.0, 0, 0))
        )
        transactionRepository.setTransactions(
            listOf(
                Transaction(
                    id = "tx_1",
                    userId = "stoic_user",
                    type = "EXPENSE",
                    accountId = "acc_1",
                    accountName = "Checking",
                    categoryId = "cat_1",
                    categoryName = "Grocery",
                    amount = 120.0,
                    date = LocalDate.now().toString(),
                    description = "Groceries",
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                ),
                Transaction(
                    id = "tx_2",
                    userId = "stoic_user",
                    type = "INCOME",
                    accountId = "acc_1",
                    accountName = "Checking",
                    categoryId = "cat_2",
                    categoryName = "Salary",
                    amount = 1000.0,
                    date = LocalDate.now().toString(),
                    description = "Monthly Salary",
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            )
        )

        viewModel = ReportViewModel(
            reportRepository = reportRepository,
            categoryRepository = categoryRepository,
            accountRepository = accountRepository,
            transactionRepository = transactionRepository,
            budgetRepository = budgetRepository,
            tokenManager = tokenManager
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state loads reports and fetches categories and accounts`() = runTest(testDispatcher) {
        viewModel.handleIntent(ReportIntent.LoadReport)
        testScheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals("monthly", state.selectedPeriod)
        assertEquals(1, state.categoriesList.size)
        assertEquals(1, state.accountsList.size)
        assertFalse(state.isLoading)
        assertNull(state.errorMsg)
    }

    @Test
    fun `period change updates state and re-fetches reports`() = runTest(testDispatcher) {
        viewModel.handleIntent(ReportIntent.ChangePeriod("weekly"))
        testScheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals("weekly", state.selectedPeriod)
        assertTrue(reportRepository.syncCalled)
    }

    @Test
    fun `filter by category updates state values`() = runTest(testDispatcher) {
        viewModel.handleIntent(ReportIntent.FilterByCategory("cat_1"))
        testScheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals("cat_1", state.selectedCategoryId)
        // Check filtering expense updates totalExpense to only cat_1 expense (120.0)
        assertEquals(120.0, state.totalExpense, 0.0)
    }

    @Test
    fun `filter by account updates state values`() = runTest(testDispatcher) {
        viewModel.handleIntent(ReportIntent.FilterByAccount("acc_1"))
        testScheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals("acc_1", state.selectedAccountId)
        assertEquals(1000.0, state.totalIncome, 0.0)
    }

    @Test
    fun `average daily spend is calculated correctly`() = runTest(testDispatcher) {
        viewModel.handleIntent(ReportIntent.ChangePeriod("weekly"))
        testScheduler.advanceUntilIdle()

        val state = viewModel.state.value
        // weekly has 7 days, expense is 120.0, so 120 / 7 = 17.14
        assertEquals(120.0 / 7.0, state.averageDailySpend, 0.01)
    }

    @Test
    fun `largest single expense is extracted correctly`() = runTest(testDispatcher) {
        viewModel.handleIntent(ReportIntent.LoadReport)
        testScheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(120.0, state.largestSingleExpense, 0.0)
        assertTrue(state.largestSingleExpenseDesc.contains("Groceries"))
    }

    @Test
    fun `export pdf success triggers effect`() = runTest(testDispatcher) {
        testScheduler.advanceUntilIdle() // settle initialization
        val dummyFile = java.io.File("dummy.pdf")
        reportRepository.exportPdfResult = Result.success(Unit)

        val effects = mutableListOf<ReportEffect>()
        val collectJob = launch {
            viewModel.effect.collect { effects.add(it) }
        }

        viewModel.handleIntent(ReportIntent.ExportPdf(dummyFile))
        testScheduler.advanceUntilIdle()

        assertEquals(1, effects.size)
        assertTrue(effects[0] is ReportEffect.OpenPdf)
        assertEquals(dummyFile, (effects[0] as ReportEffect.OpenPdf).file)

        collectJob.cancel()
    }

    @Test
    fun `export pdf failure sets error and triggers toast`() = runTest(testDispatcher) {
        testScheduler.advanceUntilIdle() // settle initialization
        val dummyFile = java.io.File("dummy.pdf")
        reportRepository.exportPdfResult = Result.failure(Exception("Disk Full"))

        val effects = mutableListOf<ReportEffect>()
        val collectJob = launch {
            viewModel.effect.collect { effects.add(it) }
        }

        viewModel.handleIntent(ReportIntent.ExportPdf(dummyFile))
        testScheduler.advanceUntilIdle()

        assertEquals(1, effects.size)
        assertTrue(effects[0] is ReportEffect.ShowToast)
        assertEquals("Disk Full", (effects[0] as ReportEffect.ShowToast).message)
        assertEquals("Disk Full", viewModel.state.value.errorMsg)

        collectJob.cancel()
    }

    // Fakes implementation
    private class FakeReportRepository : ReportRepository {
        var syncCalled = false

        override fun getIncomeExpenseReport(userId: String, period: String, date: String): Flow<IncomeExpenseReportResponse> {
            return flowOf(
                IncomeExpenseReportResponse(
                    totalIncome = 1000.0,
                    totalExpense = 120.0,
                    savings = 880.0,
                    breakdown = listOf(
                        IncomeExpenseBreakdown("Jan", 1000.0, 120.0)
                    )
                )
            )
        }

        override fun getCategoryReport(userId: String, period: String, date: String): Flow<CategoryReportResponse> {
            return flowOf(
                CategoryReportResponse(
                    totalExpense = 120.0,
                    categories = listOf(
                        CategorySpendResponse("cat_1", "Grocery", 120.0, 100.0)
                    )
                )
            )
        }

        override fun getAccountReport(userId: String, period: String, date: String): Flow<AccountReportResponse> {
            return flowOf(
                AccountReportResponse(
                    accounts = listOf(
                        AccountActivityResponse("acc_1", "Checking", 2, 120.0, 1500.0)
                    )
                )
            )
        }

        override suspend fun syncReports(userId: String, period: String, date: String): Result<Unit> {
            syncCalled = true
            return Result.success(Unit)
        }

        var exportPdfResult: Result<Unit> = Result.success(Unit)
        var exportPdfParams: List<Any?> = emptyList()

        override suspend fun exportPdfReport(
            userId: String,
            reportType: String,
            period: String,
            date: String?,
            accountId: String?,
            categoryId: String?,
            outputFile: java.io.File
        ): Result<Unit> {
            exportPdfParams = listOf(userId, reportType, period, date, accountId, categoryId, outputFile)
            return exportPdfResult
        }
    }

    private class FakeCategoryRepository : CategoryRepository {
        private val categoriesFlow = MutableStateFlow<List<Category>>(emptyList())

        fun setCategories(list: List<Category>) {
            categoriesFlow.value = list
        }

        override fun getCategories(userId: String): Flow<List<Category>> = categoriesFlow

        override suspend fun createCategory(userId: String, name: String, type: String): Result<Category> {
            return Result.success(Category("cat_new", userId, name, type, false))
        }

        override suspend fun syncCategories(userId: String): Result<Unit> = Result.success(Unit)
    }

    private class FakeAccountRepository : AccountRepository {
        private val accountsFlow = MutableStateFlow<List<Account>>(emptyList())

        fun setAccounts(list: List<Account>) {
            accountsFlow.value = list
        }

        override fun getAccounts(userId: String): Flow<List<Account>> = accountsFlow

        override suspend fun createAccount(userId: String, name: String, type: String, balance: Double): Result<Account> {
            return Result.success(Account("acc_new", userId, name, type, balance, 0, 0))
        }

        override suspend fun updateAccount(id: String, userId: String, name: String, type: String, balance: Double): Result<Account> {
            return Result.success(Account(id, userId, name, type, balance, 0, 0))
        }

        override suspend fun deleteAccount(id: String): Result<Unit> = Result.success(Unit)

        override suspend fun syncAccounts(userId: String): Result<Unit> = Result.success(Unit)
    }

    private class FakeTransactionRepository : TransactionRepository {
        private val transactionsFlow = MutableStateFlow<List<Transaction>>(emptyList())

        fun setTransactions(list: List<Transaction>) {
            transactionsFlow.value = list
        }

        override fun getTransactions(userId: String): Flow<List<Transaction>> = transactionsFlow

        override suspend fun createTransaction(
            userId: String,
            type: String,
            amount: Double,
            accountId: String,
            categoryId: String,
            description: String,
            date: String
        ): Result<Transaction> = Result.failure(Exception())

        override suspend fun updateTransaction(
            id: String,
            userId: String,
            type: String,
            amount: Double,
            accountId: String,
            categoryId: String,
            description: String,
            date: String
        ): Result<Transaction> = Result.failure(Exception())

        override suspend fun deleteTransaction(id: String): Result<Unit> = Result.success(Unit)

        override suspend fun createTransfer(
            userId: String,
            sourceAccountId: String,
            destinationAccountId: String,
            amount: Double,
            description: String,
            date: String
        ): Result<Transaction> = Result.failure(Exception())

        override suspend fun getCategories(): Result<List<CategoryResponse>> = Result.failure(Exception())

        override suspend fun syncAll(userId: String): Result<Unit> = Result.success(Unit)
    }

    private class FakeBudgetRepository : BudgetRepository {
        override fun getBudgets(userId: String): Flow<List<Budget>> = flowOf(emptyList())

        override fun getBudgetsForPeriod(userId: String, month: Int, year: Int): Flow<List<Budget>> = flowOf(emptyList())

        override suspend fun createOrUpdateBudget(
            userId: String,
            categoryId: String,
            month: Int,
            year: Int,
            amount: Double
        ): Result<Budget> = Result.failure(Exception())

        override suspend fun syncBudgets(userId: String): Result<Unit> = Result.success(Unit)
    }

    private fun createMockTokenManager(): TokenManager {
        val mockPrefs = mutableMapOf<String, String>()
        val editor = Proxy.newProxyInstance(
            SharedPreferences.Editor::class.java.classLoader,
            arrayOf(SharedPreferences.Editor::class.java)
        ) { proxy, method, args ->
            if (args != null) {
                when (method.name) {
                    "putString" -> {
                        mockPrefs[args[0] as String] = args[1] as String
                        return@newProxyInstance proxy
                    }
                    "remove" -> {
                        mockPrefs.remove(args[0] as String)
                        return@newProxyInstance proxy
                    }
                }
            }
            if (method.name == "commit") return@newProxyInstance true
            proxy
        } as SharedPreferences.Editor

        val prefsProxy = Proxy.newProxyInstance(
            SharedPreferences::class.java.classLoader,
            arrayOf(SharedPreferences::class.java)
        ) { _, method, args ->
            when (method.name) {
                "getString" -> {
                    val key = args[0] as String
                    val default = args[1] as? String
                    mockPrefs[key] ?: default
                }
                "edit" -> editor
                else -> null
            }
        } as SharedPreferences

        return TokenManager(prefsProxy)
    }
}
