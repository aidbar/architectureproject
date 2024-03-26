package com.example.architectureproject.tracking

import com.google.firebase.firestore.Exclude
import com.patrykandpatrick.vico.core.extension.sumOf
import java.time.ZonedDateTime

abstract class TrackingActivity(date: ZonedDateTime, val name: String, id: String) {
    private constructor() : this(ZonedDateTime.now(), "", "")

    var id = id
        internal set

    @Exclude
    @get:Exclude
    @set:Exclude
    var date = date
        internal set

    var impact: Float = Float.NaN
        internal set
}

class Meal(
    date: ZonedDateTime,
    name: String,
    val type: Type,
    val contents: List<Entry>,
    id: String = ""
) :
    TrackingActivity(date, name, id) {
    private constructor() : this(ZonedDateTime.now(), "", Type.Breakfast, listOf())

    enum class Type { Breakfast, Lunch, Dinner }
    data class Entry(val type: Type, val quantity: Float) {
        enum class Type { Meat, Dairy, Poultry, Egg, Fish, Vegetable, Fruit, Grain }

        private constructor() : this(Type.Fruit, 0.15f)
    }

    fun computeCarbonFootprint(): Float {
//        units in kg
// source: https://www.researchgate.net/figure/Carbon-footprint-kg-CO2eq-of-different-food-groups-per-kg-food-in-the-supermarket-and_fig1_343864236
        val carbonFootprintPerType = mapOf(
            Entry.Type.Meat to 12f,
            Entry.Type.Dairy to 10f,
            Entry.Type.Poultry to 5f,
            Entry.Type.Egg to 3f,
            Entry.Type.Fish to 4.8f,
            Entry.Type.Vegetable to 0.6f,
            Entry.Type.Fruit to 0.6f,
            Entry.Type.Grain to 1.4f
        )
        return contents.sumOf { entry ->
            (carbonFootprintPerType[entry.type] ?: 0f) * entry.quantity
        }
    }
}

class Transportation(
    date: ZonedDateTime,
    name: String,
    val stops: List<Stop>,
    val mode: Mode,
    id: String = ""
) :
    TrackingActivity(date, name, id) {
    private constructor() : this(ZonedDateTime.now(), "", listOf(), Mode.Walk)

    enum class Mode { Car, Bus, Walk, Bicycle, Train, Plane, Boat, LRT }
    data class Stop(val name: String, val long: Double, val lat: Double) {
        private constructor() : this("", 0.0, 0.0)
    }
    fun calculateTotalDistance(stops: List<Stop>): Double {
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
    }
//    trip unit in km
//    source: https://www.visualcapitalist.com/comparing-the-carbon-footprint-of-transportation-options/
    fun computeCarbonFootprint(): Float {
        val carbonFootprintPerType = mapOf(
            Mode.Car to 0.192f,
            Mode.Bus to 0.105f,
            Mode.Walk to 0f,
            Mode.Bicycle to 0f,
            Mode.Train to 0.041f,
            Mode.Plane to 0.255f,
            Mode.Boat to 0.019f,
            Mode.LRT to 0.041f,
        )
    return calculateTotalDistance(stops).toFloat() * (carbonFootprintPerType[mode]?:0f)
}
}

class Purchase(
    date: ZonedDateTime,
    name: String,
    val plasticBag: Boolean,
    id: String,
    val source: Source
) : TrackingActivity(date, name, id) {
    private constructor() : this(ZonedDateTime.now(), "", false,"", Source.InStore)

    enum class Source { InStore, Online, SecondHand }
    fun computeCarbonFootprint(): Float {
        val carbonFootprintPerType = mapOf(
            Source.InStore to 1.6f,
            Source.Online to 1.4f,
            Source.SecondHand to 0.4f
        )
        return carbonFootprintPerType[source]?:0f
    }
}