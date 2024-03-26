package com.example.architectureproject.tracking.demo

import com.example.architectureproject.tracking.Meal
import com.example.architectureproject.tracking.TrackingDataProvider
import com.example.architectureproject.tracking.Transportation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.time.ZonedDateTime

class DummyTrackingData {
    val activities = listOf(
        Meal(ZonedDateTime.now(), "Breakfast", Meal.Type.Breakfast, listOf()),
        Meal(ZonedDateTime.now(), "Lunch", Meal.Type.Lunch, listOf()),
        Transportation(ZonedDateTime.now().minusMonths(3), "Driving to work",
            listOf(
                Transportation.Stop("Home", 43.57588850506251, -79.61096747795044),
                Transportation.Stop("GO Station", 43.595679311751454, -79.64867577582866)
            ), Transportation.Mode.Car)
    )

    suspend fun addTo(provider: TrackingDataProvider) {
        coroutineScope {
            activities.map { async(Dispatchers.Default) { provider.addActivity(it) } }
                .forEach { it.await() }
        }
    }
}