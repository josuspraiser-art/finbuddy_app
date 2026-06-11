package com.example.findbuddy.data.model

import com.google.gson.annotations.SerializedName

data class TransactionCreateRequest(
    @SerializedName("type") val type: String,
    @SerializedName("amount") val amount: Double,
    @SerializedName("accountId") val accountId: String,
    @SerializedName("categoryId") val categoryId: String,
    @SerializedName("description") val description: String,
    @SerializedName("date") val date: String
)

data class TransactionUpdateRequest(
    @SerializedName("type") val type: String,
    @SerializedName("amount") val amount: Double,
    @SerializedName("accountId") val accountId: String,
    @SerializedName("categoryId") val categoryId: String,
    @SerializedName("description") val description: String,
    @SerializedName("date") val date: String
)

data class TransactionResponse(
    @SerializedName("id") val id: String,
    @SerializedName("type") val type: String,
    @SerializedName("amount") val amount: Double,
    @SerializedName("accountId") val accountId: String,
    @SerializedName("categoryId") val categoryId: String,
    @SerializedName("description") val description: String,
    @SerializedName("date") val date: String
)

data class TransferCreateRequest(
    @SerializedName("sourceAccountId") val sourceAccountId: String,
    @SerializedName("destinationAccountId") val destinationAccountId: String,
    @SerializedName("amount") val amount: Double,
    @SerializedName("description") val description: String,
    @SerializedName("date") val date: String
)

data class TransferResponse(
    @SerializedName("id") val id: String,
    @SerializedName("sourceAccountId") val sourceAccountId: String,
    @SerializedName("destinationAccountId") val destinationAccountId: String,
    @SerializedName("amount") val amount: Double,
    @SerializedName("description") val description: String,
    @SerializedName("date") val date: String
)

data class CategoryResponse(
    @SerializedName("id") val id: String,
    @SerializedName("categoryName") val categoryName: String,
    @SerializedName("categoryType") val categoryType: String,
    @SerializedName("isSystem") val isSystem: Boolean
)

