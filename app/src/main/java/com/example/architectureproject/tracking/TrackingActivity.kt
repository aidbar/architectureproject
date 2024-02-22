package com.example.architectureproject

import java.time.ZonedDateTime

open class TrackingActivity(val date: ZonedDateTime, val name: String, private val impactProvider: TrackingImpactProvider) {
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

    fun computeImpact(): TrackingEntry =
        impactProvider.computeImpact(this)
}

class Meal(date: ZonedDateTime,
           name: String,
           impactProvider: TrackingImpactProvider,
           val type: Type,
           val contents: List<Entry>
    ):

    TrackingActivity(date, name, impactProvider) {
        enum class Type { Breakfast, Lunch, Dinner }
        data class Entry(val type: Type, val quantity: Float) {
            enum class Type { Meat, Dairy, Poultry, Egg, Fish, Vegetable, Fruit, Grain }
        }
}

class Transportation(date: ZonedDateTime,
                     name: String,
                     impactProvider: TrackingImpactProvider,
                     stops: List<Stop>,
                     mode: Mode):
    TrackingActivity(date, name, impactProvider) {
    enum class Mode { Car, Bus, Walk, Bicycle, Train, Plane, Boat, LRT }
    data class Stop(val name: String, val long: Double, val lat: Double)
}

class Purchase(date: ZonedDateTime,
               name: String,
               impactProvider: TrackingImpactProvider,
               val plasticBag: Boolean): TrackingActivity(date, name, impactProvider)