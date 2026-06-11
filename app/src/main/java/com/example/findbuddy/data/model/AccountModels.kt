package com.example.findbuddy.data.model

import com.google.gson.annotations.SerializedName

data class AccountCreateRequest(
    @SerializedName("accountName") val accountName: String,
    @SerializedName("accountType") val accountType: String,
    @SerializedName("openingBalance") val openingBalance: Double
)

data class AccountUpdateRequest(
    @SerializedName("accountName") val accountName: String,
    @SerializedName("accountType") val accountType: String,
    @SerializedName("openingBalance") val openingBalance: Double
)

data class AccountResponse(
    @SerializedName("id") val id: String,
    @SerializedName("accountName") val accountName: String,
    @SerializedName("accountType") val accountType: String,
    @SerializedName("openingBalance") val openingBalance: Double
)
