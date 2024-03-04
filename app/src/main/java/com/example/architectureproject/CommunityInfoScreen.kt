package com.example.architectureproject

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                //Text(text = "")
            }
        }
    }
}