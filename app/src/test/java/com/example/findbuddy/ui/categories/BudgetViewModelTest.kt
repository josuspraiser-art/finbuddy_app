package com.example.findbuddy.ui.categories

import android.content.SharedPreferences
import com.example.findbuddy.data.local.TokenManager
import com.example.findbuddy.domain.model.Budget
import com.example.findbuddy.domain.model.Category
import com.example.findbuddy.domain.model.Transaction
import com.example.findbuddy.domain.repository.BudgetRepository
import com.example.findbuddy.domain.repository.CategoryRepository
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
class BudgetViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var fakeCategoryRepository: FakeCategoryRepository
    private lateinit var fakeBudgetRepository: FakeBudgetRepository
    private lateinit var fakeTransactionRepository: FakeTransactionRepository
    private lateinit var tokenManager: TokenManager
    private lateinit var viewModel: CategoryViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeCategoryRepository = FakeCategoryRepository()
        fakeBudgetRepository = FakeBudgetRepository()
        fakeTransactionRepository = FakeTransactionRepository()
        tokenManager = createMockTokenManager()
        tokenManager.saveToken("header.eyJzdWIiOiJzdG9pY191c2VyIn0.signature")

        viewModel = CategoryViewModel(
            fakeCategoryRepository,
            fakeBudgetRepository,
            fakeTransactionRepository,
            tokenManager
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has empty budgets list`() = runTest(testDispatcher) {
        testScheduler.advanceUntilIdle()
        val state = viewModel.state.value
        assertTrue(state.budgetsList.isEmpty())
        assertEquals(0.0, state.totalSpent, 0.0)
    }

    @Test
    fun `open limit dialog populates selected category and current limit`() = runTest(testDispatcher) {
        testScheduler.advanceUntilIdle()
        val category = Category("cat1", "stoic_user", "Grocery", "EXPENSE", false)
        viewModel.handleIntent(CategoryIntent.OpenLimitDialog(category))

        val state = viewModel.state.value
        assertTrue(state.showLimitDialog)
        assertEquals(category, state.selectedCategory)
        assertEquals("", state.limitAmount)
    }

    @Test
    fun `save limit fails when amount is empty`() = runTest(testDispatcher) {
        testScheduler.advanceUntilIdle()
        val category = Category("cat1", "stoic_user", "Grocery", "EXPENSE", false)
        viewModel.handleIntent(CategoryIntent.OpenLimitDialog(category))
        viewModel.handleIntent(CategoryIntent.ChangeLimitAmount(""))
        viewModel.handleIntent(CategoryIntent.SaveLimit)
        testScheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals("Limit amount cannot be empty", state.errorMsg)
        assertTrue(state.showLimitDialog)
    }

    @Test
    fun `save limit fails when amount is negative`() = runTest(testDispatcher) {
        testScheduler.advanceUntilIdle()
        val category = Category("cat1", "stoic_user", "Grocery", "EXPENSE", false)
        viewModel.handleIntent(CategoryIntent.OpenLimitDialog(category))
        viewModel.handleIntent(CategoryIntent.ChangeLimitAmount("-50"))
        viewModel.handleIntent(CategoryIntent.SaveLimit)
        testScheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals("Please enter a valid positive limit", state.errorMsg)
        assertTrue(state.showLimitDialog)
    }

    @Test
    fun `save limit fails when amount is invalid text`() = runTest(testDispatcher) {
        testScheduler.advanceUntilIdle()
        val category = Category("cat1", "stoic_user", "Grocery", "EXPENSE", false)
        viewModel.handleIntent(CategoryIntent.OpenLimitDialog(category))
        viewModel.handleIntent(CategoryIntent.ChangeLimitAmount("abc"))
        viewModel.handleIntent(CategoryIntent.SaveLimit)
        testScheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals("Please enter a valid positive limit", state.errorMsg)
        assertTrue(state.showLimitDialog)
    }

    @Test
    fun `save limit succeeds with valid params`() = runTest(testDispatcher) {
        testScheduler.advanceUntilIdle()
        val category = Category("cat1", "stoic_user", "Grocery", "EXPENSE", false)
        viewModel.handleIntent(CategoryIntent.OpenLimitDialog(category))
        viewModel.handleIntent(CategoryIntent.ChangeLimitAmount("500"))
        viewModel.handleIntent(CategoryIntent.SaveLimit)
        testScheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertNull(state.errorMsg)
        assertFalse(state.showLimitDialog)
    }

    // --- Fakes & Helper Utilities ---

    private class FakeCategoryRepository : CategoryRepository {
        private val categoriesFlow = MutableStateFlow<List<Category>>(emptyList())
        override fun getCategories(userId: String): Flow<List<Category>> = categoriesFlow
        override suspend fun createCategory(userId: String, name: String, type: String): Result<Category> = Result.failure(Exception())
        override suspend fun syncCategories(userId: String): Result<Unit> = Result.success(Unit)
    }

    private class FakeBudgetRepository : BudgetRepository {
        private val budgetsFlow = MutableStateFlow<List<Budget>>(emptyList())
        var syncCalled = false

        fun setBudgets(list: List<Budget>) {
            budgetsFlow.value = list
        }

        override fun getBudgets(userId: String): Flow<List<Budget>> = budgetsFlow

        override fun getBudgetsForPeriod(userId: String, month: Int, year: Int): Flow<List<Budget>> = budgetsFlow

        override suspend fun createOrUpdateBudget(
            userId: String,
            categoryId: String,
            month: Int,
            year: Int,
            amount: Double
        ): Result<Budget> {
            val newBudget = Budget(
                id = "b1",
                userId = userId,
                categoryId = categoryId,
                month = month,
                year = year,
                budgetAmount = amount,
                spentAmount = 0.0,
                remainingAmount = amount,
                usagePercentage = 0.0,
                status = "NORMAL"
            )
            budgetsFlow.value = budgetsFlow.value + newBudget
            return Result.success(newBudget)
        }

        override suspend fun syncBudgets(userId: String): Result<Unit> {
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
