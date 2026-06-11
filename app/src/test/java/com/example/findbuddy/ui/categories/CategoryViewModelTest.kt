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
class CategoryViewModelTest {

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
        // Save mock JWT token with user sub claim "stoic_user"
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
    fun `initial state has empty list and is not loading after initialization`() = runTest(testDispatcher) {
        testScheduler.advanceUntilIdle()
        val state = viewModel.state.value
        assertTrue(state.categoriesList.isEmpty())
        assertFalse(state.isLoading)
        assertNull(state.errorMsg)
        assertFalse(state.showCreateDialog)
    }

    @Test
    fun `intent load categories fetches list and triggers sync`() = runTest(testDispatcher) {
        val categories = listOf(
            Category("1", "stoic_user", "Grocery", "EXPENSE", false)
        )
        fakeCategoryRepository.setCategories(categories)

        viewModel.handleIntent(CategoryIntent.LoadCategories)
        testScheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(1, state.categoriesList.size)
        assertEquals("Grocery", state.categoriesList[0].categoryName)
        assertTrue(fakeCategoryRepository.syncCalled)
        assertFalse(state.isLoading)
    }

    @Test
    fun `create category fails when name is empty`() = runTest(testDispatcher) {
        viewModel.handleIntent(CategoryIntent.OpenCreateDialog)
        viewModel.handleIntent(CategoryIntent.ChangeNewName(""))
        viewModel.handleIntent(CategoryIntent.CreateCategory)
        testScheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals("Category name cannot be empty", state.errorMsg)
        assertTrue(state.showCreateDialog)
    }

    @Test
    fun `create category fails when name is blank`() = runTest(testDispatcher) {
        viewModel.handleIntent(CategoryIntent.OpenCreateDialog)
        viewModel.handleIntent(CategoryIntent.ChangeNewName("   "))
        viewModel.handleIntent(CategoryIntent.CreateCategory)
        testScheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals("Category name cannot be empty", state.errorMsg)
        assertTrue(state.showCreateDialog)
    }

    @Test
    fun `create category fails when name is duplicate`() = runTest(testDispatcher) {
        // Pre-populate duplicate name checking
        val categories = listOf(
            Category("1", "stoic_user", "Grocery", "EXPENSE", false)
        )
        fakeCategoryRepository.setCategories(categories)
        viewModel.handleIntent(CategoryIntent.LoadCategories)
        testScheduler.advanceUntilIdle()

        viewModel.handleIntent(CategoryIntent.OpenCreateDialog)
        viewModel.handleIntent(CategoryIntent.ChangeNewName("Grocery"))
        viewModel.handleIntent(CategoryIntent.CreateCategory)
        testScheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals("Category name must be unique", state.errorMsg)
        assertTrue(state.showCreateDialog)
    }

    @Test
    fun `create category succeeds with valid params`() = runTest(testDispatcher) {
        viewModel.handleIntent(CategoryIntent.OpenCreateDialog)
        viewModel.handleIntent(CategoryIntent.ChangeNewName("Savings"))
        viewModel.handleIntent(CategoryIntent.ChangeNewType("INCOME"))
        viewModel.handleIntent(CategoryIntent.CreateCategory)
        testScheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertNull(state.errorMsg)
        assertFalse(state.showCreateDialog)
    }

    @Test
    fun `dialog actions update state correctly`() = runTest(testDispatcher) {
        viewModel.handleIntent(CategoryIntent.OpenCreateDialog)
        var state = viewModel.state.value
        assertTrue(state.showCreateDialog)
        assertEquals("", state.newCategoryName)
        assertEquals("EXPENSE", state.newCategoryType)

        viewModel.handleIntent(CategoryIntent.ChangeNewName("Rent"))
        viewModel.handleIntent(CategoryIntent.ChangeNewType("EXPENSE"))
        state = viewModel.state.value
        assertEquals("Rent", state.newCategoryName)

        viewModel.handleIntent(CategoryIntent.DismissCreateDialog)
        state = viewModel.state.value
        assertFalse(state.showCreateDialog)
        assertEquals("", state.newCategoryName)
    }

    // --- Fakes & Helper Utilities ---

    private class FakeCategoryRepository : CategoryRepository {
        private val categoriesFlow = MutableStateFlow<List<Category>>(emptyList())
        var syncCalled = false

        fun setCategories(list: List<Category>) {
            categoriesFlow.value = list
        }

        override fun getCategories(userId: String): Flow<List<Category>> {
            return categoriesFlow
        }

        override suspend fun createCategory(
            userId: String,
            name: String,
            type: String
        ): Result<Category> {
            val exists = categoriesFlow.value.any { it.userId == userId && it.categoryName.lowercase() == name.lowercase() }
            if (exists) {
                return Result.failure(Exception("Category name must be unique"))
            }
            val newCategory = Category("new_id", userId, name, type, false)
            categoriesFlow.value = categoriesFlow.value + newCategory
            return Result.success(newCategory)
        }

        override suspend fun syncCategories(userId: String): Result<Unit> {
            syncCalled = true
            return Result.success(Unit)
        }
    }

    private class FakeBudgetRepository : BudgetRepository {
        private val budgetsFlow = MutableStateFlow<List<Budget>>(emptyList())
        override fun getBudgets(userId: String): Flow<List<Budget>> = budgetsFlow
        override fun getBudgetsForPeriod(userId: String, month: Int, year: Int): Flow<List<Budget>> = budgetsFlow
        override suspend fun createOrUpdateBudget(userId: String, categoryId: String, month: Int, year: Int, amount: Double): Result<Budget> = Result.failure(Exception())
        override suspend fun syncBudgets(userId: String): Result<Unit> = Result.success(Unit)
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
