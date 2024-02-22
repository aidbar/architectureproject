package com.example.architectureproject

import java.time.ZonedDateTime

// Represents period of time data is fetched for
// Period is start and end exclusive
data class TrackingPeriod(val start: ZonedDateTime, val end: ZonedDateTime) {
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

        fun thisMonth() = monthOf(ZonedDateTime.now())
        fun today() = dayOf(ZonedDateTime.now())
        fun thisWeek() = weekOf(ZonedDateTime.now())
        fun thisYear() = yearOf(ZonedDateTime.now())

        fun pastMonth(): TrackingPeriod {
            val now = ZonedDateTime.now()
            return TrackingPeriod(now.minusMonths(1), now)
        }

        fun pastWeek(): TrackingPeriod {
            val now = ZonedDateTime.now()
            return TrackingPeriod(now.minusWeeks(1), now)
        }

        fun pastYear(): TrackingPeriod {
            val now = ZonedDateTime.now()
            return TrackingPeriod(now.minusYears(1), now)
        }
    }
}

// Represents Granularity of data fetched
// For example, Day means fetch one value for every day
//              Month means aggregate daily values, return 1 value for each month
enum class TrackingDataGranularity {
    Day, Week, Month
}

enum class TrackingDataType {
    Emissions // in equivalent grams of CO2
}

data class TrackingEntry(val period: TrackingPeriod, val type: TrackingDataType, val value: Float) {
    fun ecoPositive(): Boolean =
        value < 0
}