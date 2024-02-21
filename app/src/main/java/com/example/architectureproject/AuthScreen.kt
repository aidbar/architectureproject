package com.example.architectureproject

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth

data class AuthScreen(val activity: Activity) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current

        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var auth = FirebaseAuth.getInstance();

        val sharedPref = activity.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp), // Add some padding around the column
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Start Your Journey with GreenTrace")

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

            Spacer(modifier = Modifier.height(16.dp)) // Add space between the text field and buttons

            // Sign In button
            Button(
                onClick = {
                    if (email.isEmpty() || password.isEmpty()){
                        Toast.makeText(
                            activity,
                            "Fields must not be empty",
                            Toast.LENGTH_SHORT
                        ).show()
                    }else{
                        auth.signInWithEmailAndPassword(email,password).addOnCompleteListener(activity) {
                            if (it.isSuccessful) {
                                it.result.user?.displayName?.let { it1 ->
                                    Log.d("display name",
                                        it1
                                    )
                                }
//                                Log.d("user",auth.currentUser.toString())
                                sharedPref.edit().putString("id", it.result.user?.displayName)
                                    .apply()
                                navigator?.push(MainScreen())
                            } else {
                                Toast.makeText(
                                    activity,
                                    "Failed: " + it.result.toString(),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
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
                            activity,
                            "Fields must not be empty",
                            Toast.LENGTH_SHORT
                        ).show()
                    }else{
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(activity,
                            OnCompleteListener {
                                if (it.isSuccessful) {
                                    sharedPref.edit().putString("id", it.result.user?.displayName).apply()
                                    navigator?.push(NewAccountSetupScreen())
                                } else {
//                                    Log.d("Regis failed", it.result.toString())
                                    Toast.makeText(
                                        activity,
                                        "Failed: "+it.result.toString(),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            })
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Register")
            }
        }

    }

}