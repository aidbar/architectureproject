package com.example.architectureproject.profile

import java.io.Serializable

data class User(val email: String,
                val name: String,
                val bio: String,
                val age: Int,
                val uid: String): Serializable {
    override fun hashCode() = uid.hashCode()
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is User) return false
        return this.uid == other.uid
    }
}
