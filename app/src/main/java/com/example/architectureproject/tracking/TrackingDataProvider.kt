package com.example.architectureproject.tracking

import com.example.architectureproject.community.CommunityInfo

interface TrackingDataProvider {
    suspend fun addActivity(activity: TrackingActivity): String
    suspend fun viewActivity(id: String): TrackingActivity
    suspend fun editActivity(id: String, new: TrackingActivity?)
    suspend fun getImpact(period: TrackingPeriod, granularity: TrackingDataGranularity): List<TrackingEntry>
    suspend fun getActivities(period: TrackingPeriod): List<TrackingActivity>
    suspend fun attachCommunity(id: String)
    suspend fun detachCommunity(id: String)
    suspend fun getCommunities(): List<CommunityInfo>
}