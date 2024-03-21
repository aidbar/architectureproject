package com.example.architectureproject.tracking

import java.time.Period
import java.time.ZonedDateTime

object TrackingDataHelpers {
    private fun periodOperator(granularity: TrackingDataGranularity) =
        when (granularity) {
            TrackingDataGranularity.Day -> TrackingPeriod::dayOf
            TrackingDataGranularity.Week -> TrackingPeriod::weekOf
            TrackingDataGranularity.Month -> TrackingPeriod::monthOf
            TrackingDataGranularity.Year -> TrackingPeriod::yearOf
        }

    fun fillGapsOrdered(
        period: TrackingPeriod,
        granularity: TrackingDataGranularity,
        pairs: Iterable<Pair<Long, TrackingEntry>>
    ): List<TrackingEntry> {
        val periodOp = periodOperator(granularity)
        var cur = periodOp(period.start).start
        val periods = linkedMapOf<Long, TrackingEntry>()
        while (cur < period.end) {
            periods[cur.toEpochSecond()] =
                TrackingEntry(periodOp(cur), TrackingDataType.Emissions, 0f)
            cur = when (granularity) {
                TrackingDataGranularity.Day -> cur::plusDays
                TrackingDataGranularity.Week -> cur::plusWeeks
                TrackingDataGranularity.Month -> cur::plusMonths
                TrackingDataGranularity.Year -> cur::plusYears
            }(1)
        }

        periods.putAll(pairs)
        return periods.map { it.value }
    }

    suspend fun aggregateImpact(
        activities: List<TrackingActivity>,
        computeImpact: suspend (TrackingActivity) -> TrackingEntry,
        period: TrackingPeriod,
        granularity: TrackingDataGranularity
    ) =
        activities.map {
            computeImpact(it).let { entry ->
                entry.copy(period = periodOperator(granularity)(entry.period.start))
            }
        }
            .groupBy { it.period.start.toEpochSecond() }
            .map {
                it.key to it.value[0].copy(value = it.value.fold(0f) { acc, cur ->
                    acc + cur.value
                })
            }
            .let { fillGapsOrdered(period, granularity, it) }
    private fun findFirstInstanceDayOrWeek(start: ZonedDateTime, schedule: RecurrenceSchedule, periodStart: ZonedDateTime): ZonedDateTime {
        // the concept of days and weeks is a fixed duration in UTC time (and also unix epoch seconds)
        // use the fast and simple path for these computations
        val deltaSecs = periodStart.toEpochSecond() - start.toEpochSecond()
        val periodSecs = schedule.period * schedule.unit.seconds()
        val alignedDelta = (periodSecs - deltaSecs % periodSecs) % periodSecs + deltaSecs
        return start.plusSeconds(alignedDelta)
    }

    fun findFirstInstance(start: ZonedDateTime, schedule: RecurrenceSchedule, periodStart: ZonedDateTime): ZonedDateTime {
        if (schedule.unit == TrackingDataGranularity.Day || schedule.unit == TrackingDataGranularity.Week)
            return findFirstInstanceDayOrWeek(start, schedule, periodStart)

        val between = Period.between(start.toLocalDate(), periodStart.toLocalDate())
        if (schedule.unit == TrackingDataGranularity.Year)
            throw UnsupportedOperationException("Yearly recurring activities are unsupported")

        TODO("recurring activities: not implemented for Monthly yet")
    }

    fun expandRecurringActivity(activity: TrackingActivity, period: TrackingPeriod): List<TrackingActivity> {
        if (!activity.isRecurring()) return listOf(activity)
        val schedule = activity.schedule!!
        var date =
            if (activity.date >= period.start) activity.date
            else findFirstInstance(activity.date, schedule, period.start)
        val output = mutableListOf<TrackingActivity>()
        while (date < schedule.endDate && date < period.end) {
            val instance = activity.copy().apply { this.date = date }
            output.add(instance)
            date = when (schedule.unit) {
                TrackingDataGranularity.Day -> date::plusDays
                TrackingDataGranularity.Week -> date::plusWeeks
                TrackingDataGranularity.Month -> date::plusMonths
                TrackingDataGranularity.Year -> date::plusYears
            }(1)
        }

        return output
    }
}