package com.example.findbuddy.ui.reports

sealed class ReportIntent {
    object LoadReport : ReportIntent()
    data class ChangePeriod(val period: String) : ReportIntent()
    data class FilterByCategory(val categoryId: String?) : ReportIntent()
    data class FilterByAccount(val accountId: String?) : ReportIntent()
    data class ChangeAnchorDate(val date: String) : ReportIntent()
    object ClearError : ReportIntent()
}
