package com.example.findbuddy.ui.dashboard

sealed class DashboardIntent {
    object LoadDashboard : DashboardIntent()
    data class ChangePeriod(val month: Int, val year: Int) : DashboardIntent()
    object ClearError : DashboardIntent()
}
