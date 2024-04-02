package com.example.architectureproject

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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import com.google.firebase.auth.FirebaseAuth

class ResetPasswordScreenModel : ScreenModel {
    var email by mutableStateOf("")
    var result by mutableStateOf("")
    private val auth = FirebaseAuth.getInstance()

    fun resetEmail() {
        auth.sendPasswordResetEmail(email).addOnCompleteListener {
            if(it.isSuccessful) {
                result = "A password reset link has been sent to your email address."
            } else {
                if (it.exception?.message != null) {
                    result = it.exception?.message!!
                } else {
                    result = "En error occurred. Please try again later."
                }
            }
        }
    }
}
class ResetPasswordScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val context = LocalContext.current

        //var email by remember { mutableStateOf("") }
        var model = rememberScreenModel{ResetPasswordScreenModel()}

        val auth = FirebaseAuth.getInstance()

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
                value = model.email,
                onValueChange = { model.email = it.replace(" ", "") },
                label = { Text(text = "Email") }
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    model.resetEmail()
                    Toast.makeText(context, model.result, Toast.LENGTH_SHORT).show()
                    model.result = ""
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