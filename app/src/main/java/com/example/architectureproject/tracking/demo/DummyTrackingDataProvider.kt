package com.example.architectureproject.tracking.demo

import com.example.architectureproject.community.CommunityInfo
import com.example.architectureproject.community.CommunityManager
import com.example.architectureproject.profile.User
import com.example.architectureproject.tracking.TrackingActivity
import com.example.architectureproject.tracking.TrackingDataGranularity
import com.example.architectureproject.tracking.TrackingDataProvider
import com.example.architectureproject.tracking.TrackingDataType
import com.example.architectureproject.tracking.TrackingEntry
import com.example.architectureproject.tracking.TrackingPeriod
import java.time.ZonedDateTime
import java.util.TreeMap
import java.util.UUID

class DummyTrackingDataProvider(val user: User, val communityManager: CommunityManager) : TrackingDataProvider {
    private val activities = hashMapOf<String, TrackingActivity>()
    private val activitiesByDay = TreeMap<Int, HashMap<String, TrackingActivity>>()
    private val communities = hashSetOf<String>()

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

    private fun periodOperator(granularity: TrackingDataGranularity) =
        when (granularity) {
            TrackingDataGranularity.Day -> TrackingPeriod::dayOf
            TrackingDataGranularity.Week -> TrackingPeriod::weekOf
            TrackingDataGranularity.Month -> TrackingPeriod::monthOf
            TrackingDataGranularity.Year -> TrackingPeriod::yearOf
        }

    private fun fillGapsOrdered(period: TrackingPeriod,
                         granularity: TrackingDataGranularity,
                         map: Map<Long, List<TrackingEntry>>): Map<Long, List<TrackingEntry>> {
        val periodOp = periodOperator(granularity)
        var cur = periodOp(period.start).start
        val periods = linkedMapOf<Long, List<TrackingEntry>>()
        while (cur < period.end) {
            periods[cur.toEpochSecond()] = listOf(
                TrackingEntry(periodOp(cur), TrackingDataType.Emissions, 0f))
            cur = when (granularity) {
                TrackingDataGranularity.Day -> cur::plusDays
                TrackingDataGranularity.Week -> cur::plusWeeks
                TrackingDataGranularity.Month -> cur::plusMonths
                TrackingDataGranularity.Year -> cur::plusYears
            }(1)
        }

        periods.putAll(map)
        return periods
    }

    override suspend fun getImpact(
        period: TrackingPeriod,
        granularity: TrackingDataGranularity
    ): List<TrackingEntry> =
        getActivities(period)
            .map {
                it.impact().let {
                    it.copy(period = periodOperator(granularity)(it.period.start))
                }
            }
            .groupBy { it.period.start.toEpochSecond() }
            .let { fillGapsOrdered(period, granularity, it) }
            .map {
                it.value[0].copy(value = it.value.fold(0f) { acc, cur ->
                    acc + cur.value
                })
            }

    override suspend fun getActivities(period: TrackingPeriod) =
        activitiesByDay.tailMap(dayOf(period.start))
            .headMap(dayOf(period.end))
            .flatMap {
                it.value.map { it.value }
            }

    override suspend fun attachCommunity(id: String) {
        communities.add(id)
        communityManager.addUserToCommunity(user, id)
    }

    override suspend fun detachCommunity(id: String) {
        communities.remove(id)
        communityManager.removeUserFromCommunity(user, id)
    }

    override suspend fun getCommunities() =
        communities.map { communityManager.getCommunityById(it)!! }
}