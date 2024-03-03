package com.example.architectureproject.community.demo

import android.net.Uri
import com.example.architectureproject.community.CommunityInfo
import com.example.architectureproject.community.CommunityManager
import com.example.architectureproject.profile.User
import com.google.firebase.ktx.Firebase
import java.util.UUID

class DemoCommunityManager: CommunityManager {
    private val communities = hashMapOf<String, CommunityInfo>()
    private fun makeInviteLink(id: String): String =
        "http://greentrace-cb8f7.firebaseapp.com/community.html?id=$id"

    override fun createCommunity(owner: User, name: String, location: String): String {
        val id = UUID.randomUUID().toString()
        val info = CommunityInfo(name, id, location, owner, hashSetOf(owner), makeInviteLink(id))
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