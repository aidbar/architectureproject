package com.example.architectureproject.tracking.demo

import com.example.architectureproject.tracking.TrackingActivity
import com.example.architectureproject.tracking.TrackingDataGranularity
import com.example.architectureproject.tracking.TrackingDataProvider
import com.example.architectureproject.tracking.TrackingEntry
import com.example.architectureproject.tracking.TrackingPeriod
import java.time.ZonedDateTime
import java.util.TreeMap
import java.util.UUID

class DummyTrackingDataProvider : TrackingDataProvider {
    val activities = hashMapOf<String, TrackingActivity>()
    val activitiesByDay = TreeMap<Int, HashMap<String, TrackingActivity>>()

    companion object {
        val SECONDS_PER_DAY: Long = 24 * 60 * 60
        private fun dayOf(date: ZonedDateTime): Int =
            (date.toEpochSecond() / SECONDS_PER_DAY).toInt()
    }

    override fun addActivity(activity: TrackingActivity): String {
        val uuid = UUID.randomUUID().toString()
        activities[uuid] = activity

        val day = dayOf(activity.date)
        val todaysActivities = activitiesByDay.getOrDefault(day, hashMapOf())
        todaysActivities[uuid] = activity
        activitiesByDay[day] = todaysActivities

        return uuid
    }

    override fun viewActivity(id: String): TrackingActivity {
        return activities[id] as TrackingActivity
    }

    override fun editActivity(id: String, new: TrackingActivity?) {
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

    override fun getImpact(
        period: TrackingPeriod,
        granularity: TrackingDataGranularity
    ): List<TrackingEntry> =
        getActivities(period)
            .map {
                it.impact().let {
                    it.copy(period = when (granularity) {
                        TrackingDataGranularity.Day -> TrackingPeriod::dayOf
                        TrackingDataGranularity.Week -> TrackingPeriod::weekOf
                        TrackingDataGranularity.Month -> TrackingPeriod::monthOf
                    }(it.period.start))
                }
            }
            .groupBy { it.period.start.toEpochSecond() }
            .map {
                it.value[0].copy(value = it.value.fold(0f) { acc, cur ->
                    acc + cur.value
                })
            }

    override fun getActivities(period: TrackingPeriod) =
        activitiesByDay.tailMap(dayOf(period.start))
            .headMap(dayOf(period.end))
            .flatMap {
                it.value.map { it.value }
            }
}