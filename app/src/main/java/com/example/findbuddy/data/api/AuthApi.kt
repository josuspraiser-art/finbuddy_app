package com.example.findbuddy.data.api

import com.example.findbuddy.data.model.UserLoginRequest
import com.example.findbuddy.data.model.UserLoginResponse
import com.example.findbuddy.data.model.UserRegisterRequest
import com.example.findbuddy.data.model.UserRegisterResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("api/auth/signup")
    suspend fun signUp(
        @Body request: UserRegisterRequest
    ): UserRegisterResponse

    @POST("api/auth/login")
    suspend fun login(
        @Body request: UserLoginRequest
    ): UserLoginResponse
}
