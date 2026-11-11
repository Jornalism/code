package com.example.campusq

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.campusq.ui.navigation.CampusQApp
import com.example.campusq.ui.theme.CampusQTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CampusQTheme {
                CampusQApp()
            }
        }
    }
}
