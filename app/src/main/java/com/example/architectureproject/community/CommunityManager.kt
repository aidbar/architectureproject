package com.example.architectureproject.community

import com.example.architectureproject.profile.User

interface CommunityManager {
    suspend fun createCommunity(owner: User, name: String, location: String): String
    suspend fun getCommunityById(id: String): CommunityInfo?
    suspend fun updateCommunity(id: String, name: String, location: String)
    suspend fun addUserToCommunity(uid: String, id: String)
    suspend fun removeUserFromCommunity(uid: String, id: String)
    suspend fun getCommunitiesByUID(uid: String): List<CommunityInfo>
    suspend fun getCommunityMembers(id: String): List<User>
}