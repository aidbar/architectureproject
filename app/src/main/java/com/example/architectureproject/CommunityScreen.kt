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
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import com.example.architectureproject.ui.theme.ArchitectureProjectTheme

class CommunityScreen :Screen {
    //var auth = FirebaseAuth.getInstance()
    private val iconStyle = Icons.Rounded
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
                    communityList = Datasource().loadCommunities(),
                    modifier = Modifier.fillMaxSize().padding(padding)
                )
            }
        }

        when {
            openCreateCommunityDialog.value -> {
                CreateCommunityDialog(
                    onDismissRequest = { openCreateCommunityDialog.value = false },
                    onConfirmation = {
                        openCreateCommunityDialog.value = false
                        println("Community successfully created") // Add logic here to handle confirmation.
                    },
                    dialogTitle = "Create a new community",
                    dialogText = "Enter the name and location of your new community - you may add a profile picture as well!",
                    icon = iconStyle.Info
                )
            }
        }
    }
    //}

    @Composable
    fun CommunityList(
        communityList: List<CommunityDataModelPlaceholderClass>,
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
        community: CommunityDataModelPlaceholderClass,
        modifier: Modifier = Modifier
    ) {
        Card(modifier = modifier) {
            Column {
                Image(
                    painter = painterResource(community.imageResourceId),
                    contentDescription = stringResource(community.nameStringResourceId),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(194.dp),
                    contentScale = ContentScale.Crop
                )
                Text(
                    text = LocalContext.current.getString(community.nameStringResourceId),
                    modifier = Modifier.padding(start = 10.dp, top = 10.dp, bottom = 5.dp, end = 0.dp),
                    style = MaterialTheme.typography.headlineSmall
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier.padding(10.dp)
                ) {
                    Icon(iconStyle.LocationOn, contentDescription = "location", modifier = Modifier.align(Alignment.CenterVertically))
                    Text(
                        text = LocalContext.current.getString(community.locationStringResourceId),
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
        onConfirmation: () -> Unit,
        dialogTitle: String,
        dialogText: String,
        icon: ImageVector,
    ) {
        AlertDialog(
            icon = {
                Icon(icon, contentDescription = "Example Icon")
            },
            title = {
                Text(text = dialogTitle)
            },
            text = {
                Text(text = dialogText)
            },
            onDismissRequest = {
                onDismissRequest()
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onConfirmation()
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onDismissRequest()
                    }
                ) {
                    Text("Dismiss")
                }
            }
        )
    }

}