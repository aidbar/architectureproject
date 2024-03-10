package com.example.architectureproject.profile

import java.io.Serializable

data class User(val email: String,
                val name: String,
                val bio: String,
                val age: Int,
                val uid: String): Serializable
