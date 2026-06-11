package com.example.findbuddy.data.api

import com.example.findbuddy.data.model.AccountReportResponse
import com.example.findbuddy.data.model.CategoryReportResponse
import com.example.findbuddy.data.model.IncomeExpenseReportResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ReportApi {

    @GET("api/reports/income-expense")
    suspend fun getIncomeExpenseReport(
        @Query("period") period: String,
        @Query("date") date: String?
    ): IncomeExpenseReportResponse

    @GET("api/reports/category")
    suspend fun getCategoryReport(
        @Query("period") period: String,
        @Query("date") date: String?
    ): CategoryReportResponse

    @GET("api/reports/account")
    suspend fun getAccountReport(
        @Query("period") period: String,
        @Query("date") date: String?
    ): AccountReportResponse
}
