package com.example.architectureproject.community

import com.example.architectureproject.profile.User

data class CommunityInfo(val name: String,
                         val id: String,
                         val location: String,
                         val owner: User,
                         val members: Set<User>,
                         val inviteLink: String)