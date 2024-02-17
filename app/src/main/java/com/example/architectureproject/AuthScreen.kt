package com.example.architectureproject

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator

data class AuthScreen(val activity: Activity) : Screen {
//    val activity = activity
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        Column() {
            Text(text = "Welcome to GreenTrace")
            Button(onClick = {
                val sharedPref = activity.getPreferences(Context.MODE_PRIVATE)
                sharedPref.edit().putString("id", "Alice").apply();
                navigator?.push(MainScreen())
            }) {
                Text("Sign in")
            }
        }

    }

}