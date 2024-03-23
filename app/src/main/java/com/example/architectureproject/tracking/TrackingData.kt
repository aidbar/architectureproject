package com.example.architectureproject.tracking

import java.time.ZonedDateTime

// Represents period of time data is fetched for
// Period is start inclusive and end exclusive
data class TrackingPeriod(val start: ZonedDateTime, val end: ZonedDateTime) {
    fun shiftPeriods(periodSize: TrackingDataGranularity, count: Long): TrackingPeriod {
        val shiftFunc = when (periodSize) {
            TrackingDataGranularity.Day -> ZonedDateTime::plusDays
            TrackingDataGranularity.Week -> ZonedDateTime::plusWeeks
            TrackingDataGranularity.Month -> ZonedDateTime::plusMonths
            TrackingDataGranularity.Year -> ZonedDateTime::plusYears
        }
        return TrackingPeriod(shiftFunc(start, count), shiftFunc(end, count))
    }

    fun seconds() = end.toEpochSecond() - start.toEpochSecond()

    companion object {
        fun dayOf(day: ZonedDateTime): TrackingPeriod {
            val dayStart = ZonedDateTime.of(
                day.year, day.monthValue, day.dayOfMonth,
                0, 0, 0, 0, day.zone
            )
            val dayEnd = dayStart.plusDays(1);
            return TrackingPeriod(dayStart, dayEnd)
        }

        fun monthOf(day: ZonedDateTime): TrackingPeriod {
            val dayStart = ZonedDateTime.of(
                day.year, day.monthValue, 1,
                0, 0, 0, 0, day.zone
            )

            val dayEnd = dayStart.plusMonths(1)
            return TrackingPeriod(dayStart, dayEnd)
        }

        fun yearOf(day: ZonedDateTime): TrackingPeriod {
            val dayStart = ZonedDateTime.of(
                day.year, 1, 1,
                0, 0, 0, 0, day.zone
            )
            val dayEnd = dayStart.plusYears(1)
            return TrackingPeriod(dayStart, dayEnd)
        }

        fun weekOf(day: ZonedDateTime): TrackingPeriod {
            val dayStart = ZonedDateTime.of(
                day.year, day.monthValue, day.dayOfMonth,
                0, 0, 0, 0, day.zone
            ).minusDays((day.dayOfWeek.value % 7).toLong())
            val dayEnd = dayStart.plusWeeks(1)
            return TrackingPeriod(dayStart, dayEnd)
        }

        fun instantOf(date: ZonedDateTime): TrackingPeriod =
            TrackingPeriod(date, date.plusNanos(1))

        fun thisMonth() = monthOf(ZonedDateTime.now())
        fun today() = dayOf(ZonedDateTime.now())
        fun thisWeek() = weekOf(ZonedDateTime.now())
        fun thisYear() = yearOf(ZonedDateTime.now())

        fun pastMonths(months: Long = 1): TrackingPeriod {
            val now = ZonedDateTime.now()
            return TrackingPeriod(now.minusMonths(months), now.plusDays(1))
        }

        fun pastWeeks(weeks: Long = 1): TrackingPeriod {
            val now = ZonedDateTime.now()
            return TrackingPeriod(now.minusWeeks(weeks), now.plusDays(1))
        }

        fun pastYears(years: Long = 1): TrackingPeriod {
            val now = ZonedDateTime.now()
            return TrackingPeriod(now.minusYears(years), now.plusDays(1))
        }
    }
}

// Represents Granularity of data fetched
// For example, Day means fetch one value for every day
//              Month means aggregate daily values, return 1 value for each month
enum class TrackingDataGranularity {
    Day, Week, Month, Year;
    fun seconds(): Long = when (this) {
        Day -> 24 * 3600
        Week -> 7 * Day.seconds()
        else -> -1
    }
}

enum class TrackingDataType {
    Emissions // in equivalent grams of CO2
}

data class TrackingEntry(val period: TrackingPeriod, val type: TrackingDataType, val value: Float) {
    fun ecoPositive(): Boolean =
        value < 0
}