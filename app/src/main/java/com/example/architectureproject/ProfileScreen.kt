package com.example.architectureproject

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.rounded.MailOutline
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator

class ProfileScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current

        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Username",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterVertically),
                    fontSize = 32.sp
                )
                IconButton(onClick = { navigator?.push(ProfileEditScreen()) }) {
                    Icon(
                        painter = rememberVectorPainter(Icons.Rounded.Person),
                        contentDescription = "Person Icon",
                        modifier = Modifier.size(64.dp),
                        tint = Color.Black
                    )
                }


            }

            Text(
                text = "APP",
                modifier = Modifier.padding(top = 16.dp),
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(onClick = { /*TODO*/ }, modifier = Modifier.fillMaxWidth()) {
                Icon(
                    painter = rememberVectorPainter(Icons.Filled.Star),
                    contentDescription = "Color Theme",
                    modifier = Modifier.size(32.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Color Theme")
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(onClick = {navigator?.push(ContactUsScreen())}, modifier = Modifier.fillMaxWidth()) {
                Icon(
                    painter = rememberVectorPainter(Icons.Rounded.MailOutline),
                    contentDescription = "Contact Us",
                    modifier = Modifier.size(32.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Contact Us")
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(onClick = { /*TODO*/ }, modifier = Modifier.fillMaxWidth()) {
                Icon(
                    painter = rememberVectorPainter(Icons.Filled.Info),
                    contentDescription = "Privacy Policy",
                    modifier = Modifier.size(32.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Privacy Policy")
            }
        }
    }
}

class ContactUsScreen:Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val email = remember { mutableStateOf(TextFieldValue()) }
        val message = remember { mutableStateOf(TextFieldValue()) }

        Column(modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {navigator?.pop()}) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Return")
                }
            }

            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                Text(text = "Contact Us", fontWeight = FontWeight.SemiBold, fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(text = "Email", fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = email.value,
                onValueChange = { email.value = it },
                shape = RoundedCornerShape(32.dp),
                placeholder = { Text(text = "Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Message", fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value =message.value,
                onValueChange = { message.value = it },
                shape = RoundedCornerShape(32.dp),
                placeholder = { Text(text = "Message") },
                modifier = Modifier.fillMaxWidth().height(160.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {navigator?.pop()},
                modifier = Modifier
                    .align(Alignment.End)
                    .fillMaxWidth()
            ) {
                Text("Submit")
            }
        }
    }
}
