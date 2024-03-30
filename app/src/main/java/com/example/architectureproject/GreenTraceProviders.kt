package com.example.architectureproject

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.content.Context
import android.os.Bundle
import com.example.architectureproject.community.CommunityManager
import com.example.architectureproject.community.FirebaseCommunityManager
import com.example.architectureproject.profile.FirebaseUserProvider
import com.example.architectureproject.profile.UserProvider
import com.example.architectureproject.tracking.BasicTrackingImpactProvider
import com.example.architectureproject.tracking.FirebaseTrackingDataProvider
import com.example.architectureproject.tracking.TrackingDataProvider
import com.example.architectureproject.tracking.TrackingImpactProvider
import com.example.architectureproject.tracking.demo.DummyMapProvider
import com.example.architectureproject.tracking.demo.DummyTrackingImpactProvider

object GreenTraceProviders {
    lateinit var userProvider: UserProvider
        private set
    lateinit var communityManager: CommunityManager
        private set
    val mapProvider: MapProvider = DummyMapProvider()
    val impactProvider: TrackingImpactProvider = BasicTrackingImpactProvider()
    lateinit var applicationContext: Context
        private set

    // we're using lifecycle callbacks, this will not leak
    @SuppressLint("StaticFieldLeak")
    lateinit var activityManager: ActivityManager
        private set

    lateinit var trackingProvider: TrackingDataProvider
        private set

    fun getActivity(): Activity = activityManager.activities.last()

    suspend fun init(activity: Activity) {
        activityManager = ActivityManager(activity)
        this.applicationContext = activity.applicationContext
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
    }
}


class ActivityManager(activity: Activity) : ActivityLifecycleCallbacks {
    val activities = mutableListOf(activity)

    init {
        activity.application.registerActivityLifecycleCallbacks(this)
    }

    override fun onActivityCreated(activity: Activity, bundle: Bundle?) {
        activities.add(activity)
    }
    override fun onActivityStarted(activity: Activity) { }
    override fun onActivityResumed(activity: Activity) { }
    override fun onActivityPaused(activity: Activity) { }
    override fun onActivityStopped(activity: Activity) { }
    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) { }
    override fun onActivityDestroyed(activity: Activity) { activities.remove(activity) }
}