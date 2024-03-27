/***
 * Adapted from https://github.com/google-developer-training/basic-android-kotlin-compose-training-affirmations/tree/main
 */
package com.example.architectureproject

//import com.patrykandpatrick.vico.core.axis.AxisPosition
//import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
//import com.patrykandpatrick.vico.compose.chart.line.lineChart
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.architectureproject.community.CommunityInfo
import com.example.architectureproject.ui.theme.ArchitectureProjectTheme
import kotlinx.coroutines.launch

class CommunityScreenModel : ScreenModel {
    var openCreateCommunityDialog by mutableStateOf(false)
    var newCommunityName by mutableStateOf("")
    var newCommunityLocation by mutableStateOf("")
    var createDialogScrollState by mutableStateOf(ScrollState(0))
    var loading by mutableStateOf(true)

    var communities by mutableStateOf(listOf<CommunityInfo>())
    private suspend fun reloadCommunities() {
        communities = GreenTraceProviders.userProvider.getCommunities()
    }

    fun showCreateCommunityDialog() {
        if (loading) return
        newCommunityName = ""
        newCommunityLocation = ""
        createDialogScrollState = ScrollState(0)
        openCreateCommunityDialog = true
    }

    fun dismissCreateCommunityDialog() {
        openCreateCommunityDialog = false
    }

    fun loadCommunities() {
        screenModelScope.launch { reloadCommunities(); loading = false }
    }

    fun createCommunity(name: String, loc: String) {
        loading = true
        screenModelScope.launch {
            val comm = GreenTraceProviders.communityManager.createCommunity(
                GreenTraceProviders.userProvider.userInfo(), name, loc
            )
            comm.let { GreenTraceProviders.userProvider.attachCommunity(it) }
            reloadCommunities()
            loading = false
        }
    }
}

class CommunityScreen : Screen {
    companion object { internal val iconStyle = Icons.Rounded }

    @Composable
    @Preview
    override fun Content() {
        val model = rememberScreenModel { CommunityScreenModel() }
        LaunchedEffect(Unit) { model.loadCommunities() }

        ArchitectureProjectTheme {
            Scaffold(
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { model.showCreateCommunityDialog() }
                    ) {
                        Icon(iconStyle.Add, "Create new community")
                    }
                }
            ) {padding ->
                if (model.loading) {
                    LoadingScreen()
                    return@Scaffold
                }

                CommunityList(
                    communityList = model.communities,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                )
            }
        }

        when {
            model.openCreateCommunityDialog -> {
                CreateCommunityDialog(
                    onDismissRequest = { model.dismissCreateCommunityDialog() },
                    onConfirmation = { name, loc ->
                        model.createCommunity(name, loc)
                        println("Community successfully created") // Add logic here to handle confirmation.
                        model.dismissCreateCommunityDialog()
                    },
                    dialogTitle = "Create a new community",
                    dialogText = "Enter the name and location of your new community - you may add a profile picture as well!"
                )
            }
        }
    }
    //}

    @Composable
    fun CommunityList(
        communityList: List<CommunityInfo>,
        modifier: Modifier = Modifier
    ) {
        LazyColumn(modifier = modifier) {
            items(communityList) { community ->
                CommunityCard(
                    community = community,
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable { println("click event received") }
                )
            }
        }
    }

    @Composable
    fun CommunityCard(
        community: CommunityInfo,
        modifier: Modifier = Modifier
    ) {
        val navigator = LocalNavigator.currentOrThrow

        Card(modifier = modifier.clickable {
            navigator.push(CommunityInfoScreen(community))
        }) {
            Column {
                Image(
                    painter = painterResource(community.image),
                    contentDescription = community.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(194.dp),
                    contentScale = ContentScale.Crop
                )
                Text(
                    text = community.name,
                    modifier = Modifier.padding(start = 10.dp, top = 10.dp, bottom = 5.dp, end = 0.dp),
                    style = MaterialTheme.typography.headlineSmall
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier.padding(10.dp)
                ) {
                    Icon(iconStyle.LocationOn, contentDescription = "location", modifier = Modifier.align(Alignment.CenterVertically))
                    Text(
                        text = community.location,
                        modifier = Modifier.align(Alignment.CenterVertically),
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }
    }
    //@OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun CreateCommunityDialog(
        onDismissRequest: () -> Unit,
        onConfirmation: (String, String) -> Unit,
        //painter: Painter,
        //imageDescription: String,
        dialogTitle: String,
        dialogText: String
    ) {
        val model = rememberScreenModel { CommunityScreenModel() }

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
                        .fillMaxSize()
                        .verticalScroll(model.createDialogScrollState),
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
                        label = {Text("Name (required)")},
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
                            Text("Create")
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