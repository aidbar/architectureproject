package com.example.architectureproject.tracking

abstract class TrackingImpactProvider {
    abstract suspend fun computeImpact(activity: TrackingActivity): TrackingEntry
}
