package com.equity.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.equity.app.core.theme.EquityTheme
import com.equity.app.presentation.navigation.MainNavigationGraph

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            EquityTheme {
                MainNavigationGraph()
            }
        }
    }
}
