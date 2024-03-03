package com.example.architectureproject

import android.net.Uri
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator

class CommunityJoinScreen(val communityURI: Uri) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current

        Text(communityURI.getQueryParameter("id")!!)
    }

}
