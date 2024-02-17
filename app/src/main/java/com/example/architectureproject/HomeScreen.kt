package com.example.architectureproject

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import com.google.firebase.auth.FirebaseAuth

class HomeScreen :Screen{
    var auth = FirebaseAuth.getInstance()
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        Column() {
            Text(text = "Welcome, "+auth.currentUser?.email)
        }

    }
}