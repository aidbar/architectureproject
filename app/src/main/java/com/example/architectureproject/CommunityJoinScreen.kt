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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.architectureproject.community.CommunityInfo
import com.example.architectureproject.profile.User
import kotlinx.coroutines.launch

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
    fun CommunityJoinButtons(community: CommunityInfo) {
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()
        var members by remember { mutableStateOf(listOf<User>()) }
        LaunchedEffect(Unit) {
            members = GreenTraceProviders.communityManager.getCommunityMembers(community.id)
        }

        if (members.isEmpty()) return
        if (members.contains(GreenTraceProviders.userProvider.userInfo())) {
            Column {
                Text("You're already a member of this community")
                Button(onClick = {
                    navigator.push(MainScreen(true))
                }) { Text("Close") }
            }
            return
        }

        Row {
            Button(onClick = {
                scope.launch {
                    GreenTraceProviders.userProvider?.attachCommunity(community.id)
                    navigator.push(MainScreen(true))
                }
            }) {
                Text("Join")
            }
            Button(onClick = {
                navigator.push(MainScreen(false))
            }) { Text("Cancel") }
        }
    }

    @Composable
    fun CommunityJoinWidget(community: CommunityInfo) {
        Column {
            Text(community.name)
            Text(community.location)
            Image(painterResource(community.image), "community image")
            CommunityJoinButtons(community)
        }
    }

    @Composable
    override fun Content() {
        val communityURI = remember { Uri.parse(communityURIStr) }
        var community by remember { mutableStateOf(null as CommunityInfo?) }
        LaunchedEffect(Unit) {
            GreenTraceProviders.initTracking()
            community = communityURI.getQueryParameter("id")?.let {
                GreenTraceProviders.communityManager?.getCommunityById(it)
            }
            isLoading = false
        }

        if (isLoading) return
        if (community == null) {
            BadCommunity(communityURIStr)
            return
        }

        CommunityJoinWidget(community!!)
    }

}
