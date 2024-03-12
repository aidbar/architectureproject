package com.example.architectureproject.tracking.demo

import com.example.architectureproject.GreenTraceProviders
import com.example.architectureproject.tracking.TrackingActivity
import com.example.architectureproject.tracking.TrackingDataGranularity
import com.example.architectureproject.tracking.TrackingDataHelpers
import com.example.architectureproject.tracking.TrackingDataProvider
import com.example.architectureproject.tracking.TrackingEntry
import com.example.architectureproject.tracking.TrackingPeriod
import java.time.ZonedDateTime
import java.util.TreeMap
import java.util.UUID

class DummyTrackingDataProvider : TrackingDataProvider {
    private val activities = hashMapOf<String, TrackingActivity>()
    private val activitiesByDay = TreeMap<Int, HashMap<String, TrackingActivity>>()

    companion object {
        private const val SECONDS_PER_DAY: Long = 24 * 60 * 60
        private fun dayOf(date: ZonedDateTime): Int =
            (date.toEpochSecond() / SECONDS_PER_DAY).toInt()
    }

    override suspend fun addActivity(activity: TrackingActivity): String {
        val uuid = UUID.randomUUID().toString()
        activities[uuid] = activity

        val day = dayOf(activity.date)
        val todayActivities = activitiesByDay.getOrDefault(day, hashMapOf())
        todayActivities[uuid] = activity
        activitiesByDay[day] = todayActivities

        return uuid
    }

    override suspend fun viewActivity(id: String): TrackingActivity {
        return activities[id] as TrackingActivity
    }

    override suspend fun editActivity(id: String, new: TrackingActivity?) {
        if (new == null) {
            // delete
            activitiesByDay[dayOf(activities[id]!!.date)]!!.remove(id)
            activities.remove(id)
            return
        }

        editActivity(id, null)
        activities[id] = new
        activitiesByDay[dayOf(new.date)]!![id] = new
    }

    override suspend fun getImpact(
        period: TrackingPeriod,
        granularity: TrackingDataGranularity,
        cid: String
    ): List<TrackingEntry> =
        getActivities(period, cid)
            .let { TrackingDataHelpers.aggregateImpact(
                it, GreenTraceProviders.impactProvider::computeImpact,
                period, granularity
            ) }

    override suspend fun getActivities(period: TrackingPeriod, cid: String) =
        if (cid.isEmpty()) {
            activitiesByDay.tailMap(dayOf(period.start))
                .headMap(dayOf(period.end))
                .flatMap {
                    it.value.map { it.value }
                }
        } else {
            TODO("unimplemented for communities")
        }
}
