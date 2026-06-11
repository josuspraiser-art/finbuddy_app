package com.example.findbuddy.ui.accounts

import android.content.SharedPreferences
import com.example.findbuddy.data.local.TokenManager
import com.example.findbuddy.domain.model.Account
import com.example.findbuddy.domain.repository.AccountRepository
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
class AccountViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var fakeRepository: FakeAccountRepository
    private lateinit var tokenManager: TokenManager
    private lateinit var viewModel: AccountViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeAccountRepository()
        tokenManager = createMockTokenManager()
        // Save mock JWT token with user sub claim "stoic_user"
        tokenManager.saveToken("header.eyJzdWIiOiJzdG9pY191c2VyIn0.signature")

        viewModel = AccountViewModel(fakeRepository, tokenManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has empty list and is not loading`() = runTest(testDispatcher) {
        val state = viewModel.state.value
        assertTrue(state.accountsList.isEmpty())
        assertFalse(state.isLoading)
        assertNull(state.errorMsg)
        assertFalse(state.showAddDialog)
        assertFalse(state.showEditDialog)
    }

    @Test
    fun `intent load accounts fetches list and triggers sync`() = runTest(testDispatcher) {
        val accounts = listOf(
            Account("1", "stoic_user", "Checking", "BANK_ACCOUNT", 1000.0, 0, 0)
        )
        fakeRepository.setAccounts(accounts)

        viewModel.handleIntent(AccountIntent.LoadAccounts)
        testScheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(1, state.accountsList.size)
        assertEquals("Checking", state.accountsList[0].accountName)
        assertTrue(fakeRepository.syncCalled)
    }

    @Test
    fun `create account fails when name is empty`() {
        viewModel.handleIntent(AccountIntent.CreateAccount("", "BANK_ACCOUNT", 100.0))
        val state = viewModel.state.value
        assertEquals("Account name cannot be empty", state.errorMsg)
    }

    @Test
    fun `create account fails when name is duplicate`() = runTest(testDispatcher) {
        // Pre-populate duplicate name checking
        val accounts = listOf(
            Account("1", "stoic_user", "Checking", "BANK_ACCOUNT", 1000.0, 0, 0)
        )
        fakeRepository.setAccounts(accounts)
        viewModel.handleIntent(AccountIntent.LoadAccounts)
        testScheduler.advanceUntilIdle()

        viewModel.handleIntent(AccountIntent.CreateAccount("Checking", "BANK_ACCOUNT", 100.0))
        testScheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals("Account name must be unique", state.errorMsg)
    }

    @Test
    fun `create account succeeds with valid params`() = runTest(testDispatcher) {
        viewModel.handleIntent(AccountIntent.CreateAccount("Savings", "BANK_ACCOUNT", 500.0))
        testScheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertNull(state.errorMsg)
        assertFalse(state.showAddDialog)
    }

    @Test
    fun `update account succeeds with valid params`() = runTest(testDispatcher) {
        viewModel.handleIntent(AccountIntent.UpdateAccount("1", "Updated Checking", "BANK_ACCOUNT", 1200.0))
        testScheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertNull(state.errorMsg)
        assertFalse(state.showEditDialog)
    }

    @Test
    fun `delete account succeeds`() = runTest(testDispatcher) {
        viewModel.handleIntent(AccountIntent.DeleteAccount("1"))
        testScheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertNull(state.errorMsg)
        assertFalse(state.showEditDialog)
    }

    @Test
    fun `delete account fails when repository throws constraint error`() = runTest(testDispatcher) {
        fakeRepository.shouldDeleteSucceed = false

        viewModel.handleIntent(AccountIntent.DeleteAccount("1"))
        testScheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals("Deletion failed", state.errorMsg)
    }

    // --- Fakes & Helper Utilities ---

    private class FakeAccountRepository : AccountRepository {
        private val accountsFlow = MutableStateFlow<List<Account>>(emptyList())
        var syncCalled = false
        var shouldDeleteSucceed = true

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
            val exists = accountsFlow.value.any { it.userId == userId && it.accountName == name }
            if (exists) {
                return Result.failure(Exception("Account name must be unique"))
            }
            val newAccount = Account("new_id", userId, name, type, balance, 0, 0)
            accountsFlow.value = accountsFlow.value + newAccount
            return Result.success(newAccount)
        }

        override suspend fun updateAccount(
            id: String,
            userId: String,
            name: String,
            type: String,
            balance: Double
        ): Result<Account> {
            val exists = accountsFlow.value.any { it.userId == userId && it.accountName == name && it.id != id }
            if (exists) {
                return Result.failure(Exception("Account name must be unique"))
            }
            val updated = Account(id, userId, name, type, balance, 0, 0)
            return Result.success(updated)
        }

        override suspend fun deleteAccount(id: String): Result<Unit> {
            return if (shouldDeleteSucceed) {
                accountsFlow.value = accountsFlow.value.filter { it.id != id }
                Result.success(Unit)
            } else {
                Result.failure(Exception("Deletion failed"))
            }
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
