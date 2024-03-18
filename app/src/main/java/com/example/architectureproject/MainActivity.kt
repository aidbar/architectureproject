package com.example.architectureproject

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.Navigator
import com.example.architectureproject.community.CommunityManager
import com.example.architectureproject.community.FirebaseCommunityManager
import com.example.architectureproject.profile.FirebaseUserProvider
import com.example.architectureproject.profile.UserProvider
import com.example.architectureproject.tracking.FirebaseTrackingDataProvider
import com.example.architectureproject.tracking.TrackingDataProvider
import com.example.architectureproject.tracking.TrackingImpactProvider
import com.example.architectureproject.tracking.demo.DummyMapProvider
import com.example.architectureproject.tracking.demo.DummyTrackingImpactProvider
import com.example.architectureproject.ui.theme.ArchitectureProjectTheme

object GreenTraceProviders {
    var userProvider: UserProvider? = null
    var communityManager: CommunityManager? = null
    val mapProvider: MapProvider = DummyMapProvider()
    val impactProvider: TrackingImpactProvider = DummyTrackingImpactProvider()
    var applicationContext: Context? = null
        private set

    var trackingProvider: TrackingDataProvider? = null
        private set

    suspend fun init(applicationContext: Context) {
        this.applicationContext = applicationContext
        initUserProvider()
    }

    private suspend fun initUserProvider() {
        userProvider = userProvider ?: FirebaseUserProvider.new()
    }

    suspend fun initTracking() {
        if (trackingProvider != null) return
        communityManager = FirebaseCommunityManager()
        trackingProvider = FirebaseTrackingDataProvider()

        // FIXME: remove demo hack
        //DummyTrackingData().addTo(trackingProvider!!)
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val communityJoinURI = intent.data
        setContent {
            ArchitectureProjectTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var isLoading by remember { mutableStateOf(true) }
                    LaunchedEffect(Unit) {
                        GreenTraceProviders.init(applicationContext)
                        isLoading = false
                    }

                    if (isLoading) {
                        LoadingScreen()
                        return@Surface
                    }

                    val sharedPref = applicationContext.getSharedPreferences("AppPreferences", MODE_PRIVATE)
                    val hasLoggedIn = sharedPref.getString("id", null)
                    Log.d("hasLoggedIn", hasLoggedIn.toString())
                    if (hasLoggedIn != null) {
                        if (!GreenTraceProviders.userProvider!!.hasUserProfile()) {
                            Navigator(NewAccountSetupScreen())
                            return@Surface
                        }

                        if (communityJoinURI != null) {
                            Navigator(CommunityJoinScreen(communityJoinURI.toString()))
                            return@Surface
                        }

                        Navigator(MainScreen(false))
                        return@Surface
                    }

                    Navigator(AuthScreen())
                }
            }
        }
    }
}
