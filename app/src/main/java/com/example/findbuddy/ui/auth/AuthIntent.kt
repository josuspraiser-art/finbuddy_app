package com.example.findbuddy.ui.auth

sealed class AuthIntent {
    data class UsernameChanged(val username: String) : AuthIntent()
    data class PasswordChanged(val password: String) : AuthIntent()
    data class ConfirmPasswordChanged(val confirmPassword: String) : AuthIntent()
    object TogglePasswordVisibility : AuthIntent()
    data class ToggleRememberMe(val checked: Boolean) : AuthIntent()
    object SubmitLogin : AuthIntent()
    object SubmitSignup : AuthIntent()
    object ClearError : AuthIntent()
}
