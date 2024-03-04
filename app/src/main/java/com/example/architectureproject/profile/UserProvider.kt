package com.example.architectureproject.profile

import com.google.firebase.auth.FirebaseAuth

interface UserProvider {
    fun userInfo(): User
    fun userLifestyle(): UserLifestyle
    fun userLifestyle(lifestyle: UserLifestyle)
    fun createUser(user: User)
}

class FirebaseUserProvider: UserProvider {
    val auth = FirebaseAuth.getInstance()
    override fun userInfo(): User {
        val currentUser = auth.currentUser!!
        return User(
            currentUser.email!!,
            "Jane", // FIXME: get user's name from a profile datastore
            23, // FIXME: get user age, possibly from firestore
            currentUser.uid
        )
    }

    override fun userLifestyle(): UserLifestyle {
        TODO("Not yet implemented")
    }

    override fun userLifestyle(lifestyle: UserLifestyle) {
        TODO("Not yet implemented")
    }

    override fun createUser(user: User) {
        TODO("Not yet implemented")
    }

}