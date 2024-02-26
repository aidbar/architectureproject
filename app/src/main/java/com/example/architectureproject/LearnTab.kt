package com.example.architectureproject

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.List
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions

object LearnTab:Tab {
    override val options: TabOptions
        @Composable
        get() {
            val title = "Learn"
            val icon  = rememberVectorPainter(Icons.Rounded.List)
            return remember{TabOptions(index=1u,title=title,icon=icon)}
        }

    @Composable
    override fun Content() {
//       Navigator(LearnScreen())
    }
}