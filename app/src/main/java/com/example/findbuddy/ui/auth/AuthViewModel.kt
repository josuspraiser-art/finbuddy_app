package com.example.findbuddy.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.findbuddy.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthEffect {
    object NavigateToDashboard : AuthEffect()
    data class ShowToast(val message: String) : AuthEffect()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<AuthEffect>()
    val effect: SharedFlow<AuthEffect> = _effect.asSharedFlow()

    fun handleIntent(intent: AuthIntent) {
        when (intent) {
            is AuthIntent.UsernameChanged -> {
                _state.update { it.copy(username = intent.username) }
            }
            is AuthIntent.PasswordChanged -> {
                _state.update { it.copy(password = intent.password) }
            }
            is AuthIntent.ConfirmPasswordChanged -> {
                _state.update { it.copy(confirmPassword = intent.confirmPassword) }
            }
            is AuthIntent.TogglePasswordVisibility -> {
                _state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            }
            is AuthIntent.ToggleRememberMe -> {
                _state.update { it.copy(isRememberMeChecked = intent.checked) }
            }
            is AuthIntent.SubmitLogin -> {
                submitLogin()
            }
            is AuthIntent.SubmitSignup -> {
                submitSignup()
            }
            is AuthIntent.ClearError -> {
                _state.update { it.copy(errorMsg = null) }
            }
        }
    }

    private fun submitLogin() {
        val currentState = _state.value
        if (currentState.username.isBlank() || currentState.password.isBlank()) {
            _state.update { it.copy(errorMsg = "Username and password cannot be empty") }
            return
        }

        _state.update { it.copy(isLoading = true, errorMsg = null) }
        viewModelScope.launch {
            authRepository.login(currentState.username, currentState.password)
                .onSuccess { token ->
                    _state.update { it.copy(isLoading = false, isSuccess = true, token = token) }
                    _effect.emit(AuthEffect.NavigateToDashboard)
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false, errorMsg = error.message ?: "Login failed") }
                }
        }
    }

    private fun submitSignup() {
        val currentState = _state.value
        if (currentState.username.isBlank() || currentState.password.isBlank() || currentState.confirmPassword.isBlank()) {
            _state.update { it.copy(errorMsg = "All fields are required") }
            return
        }

        if (currentState.password.length < 8) {
            _state.update { it.copy(errorMsg = "Password must be at least 8 characters long") }
            return
        }

        if (currentState.password != currentState.confirmPassword) {
            _state.update { it.copy(errorMsg = "Master keys do not match. Please verify your password.") }
            return
        }

        _state.update { it.copy(isLoading = true, errorMsg = null) }
        viewModelScope.launch {
            authRepository.signUp(currentState.username, currentState.password)
                .onSuccess { token ->
                    _state.update { it.copy(isLoading = false, isSuccess = true, token = token) }
                    _effect.emit(AuthEffect.NavigateToDashboard)
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false, errorMsg = error.message ?: "Registration failed") }
                }
        }
    }
}
