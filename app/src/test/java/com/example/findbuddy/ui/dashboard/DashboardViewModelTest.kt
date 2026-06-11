package com.example.findbuddy.ui.dashboard

import android.content.SharedPreferences
import com.example.findbuddy.data.local.TokenManager
import com.example.findbuddy.domain.model.Budget
import com.example.findbuddy.domain.model.DashboardData
import com.example.findbuddy.domain.model.Transaction
import com.example.findbuddy.domain.repository.DashboardRepository
import com.example.findbuddy.domain.repository.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.lang.reflect.Proxy

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var fakeDashboardRepository: FakeDashboardRepository
    private lateinit var fakeTransactionRepository: FakeTransactionRepository
    private lateinit var tokenManager: TokenManager
    private lateinit var viewModel: DashboardViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeDashboardRepository = FakeDashboardRepository()
        fakeTransactionRepository = FakeTransactionRepository()
        tokenManager = createMockTokenManager()
        tokenManager.saveToken("header.eyJzdWIiOiJzdG9pY191c2VyIn0.signature")

        // Set initial data
        fakeDashboardRepository.setDashboardData(
            DashboardData(
                totalBalance = 1000.0,
                monthlyIncome = 500.0,
                monthlyExpense = 200.0,
                netWorth = 800.0,
                budgets = emptyList()
            )
        )

        viewModel = DashboardViewModel(
            fakeDashboardRepository,
            fakeTransactionRepository,
            tokenManager
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is populated from repository`() = runTest(testDispatcher) {
        testScheduler.advanceUntilIdle()
        val state = viewModel.state.value
        assertEquals(1000.0, state.totalBalance, 0.0)
        assertEquals(500.0, state.monthlyIncome, 0.0)
        assertEquals(200.0, state.monthlyExpense, 0.0)
        assertEquals(800.0, state.netWorth, 0.0)
        assertTrue(state.budgetsList.isEmpty())
        assertTrue(state.recentTransactions.isEmpty())
    }

    @Test
    fun `intent load dashboard triggers repository sync`() = runTest(testDispatcher) {
        testScheduler.advanceUntilIdle()
        // First sync is called on VM init, reset tracker
        fakeDashboardRepository.syncCalled = false

        viewModel.handleIntent(DashboardIntent.LoadDashboard)
        testScheduler.advanceUntilIdle()

        assertTrue(fakeDashboardRepository.syncCalled)
    }

    @Test
    fun `intent change period updates selected month and year and triggers sync`() = runTest(testDispatcher) {
        testScheduler.advanceUntilIdle()
        viewModel.handleIntent(DashboardIntent.ChangePeriod(10, 2027))
        testScheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(10, state.selectedMonth)
        assertEquals(2027, state.selectedYear)
        assertTrue(fakeDashboardRepository.syncCalled)
    }

    // --- Fakes & Helper Utilities ---

    private class FakeDashboardRepository : DashboardRepository {
        private val dataFlow = MutableStateFlow(
            DashboardData(0.0, 0.0, 0.0, 0.0, emptyList())
        )
        var syncCalled = false

        fun setDashboardData(data: DashboardData) {
            dataFlow.value = data
        }

        override fun getDashboardData(userId: String, month: Int, year: Int): Flow<DashboardData> = dataFlow

        override suspend fun syncDashboard(userId: String, month: Int, year: Int): Result<Unit> {
            syncCalled = true
            return Result.success(Unit)
        }
    }

    private class FakeTransactionRepository : TransactionRepository {
        private val transactionsFlow = MutableStateFlow<List<Transaction>>(emptyList())
        override fun getTransactions(userId: String): Flow<List<Transaction>> = transactionsFlow
        override suspend fun createTransaction(userId: String, type: String, amount: Double, accountId: String, categoryId: String, description: String, date: String): Result<Transaction> = Result.failure(Exception())
        override suspend fun updateTransaction(id: String, userId: String, type: String, amount: Double, accountId: String, categoryId: String, description: String, date: String): Result<Transaction> = Result.failure(Exception())
        override suspend fun deleteTransaction(id: String): Result<Unit> = Result.success(Unit)
        override suspend fun createTransfer(userId: String, sourceAccountId: String, destinationAccountId: String, amount: Double, description: String, date: String): Result<Transaction> = Result.failure(Exception())
        override suspend fun getCategories(): Result<List<com.example.findbuddy.data.model.CategoryResponse>> = Result.failure(Exception())
        override suspend fun syncAll(userId: String): Result<Unit> = Result.success(Unit)
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
