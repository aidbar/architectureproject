/***
 * Adapted from https://github.com/google-developer-training/basic-android-kotlin-compose-training-affirmations/tree/main
 */
package com.example.architectureproject

//import com.patrykandpatrick.vico.core.axis.AxisPosition
//import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
//import com.patrykandpatrick.vico.compose.chart.line.lineChart
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Email
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import com.example.architectureproject.CommunityScreen.Companion.iconStyle
import com.example.architectureproject.community.CommunityInfo
import com.example.architectureproject.community.CommunityObserver
import com.example.architectureproject.profile.User
import com.example.architectureproject.ui.theme.ArchitectureProjectTheme
import com.lightspark.composeqr.QrCodeView
import kotlinx.coroutines.launch

class CommunityMembersScreenModel(info: CommunityInfo) : ScreenModel, CommunityObserver {
    override val cid = info.id
    val isCreator = info.owner == GreenTraceProviders.userProvider.userInfo()
    var community by mutableStateOf(info)
    var members by mutableStateOf(listOf<User>())
    var loading by mutableStateOf(true)
    var usernameToInvite by mutableStateOf("")
    var openAddMemberDialog by mutableStateOf(false)
    var openRemoveMemberDialog by mutableStateOf(false)
    var openViewMemberDialog by mutableStateOf(false)
    var deleted by mutableStateOf(false)

    override fun notify(info: List<CommunityInfo>, invites: List<CommunityInfo>, local: Boolean) {
        if (info.isEmpty()) {
            deleted = true
            return
        }

        screenModelScope.launch {
            members = GreenTraceProviders.communityManager.getCommunityMembers(info.first().id)
            loading = false
        }
    }

    fun removeMember(memberID : String, communityID : String) {
        screenModelScope.launch {
            GreenTraceProviders.communityManager.removeUserFromCommunity(memberID, communityID)
        }
    }
    fun sendInvite(context: Context) {
        screenModelScope.launch {
            val user = GreenTraceProviders.userProvider.getUserByEmail(usernameToInvite)
            if (user != null) { //this is where the calls to check the validity of the username are to be performed
                GreenTraceProviders.communityManager.inviteUser(user.uid, community.id)
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
    fun dismissAddMemberDialog() {
        openAddMemberDialog = false
    }

    fun start() {
        GreenTraceProviders.communityManager.registerObserver(this)
    }
    fun stop() {
        GreenTraceProviders.communityManager.unregisterObserver(this)
    }
}

class CommunityMembersScreen (private val info: CommunityInfo) :Screen {
    //var auth = FirebaseAuth.getInstance()
    //companion object { internal val iconStyle = Icons.Rounded }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val context = LocalContext.current
        val model = rememberScreenModel { CommunityMembersScreenModel(info) }
        LifecycleEffect(
            onStarted = { model.start() },
            onDisposed = { model.stop() }
        )

        if (model.deleted) {
            navigator.pop()
            return
        }
        if(model.openAddMemberDialog) {
            AlertDialog(
                onDismissRequest = { model.dismissAddMemberDialog() },
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
                            onClick = { model.openAddMemberDialog = false },
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
                            text = "Use the QR code or link below to invite others to join " + model.community.name + ":",
                            modifier = Modifier
                                .padding(start = 10.dp, top = 10.dp, bottom = 5.dp, end = 0.dp)
                                .align(Alignment.CenterHorizontally),
                            textAlign = TextAlign.Center,
                            fontSize = 19.sp,
                            style = MaterialTheme.typography.labelMedium
                        )
                        QrCodeView(
                            data = model.community.inviteLink,
                            modifier = Modifier
                                .size(180.dp)
                                .align(Alignment.CenterHorizontally)
                                .padding(start = 10.dp, top = 10.dp, bottom = 5.dp, end = 0.dp)
                        )
                        SelectionContainer(modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(start = 10.dp, top = 10.dp, bottom = 5.dp, end = 0.dp)) {
                            Text(model.community.inviteLink)
                        }

                        Text(
                            text = "(OR) Enter their username below:",
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
                            modifier = Modifier
                                .padding(10.dp)
                                .align(Alignment.CenterHorizontally),
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

        ArchitectureProjectTheme {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Community members")
                            IconButton(
                                onClick = {
                                    model.openAddMemberDialog = true
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = "Add member",
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        } },
                        navigationIcon = {
                            IconButton(onClick = { navigator.pop() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        }
                    )
                }
            ) {padding ->
                    if (model.loading) {
                        LoadingScreen()
                        return@Scaffold
                    }

                    MembersList(
                        info = model.community,
                        membersList = model.members, //GreenTraceProviders.trackingProvider.getCommunities(),
                        isCreator = model.isCreator,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    )
            }
        }
    }
    //}

    @Composable
    fun MembersList(
        info: CommunityInfo,
        membersList: List<User>,
        isCreator: Boolean,
        modifier: Modifier = Modifier
    ) {
        LazyColumn(modifier = modifier) {
            items(membersList) { member ->
                MemberCard(
                    info = info,
                    member = member,
                    isCreator = isCreator,
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable { println("click event received") }
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MemberCard(
        info : CommunityInfo,
        member: User,
        isCreator: Boolean,
        modifier: Modifier = Modifier
    ) {
        val model = rememberScreenModel {CommunityMembersScreenModel(info)}
        val context = LocalContext.current
        //val openRemoveMemberDialog = remember {mutableStateOf(false)}

        Card(modifier = modifier.clickable {
            //navigator.push(UserScreen(member))
            model.openViewMemberDialog = true
        }) {
            //Column {
                /*Image(
                    painter = painterResource(member.image),
                    contentDescription = member.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(194.dp),
                    contentScale = ContentScale.Crop
                )
                Text(
                    text = member.name,
                    modifier = Modifier.padding(start = 10.dp, top = 10.dp, bottom = 5.dp, end = 0.dp),
                    style = MaterialTheme.typography.headlineSmall
                )*/
                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier.padding(10.dp)
                ) {
                    Icon(iconStyle.AccountCircle, contentDescription = "member name", modifier = Modifier.align(Alignment.CenterVertically))
                    Text(
                        text = member.name,
                        modifier = Modifier.align(Alignment.CenterVertically),
                        style = MaterialTheme.typography.labelMedium,
                    )
                    Icon(iconStyle.DateRange, contentDescription = "age", modifier = Modifier.align(Alignment.CenterVertically))
                    Text(
                        text = member.age.toString() + " years old",
                        modifier = Modifier.align(Alignment.CenterVertically),
                        style = MaterialTheme.typography.labelMedium
                    )
                    Icon(iconStyle.Email, contentDescription = "email", modifier = Modifier.align(Alignment.CenterVertically))
                    Box(modifier = Modifier
                        .fillMaxWidth(fraction = 0.48f)
                        .align(Alignment.CenterVertically)) {
                        Text(
                            text = member.email,
                            //modifier = Modifier.align(Alignment.CenterVertically),
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    //Icon(iconStyle.Delete, contentDescription = "remove member " + member.name + " from this community", modifier = Modifier.align(Alignment.CenterVertically))
                    if (isCreator) {
                        TextButton(
                            onClick = { model.openRemoveMemberDialog = true },
                            modifier = Modifier.padding(8.dp),
                            colors = ButtonDefaults.buttonColors()
                        ) {
                            Icon(
                                iconStyle.Delete,
                                contentDescription = "remove member " + member.name + " from this community",
                                modifier = Modifier.align(Alignment.CenterVertically)
                            )
                        }
                    }
                }
            //}
        }

        when {
            model.openRemoveMemberDialog -> {
                RemoveMemberDialog(
                    onDismissRequest = { model.openRemoveMemberDialog = false },
                    onConfirmation = { /*name, loc ->*/
                        model.openRemoveMemberDialog = false
                        model.removeMember(member.uid, info.id)
                        Toast.makeText(context,"Member successfully removed.", Toast.LENGTH_SHORT).show()
                        println("Member successfully removed") // Add logic here to handle confirmation.
                    },
                    memberName = member.name,
                    dialogTitle = "Remove member",
                    dialogText = "Remove " + member.name + " from this community?"
                )
            }
        }
        when {
            model.openViewMemberDialog -> {
                AlertDialog(
                    title = {Text("Member information")},
                    text = {Text("Name: ${member.name} \nAge: ${member.age} years old\nEmail: ${member.email}")},
                    onDismissRequest = { model.openViewMemberDialog = false },
                    confirmButton = { },
                    dismissButton = { TextButton(onClick = { model.openViewMemberDialog = false }) {Text("Close")} }
                )
                    

            }
        }
    }
    //@OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun RemoveMemberDialog(
        onDismissRequest: () -> Unit,
        onConfirmation: () -> Unit,
        //painter: Painter,
        //imageDescription: String,
        memberName: String,
        dialogTitle: String,
        dialogText: String
    ) {

        Dialog(onDismissRequest = { onDismissRequest() }) {
            // Draw a rectangle shape with rounded corners inside the dialog
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
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
                            onClick = { onConfirmation() },
                            modifier = Modifier.padding(8.dp),
                            colors = ButtonDefaults.buttonColors()
                        ) {
                            Text("Remove")
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