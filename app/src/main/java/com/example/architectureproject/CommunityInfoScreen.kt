package com.example.architectureproject

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.rounded.Create
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
import androidx.compose.ui.window.Dialog
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.architectureproject.community.CommunityInfo
import com.lightspark.composeqr.QrCodeView

data class CommunityInfoScreen(val info: CommunityInfo): Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val context = LocalContext.current
        var usernameToInvite by remember {mutableStateOf("")}
        var userIsTheCreator by remember {mutableStateOf(false)}
        val openEditCommunityDialog = remember{mutableStateOf(false)}

        if (GreenTraceProviders.userProvider.userInfo().uid == info.owner.uid) {
            userIsTheCreator = true
        }

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
            },
            floatingActionButton = {
                if (userIsTheCreator) {
                    FloatingActionButton(
                        onClick = {
                            openEditCommunityDialog.value = !openEditCommunityDialog.value
                        }
                    ) {
                        Icon(CommunityScreen.iconStyle.Create, "Edit this community")
                    }
                }
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
                    onClick = { navigator.push(CommunityMembersScreen(userIsTheCreator, info.members.toList()))},
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.CenterHorizontally),
                    colors = ButtonDefaults.buttonColors()
                ) {Text("View members")}
            }

            when {
                openEditCommunityDialog.value -> {
                    EditCommunityDialog(
                        onDismissRequest = { openEditCommunityDialog.value = false },
                        onConfirmation = { name, loc ->
                            openEditCommunityDialog.value = false
                            val comm = GreenTraceProviders.communityManager?.createCommunity(
                                GreenTraceProviders.userProvider.userInfo(), name, loc
                            )
                            comm?.let { GreenTraceProviders.trackingProvider?.attachCommunity(it) }
                            println("Community successfully created") // Add logic here to handle confirmation.
                        },
                        dialogTitle = "Edit this community",
                        dialogText = "You may change the name, location and profile picture of this community."
                    )
                }
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

    @Composable
    fun EditCommunityDialog(
        onDismissRequest: () -> Unit,
        onConfirmation: (String, String) -> Unit,
        //painter: Painter,
        //imageDescription: String,
        dialogTitle: String,
        dialogText: String
    ) {
        var newCommunityName by remember {mutableStateOf(info.name)}
        var newCommunityLocation by remember {mutableStateOf(info.location)}

        Dialog(onDismissRequest = { onDismissRequest() }) {
            // Draw a rectangle shape with rounded corners inside the dialog
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(500.dp)
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    /*Image(
                        painter = painter,
                        contentDescription = imageDescription,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .height(160.dp)
                    )*/
                    Text(
                        text = dialogTitle,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = dialogText,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    TextField(
                        value = newCommunityName,
                        onValueChange = {newCommunityName = it},
                        label = {Text("Name")},
                        modifier = Modifier.padding(10.dp),
                        singleLine = true
                    )
                    TextField(
                        value = newCommunityLocation,
                        onValueChange = {newCommunityLocation = it},
                        label = {Text("Location")},
                        modifier = Modifier.padding(10.dp),
                        singleLine = true
                    )
                    ImageSelectorComponent()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        TextButton(
                            onClick = { onDismissRequest() },
                            modifier = Modifier.padding(8.dp),
                        ) {
                            Text("Cancel")
                        }
                        TextButton(
                            onClick = { onConfirmation(newCommunityName, newCommunityLocation) },
                            modifier = Modifier.padding(8.dp),
                            enabled = newCommunityName.isNotBlank(),
                            colors = ButtonDefaults.buttonColors()
                        ) {
                            Text("Update")
                        }
                    }
                }
            }
        }
        /*when (newCommunityName) {
            "" -> {
                inputIsValid = false
            }
            else -> {
                inputIsValid = !newCommunityName.isNullOrBlank()
            }
        }*/
    }
}