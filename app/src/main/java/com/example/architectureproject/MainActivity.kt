package com.example.architectureproject

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.Navigator
import com.example.architectureproject.ui.theme.ArchitectureProjectTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ArchitectureProjectTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val sharedPref = this.getPreferences(Context.MODE_PRIVATE)
                    val hasLoggedIn = sharedPref.getString("id", null)
                    Log.d("hasLoggedIn", hasLoggedIn.toString());
                    if (hasLoggedIn != null) {
                        Navigator(MainScreen())
                    }else{
                        Navigator(AuthScreen(this))
                    }
                }
            }
        }
    }

//    override fun onPause() {
//        super.onPause()
//        this.getPreferences(Context.MODE_PRIVATE).edit().remove("id").commit();
//    }
}