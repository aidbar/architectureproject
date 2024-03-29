package com.example.architectureproject.tracking

import java.time.Period
import java.time.ZonedDateTime
import kotlin.math.min

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
        val periodSecs = schedule.periodSeconds()
        val alignedDelta = (periodSecs - deltaSecs % periodSecs) % periodSecs + deltaSecs
        return start.plusSeconds(alignedDelta)
    }

    private fun findFirstInstance(start: ZonedDateTime, schedule: RecurrenceSchedule, periodStart: ZonedDateTime): ZonedDateTime {
        if (schedule.unit == TrackingDataGranularity.Day || schedule.unit == TrackingDataGranularity.Week)
            return findFirstInstanceDayOrWeek(start, schedule, periodStart)

        if (schedule.unit == TrackingDataGranularity.Year)
            throw UnsupportedOperationException("Yearly recurring activities are unsupported")

        // TODO: monthly is broken still
        val between = Period.between(start.toLocalDate(), periodStart.toLocalDate())
        val delta = between.months + if (between.days > 0) 1 else 0
        val alignedDelta = (schedule.period - delta % schedule.period) % schedule.period + delta
        return start.plusMonths(alignedDelta.toLong())
    }

    fun expandRecurringActivity(activity: TrackingActivity, period: TrackingPeriod): List<TrackingActivity> {
        if (!activity.isRecurring()) return listOf(activity)
        val schedule = activity.schedule!!
        var date =
            if (activity.date >= period.start) activity.date
            else findFirstInstance(activity.date, schedule, period.start)
        val output = mutableListOf<TrackingActivity>()
        while (schedule.endDate?.let { date < it } != false && date < period.end) {
            val instance = activity.copy().apply { this.date = date; this.impact = activity.impact }
            output.add(instance)
            date = when (schedule.unit) {
                TrackingDataGranularity.Day -> date::plusDays
                TrackingDataGranularity.Week -> date::plusWeeks
                TrackingDataGranularity.Month -> date::plusMonths
                TrackingDataGranularity.Year -> date::plusYears
            }(schedule.period.toLong())
        }

        return output
    }

    private fun countOccurrencesDuring(recurring: TrackingActivity, period: TrackingPeriod): Int {
        val schedule = recurring.schedule!!
        val first = findFirstInstance(recurring.date, schedule, period.start)
        // TODO: this won't work for Monthly
        val end = min(
            period.end.toEpochSecond(),
            schedule.endDate?.toEpochSecond() ?: Long.MAX_VALUE
        ) - 1
        val length = end - first.toEpochSecond()
        if (length < 0) return 0

        return (length / schedule.periodSeconds()).toInt() + 1
    }

    fun applyRecurringImpacts(recurring: Iterable<TrackingActivity>, entries: Iterable<TrackingEntry>) =
        entries.map { entry ->
            val addedImpact = recurring.sumOf {
                val count = countOccurrencesDuring(it, entry.period)
                count * it.impact.toDouble()
            }

            entry.copy(value = entry.value + addedImpact.toFloat())
        }
}