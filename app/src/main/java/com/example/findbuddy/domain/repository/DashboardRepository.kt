package com.example.findbuddy.domain.repository

import com.example.findbuddy.domain.model.DashboardData
import kotlinx.coroutines.flow.Flow

interface DashboardRepository {
    fun getDashboardData(userId: String, month: Int, year: Int): Flow<DashboardData>
    suspend fun syncDashboard(userId: String, month: Int, year: Int): Result<Unit>
}
