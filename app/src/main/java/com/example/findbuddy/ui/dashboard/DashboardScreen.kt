package com.example.findbuddy.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.findbuddy.ui.theme.BackgroundGray
import com.example.findbuddy.ui.theme.PrimaryTeal
import com.example.findbuddy.ui.theme.SurfaceContainerLow

@Composable
fun DashboardScreen(
    onNavigateToAccounts: () -> Unit,
    onNavigateToBudgets: () -> Unit,
    onNavigateToAddTransaction: () -> Unit,
    onNavigateToEditTransaction: (String) -> Unit,
    onLogout: () -> Unit
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddTransaction,
                containerColor = PrimaryTeal,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Transaction",
                    modifier = Modifier.size(32.dp)
                )
            }
        },
        bottomBar = {

            NavigationBar(
                containerColor = SurfaceContainerLow,
                modifier = Modifier.height(80.dp)
            ) {
                NavigationBarItem(
                    selected = true,
                    onClick = { /* Stay here */ },
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
                    selected = false,
                    onClick = onNavigateToBudgets,
                    icon = { Icon(Icons.Default.Savings, contentDescription = "Budgets") },
                    label = { Text("Budgets", fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { /* Placeholder */ },
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
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = PrimaryTeal,
                    modifier = Modifier.size(64.dp)
                )

                Text(
                    text = "FinBuddy Vault",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryTeal,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Welcome to your offline-first financial repository. Your data is encrypted locally.",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onLogout,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Text(
                            text = "Secure Logout",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
