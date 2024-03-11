package com.example.architectureproject.tracking

import java.time.ZonedDateTime

open class TrackingActivity(val date: ZonedDateTime, val name: String, private val impactProvider: TrackingImpactProvider, val id: String) {
    private var cachedImpact: TrackingEntry? = null
    fun impact(): TrackingEntry {
        if (!impactProvider.needsCache) {
            return computeImpact()
        }

        if (cachedImpact != null) {
            return cachedImpact as TrackingEntry
        }

        cachedImpact = computeImpact()
        return cachedImpact as TrackingEntry
    }

    internal fun impact(value: TrackingEntry) { cachedImpact = value }

    fun ecoPositive() = impact().ecoPositive()

    protected fun computeImpact(): TrackingEntry =
        impactProvider.computeImpact(this)
}

class Meal(date: ZonedDateTime,
           name: String,
           impactProvider: TrackingImpactProvider,
           val type: Type,
           val contents: List<Entry>,
           id: String = ""
    ):

    TrackingActivity(date, name, impactProvider, id) {
        enum class Type { Breakfast, Lunch, Dinner }
        data class Entry(val type: Type, val quantity: Float) {
            enum class Type { Meat, Dairy, Poultry, Egg, Fish, Vegetable, Fruit, Grain }
        }
}

class Transportation(date: ZonedDateTime,
                     name: String,
                     impactProvider: TrackingImpactProvider,
                     val stops: List<Stop>,
                     val mode: Mode,
                     id: String = ""
):
    TrackingActivity(date, name, impactProvider, id) {
    enum class Mode { Car, Bus, Walk, Bicycle, Train, Plane, Boat, LRT }
    data class Stop(val name: String, val long: Double, val lat: Double)
}

class Purchase(date: ZonedDateTime,
               name: String,
               impactProvider: TrackingImpactProvider,
               val plasticBag: Boolean,
               id: String = ""): TrackingActivity(date, name, impactProvider, id) {
    enum class Source { New, SecondHand, Refurbished }
}