package com.example.findbuddy.ui.auth

import com.example.findbuddy.domain.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var fakeRepository: FakeAuthRepository
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeAuthRepository()
        viewModel = AuthViewModel(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is empty`() {
        val state = viewModel.state.value
        assertEquals("", state.username)
        assertEquals("", state.password)
        assertEquals("", state.confirmPassword)
        assertNull(state.errorMsg)
        assertEquals(false, state.isLoading)
    }

    @Test
    fun `intent username changed updates state`() {
        viewModel.handleIntent(AuthIntent.UsernameChanged("stoic_user"))
        assertEquals("stoic_user", viewModel.state.value.username)
    }

    @Test
    fun `intent password changed updates state`() {
        viewModel.handleIntent(AuthIntent.PasswordChanged("secret_key"))
        assertEquals("secret_key", viewModel.state.value.password)
    }

    @Test
    fun `intent confirm password changed updates state`() {
        viewModel.handleIntent(AuthIntent.ConfirmPasswordChanged("secret_key"))
        assertEquals("secret_key", viewModel.state.value.confirmPassword)
    }

    @Test
    fun `login fails when fields are empty`() {
        viewModel.handleIntent(AuthIntent.SubmitLogin)
        assertEquals("Username and password cannot be empty", viewModel.state.value.errorMsg)
    }

    @Test
    fun `signup fails when master keys mismatch`() {
        viewModel.handleIntent(AuthIntent.UsernameChanged("user"))
        viewModel.handleIntent(AuthIntent.PasswordChanged("securepass123"))
        viewModel.handleIntent(AuthIntent.ConfirmPasswordChanged("mismatchpass"))
        viewModel.handleIntent(AuthIntent.SubmitSignup)
        assertEquals("Master keys do not match. Please verify your password.", viewModel.state.value.errorMsg)
    }

    @Test
    fun `signup fails when password length is under 8 characters`() {
        viewModel.handleIntent(AuthIntent.UsernameChanged("user"))
        viewModel.handleIntent(AuthIntent.PasswordChanged("short"))
        viewModel.handleIntent(AuthIntent.ConfirmPasswordChanged("short"))
        viewModel.handleIntent(AuthIntent.SubmitSignup)
        assertEquals("Password must be at least 8 characters long", viewModel.state.value.errorMsg)
    }

    @Test
    fun `signup succeeds with matching credentials`() = runTest(testDispatcher) {
        viewModel.handleIntent(AuthIntent.UsernameChanged("valid_user"))
        viewModel.handleIntent(AuthIntent.PasswordChanged("valid_password"))
        viewModel.handleIntent(AuthIntent.ConfirmPasswordChanged("valid_password"))
        viewModel.handleIntent(AuthIntent.SubmitSignup)

        testScheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(false, state.isLoading)
        assertEquals(true, state.isSuccess)
        assertNotNull(state.token)
        assertNull(state.errorMsg)
    }

    private class FakeAuthRepository : AuthRepository {
        var shouldSucceed = true

        override suspend fun signUp(username: String, password: String): Result<String> {
            return if (shouldSucceed) {
                Result.success("fake_jwt_token")
            } else {
                Result.failure(Exception("Registration failed"))
            }
        }

        override suspend fun login(username: String, password: String): Result<String> {
            return if (shouldSucceed) {
                Result.success("fake_jwt_token")
            } else {
                Result.failure(Exception("Invalid credentials"))
            }
        }
    }
}
