package com.example.architectureproject

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator

class HomeScreen :Screen{
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        Column() {
            Text(text = "home page placeholder")
        }

    }
}