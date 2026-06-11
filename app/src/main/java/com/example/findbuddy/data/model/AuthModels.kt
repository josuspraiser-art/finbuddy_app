package com.example.findbuddy.data.model

import com.google.gson.annotations.SerializedName

data class UserRegisterRequest(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String
)

data class UserRegisterResponse(
    @SerializedName("userId") val userId: String,
    @SerializedName("token") val token: String
)

data class UserLoginRequest(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String
)

data class UserLoginResponse(
    @SerializedName("token") val token: String
)
