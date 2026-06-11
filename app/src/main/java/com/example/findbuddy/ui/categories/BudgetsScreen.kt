package com.example.findbuddy.ui.categories

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.findbuddy.domain.model.Category
import com.example.findbuddy.domain.model.Budget
import com.example.findbuddy.ui.theme.*
import com.example.findbuddy.ui.transactions.dashedBorder
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetsScreen(
    state: CategoryState,
    onIntent: (CategoryIntent) -> Unit,
    onNavigateToAccounts: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    onNavigateToReports: () -> Unit,
    onLogout: () -> Unit
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = PrimaryTeal,
                            modifier = Modifier.size(28.dp)
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
                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(PrimaryTeal.copy(alpha = 0.1f))
                            .border(1.dp, OutlineVariant, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = PrimaryTeal
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
                containerColor = Color.White,
                modifier = Modifier.height(80.dp)
            ) {
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToDashboard,
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Dashboard", fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToAccounts,
                    icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Accounts") },
                    label = { Text("Accounts", fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = true,
                    onClick = { /* Stay here */ },
                    icon = { Icon(Icons.Default.Savings, contentDescription = "Budgets") },
                    label = { Text("Budgets", fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToReports,
                    icon = { Icon(Icons.Default.Analytics, contentDescription = "Reports") },
                    label = { Text("Reports", fontSize = 11.sp) }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundGray)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Context Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "MONTHLY BUDGET STATUS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnSurfaceVariant,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Overview",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryTeal
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Total Spent",
                            fontSize = 14.sp,
                            color = OnSurfaceVariant
                        )
                        Text(
                            text = "$${String.format("%,.2f", state.totalSpent)}",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryTeal
                        )
                    }
                }

                // Bento list of Category items
                state.categoriesList.forEach { category ->
                    val budget = state.budgetsList.find { it.categoryId == category.id }
                    CategoryItemCard(
                        category = category,
                        budget = budget,
                        onEditClick = { onIntent(CategoryIntent.OpenLimitDialog(category)) }
                    )
                }

                // Create New Category dashed card button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .dashedBorder(2.dp, OutlineVariant, 12.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onIntent(CategoryIntent.OpenCreateDialog) }
                        .padding(vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = null,
                            tint = OnSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "CREATE NEW BUDGET CATEGORY",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = OnSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Create dialog overlay
            if (state.showCreateDialog) {
                AlertDialog(
                    onDismissRequest = { onIntent(CategoryIntent.DismissCreateDialog) },
                    title = {
                        Text(
                            text = "Create New Category",
                            fontWeight = FontWeight.Bold,
                            color = PrimaryTeal,
                            fontSize = 18.sp
                        )
                    },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (state.errorMsg != null) {
                                Text(
                                    text = state.errorMsg,
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 14.sp
                                )
                            }

                            // Category Name
                            OutlinedTextField(
                                value = state.newCategoryName,
                                onValueChange = { onIntent(CategoryIntent.ChangeNewName(it)) },
                                label = { Text("Category Name") },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Segmented type selector
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFECEEF0), RoundedCornerShape(8.dp))
                                    .padding(4.dp)
                            ) {
                                listOf("EXPENSE", "INCOME").forEach { type ->
                                    val isSelected = state.newCategoryType == type
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isSelected) PrimaryTeal else Color.Transparent)
                                            .clickable { onIntent(CategoryIntent.ChangeNewType(type)) }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = type.lowercase(Locale.US).replaceFirstChar { it.uppercase() },
                                            color = if (isSelected) Color.White else OnSurfaceVariant,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { onIntent(CategoryIntent.CreateCategory) },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                        ) {
                            Text("Create", color = Color.White)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { onIntent(CategoryIntent.DismissCreateDialog) }) {
                            Text("Cancel", color = PrimaryTeal)
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    containerColor = Color.White
                )
            }

            // Edit budget limit dialog overlay
            if (state.showLimitDialog && state.selectedCategory != null) {
                AlertDialog(
                    onDismissRequest = { onIntent(CategoryIntent.DismissLimitDialog) },
                    title = {
                        Text(
                            text = "Set Limit: ${state.selectedCategory.categoryName}",
                            fontWeight = FontWeight.Bold,
                            color = PrimaryTeal,
                            fontSize = 18.sp
                        )
                    },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (state.errorMsg != null) {
                                Text(
                                    text = state.errorMsg,
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 14.sp
                                )
                            }

                            Text(
                                text = "Define a monthly budget limit for the current calendar month.",
                                fontSize = 13.sp,
                                color = OnSurfaceVariant
                            )

                            OutlinedTextField(
                                value = state.limitAmount,
                                onValueChange = { onIntent(CategoryIntent.ChangeLimitAmount(it)) },
                                label = { Text("Budget Limit ($)") },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { onIntent(CategoryIntent.SaveLimit) },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                        ) {
                            Text("Save", color = Color.White)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { onIntent(CategoryIntent.DismissLimitDialog) }) {
                            Text("Cancel", color = PrimaryTeal)
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    containerColor = Color.White
                )
            }
        }
    }
}

@Composable
fun CategoryItemCard(
    category: Category,
    budget: Budget?,
    onEditClick: () -> Unit
) {
    // Select icon based on name
    val icon = when (category.categoryName.lowercase(Locale.US)) {
        "grocery", "food & dining", "restaurant" -> Icons.Default.Restaurant
        "rent", "housing" -> Icons.Default.Home
        "petrol", "transportation" -> Icons.Default.DirectionsCar
        "entertainment", "movie" -> Icons.Default.Movie
        "salary", "bonus", "income" -> Icons.Default.Payments
        else -> Icons.Default.Category
    }

    val limit = budget?.budgetAmount ?: 0.0
    val spent = budget?.spentAmount ?: 0.0
    val hasLimit = limit > 0

    val utilizationPercent = if (hasLimit) budget?.usagePercentage?.toInt() ?: 0 else 0
    val isExceeded = hasLimit && spent > limit
    val isWarning = hasLimit && budget?.status == "WARNING"

    val progressColor = when {
        isExceeded -> Color(0xFFE53935)
        isWarning -> Color(0xFFFB8C00)
        else -> SecondaryGreen
    }

    val statusText = when {
        isExceeded -> "Exceeded"
        isWarning -> "Approaching Limit"
        hasLimit -> "Healthy"
        else -> "No limit set"
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, OutlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFFF2F4F6), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = PrimaryTeal
                        )
                    }
                    Column {
                        Text(
                            text = category.categoryName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = OnSurfaceDark
                        )
                        Text(
                            text = if (category.categoryType == "EXPENSE") "Expense Category" else "Income Category",
                            fontSize = 12.sp,
                            color = OnSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = onEditClick
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Budget",
                        tint = OnSurfaceVariant
                    )
                }
            }

            if (category.categoryType == "EXPENSE") {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "$${spent.toInt()}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isExceeded) Color(0xFFE53935) else PrimaryTeal
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (hasLimit) "of $${limit.toInt()} limit" else "",
                                fontSize = 12.sp,
                                color = OnSurfaceVariant
                            )
                        }
                        if (hasLimit) {
                            Text(
                                text = "$utilizationPercent% used",
                                fontSize = 12.sp,
                                color = OnSurfaceVariant
                            )
                        }
                    }

                    if (hasLimit) {
                        LinearProgressIndicator(
                            progress = { (spent / limit).toFloat().coerceAtMost(1f) },
                            color = progressColor,
                            trackColor = Color(0xFFECEEF0),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isExceeded) "$${(spent - limit).toInt()} over budget" else "",
                            fontSize = 12.sp,
                            color = Color(0xFFE53935),
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = statusText,
                            fontSize = 12.sp,
                            color = progressColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
