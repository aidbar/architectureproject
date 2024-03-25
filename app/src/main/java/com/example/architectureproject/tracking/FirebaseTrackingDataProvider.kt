package com.example.architectureproject.tracking

import com.example.architectureproject.GreenTraceProviders
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Transaction
import com.google.firebase.firestore.getField
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class FirebaseTrackingDataProvider : TrackingDataProvider {
    private val db = FirebaseFirestore.getInstance()
    override suspend fun addActivity(activity: TrackingActivity): String {
        // TODO: handle community statistics aggregation (or maybe not?)

        val ref = db.collection("activities").document()
        val uid = GreenTraceProviders.userProvider!!.uid()!!
        val impact = GreenTraceProviders.impactProvider.computeImpact(activity)

        db.runTransaction {
            if (activity.schedule == null)
                updateStatistics(uid, listOf(activity.date to impact.value), it)
            it.set(ref, hashMapOf(
                "data" to activity,
                "date" to activity.date.toEpochSecond(),
                "user" to uid,
                "impact" to impact.value,
                "schedule" to activity.schedule?.let { rec -> RecurrenceSchedule.Raw(rec) },
                "date_zone" to activity.date.zone.id,
                "type" to activity.javaClass.name
            ))
        }.await()

        return ref.id
    }

    private fun updateStatistics(id: String, deltas: List<Pair<ZonedDateTime, Float>>, txn: Transaction, community: Boolean = false) {
        val lst = listOf(
            TrackingDataGranularity.Day to TrackingPeriod::dayOf,
            TrackingDataGranularity.Week to TrackingPeriod::weekOf,
            TrackingDataGranularity.Month to TrackingPeriod::monthOf,
            TrackingDataGranularity.Year to TrackingPeriod::yearOf
        )

        // first, perform all reads
        val toUpdate = lst.map {
            val type = it.first.name.lowercase()
            type to deltas.map { (date, delta) ->
                val period = it.second(date)
                val docId =
                    "${if (community) "c" else "u"}_${id}_${type[0]}_${period.start.toEpochSecond()}"
                val ref = db.collection("statistics").document(docId)
                Triple(period, ref, txn.get(ref)) to delta
            }
        }

        // then perform all writes
        toUpdate.forEach { (type, updatesForType) ->
            updatesForType.map { (meta, delta) ->
                val (period, ref, doc) = meta
                if (doc.exists()) {
                    txn.update(ref, "impact", FieldValue.increment(delta.toDouble()))
                    return@map
                }

                txn.set(
                    ref, hashMapOf<String, Any>(
                        "type" to type,
                        (if (community) "community" else "user") to id,
                        "start_date" to period.start.toEpochSecond(),
                        "end_date" to period.end.toEpochSecond(),
                        "impact" to delta,
                        "date_zone" to period.start.zone.id
                    )
                )
            }
        }
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
        val ref = db.collection("activities").document(id)
        val uid = GreenTraceProviders.userProvider!!.uid()!!
        if (new == null) {
            // delete
            db.runTransaction {
                val doc = it.get(ref)
                val date = doc.getField<Long>("date")!!.let { epoch ->
                    ZonedDateTime.ofInstant(Instant.ofEpochSecond(epoch), ZoneId.of(
                        doc.getField<String>("date_zone")!!
                    ))
                }
                val impact = doc.getField<Float>("impact")!!

                updateStatistics(uid, listOf(date to -impact), it)
                it.delete(ref)
            }.await()
            return
        }

        val impact = GreenTraceProviders.impactProvider.computeImpact(new)
        db.runTransaction {
            val doc = it.get(ref)
            val date = doc.getField<Long>("date")!!.let { epoch ->
                ZonedDateTime.ofInstant(Instant.ofEpochSecond(epoch), ZoneId.of(
                    doc.getField<String>("date_zone")!!
                ))
            }
            val oldImpact = doc.getField<Float>("impact")!!

            if (date.toEpochSecond() == new.date.toEpochSecond())
                updateStatistics(uid, listOf(date to impact.value - oldImpact), it)
            else
                updateStatistics(uid, listOf(
                    date to -oldImpact,
                    new.date to impact.value
                ), it)

            it.update(ref, hashMapOf(
                "data" to new,
                "impact" to impact.value
            ))
        }.await()
    }

    override suspend fun getImpact(
        period: TrackingPeriod,
        granularity: TrackingDataGranularity,
        cid: String
    ): List<TrackingEntry> = db.collection("statistics")
        .whereEqualTo(
            if (cid.isEmpty()) "user" else "community",
            cid.ifEmpty { GreenTraceProviders.userProvider!!.uid()!! }
        )
        .whereEqualTo("type", granularity.name.lowercase())
        .where(Filter.or(
            Filter.and(
                Filter.greaterThanOrEqualTo("start_date", period.start.toEpochSecond()),
                Filter.lessThan("start_date", period.end.toEpochSecond())
            ),
            period.shiftPeriods(granularity, -1).let { endPeriod ->
                Filter.and(
                    Filter.greaterThan("start_date", endPeriod.start.toEpochSecond()),
                    Filter.lessThanOrEqualTo("start_date", endPeriod.end.toEpochSecond())
                )
            }
        ))
        .get()
        .await()
        .documents
        .map {
            val start = it.getField<Long>("start_date")!!
            val end = it.getField<Long>("end_date")!!
            val zone = it.getField<String>("date_zone")!!.let(ZoneId::of)
            val subPeriod = TrackingPeriod(
                ZonedDateTime.ofInstant(Instant.ofEpochSecond(start), zone),
                ZonedDateTime.ofInstant(Instant.ofEpochSecond(end), zone)
            )
            val impact = it.getField<Float>("impact")!!
            start to TrackingEntry(subPeriod, TrackingDataType.Emissions, impact)
        }
        .let { TrackingDataHelpers.fillGapsOrdered(period, granularity, it) }
        .let {
            val collection = db.collection("activities")
            val uid = GreenTraceProviders.userProvider!!.uid()!!
            val initialQuery =
                if (cid.isEmpty()) collection.whereEqualTo("user", uid)
                else collection.whereArrayContains("communities", cid)
            val recurring = activityQueryHelper(
                initialQuery.whereNotEqualTo("schedule", null),
                period
            )
            TrackingDataHelpers.applyRecurringImpacts(recurring, it)
        }

    suspend fun activityQueryHelper(baseQuery: Query, period: TrackingPeriod) =
        baseQuery
            .whereLessThanOrEqualTo("date", period.end.toEpochSecond())
            //.orderBy("date")
            .get()
            .await()
            .documents
            .map {
                val type = it.getField<String>("type")!!
                (it.get("data", Class.forName(type))!! as TrackingActivity)
                    .apply {
                        id = it.id
                        val zone = it.getField<String>("date_zone").let(ZoneId::of)
                        date = it.getField<Long>("date")!!.let { epoch ->
                            ZonedDateTime.ofInstant(Instant.ofEpochSecond(epoch), zone)
                        }
                        impact = it.getField<Float>("impact")!!
                        it.getField<RecurrenceSchedule.Raw>("schedule")?.let { rec ->
                            schedule = RecurrenceSchedule(rec, zone)
                        }
                    }
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
        val nonRecurring = activityQueryHelper(
            initialQuery
                .whereEqualTo("schedule", null)
                .whereGreaterThanOrEqualTo("date", period.start.toEpochSecond()),
            period
        )
        return activityQueryHelper(
            initialQuery.whereNotEqualTo("schedule", null), period)
            .flatMap { TrackingDataHelpers.expandRecurringActivity(it, period) }
            // we really should be using the merge step from merge sort here
            // and just use orderBy for nonRecurring, and manually sort "expanded"
            .plus(nonRecurring)
            .sortedBy { it.date }
    }
}