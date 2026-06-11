package com.example.findbuddy.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.findbuddy.data.local.TokenManager
import com.example.findbuddy.ui.auth.AuthEffect
import com.example.findbuddy.ui.auth.AuthIntent
import com.example.findbuddy.ui.auth.LoginScreen
import com.example.findbuddy.ui.auth.SignUpScreen
import com.example.findbuddy.ui.auth.AuthViewModel
import com.example.findbuddy.ui.dashboard.DashboardScreen
import com.example.findbuddy.ui.accounts.AccountsScreen
import com.example.findbuddy.ui.accounts.AccountViewModel
import kotlinx.coroutines.flow.collectLatest

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object Dashboard : Screen("dashboard")
    object Accounts : Screen("accounts")
    object AddTransaction : Screen("add_transaction")
    object EditTransaction : Screen("edit_transaction/{id}") {
        fun createRoute(id: String) = "edit_transaction/$id"
    }
}

@Composable
fun NavGraph(
    navController: NavHostController,
    tokenManager: TokenManager
) {
    val startDestination = if (tokenManager.hasToken()) {
        Screen.Dashboard.route
    } else {
        Screen.Login.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            val viewModel: AuthViewModel = hiltViewModel()
            val state by viewModel.state.collectAsState()

            LaunchedEffect(key1 = true) {
                viewModel.effect.collectLatest { effect ->
                    when (effect) {
                        is AuthEffect.NavigateToDashboard -> {
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                        is AuthEffect.ShowToast -> {
                            // Handle toast
                        }
                    }
                }
            }

            LoginScreen(
                state = state,
                onIntent = { viewModel.handleIntent(it) },
                onNavigateToSignUp = {
                    viewModel.handleIntent(AuthIntent.ClearError)
                    navController.navigate(Screen.SignUp.route)
                }
            )
        }

        composable(Screen.SignUp.route) {
            val viewModel: AuthViewModel = hiltViewModel()
            val state by viewModel.state.collectAsState()

            LaunchedEffect(key1 = true) {
                viewModel.effect.collectLatest { effect ->
                    when (effect) {
                        is AuthEffect.NavigateToDashboard -> {
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo(Screen.SignUp.route) { inclusive = true }
                            }
                        }
                        is AuthEffect.ShowToast -> {
                            // Handle toast
                        }
                    }
                }
            }

            SignUpScreen(
                state = state,
                onIntent = { viewModel.handleIntent(it) },
                onNavigateToLogin = {
                    viewModel.handleIntent(AuthIntent.ClearError)
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.SignUp.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToAccounts = {
                    navController.navigate(Screen.Accounts.route) {
                        popUpTo(Screen.Dashboard.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToAddTransaction = {
                    navController.navigate(Screen.AddTransaction.route)
                },
                onNavigateToEditTransaction = { id ->
                    navController.navigate(Screen.EditTransaction.createRoute(id))
                },
                onLogout = {
                    tokenManager.deleteToken()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Accounts.route) {
            val viewModel: AccountViewModel = hiltViewModel()
            val state by viewModel.state.collectAsState()

            AccountsScreen(
                state = state,
                onIntent = { viewModel.handleIntent(it) },
                onNavigateToDashboard = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                },
                onLogout = {
                    tokenManager.deleteToken()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.AddTransaction.route) {
            val viewModel: com.example.findbuddy.ui.transactions.TransactionViewModel = hiltViewModel()
            val state by viewModel.state.collectAsState()

            com.example.findbuddy.ui.transactions.AddTransactionScreen(
                state = state,
                onIntent = { viewModel.handleIntent(it) },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.EditTransaction.route,
            arguments = listOf(
                androidx.navigation.navArgument("id") {
                    type = androidx.navigation.NavType.StringType
                }
            )
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            val viewModel: com.example.findbuddy.ui.transactions.TransactionViewModel = hiltViewModel()
            val state by viewModel.state.collectAsState()

            LaunchedEffect(id) {
                if (id.isNotEmpty()) {
                    viewModel.handleIntent(com.example.findbuddy.ui.transactions.TransactionIntent.LoadDetails(id))
                }
            }

            com.example.findbuddy.ui.transactions.AddTransactionScreen(
                state = state,
                onIntent = { viewModel.handleIntent(it) },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
