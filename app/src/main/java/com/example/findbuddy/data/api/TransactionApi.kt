package com.example.findbuddy.data.api

import com.example.findbuddy.data.model.*
import retrofit2.http.*

interface TransactionApi {

    @POST("api/transactions")
    suspend fun createTransaction(
        @Body request: TransactionCreateRequest
    ): TransactionResponse

    @GET("api/transactions")
    suspend fun getTransactions(
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null,
        @Query("type") type: String? = null,
        @Query("accountId") accountId: String? = null,
        @Query("categoryId") categoryId: String? = null
    ): List<TransactionResponse>

    @PUT("api/transactions/{id}")
    suspend fun updateTransaction(
        @Path("id") id: String,
        @Body request: TransactionUpdateRequest
    ): TransactionResponse

    @DELETE("api/transactions/{id}")
    suspend fun deleteTransaction(
        @Path("id") id: String
    )

    @POST("api/transfers")
    suspend fun createTransfer(
        @Body request: TransferCreateRequest
    ): TransferResponse

    @GET("api/categories")
    suspend fun getCategories(): List<CategoryResponse>
}
