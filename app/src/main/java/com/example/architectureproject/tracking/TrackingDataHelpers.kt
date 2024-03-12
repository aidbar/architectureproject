package com.example.architectureproject.tracking

object TrackingDataHelpers {
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
    suspend fun aggregateImpact(
        activities: List<TrackingActivity>,
        computeImpact: suspend (TrackingActivity) -> TrackingEntry,
        period: TrackingPeriod,
        granularity: TrackingDataGranularity
    ) =
        activities.map {
            computeImpact(it).let {
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
}