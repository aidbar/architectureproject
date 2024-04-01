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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.Create
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.rememberCoroutineScope
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
import cafe.adriel.voyager.core.lifecycle.LifecycleEffect
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.architectureproject.community.CommunityInfo
import com.example.architectureproject.community.CommunityObserver
import com.lightspark.composeqr.QrCodeView
import kotlinx.coroutines.launch

class CommunityInfoScreenModel(info: CommunityInfo) : ScreenModel, CommunityObserver {
    override val id = info.id
    val userIsTheCreator = info.owner == GreenTraceProviders.userProvider.userInfo()
    var newCommunityName by mutableStateOf(info.name)
    var newCommunityLocation by mutableStateOf(info.location)
    var openEditCommunityDialog by mutableStateOf(false)
    var info by mutableStateOf(info)
    var loading by mutableStateOf(false)
    var deleted by mutableStateOf(false)

    var usernameToInvite by mutableStateOf("")

    fun showEditCommunityDialog() {
        newCommunityName = info.name
        newCommunityLocation = info.location
        openEditCommunityDialog = true
    }

    fun editCommunity(name: String, loc: String) {
        loading = true
        screenModelScope.launch {
            GreenTraceProviders.communityManager.updateCommunity(id, name, loc)
        }
    }

    fun dismissEditCommunityDialog() {
        openEditCommunityDialog = false
    }

    fun start() {
        GreenTraceProviders.communityManager.registerObserver(this)
    }

    fun stop() {
        GreenTraceProviders.communityManager.unregisterObserver(this)
    }

    override fun notify(info: List<CommunityInfo>, invites: List<CommunityInfo>, local: Boolean) {
        if (info.isEmpty()) {
            deleted = true
            return
        }

        this.info = info.first()
        loading = false
    }

    fun sendInvite(context: Context) {
        screenModelScope.launch {
            val user = GreenTraceProviders.userProvider.getUserByEmail(usernameToInvite)
            if (user != null) { //this is where the calls to check the validity of the username are to be performed
                GreenTraceProviders.communityManager.inviteUser(user.uid, info.id)
                println("Invite sent!")
                usernameToInvite = ""
                Toast.makeText(context, "Invite sent!", Toast.LENGTH_LONG).show()
                return@launch
            }

            //display an error message if the user does not exist
            println("This user does not exist. Check the username and try again.")
            Toast.makeText(
                context,
                "This user does not exist. Check the username and try again.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}

data class CommunityInfoScreen(val info: CommunityInfo): Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val context = LocalContext.current
        val model = rememberScreenModel { CommunityInfoScreenModel(info) }

        val showDialog = remember { mutableStateOf(false) }
        LifecycleEffect(
            onStarted = { model.start() },
            onDisposed = { model.stop() }
        )

        if (model.deleted) {
            navigator.pop()
            return
        }

        if (model.loading) {
            LoadingScreen()
            return
        }

        if(showDialog.value) {
            AlertDialog(
                onDismissRequest = { showDialog.value = false },
                title = {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Add Member",
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .padding(start = 16.dp, end = 0.dp)
                        )
                        IconButton(
                            onClick = { showDialog.value = false },
                            modifier = Modifier.align(Alignment.CenterVertically)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close"
                            )
                        }
                    }
                },
                dismissButton = {

                },
                text = {
                    Column(Modifier.verticalScroll(rememberScrollState())) {
                        Text(
                            text = "Use the QR code or link below to invite others to join " + model.info.name + ":",
                            modifier = Modifier
                                .padding(start = 10.dp, top = 10.dp, bottom = 5.dp, end = 0.dp)
                                .align(Alignment.CenterHorizontally),
                            textAlign = TextAlign.Center,
                            fontSize = 19.sp,
                            style = MaterialTheme.typography.labelMedium
                        )
                        QrCodeView(
                            data = model.info.inviteLink,
                            modifier = Modifier
                                .size(180.dp)
                                .align(Alignment.CenterHorizontally)
                                .padding(start = 10.dp, top = 10.dp, bottom = 5.dp, end = 0.dp)
                        )
                        SelectionContainer(modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(start = 10.dp, top = 10.dp, bottom = 5.dp, end = 0.dp)) {
                            Text(model.info.inviteLink)
                        }

                        Text(
                            text = "(OR) Send them an email invite below:",
                            modifier = Modifier
                                .padding(start = 10.dp, top = 10.dp, bottom = 5.dp, end = 0.dp)
                                .align(Alignment.CenterHorizontally),
                            textAlign = TextAlign.Center,
                            fontSize = 19.sp,
                            style = MaterialTheme.typography.labelMedium
                        )
                        TextField(
                            value = model.usernameToInvite,
                            onValueChange = { model.usernameToInvite = it },
                            label = { Text("Username") },
                            modifier = Modifier.padding(10.dp),
                            singleLine = true
                        )
                        TextButton(
                            onClick = { model.sendInvite(context) },
                            modifier = Modifier
                                .padding(8.dp)
                                .fillMaxWidth(),
                            enabled = model.usernameToInvite.isNotBlank(),
                            colors = ButtonDefaults.buttonColors()
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text("Invite")
                            }
                        }

                    }
                },
                confirmButton = {

                }
            )
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(model.info.name)
                            IconButton(
                                onClick = {
                                    showDialog.value = true
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = "Add member",
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            },
            floatingActionButton = {
                if (model.userIsTheCreator) {
                    FloatingActionButton(
                        onClick = { model.showEditCommunityDialog() }
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
                    painter = painterResource(model.info.image),
                    contentDescription = "Community image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    contentScale = ContentScale.Crop
                )
//                Text(
//                    text = model.info.name,
//                    modifier = Modifier
//                        .padding(start = 10.dp, top = 10.dp, bottom = 5.dp, end = 0.dp)
//                        .align(Alignment.CenterHorizontally),
//                    textAlign = TextAlign.Center,
//                    style = MaterialTheme.typography.headlineSmall
//                )
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
                        text = model.info.location,
                        modifier = Modifier.align(Alignment.CenterVertically),
                        style = MaterialTheme.typography.labelMedium,
                    )
                }

                val scope = rememberCoroutineScope()
                TextButton(
                    onClick = { scope.launch {
                        navigator.push(CommunityMembersScreen(
                            info
                        ))
                    } },
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.CenterHorizontally),
                    colors = ButtonDefaults.buttonColors()
                ) {Text("View members")}
            }

            when {
                model.openEditCommunityDialog -> {
                    EditCommunityDialog(
                        onDismissRequest = { model.dismissEditCommunityDialog() },
                        onConfirmation = { name, loc ->
                            model.editCommunity(name, loc)
                            println("Community successfully edited") // Add logic here to handle confirmation.
                            model.dismissEditCommunityDialog()
                         },
                        dialogTitle = "Edit this community",
                        dialogText = "You may change the name, location and profile picture of this community."
                    )
                }
            }
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
        val model = rememberScreenModel { CommunityInfoScreenModel(info) }

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
                        value = model.newCommunityName,
                        onValueChange = { model.newCommunityName = it },
                        label = {Text("Name")},
                        modifier = Modifier.padding(10.dp),
                        singleLine = true
                    )
                    TextField(
                        value = model.newCommunityLocation,
                        onValueChange = { model.newCommunityLocation = it },
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
                            onClick = { onConfirmation(model.newCommunityName, model.newCommunityLocation) },
                            modifier = Modifier.padding(8.dp),
                            enabled = model.newCommunityName.isNotBlank(),
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