package com.example.architectureproject.community.demo

import com.example.architectureproject.community.CommunityInfo
import com.example.architectureproject.community.CommunityManager
import com.example.architectureproject.profile.User
import java.util.UUID

class DemoCommunityManager: CommunityManager {
    private val communities = hashMapOf<String, CommunityInfo>()
    override fun createCommunity(owner: User, name: String, location: String): String {
        val info = CommunityInfo(name, UUID.randomUUID().toString(), location, owner, hashSetOf(owner))
        communities[info.id] = info
        return info.id
    }

    override fun getCommunityById(id: String): CommunityInfo? =
        communities[id]

    override fun addUserToCommunity(user: User, id: String) {
        (getCommunityById(id)!!.members as HashSet).add(user)
    }

    override fun removeUserFromCommunity(user: User, id: String) {
        (getCommunityById(id)!!.members as HashSet).remove(user)
    }
}