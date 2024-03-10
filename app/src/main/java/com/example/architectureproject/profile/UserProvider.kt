package com.example.architectureproject.profile

import android.content.ContentValues
import android.util.Log
import android.widget.Toast
import com.example.architectureproject.MainScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.getField
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

interface UserProvider {
    fun userInfo(): User
    fun uid(): String?
    fun userLifestyle(): UserLifestyle
    suspend fun userLifestyle(lifestyle: UserLifestyle)
    suspend fun userProfile(name: String, bio: String, age: Int): String?
    suspend fun loginUser(email: String, password: String): String?
    suspend fun createAndLoginUser(email: String, password: String): String?
}

class FirebaseUserProvider private constructor(
    private var firebaseUser: FirebaseUser?,
    private var profile: Profile?
): UserProvider {
    private data class Profile(val name: String, val bio: String, val age: Int)

    override fun userInfo(): User {
        return User(
            firebaseUser?.email!!,
            profile!!.name, // FIXME: get user's name from a profile datastore
            profile!!.age, // FIXME: get user age, possibly from firestore
            firebaseUser?.uid!!
        )
    }

    override fun uid(): String? = firebaseUser?.uid

    override fun userLifestyle(): UserLifestyle {
        TODO("Not yet implemented")
    }

    override suspend fun userLifestyle(lifestyle: UserLifestyle) {
        TODO("Not yet implemented")
    }

    override suspend fun userProfile(name: String, bio: String, age: Int): String? {
        val db = FirebaseFirestore.getInstance()
        val userDocRef = db.collection("users").document(uid()!!)

        // Create a hashmap of data to update
        val update = hashMapOf<String, Any>(
            "name" to name,
            "bio" to bio,
            "age" to age
        )


        return suspendCoroutine { cont ->
            userDocRef.set(update)
                .addOnSuccessListener {
                    Log.d(ContentValues.TAG, "User document successfully added!")
                    cont.resume(null)
                }
                .addOnFailureListener { e -> cont.resume(e.message) }
        }
    }

    override suspend fun loginUser(email: String, password: String): String? {
        val auth = FirebaseAuth.getInstance()
        return suspendCoroutine { cont ->
            auth.signInWithEmailAndPassword(email,password).addOnCompleteListener {
                if (it.isSuccessful) {
                    it.result.user?.uid?.let { it1 -> Log.d("uid", it1) }
                    // Log.d("user",auth.currentUser.toString())
                    cont.resume(null)
                }

                cont.resume(it.exception?.message)
            }
        }
    }

    override suspend fun createAndLoginUser(email: String, password: String): String? {
        val auth = FirebaseAuth.getInstance()
        return suspendCoroutine { cont ->
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                if (it.isSuccessful) {
                    firebaseUser = auth.currentUser
                    cont.resume(null)
                }

                cont.resume(it.exception?.message)
            }
        }
    }

    companion object {
        private suspend fun loadUserDocument(uid: String?): Profile? {
            if (uid == null) return null
            val db = FirebaseFirestore.getInstance()

            val userDocRef = db.collection("users").document(uid)
            try {
                val document = userDocRef.get().await()
                if (document != null && document.exists()) {
                    Log.d(ContentValues.TAG, "Successfully fetched user document")
                    return Profile(
                        document.getString("name") ?: "(null)",
                        document.getString("bio") ?: "",
                        document.getField<Int>("age") ?: -1
                    )
                }
            } catch (e: Exception) {
                Log.e(ContentValues.TAG, "Failed to fetch user document", e)
            }

            return null
        }

        suspend fun new(): FirebaseUserProvider {
            val auth = FirebaseAuth.getInstance()
            return FirebaseUserProvider(
                auth.currentUser,
                loadUserDocument(auth.currentUser?.uid)
            )
        }
    }
}