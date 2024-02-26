package com.example.architectureproject

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.List
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions

object CommunityTab:Tab {
    override val options: TabOptions
        @Composable
        get() {
            val title = "Community"
            val icon  = rememberVectorPainter(Icons.Rounded.LocationOn)
            return remember{TabOptions(index=3u,title=title,icon=icon)}
        }

    @Composable
    override fun Content() {
//       Navigator(NewActivityScreen())
    }
}