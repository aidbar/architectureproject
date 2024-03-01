package com.example.architectureproject

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator

class ProfileScreen:Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current

        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Username on the top left
                Text(
                    text = "Username",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterVertically),
                    fontSize = 32.sp
                )
                // Profile picture on the top right
                IconButton(onClick ={navigator?.push(ProfileEditScreen())}) {
                    Icon(
                        painter = rememberVectorPainter(Icons.Rounded.Person), // Use Icons.Rounded.Person
                        contentDescription = "Person Icon",
                        modifier = Modifier.size(64.dp),
                        tint = Color.Black
                    )
                }


            }

            // Bio beneath the row
            Text(
                text = "bio",
                modifier = Modifier.padding(top = 16.dp),
                fontSize = 14.sp // Smaller font size for the bio
            )
        }
    }
}