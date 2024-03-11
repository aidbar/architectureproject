package com.example.architectureproject.community

import androidx.annotation.DrawableRes
import com.example.architectureproject.profile.User
import java.io.Serializable

data class CommunityInfo(val name: String,
                         val id: String,
                         val location: String,
                         val owner: User,
                         val members: Set<User>,
                         val inviteLink: String,
                         @DrawableRes val image: Int) : Serializable {
    override fun hashCode() = id.hashCode()
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CommunityInfo) return false
        return this.id == other.id
    }
}