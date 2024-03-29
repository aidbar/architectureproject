package com.example.architectureproject.community

import androidx.annotation.DrawableRes
import com.example.architectureproject.profile.User
import java.io.Serializable

data class CommunityInfo(val name: String,
                         val id: String,
                         val location: String,
                         val owner: User,
                         val inviteLink: String,
                         @DrawableRes val image: Int) : Serializable