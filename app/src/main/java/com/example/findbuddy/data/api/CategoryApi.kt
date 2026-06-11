package com.example.findbuddy.data.api

import com.example.findbuddy.data.model.CategoryCreateRequest
import com.example.findbuddy.data.model.CategoryResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface CategoryApi {

    @POST("api/categories")
    suspend fun createCategory(
        @Body request: CategoryCreateRequest
    ): CategoryResponse

    @GET("api/categories")
    suspend fun getCategories(): List<CategoryResponse>
}
