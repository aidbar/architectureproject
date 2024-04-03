package com.example.architectureproject

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.Create
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
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
import com.example.architectureproject.community.CommunityChallenge
import com.example.architectureproject.community.CommunityChallengeState
import com.example.architectureproject.community.CommunityChallengesObserver
import com.example.architectureproject.community.CommunityInfo
import com.example.architectureproject.community.CommunityObserver
import com.example.architectureproject.profile.User
import com.lightspark.composeqr.QrCodeView
import kotlinx.coroutines.launch

class CommunityInfoScreenModel(info: CommunityInfo) : ScreenModel, CommunityObserver, CommunityChallengesObserver {
    override val cid = info.id

    val currentUserId = GreenTraceProviders.userProvider.userInfo().uid
    val userIsTheCreator = info.owner == GreenTraceProviders.userProvider.userInfo()
    var newCommunityName by mutableStateOf(info.name)
    var newCommunityLocation by mutableStateOf(info.location)
    var openEditCommunityDialog by mutableStateOf(false)
    //var openAddMemberDialog by mutableStateOf(false)
    var openLeaveCommunityDialog by mutableStateOf(false)
    var openDeleteCommunityDialog by mutableStateOf(false)
    var openAddProgressDialog by mutableStateOf(false)
    var info by mutableStateOf(info)
    var loading by mutableStateOf(false)
    var deleted by mutableStateOf(false)

    //var usernameToInvite by mutableStateOf("")
    var challenges by mutableStateOf(listOf<Pair<CommunityChallenge, CommunityChallengeState>>())
    var leaderboard by mutableStateOf(listOf<Pair<User, Float>>())
    var selectedChallenge by mutableStateOf(-1)
    var currentImpact by mutableStateOf(0f)
    var currentUserImpact by mutableStateOf(0f)

    var challengeDialogInput by mutableStateOf("")

    fun showEditCommunityDialog() {
        newCommunityName = info.name
        newCommunityLocation = info.location
        openEditCommunityDialog = true
    }

    fun editCommunity(name: String, loc: String) {
        loading = true
        screenModelScope.launch {
            GreenTraceProviders.communityManager.updateCommunity(cid, name, loc)
        }
    }

    fun leaveCommunity(userId : String) {
        screenModelScope.launch {
            GreenTraceProviders.communityManager.removeUserFromCommunity(userId, info.id)
        }

    }
    fun deleteCommunity() {
        screenModelScope.launch {
            GreenTraceProviders.communityManager.deleteCommunity(info.id)
        }
    }

    fun dismissEditCommunityDialog() {
        openEditCommunityDialog = false
    }

    fun dismissLeaveCommunityDialog() {
        openLeaveCommunityDialog = false
    }
    fun dismissDeleteCommunityDialog() {
        openDeleteCommunityDialog = false
    }

    fun start() {
        GreenTraceProviders.communityManager.registerObserver(this)
        GreenTraceProviders.communityManager.registerChallengesObserver(this)
    }

    fun stop() {
        GreenTraceProviders.communityManager.unregisterObserver(this)
        GreenTraceProviders.communityManager.unregisterChallengesObserver(this)
    }

    override fun notify(info: List<CommunityInfo>, invites: List<CommunityInfo>, local: Boolean) {
        if (info.isEmpty()) {
            deleted = true
            return
        }

        this.info = info.first()
        loading = false
    }

    override fun notify(
        info: List<Pair<CommunityChallenge, CommunityChallengeState>>,
        leaderboard: List<Pair<User, Float>>,
        currentImpact: Float,
        currentUserImpact: Float,
        local: Boolean
    ) {
        challenges = info
        leaderboard.forEachIndexed { i, (k, v) -> Log.i("leaderboard", "leaderboard[$i] = ($k, $v)") }
        this.leaderboard = leaderboard
        this.currentImpact = currentImpact
        this.currentUserImpact = currentUserImpact
    }

    fun addProgress(delta: Float) {
        val (challenge, _) = challenges[selectedChallenge]
        screenModelScope.launch {
            GreenTraceProviders.communityManager.addChallengeProgress(
                challenge,
                delta
            )
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

        /*if (model.selectedChallenge != -1) {
            ChallengeDialog()
        }*/

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
                            /*IconButton(
                                onClick = {
                                    model.openAddMemberDialog = true
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = "Add member",
                                    modifier = Modifier.size(30.dp)
                                )
                            }*/
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
                Text("Community leaderboard", fontSize = 24.sp, modifier = Modifier.align(Alignment.CenterHorizontally), style = MaterialTheme.typography.labelLarge)
                LazyColumn(
                    Modifier
                        .height(100.dp)
                        .align(Alignment.CenterHorizontally)
                        .padding(10.dp)) {
                    items(model.leaderboard.size) { index ->
                        val (user, points) = model.leaderboard[index]
                        Surface(
                            color = Color.LightGray,
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp, horizontal = 16.dp)
                            ) {
                                Text(
                                    text = user.name,
                                    fontSize = 18.sp,
                                    style = MaterialTheme.typography.labelLarge
                                )
                                Text(
                                    text = "$points",
                                    fontSize = 18.sp,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }
                Text("Challenges", fontSize = 24.sp, modifier = Modifier.align(Alignment.CenterHorizontally), style = MaterialTheme.typography.labelLarge)
                LazyColumn(
                    modifier = Modifier
                        .height(300.dp)
                        .fillMaxWidth()
                        .padding(10.dp)
                ) {
                    items(model.challenges.size) { index ->
                        val (challenge, _) = model.challenges[index]
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 16.dp),
                            elevation = 4.dp
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = challenge.name,
                                    fontSize = 18.sp,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Button(
                                        onClick = {
                                            model.selectedChallenge = index
                                            model.openAddProgressDialog = true
                                        }
                                    ) {
                                        Text("View Details")
                                    }
                                }
                            }
                        }
                    }
                }
                Text("Current impact: ${model.currentImpact}", fontSize = 24.sp, modifier = Modifier.align(Alignment.CenterHorizontally), style = MaterialTheme.typography.labelLarge)
                Text("My current impact: ${model.currentUserImpact}", fontSize = 24.sp, modifier = Modifier.align(Alignment.CenterHorizontally), style = MaterialTheme.typography.labelLarge)
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

                TextButton(
                    onClick = {
                        model.openLeaveCommunityDialog = true
                    },
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.CenterHorizontally),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Leave " + model.info.name)
                }
                if (model.userIsTheCreator) {
                    TextButton(onClick = {
                        model.openDeleteCommunityDialog = true
                    },
                        modifier = Modifier
                            .padding(8.dp)
                            .align(Alignment.CenterHorizontally),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                        Text("Delete " + model.info.name)
                    }
                }
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
            when {
                model.openLeaveCommunityDialog -> {
                    AlertDialog(
                        title = {Text("Leave " + model.info.name + "?")},
                        text = {Text("Are you sure want to leave this community?")},
                        onDismissRequest = { model.dismissLeaveCommunityDialog() },
                        confirmButton = { TextButton(onClick = {
                            model.leaveCommunity(model.currentUserId)
                            println("the user has successfully left the community")
                            Toast.makeText(context,"You have left " + model.info.name, Toast.LENGTH_SHORT).show()
                            model.dismissLeaveCommunityDialog()
                                                               }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {Text("Leave")}},
                        dismissButton = { TextButton(onClick = { model.dismissLeaveCommunityDialog() }) {Text("Cancel")} }
                    )
                }
            }
            when {
                model.openDeleteCommunityDialog -> {
                    AlertDialog(
                        title = {Text("Delete " + model.info.name + "?")},
                        text = {Text("Are you sure want to delete this community? WARNING: This action cannot be undone.")},
                        onDismissRequest = { model.dismissDeleteCommunityDialog() },
                        confirmButton = { TextButton(onClick = {
                            model.deleteCommunity()
                            println("the community has successfully been deleted")
                            Toast.makeText(context,"You have deleted " + model.info.name, Toast.LENGTH_SHORT).show()
                            model.dismissDeleteCommunityDialog()
                        }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {Text("Delete")}},
                        dismissButton = { TextButton(onClick = { model.dismissDeleteCommunityDialog() }) {Text("Cancel")} }
                    )
                }
            }
            when {
                model.openAddProgressDialog -> {
                    ChallengeDialog()
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    private @Composable
    fun ChallengeDialog() {
        val model = rememberScreenModel { CommunityInfoScreenModel(info) }
        //var text by remember { mutableStateOf("") }
        val (challenge, state) = model.challenges[model.selectedChallenge]
        AlertDialog(
            onDismissRequest = { model.selectedChallenge = -1; model.openAddProgressDialog = false },
            title = {
                Text(
                    text = challenge.name,
                    textAlign = TextAlign.Center
                )
            },
            dismissButton = {
                Button(onClick = {model.openAddProgressDialog = false; model.selectedChallenge = -1; model.challengeDialogInput = ""}, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) {Text("Cancel")}
            },
            text = {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState())
                                           .align(Alignment.Center)
                    ) {
                        Text("${state.progress} / ${challenge.goal}", modifier = Modifier.padding(10.dp), textAlign = TextAlign.Center)
                        if (challenge.desc.isNotBlank()) {
                            Text(challenge.desc, modifier = Modifier.padding(10.dp), textAlign = TextAlign.Center)
                        }
                        TextField(value = model.challengeDialogInput, onValueChange = { model.challengeDialogInput = it }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier
                            .width(95.dp)
                            .padding(10.dp))
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    model.addProgress(model.challengeDialogInput.toFloatOrNull() ?: 0f)
                    model.challengeDialogInput = ""
                    model.openAddProgressDialog = false
                }) { Text("Submit") }
            }
        )
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