package com.example.architectureproject

interface TrackingDataProvider {
    fun addActivity(activity: TrackingActivity): String
    fun viewActivity(id: String): TrackingActivity
    fun editActivity(id: String, new: TrackingActivity)
    fun getImpact(period: TrackingPeriod, granularity: TrackingDataGranularity): List<TrackingEntry>
    fun getActivities(period: TrackingPeriod): List<TrackingActivity>
}