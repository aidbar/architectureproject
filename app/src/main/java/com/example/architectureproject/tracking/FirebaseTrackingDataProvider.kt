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
        // FIXME: should we even allow users to specify date?
        //   figuring out which communities a user
        //   is part of on any given day in the past is difficult

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
            "date" to activity.date,
            "user" to uid,
            "communities" to communities.await(),
            "impact" to impact.await()
        )

        doc.set(update).await()
        return doc.id
    }

    override suspend fun viewActivity(id: String) =
        db.collection("activities")
            .document(id)
            .get()
            .await()
            .getField<TrackingActivity>("data")!!
            .apply { this.id = id }

    override suspend fun editActivity(id: String, new: TrackingActivity?) {
        // FIXME: editing date here should *not* be allowed.
        //   everything else can be edited.
        //   this is because figuring out which communities a user
        //   is part of on any given day in the past is difficult
        val doc = db.collection("activities").document(id)
        if (new == null) {
            // delete
            doc.delete().await()
            return
        }

        val impact = GreenTraceProviders.impactProvider.computeImpact(new)
        val update = hashMapOf(
            "data" to new,
            "impact" to impact
        )

        doc.update(update).await()
    }

    override suspend fun getImpact(
        period: TrackingPeriod,
        granularity: TrackingDataGranularity,
        cid: String
    ): List<TrackingEntry> =
        // we are essentially loading all relevant data, as activity objects, into our app's
        //   memory. this could be a really bad idea, but it should work
        // firebase does not have an equivalent to something like
        //   SELECT date, SUM(impact) FROM activities
        //   WHERE ${period.start} <= date AND date < ${period.end}
        //   GROUP BY [DAY/MONTH/YEAR/WEEK](date)
        //   ORDER BY date
        // so our solution is to do the equivalent of that, on the client side
        // our other option would be to issue parallel calls
        //   where each call performs aggregation to produce one single TrackingEntry
        //   using Firestore read-time aggregations
        // the final option would be to pre-aggregate impact data for each possible granularity
        //   upon the addActivity operation, on a per-user and per-community basis
        //   and then fetch that here. this may be what we do eventually
        getActivities(period).let { TrackingDataHelpers.aggregateImpact(
            it, GreenTraceProviders.impactProvider::computeImpact,
            period, granularity
        ) }

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