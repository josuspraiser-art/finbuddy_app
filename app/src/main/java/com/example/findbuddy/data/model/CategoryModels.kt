package com.example.findbuddy.data.model

import com.google.gson.annotations.SerializedName

data class CategoryCreateRequest(
    @SerializedName("categoryName") val categoryName: String,
    @SerializedName("categoryType") val categoryType: String // INCOME or EXPENSE
)
