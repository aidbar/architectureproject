package com.example.architectureproject

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions

object NewActivityTab:Tab {
    private fun readResolve(): Any = NewActivityTab
    override val options: TabOptions
        @Composable
        get() {
            val title = "New Activity"
            val icon  = rememberVectorPainter(Icons.Rounded.Add)
            return remember{TabOptions(index=2u,title=title,icon=icon)}
        }

    @Composable
    override fun Content() {
       Navigator(NewActivityScreen())
    }
}