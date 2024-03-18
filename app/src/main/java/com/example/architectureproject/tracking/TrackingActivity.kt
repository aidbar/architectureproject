package com.example.architectureproject.tracking

import com.google.firebase.firestore.Exclude
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

class Meal(date: ZonedDateTime,
           name: String,
           val type: Type,
           val contents: List<Entry>,
           id: String = ""
):
    TrackingActivity(date, name, id) {
        private constructor() : this(ZonedDateTime.now(), "", Type.Breakfast, listOf())
        enum class Type { Breakfast, Lunch, Dinner }
        data class Entry(val type: Type, val quantity: Float) {
            enum class Type { Meat, Dairy, Poultry, Egg, Fish, Vegetable, Fruit, Grain }
            private constructor() : this(Type.Fruit, 0.0f)
        }
    }

class Transportation(date: ZonedDateTime,
                     name: String,
                     val stops: List<Stop>,
                     val mode: Mode,
                     id: String = ""
):
    TrackingActivity(date, name, id) {
    private constructor() : this(ZonedDateTime.now(), "", listOf(), Mode.Walk)
    enum class Mode { Car, Bus, Walk, Bicycle, Train, Plane, Boat, LRT }
    data class Stop(val name: String, val long: Double, val lat: Double) {
        private constructor() : this("", 0.0, 0.0)
    }
}

class Purchase(date: ZonedDateTime,
               name: String,
               val plasticBag: Boolean,
               id: String = ""): TrackingActivity(date, name, id) {
    private constructor() : this(ZonedDateTime.now(), "", false)
    enum class Source { New, SecondHand, Refurbished }
}