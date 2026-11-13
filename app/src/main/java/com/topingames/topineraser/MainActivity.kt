package com.topingames.topineraser

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.topingames.topineraser.ui.navigation.TopinEraserNavGraph
import com.topingames.topineraser.ui.theme.TopinEraserTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TopinEraserTheme {
                TopinEraserNavGraph()
            }
        }
    }
}
