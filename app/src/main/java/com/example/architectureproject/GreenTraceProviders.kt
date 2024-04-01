package com.example.architectureproject

import com.example.architectureproject.community.CommunityChallenge
import com.example.architectureproject.community.CommunityManager
import com.example.architectureproject.community.FirebaseCommunityManager
import com.example.architectureproject.profile.FirebaseUserProvider
import com.example.architectureproject.profile.UserProvider
import com.example.architectureproject.tracking.BasicTrackingImpactProvider
import com.example.architectureproject.tracking.FirebaseTrackingDataProvider
import com.example.architectureproject.tracking.TrackingDataProvider
import com.example.architectureproject.tracking.TrackingImpactProvider
import com.example.architectureproject.tracking.demo.DummyMapProvider

object GreenTraceProviders {
    lateinit var userProvider: UserProvider
        private set
    lateinit var communityManager: CommunityManager
        private set
    val mapProvider: MapProvider = DummyMapProvider()
    val impactProvider: TrackingImpactProvider = BasicTrackingImpactProvider()

    lateinit var trackingProvider: TrackingDataProvider
        private set
    suspend fun init() {
        initUserProvider()
    }

    private suspend fun initUserProvider() {
        if (this::userProvider.isInitialized) return
        userProvider = FirebaseUserProvider.new()
    }

    suspend fun initTracking() {
        if (this::trackingProvider.isInitialized) return
        communityManager = FirebaseCommunityManager()
        trackingProvider = FirebaseTrackingDataProvider()

        // FIXME: remove demo hack
        //DummyTrackingData().addTo(trackingProvider!!)
//        trackingProvider!!.addActivity(
//            Transportation(
//                ZonedDateTime.ofInstant(
//                Instant.ofEpochSecond(1679723785),
//                ZoneId.of("America/Toronto")
//            ), "Commute 2",
//            listOf(
//                Transportation.Stop("Home", 43.57588850506251, -79.61096747795044),
//                Transportation.Stop("GO Station", 43.595679311751454, -79.64867577582866)
//            ), Transportation.Mode.Car, RecurrenceSchedule(
//                TrackingDataGranularity.Week, 2
//            )
//            )
//        )

        listOf(
            CommunityChallenge(
                "",
                "",
                0f,
                0f
            )
        ).forEach { communityManager.addCommunityChallenge(it) }
    }
}