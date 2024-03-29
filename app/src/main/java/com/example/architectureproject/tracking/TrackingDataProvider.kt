package com.example.architectureproject.tracking

interface TrackingDataProvider {
    suspend fun addActivity(activity: TrackingActivity): String
    suspend fun viewActivity(id: String): TrackingActivity
    suspend fun editActivity(id: String, new: TrackingActivity?)
    suspend fun getImpact(
        period: TrackingPeriod,
        granularity: TrackingDataGranularity,
        cid: String = ""
    ): List<TrackingEntry>

    suspend fun getActivities(
        period: TrackingPeriod,
        cid: String = ""
    ): List<TrackingActivity>
}