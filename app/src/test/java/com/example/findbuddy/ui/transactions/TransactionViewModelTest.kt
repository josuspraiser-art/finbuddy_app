package com.example.findbuddy.ui.transactions

import android.content.SharedPreferences
import com.example.findbuddy.data.local.TokenManager
import com.example.findbuddy.data.model.CategoryResponse
import com.example.findbuddy.domain.model.Account
import com.example.findbuddy.domain.model.Transaction
import com.example.findbuddy.domain.repository.AccountRepository
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
class TransactionViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var fakeTransactionRepository: FakeTransactionRepository
    private lateinit var fakeAccountRepository: FakeAccountRepository
    private lateinit var tokenManager: TokenManager
    private lateinit var viewModel: TransactionViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeTransactionRepository = FakeTransactionRepository()
        fakeAccountRepository = FakeAccountRepository()
        tokenManager = createMockTokenManager()
        // Save mock JWT token with user sub claim "stoic_user"
        tokenManager.saveToken("header.eyJzdWIiOiJzdG9pY191c2VyIn0.signature")

        // Pre-populate some accounts
        val accounts = listOf(
            Account("acc_1", "stoic_user", "Checking", "BANK_ACCOUNT", 1000.0, 0, 0),
            Account("acc_2", "stoic_user", "Savings", "BANK_ACCOUNT", 2000.0, 0, 0)
        )
        fakeAccountRepository.setAccounts(accounts)

        viewModel = TransactionViewModel(fakeTransactionRepository, fakeAccountRepository, tokenManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state sets default date and is loading`() = runTest(testDispatcher) {
        val state = viewModel.state.value
        assertFalse(state.date.isEmpty())
        assertEquals("EXPENSE", state.type)
        assertTrue(state.amount.isEmpty())
        assertTrue(state.description.isEmpty())
    }

    @Test
    fun `intent load initial data fetches accounts and categories`() = runTest(testDispatcher) {
        viewModel.handleIntent(TransactionIntent.LoadInitialData)
        testScheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(2, state.accountsList.size)
        assertEquals("acc_1", state.selectedAccountId)
        assertEquals("acc_2", state.selectedDestinationAccountId)
        assertEquals(2, state.categoriesList.size)
        assertEquals("cat_1", state.selectedCategoryId)
        assertFalse(state.isLoading)
    }

    @Test
    fun `intent change type and inputs updates state correctly`() {
        viewModel.handleIntent(TransactionIntent.ChangeType("INCOME"))
        viewModel.handleIntent(TransactionIntent.ChangeAmount("150.00"))
        viewModel.handleIntent(TransactionIntent.ChangeDescription("Bonus"))

        val state = viewModel.state.value
        assertEquals("INCOME", state.type)
        assertEquals("150.00", state.amount)
        assertEquals("Bonus", state.description)
    }

    @Test
    fun `save fails when amount is negative or zero`() = runTest(testDispatcher) {
        viewModel.handleIntent(TransactionIntent.LoadInitialData)
        testScheduler.advanceUntilIdle()

        viewModel.handleIntent(TransactionIntent.ChangeAmount("0"))
        viewModel.handleIntent(TransactionIntent.Save)

        val state = viewModel.state.value
        assertEquals("Amount must be greater than zero", state.errorMsg)
    }

    @Test
    fun `save fails when description is empty`() = runTest(testDispatcher) {
        viewModel.handleIntent(TransactionIntent.LoadInitialData)
        testScheduler.advanceUntilIdle()

        viewModel.handleIntent(TransactionIntent.ChangeAmount("100.00"))
        viewModel.handleIntent(TransactionIntent.ChangeDescription(""))
        viewModel.handleIntent(TransactionIntent.Save)

        val state = viewModel.state.value
        assertEquals("Description cannot be empty", state.errorMsg)
    }

    @Test
    fun `save standard transaction succeeds`() = runTest(testDispatcher) {
        viewModel.handleIntent(TransactionIntent.LoadInitialData)
        testScheduler.advanceUntilIdle()

        viewModel.handleIntent(TransactionIntent.ChangeAmount("100.00"))
        viewModel.handleIntent(TransactionIntent.ChangeDescription("Groceries"))
        viewModel.handleIntent(TransactionIntent.Save)
        testScheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertNull(state.errorMsg)
        assertTrue(state.isSuccess)
    }

    @Test
    fun `save transfer fails when accounts are identical`() = runTest(testDispatcher) {
        viewModel.handleIntent(TransactionIntent.LoadInitialData)
        testScheduler.advanceUntilIdle()

        viewModel.handleIntent(TransactionIntent.ChangeType("TRANSFER"))
        viewModel.handleIntent(TransactionIntent.SelectAccount("acc_1"))
        viewModel.handleIntent(TransactionIntent.SelectDestinationAccount("acc_1"))
        viewModel.handleIntent(TransactionIntent.ChangeAmount("100.00"))
        viewModel.handleIntent(TransactionIntent.ChangeDescription("Intra-transfer"))
        viewModel.handleIntent(TransactionIntent.Save)
        testScheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals("Source and destination accounts must not be identical", state.errorMsg)
    }

    @Test
    fun `save transfer succeeds`() = runTest(testDispatcher) {
        viewModel.handleIntent(TransactionIntent.LoadInitialData)
        testScheduler.advanceUntilIdle()

        viewModel.handleIntent(TransactionIntent.ChangeType("TRANSFER"))
        viewModel.handleIntent(TransactionIntent.SelectAccount("acc_1"))
        viewModel.handleIntent(TransactionIntent.SelectDestinationAccount("acc_2"))
        viewModel.handleIntent(TransactionIntent.ChangeAmount("100.00"))
        viewModel.handleIntent(TransactionIntent.ChangeDescription("Savings transfer"))
        viewModel.handleIntent(TransactionIntent.Save)
        testScheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertNull(state.errorMsg)
        assertTrue(state.isSuccess)
    }

    @Test
    fun `delete transaction succeeds`() = runTest(testDispatcher) {
        viewModel.handleIntent(TransactionIntent.LoadDetails("tx_1"))
        testScheduler.advanceUntilIdle()

        viewModel.handleIntent(TransactionIntent.Delete)
        testScheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertNull(state.errorMsg)
        assertTrue(state.isSuccess)
    }

    // --- Fakes & Helper Utilities ---

    private class FakeTransactionRepository : TransactionRepository {
        var syncAllCalled = false
        var shouldDeleteSucceed = true
        var shouldSaveSucceed = true
        private val transactionsFlow = MutableStateFlow<List<Transaction>>(emptyList())
        private val categories = listOf(
            CategoryResponse("cat_1", "Grocery", "EXPENSE", true),
            CategoryResponse("cat_2", "Salary", "INCOME", true)
        )

        fun setTransactions(list: List<Transaction>) {
            transactionsFlow.value = list
        }

        override fun getTransactions(userId: String): Flow<List<Transaction>> {
            return transactionsFlow
        }

        override suspend fun createTransaction(
            userId: String,
            type: String,
            amount: Double,
            accountId: String,
            categoryId: String,
            description: String,
            date: String
        ): Result<Transaction> {
            return if (shouldSaveSucceed) {
                val tx = Transaction(
                    id = "tx_new",
                    userId = userId,
                    type = type,
                    accountId = accountId,
                    accountName = "Checking",
                    categoryId = categoryId,
                    categoryName = "Grocery",
                    amount = amount,
                    date = date,
                    description = description,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                Result.success(tx)
            } else {
                Result.failure(Exception("Save failed"))
            }
        }

        override suspend fun updateTransaction(
            id: String,
            userId: String,
            type: String,
            amount: Double,
            accountId: String,
            categoryId: String,
            description: String,
            date: String
        ): Result<Transaction> {
            return if (shouldSaveSucceed) {
                val tx = Transaction(
                    id = id,
                    userId = userId,
                    type = type,
                    accountId = accountId,
                    accountName = "Checking",
                    categoryId = categoryId,
                    categoryName = "Grocery",
                    amount = amount,
                    date = date,
                    description = description,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                Result.success(tx)
            } else {
                Result.failure(Exception("Save failed"))
            }
        }

        override suspend fun deleteTransaction(id: String): Result<Unit> {
            return if (shouldDeleteSucceed) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Delete failed"))
            }
        }

        override suspend fun createTransfer(
            userId: String,
            sourceAccountId: String,
            destinationAccountId: String,
            amount: Double,
            description: String,
            date: String
        ): Result<Transaction> {
            return if (shouldSaveSucceed) {
                val tx = Transaction(
                    id = "tf_new",
                    userId = userId,
                    type = "TRANSFER",
                    accountId = sourceAccountId,
                    accountName = "Checking",
                    destinationAccountId = destinationAccountId,
                    destinationAccountName = "Savings",
                    amount = amount,
                    date = date,
                    description = description,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                Result.success(tx)
            } else {
                Result.failure(Exception("Save failed"))
            }
        }

        override suspend fun getCategories(): Result<List<CategoryResponse>> {
            return Result.success(categories)
        }

        override suspend fun syncAll(userId: String): Result<Unit> {
            syncAllCalled = true
            return Result.success(Unit)
        }
    }

    private class FakeAccountRepository : AccountRepository {
        private val accountsFlow = MutableStateFlow<List<Account>>(emptyList())
        var syncCalled = false

        fun setAccounts(list: List<Account>) {
            accountsFlow.value = list
        }

        override fun getAccounts(userId: String): Flow<List<Account>> {
            return accountsFlow
        }

        override suspend fun createAccount(
            userId: String,
            name: String,
            type: String,
            balance: Double
        ): Result<Account> {
            return Result.success(Account("new_id", userId, name, type, balance, 0, 0))
        }

        override suspend fun updateAccount(
            id: String,
            userId: String,
            name: String,
            type: String,
            balance: Double
        ): Result<Account> {
            return Result.success(Account(id, userId, name, type, balance, 0, 0))
        }

        override suspend fun deleteAccount(id: String): Result<Unit> {
            return Result.success(Unit)
        }

        override suspend fun syncAccounts(userId: String): Result<Unit> {
            syncCalled = true
            return Result.success(Unit)
        }
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
