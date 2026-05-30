package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.ui.screens.MainAppContainer
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.VoterViewModel

class MainActivity : ComponentActivity() {
    
    // Instantiate our shared ViewModel with ViewModel lazy delegation
    private val viewModel: VoterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Setup modern full bleed Edge-to-Edge window insets
        enableEdgeToEdge()
        
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainAppContainer(viewModel = viewModel)
                }
            }
        }
    }
}
