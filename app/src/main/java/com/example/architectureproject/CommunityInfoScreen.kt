package com.example.architectureproject

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.architectureproject.community.CommunityInfo
import com.example.architectureproject.profile.User
import com.lightspark.composeqr.QrCodeView

data class CommunityInfoScreen(val info: CommunityInfo): Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val context = LocalContext.current
        var usernameToInvite by remember {mutableStateOf("")}

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(info.name) },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                Modifier
                    .padding(padding)
                    .verticalScroll(state = rememberScrollState())
                ) {
                Image(
                    painter = painterResource(info.image),
                    contentDescription = "Community image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    contentScale = ContentScale.Crop
                )
                Text(
                    text = info.name,
                    modifier = Modifier
                        .padding(start = 10.dp, top = 10.dp, bottom = 5.dp, end = 0.dp)
                        .align(Alignment.CenterHorizontally),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineSmall
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier
                        .padding(10.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    Icon(
                        CommunityScreen.iconStyle.LocationOn, contentDescription = "location", modifier = Modifier.align(
                            Alignment.CenterVertically))
                    Text(
                        text = info.location,
                        modifier = Modifier.align(Alignment.CenterVertically),
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
                Text(
                    text = "Use the QR code or link below to invite others to join " + info.name + ":",
                    modifier = Modifier
                        .padding(start = 10.dp, top = 10.dp, bottom = 5.dp, end = 0.dp)
                        .align(Alignment.CenterHorizontally),
                    textAlign = TextAlign.Center,
                    fontSize = 19.sp,
                    style = MaterialTheme.typography.labelMedium
                )
                QrCodeView(
                    data = info.inviteLink,
                    modifier = Modifier
                        .size(180.dp)
                        .align(Alignment.CenterHorizontally)
                        .padding(start = 10.dp, top = 10.dp, bottom = 5.dp, end = 0.dp)
                )
                SelectionContainer(modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(start = 10.dp, top = 10.dp, bottom = 5.dp, end = 0.dp)) {
                    Text(info.inviteLink)
                }
                Row(modifier = Modifier
                    .align(Alignment.CenterHorizontally)) {
                    TextField(
                        value = usernameToInvite,
                        onValueChange = { usernameToInvite = it },
                        label = { Text("Username") },
                        modifier = Modifier.padding(10.dp),
                        singleLine = true
                    )
                    TextButton(
                        onClick = { if (checkifUserExists(context, usernameToInvite)) {usernameToInvite = ""} },
                        modifier = Modifier
                            .padding(8.dp)
                            .align(Alignment.CenterVertically),
                        enabled = usernameToInvite.isNotBlank(),
                        colors = ButtonDefaults.buttonColors()
                    ) {
                        Text("Invite")
                    }
                }
                TextButton(
                    onClick = { navigator.push(CommunityMembersScreen(info.members.toList()))},
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.CenterHorizontally),
                    colors = ButtonDefaults.buttonColors()
                ) {Text("View members")}
            }
        }
    }

    private fun checkifUserExists(context: Context, usernameToInvite: String): Boolean {
        if (true) { //this is where the calls to check the validity of the username are to be performed
            println("Invite sent!")
            Toast.makeText(context, "Invite sent!", Toast.LENGTH_LONG).show()
            return true
        } else { //display an error message if the user does not exist
            println("This user does not exist. Check the username and try again.")
            Toast.makeText(context, "This user does not exist. Check the username and try again.", Toast.LENGTH_SHORT).show()
            return false
        }
    }
}