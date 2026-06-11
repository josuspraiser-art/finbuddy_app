package com.example.findbuddy.ui.auth

data class AuthState(
    val username: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val isRememberMeChecked: Boolean = false,
    val isLoading: Boolean = false,
    val errorMsg: String? = null,
    val isSuccess: Boolean = false,
    val token: String? = null
)
