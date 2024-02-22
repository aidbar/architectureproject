package com.example.architectureproject

abstract class TrackingImpactProvider(val needsCache: Boolean) {
    abstract fun computeImpact(activity: TrackingActivity): TrackingEntry
}
