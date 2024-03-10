package com.example.architectureproject.profile

import android.content.ContentValues
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.getField
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

interface UserProvider {
    fun userInfo(): User
    fun uid(): String?
    fun userLifestyle(): UserLifestyle
    fun hasUserLifestyle(): Boolean
    fun hasUserProfile(): Boolean
    suspend fun userLifestyle(lifestyle: UserLifestyle): String?
    suspend fun userProfile(name: String, bio: String, age: Int): String?
    suspend fun loginUser(email: String, password: String): String?
    suspend fun createAndLoginUser(email: String, password: String): String?
}

class FirebaseUserProvider private constructor(
    private var firebaseUser: FirebaseUser?,
    private var profile: Profile?,
    private var lifestyle: UserLifestyle?
): UserProvider {
    private data class Profile(val name: String, val bio: String, val age: Int)

    override fun userInfo(): User {
        return User(
            firebaseUser?.email!!,
            profile?.name!!,
            profile?.bio!!,
            profile?.age!!,
            firebaseUser?.uid!!
        )
    }

    override fun uid(): String? = firebaseUser?.uid

    override fun userLifestyle(): UserLifestyle {
        return lifestyle!!
    }

    override suspend fun userLifestyle(lifestyle: UserLifestyle): String? {
        val db = FirebaseFirestore.getInstance()
        val userDocRef = db.collection("userLifestyle").document(uid()!!)

        // Create a hashmap of data to update
        val update = hashMapOf(
            "transportationMethod" to lifestyle.transportationPreference,
            "disabilities" to lifestyle.disabilities.toList(),
            "dietRestriction" to lifestyle.diet,
            "shoppingMethod" to lifestyle.shoppingPreference,
            "locallySourcedFoodPreference" to lifestyle.locallySourcedFoodPreference,
            "sustainableShoppingPreference" to lifestyle.sustainabilityInfluence
        )

        return suspendCoroutine { cont ->
            userDocRef.set(update)
                .addOnSuccessListener {
                    Log.d(ContentValues.TAG, "User document successfully added!")
                    this.lifestyle = lifestyle
                    cont.resume(null)
                }
                .addOnFailureListener { e -> cont.resume(e.message) }
        }
    }

    override fun hasUserLifestyle() = lifestyle != null

    override fun hasUserProfile() = profile != null

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
                    profile = Profile(name, bio, age)
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
                    it.result.user?.uid?.let { it1 ->
                        runBlocking {
                            profile = loadUserDocument(it1)
                            lifestyle = loadLifestyleDocument(it1)
                            Log.d("uid", it1)
                        }
                    }
                    firebaseUser = it.result.user
                    cont.resume(null)
                    return@addOnCompleteListener
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
                    return@addOnCompleteListener
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

        private suspend fun loadLifestyleDocument(uid: String?): UserLifestyle? {
            if (uid == null) return null
            val db = FirebaseFirestore.getInstance()
            val userDocRef = db.collection("userLifestyle").document(uid)
            try {
                val document = userDocRef.get().await()
                if (document != null && document.exists()) {
                    Log.d(ContentValues.TAG, "Successfully fetched user lifestyle document")
                    return UserLifestyle(
                        (document.get("disabilities")!! as List<UserLifestyle.Disability>).toSet(),
                        document.getField<UserLifestyle.TransportationMethod>("transportationMethod")!!,
                        document.getField<UserLifestyle.Diet>("dietRestriction")!!,
                        document.getField<UserLifestyle.Frequency>("sustainableShoppingPreference")!!,
                        document.getField<UserLifestyle.Frequency>("locallySourcedFoodPreference")!!,
                        document.getField<UserLifestyle.ShoppingMethod>("shoppingMethod")!!,
                    )
                }
            } catch (e: Exception) {
                Log.e(ContentValues.TAG, "Failed to fetch user lifestyle document", e)
            }

            return null
        }

        suspend fun new(): FirebaseUserProvider {
            val firebaseUser = FirebaseAuth.getInstance().currentUser
            return FirebaseUserProvider(
                firebaseUser,
                loadUserDocument(firebaseUser?.uid),
                loadLifestyleDocument(firebaseUser?.uid)
            )
        }
    }
}