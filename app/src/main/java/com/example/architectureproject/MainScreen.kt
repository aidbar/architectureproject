package com.example.architectureproject

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator

@Composable
fun LoadingScreen() {
    // Show loading screen when fetching things
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

class MainScreen(val fromJoinScreen: Boolean) : Screen {
    @Composable
    override fun Content() {
        var isLoading by remember { mutableStateOf(true) }
        LaunchedEffect(Unit) {
            GreenTraceProviders.initTracking()
            isLoading = false
        }

        if (isLoading) {
            LoadingScreen()
            return
        }

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