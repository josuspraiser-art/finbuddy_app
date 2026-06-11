package com.example.findbuddy.domain.repository

import com.example.findbuddy.data.model.AccountReportResponse
import com.example.findbuddy.data.model.CategoryReportResponse
import com.example.findbuddy.data.model.IncomeExpenseReportResponse
import kotlinx.coroutines.flow.Flow

interface ReportRepository {
    fun getIncomeExpenseReport(userId: String, period: String, date: String): Flow<IncomeExpenseReportResponse>
    fun getCategoryReport(userId: String, period: String, date: String): Flow<CategoryReportResponse>
    fun getAccountReport(userId: String, period: String, date: String): Flow<AccountReportResponse>
    suspend fun syncReports(userId: String, period: String, date: String): Result<Unit>
}
