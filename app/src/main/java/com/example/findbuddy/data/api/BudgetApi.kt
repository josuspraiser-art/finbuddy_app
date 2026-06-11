package com.example.findbuddy.data.api

import com.example.findbuddy.data.model.BudgetCreateRequest
import com.example.findbuddy.data.model.BudgetResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface BudgetApi {

    @POST("api/budgets")
    suspend fun createBudget(
        @Body request: BudgetCreateRequest
    ): BudgetResponse

    @GET("api/budgets")
    suspend fun getBudgets(): List<BudgetResponse>
}
