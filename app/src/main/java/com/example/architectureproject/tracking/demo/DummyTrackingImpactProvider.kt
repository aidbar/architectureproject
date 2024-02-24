package com.example.architectureproject.tracking.demo

import com.example.architectureproject.MapProvider
import com.example.architectureproject.tracking.Meal
import com.example.architectureproject.tracking.Purchase
import com.example.architectureproject.tracking.TrackingActivity
import com.example.architectureproject.tracking.TrackingDataType
import com.example.architectureproject.tracking.TrackingEntry
import com.example.architectureproject.tracking.TrackingImpactProvider
import com.example.architectureproject.tracking.TrackingPeriod
import com.example.architectureproject.tracking.Transportation
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sqrt
import kotlin.random.Random

class DummyMapProvider : MapProvider {
    // https://stackoverflow.com/questions/27928/calculate-distance-between-two-latitude-longitude-points-haversine-formula
    private fun distance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // km
        val p = Math.PI / 180.0

        val a = 0.5 - cos((lat2 - lat1) * p) / 2.0
        + cos(lat1 * p) * cos(lat2 * p) *
                (1.0 - cos((lon2 - lon1) * p)) / 2

        return 2.0 * r * asin(sqrt(a))
    }

    override fun computeDistance(src: Transportation.Stop, dst: Transportation.Stop): Float {
        return distance(src.lat, src.long, dst.lat, dst.long).toFloat()
    }

}

class DummyTrackingImpactProvider : TrackingImpactProvider(false) {
    private val map: MapProvider = DummyMapProvider()
    private fun computeMealEmissions(@Suppress("UNUSED_PARAMETER") meal: Meal): Float {
        return 2300f;
    }

    private fun computeTransportationEmissions(transport: Transportation): Float {
        return 400f * map.computeDistance(transport.stops)
    }

    private fun computePurchaseEmissions(purchase: Purchase): Float {
        return Random.nextFloat() * 1000f + (if (purchase.plasticBag) 6920f else 0f)
    }

    override fun computeImpact(activity: TrackingActivity): TrackingEntry {
        val period = TrackingPeriod.instantOf(activity.date)
        val emissions = when (activity) {
            is Meal -> computeMealEmissions(activity)
            is Transportation -> computeTransportationEmissions(activity)
            is Purchase -> computePurchaseEmissions(activity)
            else -> throw UnsupportedOperationException("unsupported activity type $activity")
        }

        return TrackingEntry(period, TrackingDataType.Emissions, emissions)
    }
}