package com.example.architectureproject

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.Navigator
import com.example.architectureproject.community.CommunityManager
import com.example.architectureproject.community.demo.DemoCommunityManager
import com.example.architectureproject.profile.FirebaseUserProvider
import com.example.architectureproject.profile.UserProvider
import com.example.architectureproject.tracking.TrackingDataProvider
import com.example.architectureproject.tracking.TrackingImpactProvider
import com.example.architectureproject.tracking.demo.DummyTrackingDataProvider
import com.example.architectureproject.tracking.demo.DummyTrackingImpactProvider
import com.example.architectureproject.ui.theme.ArchitectureProjectTheme

object GreenTraceProviders {
    val userProvider: UserProvider = FirebaseUserProvider()
    var communityManager: CommunityManager? = null
    val impactProvider: TrackingImpactProvider = DummyTrackingImpactProvider()
    var applicationContext: Context? = null
        private set

    var trackingProvider: TrackingDataProvider? = null
        private set

    fun init(applicationContext: Context) {
        this.applicationContext = applicationContext
    }

    fun initTracking() {
        if (trackingProvider != null) return
        communityManager =  DemoCommunityManager()
        trackingProvider = DummyTrackingDataProvider(userProvider.userInfo(), communityManager!!)
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GreenTraceProviders.init(applicationContext)
        val communityJoinURI = intent.data
        setContent {
            ArchitectureProjectTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val sharedPref = applicationContext.getSharedPreferences("AppPreferences", MODE_PRIVATE)
                    val hasLoggedIn = sharedPref.getString("id", null)
                    Log.d("hasLoggedIn", hasLoggedIn.toString())
                    if (hasLoggedIn != null) {
                        if (communityJoinURI != null) {
                            Navigator(CommunityJoinScreen(communityJoinURI.toString()))
                        }
                        else {
                            Navigator(MainScreen(false))
                        }
                    }else{
                        Navigator(AuthScreen())
                    }
                }
            }
        }
    }

}