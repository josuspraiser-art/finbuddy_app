package com.example.findbuddy.domain.repository

interface AuthRepository {
    suspend fun signUp(username: String, password: String): Result<String>
    suspend fun login(username: String, password: String): Result<String>
}
