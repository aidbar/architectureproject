package com.example.architectureproject.tracking

import com.google.firebase.firestore.Exclude
import java.time.ZonedDateTime

open class TrackingActivity(date: ZonedDateTime, val name: String, id: String) {
    var id = id
        internal set

    @Exclude
    @get:Exclude
    @set:Exclude
    var date = date
        internal set
}

class Meal(date: ZonedDateTime,
           name: String,
           val type: Type,
           val contents: List<Entry>,
           id: String = ""
    ):

    TrackingActivity(date, name, id) {
        enum class Type { Breakfast, Lunch, Dinner }
        data class Entry(val type: Type, val quantity: Float) {
            enum class Type { Meat, Dairy, Poultry, Egg, Fish, Vegetable, Fruit, Grain }
        }
}

class Transportation(date: ZonedDateTime,
                     name: String,
                     val stops: List<Stop>,
                     val mode: Mode,
                     id: String = ""
):
    TrackingActivity(date, name, id) {
    enum class Mode { Car, Bus, Walk, Bicycle, Train, Plane, Boat, LRT }
    data class Stop(val name: String, val long: Double, val lat: Double)
}

class Purchase(date: ZonedDateTime,
               name: String,
               val plasticBag: Boolean,
               id: String = ""): TrackingActivity(date, name, id) {
    enum class Source { New, SecondHand, Refurbished }
}