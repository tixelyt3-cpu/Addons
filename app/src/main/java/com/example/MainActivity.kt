package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.data.AppDatabase
import com.example.data.AddonRepository
import com.example.ui.AddonViewModel
import com.example.ui.AddonViewModelFactory
import com.example.ui.screens.MainAppContent
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()
        
        // Initialize Room Database, DAO and Repository
        val database = AppDatabase.getDatabase(this)
        val repository = AddonRepository(database.addonDao())
        val factory = AddonViewModelFactory(repository)
        val viewModel: AddonViewModel by viewModels { factory }

        setContent {
            MyApplicationTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}
