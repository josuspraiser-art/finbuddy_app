package com.example.findbuddy.data.api

import com.example.findbuddy.data.model.AccountCreateRequest
import com.example.findbuddy.data.model.AccountResponse
import com.example.findbuddy.data.model.AccountUpdateRequest
import retrofit2.http.*

interface AccountApi {

    @POST("api/accounts")
    suspend fun createAccount(
        @Body request: AccountCreateRequest
    ): AccountResponse

    @GET("api/accounts")
    suspend fun getAccounts(): List<AccountResponse>

    @PUT("api/accounts/{id}")
    suspend fun updateAccount(
        @Path("id") id: String,
        @Body request: AccountUpdateRequest
    ): AccountResponse

    @DELETE("api/accounts/{id}")
    suspend fun deleteAccount(
        @Path("id") id: String
    )
}
