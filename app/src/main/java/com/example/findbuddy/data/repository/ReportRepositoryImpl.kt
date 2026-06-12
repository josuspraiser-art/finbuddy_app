package com.example.findbuddy.data.repository

import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import com.example.findbuddy.data.api.ReportApi
import com.example.findbuddy.data.local.dao.AccountDao
import com.example.findbuddy.data.local.dao.CategoryDao
import com.example.findbuddy.data.local.dao.TransactionDao
import com.example.findbuddy.data.model.*
import com.example.findbuddy.domain.repository.AccountRepository
import com.example.findbuddy.domain.repository.BudgetRepository
import com.example.findbuddy.domain.repository.ReportRepository
import com.example.findbuddy.domain.repository.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportRepositoryImpl @Inject constructor(
    private val accountDao: AccountDao,
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val reportApi: ReportApi,
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val budgetRepository: BudgetRepository
) : ReportRepository {

    private fun getWeeklyBounds(anchor: LocalDate): Pair<LocalDate, LocalDate> {
        val dayOfWeek = anchor.dayOfWeek.value
        val monday = anchor.minusDays((dayOfWeek - 1).toLong())
        val sunday = monday.plusDays(6)
        return Pair(monday, sunday)
    }

    private fun getMonthlyBounds(anchor: LocalDate): Pair<LocalDate, LocalDate> {
        val startOfMonth = anchor.withDayOfMonth(1)
        val endOfMonth = anchor.withDayOfMonth(anchor.lengthOfMonth())
        return Pair(startOfMonth, endOfMonth)
    }

    override fun getIncomeExpenseReport(
        userId: String,
        period: String,
        date: String
    ): Flow<IncomeExpenseReportResponse> {
        val transactionsFlow = transactionDao.getTransactions(userId)

        return transactionsFlow.map { transactions ->
            val anchor = try {
                LocalDate.parse(date)
            } catch (e: Exception) {
                LocalDate.now()
            }

            if (period.equals("weekly", ignoreCase = true)) {
                val (monday, sunday) = getWeeklyBounds(anchor)
                val breakdown = ArrayList<IncomeExpenseBreakdown>()
                var totalInc = 0.0
                var totalExp = 0.0

                for (i in 0..6) {
                    val day = monday.plusDays(i.toLong())
                    val dayStr = day.toString()
                    val dayTxList = transactions.filter { it.date == dayStr }
                    val inc = dayTxList.filter { it.type == "INCOME" }.sumOf { it.amount }
                    val exp = dayTxList.filter { it.type == "EXPENSE" }.sumOf { it.amount }

                    breakdown.add(
                        IncomeExpenseBreakdown(
                            label = dayStr,
                            income = inc,
                            expense = exp
                        )
                    )
                    totalInc += inc
                    totalExp += exp
                }

                IncomeExpenseReportResponse(
                    totalIncome = totalInc,
                    totalExpense = totalExp,
                    savings = totalInc - totalExp,
                    breakdown = breakdown
                )
            } else {
                // Monthly: Show last 6 months ending at anchor month
                val breakdown = ArrayList<IncomeExpenseBreakdown>()
                var totalInc = 0.0
                var totalExp = 0.0

                for (i in 5 downTo 0) {
                    val monthDate = anchor.minusMonths(i.toLong())
                    val (start, end) = getMonthlyBounds(monthDate)

                    val startStr = start.toString()
                    val endStr = end.toString()

                    val monthTxList = transactions.filter { it.date in startStr..endStr }
                    val inc = monthTxList.filter { it.type == "INCOME" }.sumOf { it.amount }
                    val exp = monthTxList.filter { it.type == "EXPENSE" }.sumOf { it.amount }

                    val monthLabel = monthDate.month.getDisplayName(TextStyle.SHORT, Locale.US)
                    breakdown.add(
                        IncomeExpenseBreakdown(
                            label = monthLabel,
                            income = inc,
                            expense = exp
                        )
                    )

                    if (i == 0) {
                        // Current anchor month totals
                        totalInc = inc
                        totalExp = exp
                    }
                }

                IncomeExpenseReportResponse(
                    totalIncome = totalInc,
                    totalExpense = totalExp,
                    savings = totalInc - totalExp,
                    breakdown = breakdown
                )
            }
        }.flowOn(Dispatchers.IO)
    }

    override fun getCategoryReport(
        userId: String,
        period: String,
        date: String
    ): Flow<CategoryReportResponse> {
        val transactionsFlow = transactionDao.getTransactions(userId)
        val categoriesFlow = categoryDao.getCategories(userId)

        return combine(transactionsFlow, categoriesFlow) { transactions, categories ->
            val anchor = try {
                LocalDate.parse(date)
            } catch (e: Exception) {
                LocalDate.now()
            }

            val (start, end) = if (period.equals("weekly", ignoreCase = true)) {
                getWeeklyBounds(anchor)
            } else {
                getMonthlyBounds(anchor)
            }

            val startStr = start.toString()
            val endStr = end.toString()

            val periodTx = transactions.filter { it.date in startStr..endStr }
            val expenses = periodTx.filter { it.type == "EXPENSE" }
            val totalExp = expenses.sumOf { it.amount }

            val categoryMap = expenses.groupBy { it.categoryId }
            val categorySpends = categories.map { cat ->
                val spend = categoryMap[cat.id]?.sumOf { it.amount } ?: 0.0
                val percentage = if (totalExp > 0) (spend / totalExp) * 100 else 0.0
                CategorySpendResponse(
                    categoryId = cat.id,
                    categoryName = cat.categoryName,
                    spend = spend,
                    percentage = percentage
                )
            }.filter { it.spend > 0.0 }
             .sortedByDescending { it.spend }

            CategoryReportResponse(
                totalExpense = totalExp,
                categories = categorySpends
            )
        }.flowOn(Dispatchers.IO)
    }

    override fun getAccountReport(
        userId: String,
        period: String,
        date: String
    ): Flow<AccountReportResponse> {
        val transactionsFlow = transactionDao.getTransactions(userId)
        val accountsFlow = accountDao.getAccounts(userId)

        return combine(transactionsFlow, accountsFlow) { transactions, accounts ->
            val anchor = try {
                LocalDate.parse(date)
            } catch (e: Exception) {
                LocalDate.now()
            }

            val (start, end) = if (period.equals("weekly", ignoreCase = true)) {
                getWeeklyBounds(anchor)
            } else {
                getMonthlyBounds(anchor)
            }

            val startStr = start.toString()
            val endStr = end.toString()

            val periodTx = transactions.filter { it.date in startStr..endStr }

            val accountActivities = accounts.map { acc ->
                val accTx = periodTx.filter { it.accountId == acc.id }
                val txCount = accTx.size
                val spending = accTx.filter { it.type == "EXPENSE" }.sumOf { it.amount }

                AccountActivityResponse(
                    accountId = acc.id,
                    accountName = acc.accountName,
                    transactionCount = txCount,
                    totalSpending = spending,
                    currentBalance = acc.openingBalance
                )
            }

            AccountReportResponse(accounts = accountActivities)
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun syncReports(
        userId: String,
        period: String,
        date: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Sync all dependency tables
            accountRepository.syncAccounts(userId)
            transactionRepository.syncAll(userId)
            budgetRepository.syncBudgets(userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun exportPdfReport(
        userId: String,
        reportType: String,
        period: String,
        date: String?,
        accountId: String?,
        categoryId: String?,
        outputFile: File
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // First, try network download from server endpoint
            val responseBody = try {
                reportApi.exportPdfReport(
                    reportType = reportType,
                    period = period,
                    date = date,
                    accountId = accountId,
                    categoryId = categoryId
                )
            } catch (e: Exception) {
                null
            }

            if (responseBody != null) {
                FileOutputStream(outputFile).use { out ->
                    responseBody.byteStream().use { inp ->
                        inp.copyTo(out)
                    }
                }
                Result.success(Unit)
            } else {
                // Fallback to local PDF generation using native android.graphics.pdf.PdfDocument
                generateLocalPdf(
                    userId = userId,
                    reportType = reportType,
                    period = period,
                    date = date,
                    accountId = accountId,
                    categoryId = categoryId,
                    outputFile = outputFile
                )
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun generateLocalPdf(
        userId: String,
        reportType: String,
        period: String,
        date: String?,
        accountId: String?,
        categoryId: String?,
        outputFile: File
    ) {
        val anchor = try {
            LocalDate.parse(date)
        } catch (e: Exception) {
            LocalDate.now()
        }

        val (start, end) = if (period.equals("weekly", ignoreCase = true)) {
            getWeeklyBounds(anchor)
        } else {
            getMonthlyBounds(anchor)
        }

        val startStr = start.toString()
        val endStr = end.toString()

        val allTransactions = transactionDao.getTransactions(userId).first()
        val allCategories = categoryDao.getCategories(userId).first()
        val allAccounts = accountDao.getAccounts(userId).first()

        val categoryNameMap = allCategories.associateBy({ it.id }, { it.categoryName })
        val accountNameMap = allAccounts.associateBy({ it.id }, { it.accountName })

        val periodTx = allTransactions.filter { it.date in startStr..endStr }
            .filter { categoryId == null || it.categoryId == categoryId }
            .filter { accountId == null || it.accountId == accountId }
            .sortedByDescending { it.date }

        val totalIncome = periodTx.filter { it.type == "INCOME" }.sumOf { it.amount }
        val totalExpense = periodTx.filter { it.type == "EXPENSE" }.sumOf { it.amount }
        val savings = totalIncome - totalExpense

        val pdfDocument = PdfDocument()
        val paint = Paint()
        
        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        // Header Helper
        fun drawPageHeader(canvas: android.graphics.Canvas, isContinuation: Boolean) {
            paint.reset()
            if (!isContinuation) {
                // Background accent
                paint.color = 0xFF003336.toInt() // Primary Teal
                canvas.drawRect(40f, 40f, 555f, 45f, paint)

                paint.color = 0xFF003336.toInt()
                paint.textSize = 18f
                paint.isFakeBoldText = true
                canvas.drawText("FinBuddy Personal Ledger Report", 40f, 70f, paint)

                paint.color = 0xFF64748B.toInt() // Slate grey
                paint.textSize = 9f
                paint.isFakeBoldText = false
                val dateStr = LocalDate.now().toString()
                canvas.drawText("Generated: $dateStr | Period: ${period.uppercase()}", 40f, 85f, paint)

                val catFilterText = allCategories.find { it.id == categoryId }?.categoryName ?: "All Categories"
                val accFilterText = allAccounts.find { it.id == accountId }?.accountName ?: "All Accounts"
                canvas.drawText("Filters - Category: $catFilterText | Account: $accFilterText", 40f, 98f, paint)
                canvas.drawText("Date Range: $startStr to $endStr", 40f, 111f, paint)

                paint.color = 0xFFE2E8F0.toInt() // border subtle
                canvas.drawLine(40f, 120f, 555f, 120f, paint)
            } else {
                paint.color = 0xFF003336.toInt()
                paint.textSize = 10f
                paint.isFakeBoldText = true
                canvas.drawText("FinBuddy Ledger Report (Continued)", 40f, 55f, paint)

                paint.color = 0xFF64748B.toInt()
                paint.textSize = 8f
                paint.isFakeBoldText = false
                canvas.drawText("Date Range: $startStr to $endStr", 400f, 55f, paint)

                paint.color = 0xFFE2E8F0.toInt()
                canvas.drawLine(40f, 65f, 555f, 65f, paint)
            }
        }

        // Footer Helper
        fun drawPageFooter(canvas: android.graphics.Canvas, pageIdx: Int) {
            paint.reset()
            paint.color = 0xFF64748B.toInt()
            paint.textSize = 8f
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("Page $pageIdx", 297.5f, 815f, paint)
        }

        // Draw initial page header
        drawPageHeader(canvas, false)

        // Draw Summary Metrics Bento Cards
        paint.reset()
        // Income Card
        paint.color = 0xFFE8F5E9.toInt() // Growth green bg
        canvas.drawRoundRect(40f, 135f, 190f, 190f, 6f, 6f, paint)
        paint.color = 0xFF2E7D32.toInt() // Dark green text
        paint.textSize = 8f
        paint.isFakeBoldText = true
        canvas.drawText("TOTAL INCOME", 50f, 155f, paint)
        paint.textSize = 14f
        canvas.drawText(String.format("$%,.2f", totalIncome), 50f, 175f, paint)

        // Expense Card
        paint.color = 0xFFFFEBEE.toInt() // Expense coral bg
        canvas.drawRoundRect(205f, 135f, 355f, 190f, 6f, 6f, paint)
        paint.color = 0xFFC62828.toInt() // Dark red text
        paint.textSize = 8f
        paint.isFakeBoldText = true
        canvas.drawText("TOTAL EXPENSES", 215f, 155f, paint)
        paint.textSize = 14f
        canvas.drawText(String.format("$%,.2f", totalExpense), 215f, 175f, paint)

        // Net Savings Card
        paint.color = 0xFFE0F2F1.toInt() // Teal light bg
        canvas.drawRoundRect(370f, 135f, 520f, 190f, 6f, 6f, paint)
        paint.color = 0xFF00695C.toInt() // Dark teal text
        paint.textSize = 8f
        paint.isFakeBoldText = true
        canvas.drawText("NET SAVINGS", 380f, 155f, paint)
        paint.textSize = 14f
        canvas.drawText(String.format("$%,.2f", savings), 380f, 175f, paint)

        var currentY = 210f

        // Draw Category Donut Chart
        if (periodTx.isNotEmpty()) {
            val expenses = periodTx.filter { it.type == "EXPENSE" }
            val totalExpenseAmount = expenses.sumOf { it.amount }

            if (totalExpenseAmount > 0.0) {
                paint.reset()
                paint.color = 0xFF003336.toInt()
                paint.textSize = 11f
                paint.isFakeBoldText = true
                canvas.drawText("Category Spend Share", 40f, currentY + 15f, paint)

                val categoryGroups = expenses.groupBy { it.categoryId }
                val spendShares = categoryGroups.map { (catId, list) ->
                    val spend = list.sumOf { it.amount }
                    val name = categoryNameMap[catId] ?: "Others"
                    Pair(name, spend)
                }.sortedByDescending { it.second }

                val chartColors = intArrayOf(
                    0xFF004B50.toInt(), // Teal
                    0xFF43A047.toInt(), // Green
                    0xFFE5A93B.toInt(), // Yellow
                    0xFF8B5CF6.toInt(), // Violet
                    0xFFEC4899.toInt(), // Pink
                    0xFF06B6D4.toInt(), // Cyan
                    0xFFF59E0B.toInt()  // Amber
                )

                val rectF = RectF(60f, currentY + 30f, 160f, currentY + 130f)
                var startAngle = -90f
                spendShares.forEachIndexed { index, share ->
                    val sweepAngle = ((share.second / totalExpenseAmount) * 360f).toFloat()
                    paint.reset()
                    paint.color = chartColors[index % chartColors.size]
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = 16f
                    canvas.drawArc(rectF, startAngle, sweepAngle, false, paint)
                    startAngle += sweepAngle
                }

                var legendY = currentY + 45f
                spendShares.take(5).forEachIndexed { index, share ->
                    paint.reset()
                    paint.color = chartColors[index % chartColors.size]
                    paint.style = Paint.Style.FILL
                    canvas.drawCircle(200f, legendY - 3f, 4f, paint)

                    paint.color = 0xFF1E293B.toInt()
                    paint.textSize = 9f
                    paint.isFakeBoldText = false
                    val text = String.format("%s: $%,.2f (%.1f%%)", share.first, share.second, (share.second / totalExpenseAmount) * 100.0)
                    canvas.drawText(text, 212f, legendY, paint)
                    legendY += 16f
                }

                currentY += 150f
            } else {
                currentY += 20f
            }
        }

        // Draw Transactions Table Title
        paint.reset()
        paint.color = 0xFF003336.toInt()
        paint.textSize = 11f
        paint.isFakeBoldText = true
        canvas.drawText("Transactions Record", 40f, currentY, paint)
        currentY += 15f

        // Table Header
        paint.color = 0xFFE2E8F0.toInt()
        canvas.drawRect(40f, currentY, 555f, currentY + 20f, paint)

        paint.color = 0xFF1E293B.toInt()
        paint.textSize = 8f
        paint.isFakeBoldText = true
        canvas.drawText("Date", 45f, currentY + 13f, paint)
        canvas.drawText("Description", 110f, currentY + 13f, paint)
        canvas.drawText("Category", 220f, currentY + 13f, paint)
        canvas.drawText("Account", 310f, currentY + 13f, paint)
        canvas.drawText("Type", 400f, currentY + 13f, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Amount", 550f, currentY + 13f, paint)

        currentY += 20f

        if (periodTx.isEmpty()) {
            paint.reset()
            paint.color = 0xFF64748B.toInt()
            paint.textSize = 10f
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("No transaction data found for the selected filters", 297.5f, currentY + 40f, paint)
        } else {
            periodTx.forEach { tx ->
                val rowHeight = 20f
                if (currentY + rowHeight > 780f) {
                    drawPageFooter(canvas, pageNumber)
                    pdfDocument.finishPage(page)

                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas

                    drawPageHeader(canvas, true)

                    paint.reset()
                    paint.color = 0xFFE2E8F0.toInt()
                    canvas.drawRect(40f, 75f, 555f, 95f, paint)

                    paint.color = 0xFF1E293B.toInt()
                    paint.textSize = 8f
                    paint.isFakeBoldText = true
                    canvas.drawText("Date", 45f, 88f, paint)
                    canvas.drawText("Description", 110f, 88f, paint)
                    canvas.drawText("Category", 220f, 88f, paint)
                    canvas.drawText("Account", 310f, 88f, paint)
                    canvas.drawText("Type", 400f, 88f, paint)
                    paint.textAlign = Paint.Align.RIGHT
                    canvas.drawText("Amount", 550f, 88f, paint)

                    currentY = 95f
                }

                paint.reset()
                paint.color = 0xFF1E293B.toInt()
                paint.textSize = 8f
                paint.isFakeBoldText = false
                paint.textAlign = Paint.Align.LEFT
                canvas.drawText(tx.date, 45f, currentY + 13f, paint)

                val desc = if (tx.description.length > 22) tx.description.take(20) + ".." else tx.description
                canvas.drawText(desc, 110f, currentY + 13f, paint)

                val catName = categoryNameMap[tx.categoryId] ?: "Others"
                canvas.drawText(catName, 220f, currentY + 13f, paint)

                val accName = accountNameMap[tx.accountId] ?: "Checking"
                canvas.drawText(accName, 310f, currentY + 13f, paint)

                canvas.drawText(tx.type, 400f, currentY + 13f, paint)

                val amountText = when (tx.type) {
                    "INCOME" -> String.format("+$%,.2f", tx.amount)
                    "EXPENSE" -> String.format("-$%,.2f", tx.amount)
                    else -> String.format("$%,.2f", tx.amount)
                }
                paint.color = when (tx.type) {
                    "INCOME" -> 0xFF2E7D32.toInt()
                    "EXPENSE" -> 0xFFC62828.toInt()
                    else -> 0xFF475569.toInt()
                }
                paint.isFakeBoldText = true
                paint.textAlign = Paint.Align.RIGHT
                canvas.drawText(amountText, 550f, currentY + 13f, paint)

                currentY += rowHeight
            }
        }

        drawPageFooter(canvas, pageNumber)
        pdfDocument.finishPage(page)

        FileOutputStream(outputFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()
    }
}
