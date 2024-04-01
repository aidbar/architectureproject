package com.example.architectureproject

import android.content.ContentValues.TAG
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import com.google.firebase.auth.FirebaseAuth

class ResetPasswordScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val context = LocalContext.current

        var email by remember { mutableStateOf("") }

        val auth = FirebaseAuth.getInstance()

        fun sendResetEmail(email: String) {
            if (email.isEmpty()) {
                Toast.makeText(
                    context,
                    "Enter the email associated with your account",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                auth.sendPasswordResetEmail(email).addOnCompleteListener {
                    if(it.isSuccessful) {
                        Log.d(TAG, "Password reset email sent successfully")
                        Toast.makeText(
                            context,
                            "A password reset link has been sent to your email address. Follow that link to reset your password.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Log.e(TAG, "Failed to send password reset email", it.exception)
                        val error = it.exception?.message
                        if (error != null && error.contains("no user record")) {
                            Toast.makeText(
                                context,
                                "Could not find a user account associated with the provided email. Please try again.",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                context,
                                "Failed to send password reset email. Please try again later.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(100.dp))
            Text(text = "Update your password with just a few clicks!", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(start = 10.dp))
            Spacer(modifier = Modifier.height(26.dp))
            TextField(
                value = email,
                onValueChange = { email = it.replace(" ", "") },
                label = { Text(text = "Email") }
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    sendResetEmail(email)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Reset password")
            }
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    navigator?.pop()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Back to SignIn")
            }
        }
    }
}