package com.example.architectureproject.tracking

import com.example.architectureproject.GreenTraceProviders
import com.patrykandpatrick.vico.core.extension.sumOf

class BasicTrackingImpactProvider : TrackingImpactProvider() {
    private fun computeMealEmissions(meal: Meal): Float {
//        units in kg
// source: https://www.researchgate.net/figure/Carbon-footprint-kg-CO2eq-of-different-food-groups-per-kg-food-in-the-supermarket-and_fig1_343864236
        val carbonFootprintPerType = mapOf(
            Meal.Entry.Type.Meat to 12f,
            Meal.Entry.Type.Dairy to 10f,
            Meal.Entry.Type.Poultry to 5f,
            Meal.Entry.Type.Egg to 3f,
            Meal.Entry.Type.Fish to 4.8f,
            Meal.Entry.Type.Vegetable to 0.6f,
            Meal.Entry.Type.Fruit to 0.6f,
            Meal.Entry.Type.Grain to 1.4f
        )
        return meal.contents.sumOf { entry ->
            (carbonFootprintPerType[entry.type] ?: 0f) * entry.quantity
        }
    }

    private fun computeTransportationEmissions(transport: Transportation): Float {
        val dist = GreenTraceProviders.mapProvider.computeDistance(transport.stops)
    //    trip unit in km
//    source: https://www.visualcapitalist.com/comparing-the-carbon-footprint-of-transportation-options/
        val carbonFootprintPerType = mapOf(
            Transportation.Mode.Car to 0.192f,
            Transportation.Mode.Bus to 0.105f,
            Transportation.Mode.Walk to 0f,
            Transportation.Mode.Bicycle to 0f,
            Transportation.Mode.Train to 0.041f,
            Transportation.Mode.Plane to 0.255f,
            Transportation.Mode.Boat to 0.019f,
            Transportation.Mode.LRT to 0.041f,
        )
        return dist * (carbonFootprintPerType[transport.mode] ?: 0f) * 1000
    }

    private fun computePurchaseEmissions(purchase: Purchase): Float {
        val carbonFootprintPerType = mapOf(
            Purchase.Source.InStore to 1.6f,
            Purchase.Source.Online to 1.4f,
            Purchase.Source.SecondHand to 0.4f
        )

        // https://news.climate.columbia.edu/2020/04/30/plastic-paper-cotton-bags/
        // the source above claims 0.04 tons of CO2 per 1500 bags
        val footprintForBag = 0.04f * 1e6f / 1500
        return (carbonFootprintPerType[purchase.source] ?: 0f) * 1000 +
                (if (purchase.plasticBag) footprintForBag else 0f)
    }

    override suspend fun computeImpact(activity: TrackingActivity): TrackingEntry {
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