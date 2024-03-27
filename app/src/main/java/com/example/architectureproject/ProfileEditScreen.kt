package com.example.architectureproject

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch

class ProfileEditScreenModel : ScreenModel {
    private val info = GreenTraceProviders.userProvider!!.userInfo()
    var alias by mutableStateOf(info.name)
    var bio by mutableStateOf(info.bio)
    var age by mutableStateOf(info.age.toString())
}

class ProfileEditScreen:Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val model = rememberScreenModel { ProfileEditScreenModel() }

        Column(modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {navigator?.pop()}) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Return")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("Edit Profile", style = MaterialTheme.typography.headlineMedium)
            }

            Spacer(modifier = Modifier.height(16.dp))

//            Button(onClick = { /* Implement profile picture editing functionality */ }) {
//                Text("Edit Profile Picture")
//                Spacer(modifier = Modifier.width(8.dp))
//                Icon(Icons.Default.Edit, contentDescription = "Edit Profile Picture")
//            }

            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                Image(
                    painter = rememberAsyncImagePainter("https://picsum.photos/id/870/200/300"),
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(120.dp)
                        .align(Alignment.Center)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                // Edit button below the profile picture
//                Button(
//                    onClick = {},
//                    shape = CircleShape,
//                    modifier = Modifier
//                        .align(Alignment.BottomCenter).size(64.dp)
//                        .padding(top = 8.dp)
//                ) {
////                    Text("Edit")
//                    Icon(Icons.Default.Edit, contentDescription = "Edit Profile Picture", modifier = Modifier.size(
//                        ButtonDefaults.IconSize))
//                }
            }
            Box(
                modifier = Modifier.fillMaxWidth(), // This will make the Box fill the entire screen
                contentAlignment = Alignment.Center // This will align the IconButton in the center of the Box
            ) {
                IconButton(onClick = { /* Upload image */ }, modifier = Modifier
                    .offset(40.dp, (-30).dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)) {
                    Icon(Icons.Filled.Edit, contentDescription = "Edit Profile Picture",
                        modifier = Modifier.size(
                            ButtonDefaults.IconSize), tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = model.alias,
                onValueChange = { model.alias = it },
                shape = RoundedCornerShape(32.dp),
                placeholder = { Text(text = "alias") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = model.bio,
                onValueChange = { model.bio = it },
                shape = RoundedCornerShape(32.dp),
                placeholder = { Text(text = "bio") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = model.age,
                onValueChange = { model.age = it },
                shape = RoundedCornerShape(32.dp),
                placeholder = { Text(text = "age") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            val scope = rememberCoroutineScope()
            Button(
                onClick = {
                    val ageStr = model.age.trim()
                    if (ageStr.isEmpty() || ageStr.contains(Regex.fromLiteral("[^0-9]"))) {
                        Log.e("ProfileSetupScreen", "bad age value: $ageStr")
                        return@Button
                    }

                    scope.launch {
                        GreenTraceProviders.userProvider?.userProfile(
                            model.alias,
                            model.bio,
                            ageStr.toInt()
                        )?.let { Log.e("ProfileEditScreen", it) }
                        navigator?.pop()
                    }
                },
                modifier = Modifier
                    .align(Alignment.End)
                    .fillMaxWidth()
            ) {
                Text("Save Changes")
            }
        }
    }
}