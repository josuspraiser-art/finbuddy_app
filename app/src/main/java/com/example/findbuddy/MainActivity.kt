package com.example.findbuddy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.example.findbuddy.data.local.TokenManager
import com.example.findbuddy.ui.navigation.NavGraph
import com.example.findbuddy.ui.theme.FindBuddyTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FindBuddyTheme {
                val navController = rememberNavController()
                NavGraph(
                    navController = navController,
                    tokenManager = tokenManager
                )
            }
        }
    }
}