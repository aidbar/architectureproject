package com.example.architectureproject.tracking.demo

import com.example.architectureproject.tracking.TrackingActivity
import com.example.architectureproject.tracking.TrackingEntry
import com.example.architectureproject.tracking.TrackingImpactProvider

class DummyTrackingImpactProvider : TrackingImpactProvider(false) {
    override fun computeImpact(activity: TrackingActivity): TrackingEntry {
        TODO("Not yet implemented")
    }
}