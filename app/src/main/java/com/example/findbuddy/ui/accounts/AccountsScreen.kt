package com.example.findbuddy.ui.accounts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.findbuddy.domain.model.Account
import com.example.findbuddy.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(
    state: AccountState,
    onIntent: (AccountIntent) -> Unit,
    onNavigateToDashboard: () -> Unit,
    onNavigateToBudgets: () -> Unit,
    onNavigateToReports: () -> Unit,
    onLogout: () -> Unit
) {
    var filterType by remember { mutableStateOf<String?>(null) } // null = All, BANK_ACCOUNT, CREDIT_CARD, CASH_WALLET
    var showFilterMenu by remember { mutableStateOf(false) }

    // Dialog state holders
    var addName by remember { mutableStateOf("") }
    var addType by remember { mutableStateOf("BANK_ACCOUNT") }
    var addBalance by remember { mutableStateOf("") }

    var editName by remember { mutableStateOf("") }
    var editType by remember { mutableStateOf("BANK_ACCOUNT") }
    var editBalance by remember { mutableStateOf("") }

    // Reset fields on dialog trigger
    LaunchedEffect(state.showAddDialog) {
        if (state.showAddDialog) {
            addName = ""
            addType = "BANK_ACCOUNT"
            addBalance = "0.0"
        }
    }

    LaunchedEffect(state.showEditDialog, state.selectedAccount) {
        if (state.showEditDialog && state.selectedAccount != null) {
            editName = state.selectedAccount.accountName
            editType = state.selectedAccount.accountType
            editBalance = state.selectedAccount.openingBalance.toString()
        }
    }

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
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryTeal
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Logout",
                            tint = OnSurfaceVariant
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
                    label = { Text("Dashboard", fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = true,
                    onClick = { /* Stay here */ },
                    icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Accounts") },
                    label = { Text("Accounts", fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToBudgets,
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onIntent(AccountIntent.OpenAddDialog) },
                containerColor = PrimaryTeal,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Account")
            }
        }
    ) { paddingValues ->
        // Calculate portfolio values
        val activeAccounts = state.accountsList
        val assets = activeAccounts.filter { it.accountType != "CREDIT_CARD" && it.openingBalance > 0 }.sumOf { it.openingBalance }
        val liabilities = activeAccounts.filter { it.accountType == "CREDIT_CARD" }.sumOf { it.openingBalance } // typically negative or positive liability
        // Standardize Credit Card liability representation as negative
        val liabilitiesTotal = if (liabilities > 0) -liabilities else liabilities
        val netWorth = assets + liabilitiesTotal

        val liquidity = activeAccounts.filter { it.accountType == "BANK_ACCOUNT" || it.accountType == "CASH_WALLET" }.sumOf { it.openingBalance }

        val filteredAccounts = if (filterType != null) {
            activeAccounts.filter { it.accountType == filterType }
        } else {
            activeAccounts
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundGray)
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
            ) {
                // 1. Portfolio Overview Header & Net Worth
                item {
                    Text(
                        text = "Portfolio Overview",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceDark
                    )
                }

                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "TOTAL NET WORTH",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnSurfaceMuted,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Text(
                            text = "$${String.format("%,.2f", netWorth)}",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryTeal
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = SecondaryGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "+2.4% this month",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SecondaryGreen
                            )
                        }

                        // Horizontal metrics scroll
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 24.dp)
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            MetricBreakdownCard(label = "Liquidity", value = "$${String.format("%,.0f", liquidity)}", color = PrimaryTeal)
                            MetricBreakdownCard(label = "Investments", value = "$0", color = PrimaryTeal)
                            MetricBreakdownCard(label = "Liabilities", value = "-$${String.format("%,.0f", Math.abs(liabilitiesTotal))}", color = Color(0xFFBA1A1A))
                        }
                    }
                }

                // 2. Add Account Quick Card
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(PrimaryContainerTeal, RoundedCornerShape(12.dp))
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(24.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddCard,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "New Account",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Securely link a new financial source to your vault.",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center
                            )
                        }
                        Button(
                            onClick = { onIntent(AccountIntent.OpenAddDialog) },
                            colors = ButtonDefaults.buttonColors(containerColor = SecondaryGreen),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text(
                                text = "Add Account",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // 3. Accounts List Section Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Financial Accounts",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = OnSurfaceDark
                            )
                            Text(
                                text = "Secure offline-first management",
                                fontSize = 13.sp,
                                color = OnSurfaceMuted
                            )
                        }

                        Box {
                            IconButton(onClick = { showFilterMenu = true }) {
                                Icon(Icons.Default.Tune, contentDescription = "Filter")
                            }
                            DropdownMenu(
                                expanded = showFilterMenu,
                                onDismissRequest = { showFilterMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("All Accounts") },
                                    onClick = { filterType = null; showFilterMenu = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("Bank Accounts") },
                                    onClick = { filterType = "BANK_ACCOUNT"; showFilterMenu = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("Credit Cards") },
                                    onClick = { filterType = "CREDIT_CARD"; showFilterMenu = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("Cash Wallets") },
                                    onClick = { filterType = "CASH_WALLET"; showFilterMenu = false }
                                )
                            }
                        }
                    }
                }

                // 4. Accounts List
                if (filteredAccounts.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Text(
                                text = "No accounts registered yet. Tap 'Add Account' to start tracking your vault.",
                                modifier = Modifier.padding(24.dp),
                                color = OnSurfaceMuted,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(filteredAccounts) { account ->
                        AccountItemRow(account = account) {
                            onIntent(AccountIntent.OpenEditDialog(account))
                        }
                    }
                }

                // 5. Account Distribution Section
                item {
                    Text(
                        text = "Account Distribution",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceDark,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "ASSET ALLOCATION",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnSurfaceMuted
                        )

                        val checkingSavingsTotal = activeAccounts
                            .filter { it.accountType == "BANK_ACCOUNT" }
                            .sumOf { it.openingBalance }
                        val cashTotal = activeAccounts
                            .filter { it.accountType == "CASH_WALLET" }
                            .sumOf { it.openingBalance }
                        val absoluteTotal = Math.max(1.0, checkingSavingsTotal + cashTotal)

                        val cashLiquidPercentage = ((checkingSavingsTotal + cashTotal) / absoluteTotal * 100).toInt()
                        
                        // We strictly calculate allocations dynamically (Cash & Liquid 100%, others 0%)
                        AssetAllocationProgress(label = "Cash & Liquid", percentage = cashLiquidPercentage, color = SecondaryGreen)
                        AssetAllocationProgress(label = "Brokerage", percentage = 0, color = PrimaryTeal)
                        AssetAllocationProgress(label = "Other", percentage = 0, color = OnSurfaceMuted)
                    }
                }

                // 6. Optimization Tip
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFECEFF1), RoundedCornerShape(12.dp))
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "OPTIMIZATION TIP",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnSurfaceMuted
                        )
                        Text(
                            text = "You have over $12,000 in your Checking account. Consider moving $5,000 to your Marcus Savings to earn 4.50% APY.",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = PrimaryTeal
                        )
                    }
                }
            }

            // Spinner Loading Overlay
            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryTeal)
                }
            }
        }
    }

    // --- DIALOGS ---

    // Add Account Dialog
    if (state.showAddDialog) {
        AlertDialog(
            onDismissRequest = { onIntent(AccountIntent.DismissDialog) },
            confirmButton = {
                Button(
                    onClick = {
                        val balance = addBalance.toDoubleOrNull() ?: 0.0
                        onIntent(AccountIntent.CreateAccount(addName, addType, balance))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { onIntent(AccountIntent.DismissDialog) }) {
                    Text("Cancel", color = PrimaryTeal)
                }
            },
            title = { Text("Add Financial Account") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (state.errorMsg != null) {
                        Text(
                            text = state.errorMsg,
                            color = Color(0xFFBA1A1A),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    OutlinedTextField(
                        value = addName,
                        onValueChange = { addName = it },
                        label = { Text("Account Name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    var typeExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = typeExpanded,
                        onExpandedChange = { typeExpanded = !typeExpanded }
                    ) {
                        OutlinedTextField(
                            value = when (addType) {
                                "BANK_ACCOUNT" -> "Bank Account"
                                "CREDIT_CARD" -> "Credit Card"
                                else -> "Cash Wallet"
                            },
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Account Type") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = typeExpanded,
                            onDismissRequest = { typeExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Bank Account") },
                                onClick = { addType = "BANK_ACCOUNT"; typeExpanded = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Credit Card") },
                                onClick = { addType = "CREDIT_CARD"; typeExpanded = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Cash Wallet") },
                                onClick = { addType = "CASH_WALLET"; typeExpanded = false }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = addBalance,
                        onValueChange = { addBalance = it },
                        label = { Text("Opening Balance") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        )
    }

    // Edit / Delete Account Dialog
    if (state.showEditDialog && state.selectedAccount != null) {
        AlertDialog(
            onDismissRequest = { onIntent(AccountIntent.DismissDialog) },
            confirmButton = {
                Button(
                    onClick = {
                        val balance = editBalance.toDoubleOrNull() ?: 0.0
                        onIntent(AccountIntent.UpdateAccount(state.selectedAccount.id, editName, editType, balance))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = { onIntent(AccountIntent.DeleteAccount(state.selectedAccount.id)) },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFBA1A1A))
                    ) {
                        Text("Delete Account")
                    }
                    TextButton(onClick = { onIntent(AccountIntent.DismissDialog) }) {
                        Text("Cancel", color = PrimaryTeal)
                    }
                }
            },
            title = { Text("Edit Financial Account") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (state.errorMsg != null) {
                        Text(
                            text = state.errorMsg,
                            color = Color(0xFFBA1A1A),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Account Name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    var typeExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = typeExpanded,
                        onExpandedChange = { typeExpanded = !typeExpanded }
                    ) {
                        OutlinedTextField(
                            value = when (editType) {
                                "BANK_ACCOUNT" -> "Bank Account"
                                "CREDIT_CARD" -> "Credit Card"
                                else -> "Cash Wallet"
                            },
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Account Type") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = typeExpanded,
                            onDismissRequest = { typeExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Bank Account") },
                                onClick = { editType = "BANK_ACCOUNT"; typeExpanded = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Credit Card") },
                                onClick = { editType = "CREDIT_CARD"; typeExpanded = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Cash Wallet") },
                                onClick = { editType = "CASH_WALLET"; typeExpanded = false }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = editBalance,
                        onValueChange = { editBalance = it },
                        label = { Text("Opening Balance") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        )
    }
}

@Composable
fun MetricBreakdownCard(label: String, value: String, color: Color) {
    Column(
        modifier = Modifier
            .width(110.dp)
            .background(SurfaceContainerLow, RoundedCornerShape(8.dp))
            .padding(vertical = 12.dp, horizontal = 16.dp)
    ) {
        Text(text = label, fontSize = 11.sp, color = OnSurfaceMuted)
        Text(text = value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
fun AccountItemRow(account: Account, onClick: () -> Unit) {
    val barColor = when (account.accountType) {
        "BANK_ACCOUNT" -> SecondaryGreen
        "CREDIT_CARD" -> Color(0xFFBA1A1A)
        else -> OnSurfaceMuted
    }

    val icon = when (account.accountType) {
        "BANK_ACCOUNT" -> Icons.Default.AccountBalance
        "CREDIT_CARD" -> Icons.Default.CreditCard
        else -> Icons.Default.Wallet
    }

    val iconBg = when (account.accountType) {
        "BANK_ACCOUNT" -> GrowthGreen
        "CREDIT_CARD" -> Color(0xFFFFEBEE)
        else -> SurfaceContainerLow
    }

    val balanceColor = if (account.accountType == "CREDIT_CARD") {
        Color(0xFFBA1A1A)
    } else {
        PrimaryTeal
    }

    val balanceText = if (account.accountType == "CREDIT_CARD") {
        val balance = if (account.openingBalance > 0) -account.openingBalance else account.openingBalance
        "-$${String.format("%,.2f", Math.abs(balance))}"
    } else {
        "$${String.format("%,.2f", account.openingBalance)}"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Vertical indicator bar
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(40.dp)
                .background(barColor, RoundedCornerShape(2.dp))
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Icon Container
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(iconBg, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = barColor,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Name and details
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = account.accountName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryTeal
            )
            val subText = when (account.accountType) {
                "BANK_ACCOUNT" -> "Bank Account • Checking"
                "CREDIT_CARD" -> "Credit Card • Debt"
                else -> "Cash Wallet • petty cash"
            }
            Text(
                text = subText,
                fontSize = 12.sp,
                color = OnSurfaceMuted
            )
        }

        // Balance and Synced status
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = balanceText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = balanceColor
            )
            Text(
                text = "Manual sync",
                fontSize = 11.sp,
                color = OnSurfaceMuted
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Details",
            tint = OnSurfaceMuted
        )
    }
}

@Composable
fun AssetAllocationProgress(label: String, percentage: Int, color: Color) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = PrimaryTeal)
            Text(text = "$percentage%", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = PrimaryTeal)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { percentage / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = color,
            trackColor = SurfaceContainerLow,
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
        )
    }
}
