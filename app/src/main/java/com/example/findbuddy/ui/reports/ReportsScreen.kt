package com.example.findbuddy.ui.reports

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.findbuddy.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    state: ReportState,
    onIntent: (ReportIntent) -> Unit,
    onNavigateToDashboard: () -> Unit,
    onNavigateToAccounts: () -> Unit,
    onNavigateToBudgets: () -> Unit,
    onLogout: () -> Unit
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    var isCategoryDropdownExpanded by remember { mutableStateOf(false) }
    var isAccountDropdownExpanded by remember { mutableStateOf(false) }

    val selectedCategoryName = state.categoriesList.find { it.id == state.selectedCategoryId }?.categoryName ?: "All Categories"
    val selectedAccountName = state.accountsList.find { it.id == state.selectedAccountId }?.accountName ?: "All Accounts"

    // Theme Colors matching design specs
    val ExpenseCoral = Color(0xFFFFEBEE)
    val SecondaryContainerGreen = Color(0xFF98F994)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Shield",
                            tint = PrimaryTeal,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "FinBuddy",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = PrimaryTeal
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = OnSurfaceVariant
                        )
                    }
                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(PrimaryTeal)
                            .clickable { onLogout() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "SU",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceContainerLow,
                modifier = Modifier.height(80.dp)
            ) {
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToDashboard,
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Dashboard", fontSize = 11.sp, fontWeight = FontWeight.Medium) }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToAccounts,
                    icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Accounts") },
                    label = { Text("Accounts", fontSize = 11.sp, fontWeight = FontWeight.Medium) }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToBudgets,
                    icon = { Icon(Icons.Default.Savings, contentDescription = "Budgets") },
                    label = { Text("Budgets", fontSize = 11.sp, fontWeight = FontWeight.Medium) }
                )
                NavigationBarItem(
                    selected = true,
                    onClick = { /* Stay here */ },
                    icon = { Icon(Icons.Default.Analytics, contentDescription = "Reports") },
                    label = { Text("Reports", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryTeal,
                        selectedTextColor = PrimaryTeal,
                        indicatorColor = OutlineVariant.copy(alpha = 0.3f)
                    )
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundGray)
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (state.isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = PrimaryTeal
                )
            }

            // Metrics Summary Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Card 1: Total Income
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .fillMaxHeight()
                                .width(4.dp)
                                .background(SecondaryGreen)
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .padding(start = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "TOTAL INCOME",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = OnSurfaceMuted,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = String.format("$%,.2f", state.totalIncome),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryTeal
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    tint = SecondaryGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                val growthText = if (state.incomeGrowthPercentage >= 0) {
                                    String.format("+%.1f%% from last period", state.incomeGrowthPercentage)
                                } else {
                                    String.format("%.1f%% from last period", state.incomeGrowthPercentage)
                                }
                                Text(
                                    text = growthText,
                                    fontSize = 11.sp,
                                    color = SecondaryGreen,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Card 2: Total Expenses
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .fillMaxHeight()
                                .width(4.dp)
                                .background(TertiaryCoral)
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .padding(start = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "TOTAL EXPENSES",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = OnSurfaceMuted,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = String.format("$%,.2f", state.totalExpense),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = TertiaryCoral
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TrendingDown,
                                    contentDescription = null,
                                    tint = TertiaryCoral,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Monthly Spending Trend",
                                    fontSize = 11.sp,
                                    color = TertiaryCoral,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Card 3: Net Savings
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .fillMaxHeight()
                                .width(4.dp)
                                .background(SecondaryContainerGreen)
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .padding(start = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "NET SAVINGS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = OnSurfaceMuted,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = String.format("$%,.2f", state.savings),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = OnSecondaryGreenFixed
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Savings,
                                    contentDescription = null,
                                    tint = OnSecondaryGreenFixed,
                                    modifier = Modifier.size(16.dp)
                                )
                                val savingsRate = if (state.totalIncome > 0) (state.savings / state.totalIncome) * 100 else 0.0
                                Text(
                                    text = String.format("%d%% Savings Rate", savingsRate.toInt().coerceAtLeast(0)),
                                    fontSize = 11.sp,
                                    color = OnSecondaryGreenFixed,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // Filters & Actions
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Period Toggle Buttons Row
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(SurfaceContainerLow)
                        .border(1.dp, OutlineVariant, RoundedCornerShape(24.dp))
                        .padding(4.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        val isWeekly = state.selectedPeriod.equals("weekly", ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isWeekly) PrimaryTeal else Color.Transparent)
                                .clickable { onIntent(ReportIntent.ChangePeriod("weekly")) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Weekly",
                                color = if (isWeekly) Color.White else OnSurfaceVariant,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (!isWeekly) PrimaryTeal else Color.Transparent)
                                .clickable { onIntent(ReportIntent.ChangePeriod("monthly")) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Monthly",
                                color = if (!isWeekly) Color.White else OnSurfaceVariant,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                // Dropdowns & Action Button Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Category Dropdown
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)
                            .border(1.dp, OutlineVariant, RoundedCornerShape(8.dp))
                            .clickable { isCategoryDropdownExpanded = true }
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Category,
                                contentDescription = null,
                                tint = OnSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = selectedCategoryName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = OnSurfaceDark,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = OnSurfaceVariant
                            )
                        }

                        DropdownMenu(
                            expanded = isCategoryDropdownExpanded,
                            onDismissRequest = { isCategoryDropdownExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("All Categories", fontSize = 13.sp) },
                                onClick = {
                                    onIntent(ReportIntent.FilterByCategory(null))
                                    isCategoryDropdownExpanded = false
                                }
                            )
                            state.categoriesList.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category.categoryName, fontSize = 13.sp) },
                                    onClick = {
                                        onIntent(ReportIntent.FilterByCategory(category.id))
                                        isCategoryDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Account Dropdown
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)
                            .border(1.dp, OutlineVariant, RoundedCornerShape(8.dp))
                            .clickable { isAccountDropdownExpanded = true }
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = OnSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = selectedAccountName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = OnSurfaceDark,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = OnSurfaceVariant
                            )
                        }

                        DropdownMenu(
                            expanded = isAccountDropdownExpanded,
                            onDismissRequest = { isAccountDropdownExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("All Accounts", fontSize = 13.sp) },
                                onClick = {
                                    onIntent(ReportIntent.FilterByAccount(null))
                                    isAccountDropdownExpanded = false
                                }
                            )
                            state.accountsList.forEach { account ->
                                DropdownMenuItem(
                                    text = { Text(account.accountName, fontSize = 13.sp) },
                                    onClick = {
                                        onIntent(ReportIntent.FilterByAccount(account.id))
                                        isAccountDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Export PDF Action
                Button(
                    onClick = {
                        val file = java.io.File(context.cacheDir, "finbuddy_report.pdf")
                        onIntent(ReportIntent.ExportPdf(file))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Export PDF",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Category Distribution Donut Chart Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Category Distribution",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = PrimaryTeal
                        )
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Category distribution info",
                            tint = OnSurfaceMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    if (state.categoryDistribution.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No expense data for this period",
                                fontSize = 13.sp,
                                color = OnSurfaceMuted
                            )
                        }
                    } else {
                        val chartColors = listOf(
                            PrimaryTeal,
                            SecondaryGreen,
                            Color(0xFFE5A93B),
                            Color(0xFF8B5CF6),
                            Color(0xFFEC4899),
                            Color(0xFF06B6D4),
                            Color(0xFFF59E0B),
                            Color(0xFF10B981)
                        )

                        // Outer Box to layer Canvas and center label
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(
                                modifier = Modifier
                                    .size(180.dp)
                            ) {
                                var currentStartAngle = -90f
                                state.categoryDistribution.forEachIndexed { index, item ->
                                    val sweep = (item.percentage.toFloat() * 3.6f)
                                    val color = chartColors[index % chartColors.size]
                                    drawArc(
                                        color = color,
                                        startAngle = currentStartAngle,
                                        sweepAngle = sweep,
                                        useCenter = false,
                                        size = Size(size.width, size.height),
                                        style = Stroke(width = 50f)
                                    )
                                    currentStartAngle += sweep
                                }
                            }

                            // Center Text inside Donut
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "Total",
                                    fontSize = 12.sp,
                                    color = OnSurfaceMuted,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = String.format("$%,.1fk", state.totalExpense / 1000.0),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryTeal
                                )
                            }
                        }

                        // Legend
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            state.categoryDistribution.forEachIndexed { index, item ->
                                val color = chartColors[index % chartColors.size]
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .clip(CircleShape)
                                                .background(color)
                                        )
                                        Text(
                                            text = item.categoryName,
                                            fontSize = 13.sp,
                                            color = OnSurfaceDark
                                        )
                                    }
                                    Text(
                                        text = String.format("%d%% ($%,.2f)", item.percentage.toInt(), item.spend),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = OnSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Monthly Trends Bar Chart Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (state.selectedPeriod.equals("weekly", ignoreCase = true)) "Weekly Trends" else "Monthly Trends",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = PrimaryTeal
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(PrimaryTeal, RoundedCornerShape(2.dp))
                                )
                                Text("Income", fontSize = 11.sp, color = OnSurfaceMuted)
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(TertiaryCoral, RoundedCornerShape(2.dp))
                                )
                                Text("Expense", fontSize = 11.sp, color = OnSurfaceMuted)
                            }
                        }
                    }

                    if (state.breakdownList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No historical transactions found",
                                fontSize = 13.sp,
                                color = OnSurfaceMuted
                            )
                        }
                    } else {
                        // Drawing custom side-by-side vertical bar chart
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        ) {
                            Canvas(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                val width = size.width
                                val height = size.height

                                // Determine max scale height
                                val maxAmount = state.breakdownList.flatMap { listOf(it.income, it.expense) }
                                    .maxOrNull()?.coerceAtLeast(1.0) ?: 1.0

                                val gridCount = 4
                                val labelHeightPadding = 40f
                                val plotHeight = height - labelHeightPadding

                                // 1. Draw Grid Lines
                                for (i in 0..gridCount) {
                                    val y = plotHeight * (1f - (i.toFloat() / gridCount))
                                    drawLine(
                                        color = OutlineVariant.copy(alpha = 0.5f),
                                        start = Offset(0f, y),
                                        end = Offset(width, y),
                                        strokeWidth = 1.dp.toPx()
                                    )
                                }

                                // 2. Draw Bars
                                val itemsCount = state.breakdownList.size
                                val sectionWidth = width / itemsCount
                                val barSpacing = 8f
                                val barWidth = (sectionWidth * 0.3f).coerceIn(10f, 40f)

                                state.breakdownList.forEachIndexed { idx, item ->
                                    val sectionCenterX = (idx * sectionWidth) + (sectionWidth / 2)

                                    // Income Bar
                                    val incomeHeight = (item.income / maxAmount) * plotHeight
                                    val incomeLeft = sectionCenterX - barWidth - (barSpacing / 2)
                                    val incomeTop = plotHeight - incomeHeight
                                    drawRect(
                                        color = PrimaryTeal,
                                        topLeft = Offset(incomeLeft, incomeTop.toFloat()),
                                        size = Size(barWidth, incomeHeight.toFloat())
                                    )

                                    // Expense Bar
                                    val expenseHeight = (item.expense / maxAmount) * plotHeight
                                    val expenseLeft = sectionCenterX + (barSpacing / 2)
                                    val expenseTop = plotHeight - expenseHeight
                                    drawRect(
                                        color = TertiaryCoral,
                                        topLeft = Offset(expenseLeft, expenseTop.toFloat()),
                                        size = Size(barWidth, expenseHeight.toFloat())
                                    )

                                    // Draw label text under bar pair (e.g. short month name or day number)
                                    val label = if (item.label.length >= 10) {
                                        // YYYY-MM-DD -> DD
                                        item.label.substring(8, 10)
                                    } else {
                                        item.label
                                    }
                                    
                                    val paint = android.graphics.Paint().apply {
                                        color = android.graphics.Color.parseColor("#64748B")
                                        textSize = 10.dp.toPx()
                                        textAlign = android.graphics.Paint.Align.CENTER
                                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                                    }
                                    drawContext.canvas.nativeCanvas.drawText(
                                        label,
                                        sectionCenterX,
                                        height - 8f,
                                        paint
                                    )
                                }
                            }
                        }

                        // Insights Card below
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceContainerLow)
                                .padding(12.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    tint = PrimaryTeal,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = if (state.savings > 0) {
                                        "Your savings gap is positive this period. Keep up the disciplined spending on miscellaneous items!"
                                    } else {
                                        "Your expenses exceeded income this period. Focus on reducing shopping and other category spending."
                                    },
                                    fontSize = 12.sp,
                                    color = OnSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Detailed Insights Bento Card Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Detailed Insights",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = PrimaryTeal
                    )

                    // 2x2 Bento Grid equivalent
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Column 1: Largest Single Expense
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Largest Single Expense",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OnSurfaceMuted,
                                    letterSpacing = 0.5.sp
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(ExpenseCoral)
                                            .padding(6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = null,
                                            tint = TertiaryCoral,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Text(
                                        text = String.format("$%,.2f", state.largestSingleExpense),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TertiaryCoral
                                    )
                                }
                                Text(
                                    text = state.largestSingleExpenseDesc,
                                    fontSize = 11.sp,
                                    color = OnSurfaceMuted
                                )
                            }

                            // Column 2: Average Daily Spend
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Average Daily Spend",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OnSurfaceMuted,
                                    letterSpacing = 0.5.sp
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(PrimaryTeal.copy(alpha = 0.1f))
                                            .padding(6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Payments,
                                            contentDescription = null,
                                            tint = PrimaryTeal,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Text(
                                        text = String.format("$%,.2f", state.averageDailySpend),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryTeal
                                    )
                                }
                                Text(
                                    text = "Across all categories",
                                    fontSize = 11.sp,
                                    color = OnSurfaceMuted
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Column 3: Income Growth
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Income Growth",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OnSurfaceMuted,
                                    letterSpacing = 0.5.sp
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(GrowthGreen)
                                            .padding(6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowUpward,
                                            contentDescription = null,
                                            tint = SecondaryGreen,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    val growthText = if (state.incomeGrowthPercentage >= 0) {
                                        String.format("+%.1f%%", state.incomeGrowthPercentage)
                                    } else {
                                        String.format("%.1f%%", state.incomeGrowthPercentage)
                                    }
                                    Text(
                                        text = growthText,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SecondaryGreen
                                    )
                                }
                                Text(
                                    text = "Previous period comparison",
                                    fontSize = 11.sp,
                                    color = OnSurfaceMuted
                                )
                            }

                            // Column 4: Budget Health
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Budget Health",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OnSurfaceMuted,
                                    letterSpacing = 0.5.sp
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    val isOptimal = state.budgetHealthStatus.equals("Optimal", ignoreCase = true)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (isOptimal) SecondaryContainerGreen else ExpenseCoral)
                                            .padding(6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Verified,
                                            contentDescription = null,
                                            tint = if (isOptimal) OnSecondaryGreenFixed else TertiaryCoral,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Text(
                                        text = state.budgetHealthStatus,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isOptimal) OnSecondaryGreenFixed else TertiaryCoral
                                    )
                                }
                                val budgetDesc = state.budgetHealthDesc
                                Text(
                                    text = budgetDesc,
                                    fontSize = 11.sp,
                                    color = OnSurfaceMuted
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
