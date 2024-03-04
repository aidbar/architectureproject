package com.example.architectureproject.community

import android.net.Uri
import com.example.architectureproject.profile.User

interface CommunityManager {
    fun createCommunity(owner: User, name: String, location: String): String
    fun getCommunityById(id: String): CommunityInfo?
    fun addUserToCommunity(user: User, id: String)
    fun removeUserFromCommunity(user: User, id: String)
}