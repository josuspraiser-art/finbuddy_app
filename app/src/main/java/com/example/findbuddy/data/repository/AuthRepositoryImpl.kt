package com.example.findbuddy.data.repository

import com.example.findbuddy.data.api.AuthApi
import com.example.findbuddy.data.local.TokenManager
import com.example.findbuddy.data.model.UserLoginRequest
import com.example.findbuddy.data.model.UserRegisterRequest
import com.example.findbuddy.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager
) : AuthRepository {

    override suspend fun signUp(username: String, password: String): Result<String> {
        return try {
            val response = authApi.signUp(UserRegisterRequest(username, password))
            tokenManager.saveToken(response.token)
            Result.success(response.token)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun login(username: String, password: String): Result<String> {
        return try {
            val response = authApi.login(UserLoginRequest(username, password))
            tokenManager.saveToken(response.token)
            Result.success(response.token)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
