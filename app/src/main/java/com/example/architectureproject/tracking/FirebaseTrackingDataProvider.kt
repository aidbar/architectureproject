package com.example.architectureproject.tracking

import com.example.architectureproject.GreenTraceProviders
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseTrackingDataProvider : TrackingDataProvider {
    val db = FirebaseFirestore.getInstance()
    override suspend fun addActivity(activity: TrackingActivity): String {
        val doc = db.collection("activities").document()
        val user = GreenTraceProviders.userProvider!!.userInfo()
        val impact = GreenTraceProviders.impactProvider.computeImpact(activity)
        val update = hashMapOf(
            "data" to activity,
            "user" to user.uid,
            "impact" to impact
        )

        doc.set(update).await()
        return doc.id
    }

    override suspend fun viewActivity(id: String): TrackingActivity {
        TODO("Not yet implemented")
    }

    override suspend fun editActivity(id: String, new: TrackingActivity?) {
        TODO("Not yet implemented")
    }

    override suspend fun getImpact(
        period: TrackingPeriod,
        granularity: TrackingDataGranularity
    ): List<TrackingEntry> {
        TODO("Not yet implemented")
    }

    override suspend fun getActivities(period: TrackingPeriod): List<TrackingActivity> {
        TODO("Not yet implemented")
    }
}