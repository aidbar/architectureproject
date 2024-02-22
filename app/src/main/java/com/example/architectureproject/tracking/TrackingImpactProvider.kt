package com.example.architectureproject.tracking

abstract class TrackingImpactProvider(val needsCache: Boolean) {
    abstract fun computeImpact(activity: TrackingActivity): TrackingEntry
}
