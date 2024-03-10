/***
 * Adapted from https://github.com/google-developer-training/basic-android-kotlin-compose-training-affirmations/tree/main
 */
package com.example.architectureproject

//import com.patrykandpatrick.vico.core.axis.AxisPosition
//import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
//import com.patrykandpatrick.vico.compose.chart.line.lineChart
import androidx.compose.foundation.Image
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.architectureproject.community.CommunityInfo
import com.example.architectureproject.ui.theme.ArchitectureProjectTheme

class CommunityScreen :Screen {
    //var auth = FirebaseAuth.getInstance()
    companion object { internal val iconStyle = Icons.Rounded }

    @Composable
    @Preview
    override fun Content() {
        val openCreateCommunityDialog = remember {mutableStateOf(false)}
        ArchitectureProjectTheme {
            Scaffold(
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = {
                            openCreateCommunityDialog.value = !openCreateCommunityDialog.value
                        }
                    ) {
                        Icon(iconStyle.Add, "Create new community")
                    }
                }
            ) {padding ->
                CommunityList(
                    communityList = GreenTraceProviders.trackingProvider!!.getCommunities(),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                )
            }
        }

        when {
            openCreateCommunityDialog.value -> {
                CreateCommunityDialog(
                    onDismissRequest = { openCreateCommunityDialog.value = false },
                    onConfirmation = { name, loc ->
                        openCreateCommunityDialog.value = false
                        val comm = GreenTraceProviders.communityManager?.createCommunity(
                            GreenTraceProviders.userProvider!!.userInfo(), name, loc
                        )
                        comm?.let { GreenTraceProviders.trackingProvider?.attachCommunity(it) }
                        println("Community successfully created") // Add logic here to handle confirmation.
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
        var newCommunityName by remember {mutableStateOf("")}
        var newCommunityLocation by remember {mutableStateOf("")}

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
                        label = {Text("Name (required)")},
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