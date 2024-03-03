package com.example.architectureproject.tracking

import com.example.architectureproject.community.CommunityInfo

interface TrackingDataProvider {
    fun addActivity(activity: TrackingActivity): String
    fun viewActivity(id: String): TrackingActivity
    fun editActivity(id: String, new: TrackingActivity?)
    fun getImpact(period: TrackingPeriod, granularity: TrackingDataGranularity): List<TrackingEntry>
    fun getActivities(period: TrackingPeriod): List<TrackingActivity>
    fun attachCommunity(id: String)
    fun detachCommunity(id: String)
    fun getCommunities(): List<CommunityInfo>
}