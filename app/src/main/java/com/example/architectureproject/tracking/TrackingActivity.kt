package com.example.architectureproject.tracking

import com.google.firebase.firestore.Exclude
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import com.patrykandpatrick.vico.core.extension.sumOf

class RecurrenceSchedule(val unit: TrackingDataGranularity, val period: Int, val endDate: ZonedDateTime? = null) {
    private constructor() : this(TrackingDataGranularity.Day, 0, ZonedDateTime.now())
    data class Raw(val unit: TrackingDataGranularity, val period: Int, val endDate: Long? = null) {
        constructor(base: RecurrenceSchedule) : this(base.unit, base.period, base.endDate?.toEpochSecond())
        private constructor() : this(RecurrenceSchedule())
    }

    constructor(raw: Raw, zone: ZoneId) : this(
        raw.unit,
        raw.period,
        raw.endDate?.let { ZonedDateTime.ofInstant(Instant.ofEpochSecond(it), zone) })

    fun periodSeconds() = unit.seconds() * period
}

abstract class TrackingActivity(date: ZonedDateTime, open val name: String, id: String, schedule: RecurrenceSchedule?) {
    private constructor() : this(ZonedDateTime.now(), "", "", null)
    open var id = id
        internal set

    @Exclude
    @get:Exclude
    open var date = date

    @Exclude
    @get:Exclude
    open var schedule = schedule
        internal set

    open var impact: Float = Float.NaN
        internal set

    fun isRecurring() = schedule != null
    abstract fun copy(): TrackingActivity
}

data class Meal(@Exclude @get:Exclude override var date: ZonedDateTime,
                override var name: String,
                val type: Type,
                val contents: List<Entry>,
                @Exclude @get:Exclude override var schedule: RecurrenceSchedule? = null,
                override var id: String = ""
):
    TrackingActivity(date, name, id, schedule) {
        private constructor() : this(ZonedDateTime.now(), "", Type.Breakfast, listOf())
        enum class Type { Breakfast, Lunch, Dinner }
        data class Entry(val type: Type, val quantity: Float) {
            enum class Type { Meat, Dairy, Poultry, Egg, Fish, Vegetable, Fruit, Grain }
            private constructor() : this(Type.Fruit, 0.0f)
        }

    override fun copy() = copy(id = id)
}

data class Transportation(
    @Exclude @get:Exclude override var date: ZonedDateTime,
    override val name: String,
    val stops: List<Stop>,
    val mode: Mode,
    @Exclude @get:Exclude override var schedule: RecurrenceSchedule? = null,
    override var id: String = ""
) :
    TrackingActivity(date, name, id, schedule) {
    private constructor() : this(ZonedDateTime.now(), "", listOf(), Mode.Walk)

    enum class Mode { Car, Bus, Walk, Bicycle, Train, Plane, Boat, LRT }
    data class Stop(val name: String, val long: Double, val lat: Double) {
        private constructor() : this("", 0.0, 0.0)
    }

    override fun copy() = copy(id = id)

    /*fun calculateTotalDistance(stops: List<Stop>): Double {
        if (stops.size < 2) return 0.0

        val earthRadius = 6371 // Radius of the earth in kilometers

        fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
            val dLat = Math.toRadians(lat2 - lat1)
            val dLon = Math.toRadians(lon2 - lon1)
            val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                    Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                    Math.sin(dLon / 2) * Math.sin(dLon / 2)
            val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
            return earthRadius * c
        }

        var totalDistance = 0.0
        for (i in 0 until stops.size - 1) {
            totalDistance += haversine(
                stops[i].lat, stops[i].long,
                stops[i + 1].lat, stops[i + 1].long
            )
        }

        return totalDistance
    }*/
}

data class Purchase(
    @Exclude @get:Exclude override var date: ZonedDateTime,
    override val name: String,
    val plasticBag: Boolean,
    @Exclude @get:Exclude override var schedule: RecurrenceSchedule? = null,
    val source: Source = Source.InStore,
    override var id: String = ""
) : TrackingActivity(date, name, id, schedule) {
    private constructor() : this(ZonedDateTime.now(), "", false)

    enum class Source { InStore, Online, SecondHand }

    override fun copy() = copy(id = id)
}