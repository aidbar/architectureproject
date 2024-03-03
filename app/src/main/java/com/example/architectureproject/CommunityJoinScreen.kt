package com.example.architectureproject

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.painterResource
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow

class CommunityJoinScreen(private val communityURIStr: String) : Screen {
    init {
        GreenTraceProviders.initTracking()
    }

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val communityURI = remember { Uri.parse(communityURIStr) }
        val community = remember {
            communityURI.getQueryParameter("id")?.let {
                GreenTraceProviders.communityManager?.getCommunityById(it)
            }
        }

        if (community == null) {
            Column {
                Text("Error: invalid community link: $communityURIStr")
                Button(onClick = {
                    navigator.push(MainScreen(false))
                }) { Text("Close") }
            }
            return
        }

        Column {
            Text(community.name)
            Text(community.location)
            Image(painterResource(community.image), "community image")
            Row {
                Button(onClick = {
                    GreenTraceProviders.trackingProvider?.attachCommunity(community.id)
                    navigator.push(MainScreen(true))
                }) {
                    Text("Join")
                }
                Button(onClick = {
                    navigator.push(MainScreen(false))
                }) { Text("Cancel") }
            }
        }
    }

}
