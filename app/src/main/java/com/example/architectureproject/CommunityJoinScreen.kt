package com.example.architectureproject

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.architectureproject.community.CommunityInfo

class CommunityJoinScreen(private val communityURIStr: String) : Screen {
    private var isLoading by mutableStateOf(true)

    @Composable
    fun BadCommunity(communityURIStr: String) {
        val navigator = LocalNavigator.currentOrThrow
        Column {
            Text("Error: invalid community link: $communityURIStr")
            Button(onClick = {
                navigator.push(MainScreen(false))
            }) { Text("Close") }
        }
    }

    @Composable
    fun CommunityJoinWidget(community: CommunityInfo) {
        val navigator = LocalNavigator.currentOrThrow
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

    @Composable
    override fun Content() {
        LaunchedEffect(Unit) {
            GreenTraceProviders.initTracking()
            isLoading = false
        }

        if (isLoading) return

        val communityURI = remember { Uri.parse(communityURIStr) }
        val community = remember {
            communityURI.getQueryParameter("id")?.let {
                GreenTraceProviders.communityManager?.getCommunityById(it)
            }
        }

        if (community == null) {
            BadCommunity(communityURIStr)
            return
        }

        CommunityJoinWidget(community)
    }

}
