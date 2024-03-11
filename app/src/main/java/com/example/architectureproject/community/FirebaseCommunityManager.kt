package com.example.architectureproject.community

import com.example.architectureproject.profile.User

class FirebaseCommunityManager : CommunityManager {
    override suspend fun createCommunity(owner: User, name: String, location: String): String {
        TODO("Not yet implemented")
    }

    override suspend fun getCommunityById(id: String): CommunityInfo? {
        TODO("Not yet implemented")
    }

    override suspend fun updateCommunity(id: String, name: String, location: String) {
        TODO("Not yet implemented")
    }

    override suspend fun addUserToCommunity(user: User, id: String) {
        TODO("Not yet implemented")
    }

    override suspend fun removeUserFromCommunity(user: User, id: String) {
        TODO("Not yet implemented")
    }
}