package com.example.architectureproject

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator

class MainScreen(val fromJoinScreen: Boolean) : Screen {
    init {
        GreenTraceProviders.initTracking()
    }

    @Composable
    override fun Content() {
        TabNavigator(if (fromJoinScreen) CommunityTab else HomeTab) {
            Scaffold(bottomBar = {
                NavigationBar {
                    TabNavigatorItem(tab = HomeTab)
                    TabNavigatorItem(tab = LearnTab)
                    TabNavigatorItem(tab = NewActivityTab)
                    TabNavigatorItem(tab = CommunityTab)
                    TabNavigatorItem(tab = ProfileTab)
                }
            }) {
                Box(modifier = Modifier.padding(bottom = it.calculateBottomPadding())) {
                    CurrentTab()
                }
            }
        }

    }
}

@Composable
private fun RowScope.TabNavigatorItem(tab: Tab) {
    val tabNavigator = LocalTabNavigator.current
    NavigationBarItem(
        selected = tabNavigator.current == tab,
        onClick = { tabNavigator.current = tab },
        icon = {
            tab.options.icon?.let {
                Icon(painter = it, contentDescription = tab.options.title)
            }
        },
        label = {
            if (tab.options.title != "New Activity") {
                Text(text = tab.options.title,maxLines = 1, fontSize = 10.sp)
            }
        })

}