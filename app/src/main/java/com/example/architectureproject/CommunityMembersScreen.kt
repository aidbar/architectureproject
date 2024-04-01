/***
 * Adapted from https://github.com/google-developer-training/basic-android-kotlin-compose-training-affirmations/tree/main
 */
package com.example.architectureproject

//import com.patrykandpatrick.vico.core.axis.AxisPosition
//import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
//import com.patrykandpatrick.vico.compose.chart.line.lineChart
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
import kotlinx.coroutines.launch

class CommunityMembersScreenModel(info: CommunityInfo) : ScreenModel, CommunityObserver {
    override val id = info.id
    val isCreator = info.owner == GreenTraceProviders.userProvider.userInfo()
    var members by mutableStateOf(listOf<User>())
    var loading by mutableStateOf(true)
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
        val model = rememberScreenModel { CommunityMembersScreenModel(info) }
        LifecycleEffect(
            onStarted = { model.start() },
            onDisposed = { model.stop() }
        )

        if (model.deleted) {
            navigator.pop()
            return
        }
        
        ArchitectureProjectTheme {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Community members") },
                        navigationIcon = {
                            IconButton(onClick = { navigator.pop() }) {
                                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
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
        membersList: List<User>,
        isCreator: Boolean,
        modifier: Modifier = Modifier
    ) {
        LazyColumn(modifier = modifier) {
            items(membersList) { member ->
                MemberCard(
                    member = member,
                    isCreator = isCreator,
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable { println("click event received") }
                )
            }
        }
    }

    @Composable
    fun MemberCard(
        member: User,
        isCreator: Boolean,
        modifier: Modifier = Modifier
    ) {
        val context = LocalContext.current
        val openRemoveMemberDialog = remember {mutableStateOf(false)}

        Card(/*modifier = modifier.clickable {
            //navigator.push(UserScreen(member))
        }*/) {
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
                    Text(
                        text = member.email,
                        modifier = Modifier.align(Alignment.CenterVertically),
                        style = MaterialTheme.typography.labelMedium
                    )
                    //Icon(iconStyle.Delete, contentDescription = "remove member " + member.name + " from this community", modifier = Modifier.align(Alignment.CenterVertically))
                    if (isCreator) {
                        TextButton(
                            onClick = { openRemoveMemberDialog.value = true },
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
            openRemoveMemberDialog.value -> {
                RemoveMemberDialog(
                    onDismissRequest = { openRemoveMemberDialog.value = false },
                    onConfirmation = { /*name, loc ->*/
                        openRemoveMemberDialog.value = false
                        /*val comm = GreenTraceProviders.memberManager?.createCommunity(
                            GreenTraceProviders.userProvider.userInfo(), name, loc
                        )
                        comm?.let { GreenTraceProviders.trackingProvider?.attachCommunity(it) }*/
                        Toast.makeText(context,"Member successfully removed.", Toast.LENGTH_SHORT).show()
                        println("Member successfully removed") // Add logic here to handle confirmation.
                    },
                    memberName = member.name,
                    dialogTitle = "Remove member",
                    dialogText = "Remove " + member.name + " from this community?"
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