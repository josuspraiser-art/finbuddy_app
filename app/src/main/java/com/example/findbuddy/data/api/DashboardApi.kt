package com.example.findbuddy.data.api

import com.example.findbuddy.data.model.DashboardResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface DashboardApi {

    @GET("api/dashboard")
    suspend fun getDashboardData(
        @Query("month") month: Int?,
        @Query("year") year: Int?
    ): DashboardResponse
}
