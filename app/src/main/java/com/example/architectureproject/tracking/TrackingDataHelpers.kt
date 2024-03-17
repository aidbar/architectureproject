package com.example.architectureproject.tracking

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
            computeImpact(it).let {
                it.copy(period = periodOperator(granularity)(it.period.start))
            }
        }
            .groupBy { it.period.start.toEpochSecond() }
            .map {
                it.key to it.value[0].copy(value = it.value.fold(0f) { acc, cur ->
                    acc + cur.value
                })
            }
            .let { fillGapsOrdered(period, granularity, it) }
}