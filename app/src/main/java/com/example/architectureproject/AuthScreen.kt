package com.example.architectureproject

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import kotlinx.coroutines.launch

class AuthScreen : Screen {
    @Composable
    override fun Content() {
        val scope = rememberCoroutineScope()
        val provider = GreenTraceProviders.userProvider
        val navigator = LocalNavigator.current

        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        val context = GreenTraceProviders.applicationContext

        //val sharedPref = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp), // Add some padding around the column
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Start Your Journey with GreenTrace", style = MaterialTheme.typography.headlineLarge, modifier = Modifier.padding(start = 10.dp))

            Spacer(modifier = Modifier.height(32.dp)) // Add space between the buttons
            // Email input field
            TextField(
                value = email,
                onValueChange = { email = it.replace(" ", "") },
                label = { Text(text = "Email") }
            )

            Spacer(modifier = Modifier.height(8.dp)) // Add space between the text fields

            // Password input field
            TextField(
                value = password,
                onValueChange = { password = it.replace(" ", "") },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            Spacer(modifier = Modifier.height(24.dp)) // Add space between the text field and buttons

            // Sign In button
            Button(
                onClick = {
                    if (email.isEmpty() || password.isEmpty()){
                        Toast.makeText(
                            context,
                            "Fields must not be empty",
                            Toast.LENGTH_SHORT
                        ).show()
                    }else{
                        scope.launch {
                            val err = provider.loginUser(email, password)
                            if (err != null) {
                                Toast.makeText(
                                    context,
                                    "Error: $err",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@launch
                            }
                            
                            navigator?.push(MainScreen(false))
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Sign in")
            }

            Spacer(modifier = Modifier.height(16.dp)) // Add space between the buttons

            // Register button
            Button(
                onClick = {
                    if (email.isEmpty() || password.isEmpty()){
                        Toast.makeText(
                            context,
                            "Fields must not be empty",
                            Toast.LENGTH_SHORT
                        ).show()
                    }else{
                        scope.launch {
                            val error = provider.createAndLoginUser(email, password)
                            if (error != null) {
                                Toast.makeText(
                                    context,
                                    "Error: $error",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@launch
                            }

                            navigator?.push(NewAccountSetupScreen())
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Register")
            }

            Spacer(modifier = Modifier.height(8.dp)) // Add space between the Register button and the Forgot Password text

            // Forgot Password button
            Text(
                text = "Forgot Password?",
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable {
                    navigator?.push(ResetPasswordScreen())
                }
            )
        }
    }

}