package com.example.architectureproject.tracking

import com.example.architectureproject.GreenTraceProviders
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.getField
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await

class FirebaseTrackingDataProvider : TrackingDataProvider {
    val db = FirebaseFirestore.getInstance()
    override suspend fun addActivity(activity: TrackingActivity): String {
        val doc = db.collection("activities").document()
        val uid = GreenTraceProviders.userProvider!!.uid()
        val impact = coroutineScope { async {
            GreenTraceProviders.impactProvider.computeImpact(activity)
        } }
        val communities = coroutineScope { async {
            GreenTraceProviders.userProvider!!.getCommunities().map { it.id }
        } }

        val update = hashMapOf(
            "data" to activity,
            "user" to uid,
            "communities" to communities.await(),
            "impact" to impact.await()
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
        granularity: TrackingDataGranularity,
        cid: String
    ): List<TrackingEntry> {
        TODO("Not yet implemented")
    }

    override suspend fun getActivities(
        period: TrackingPeriod,
        cid: String
    ): List<TrackingActivity> {
        val collection = db.collection("activities")
        val uid = GreenTraceProviders.userProvider!!.uid()
        val initialQuery =
            if (cid.isEmpty()) collection.whereEqualTo("user", uid)
            else collection.whereArrayContains("communities", cid)
        return initialQuery
            .whereGreaterThanOrEqualTo("date", period.start)
            .whereLessThanOrEqualTo("date", period.end)
            .get()
            .await()
            .documents
            .map {
                it.getField<TrackingActivity>("data")!!
                    .apply { id = it.id }
            }
    }
}