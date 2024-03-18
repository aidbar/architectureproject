package com.example.architectureproject

import android.content.Context
import com.example.architectureproject.community.CommunityManager
import com.example.architectureproject.community.FirebaseCommunityManager
import com.example.architectureproject.profile.FirebaseUserProvider
import com.example.architectureproject.profile.UserProvider
import com.example.architectureproject.tracking.FirebaseTrackingDataProvider
import com.example.architectureproject.tracking.TrackingDataProvider
import com.example.architectureproject.tracking.TrackingImpactProvider
import com.example.architectureproject.tracking.demo.DummyMapProvider
import com.example.architectureproject.tracking.demo.DummyTrackingImpactProvider

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