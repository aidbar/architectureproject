package com.example.architectureproject.profile

import android.content.ContentValues
import android.util.Log
import com.example.architectureproject.GreenTraceProviders
import com.example.architectureproject.community.CommunityInfo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.getField
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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
    suspend fun getUserById(ids: List<String>): List<User>
    suspend fun attachCommunity(id: String)
    suspend fun detachCommunity(id: String)
    suspend fun getCommunities(): List<CommunityInfo>
}

class FirebaseUserProvider private constructor(
    private var firebaseUser: FirebaseUser?,
    private var profile: Profile?,
    private var lifestyle: UserLifestyle?
): UserProvider {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private data class Profile(
        val name: String,
        val bio: String,
        val age: Int)

    override fun userInfo(): User = userInfoInternal(profile!!, firebaseUser!!)

    override fun uid(): String? = firebaseUser?.uid

    override fun userLifestyle(): UserLifestyle {
        return lifestyle!!
    }

    override suspend fun userLifestyle(lifestyle: UserLifestyle): String? {
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
        val userDocRef = db.collection("users").document(uid()!!)

        // Create a hashmap of data to update
        val update = hashMapOf<String, Any>(
            "name" to name,
            "bio" to bio,
            "age" to age
        )

        return suspendCoroutine { cont ->
            userDocRef.set(update, SetOptions.merge())
                .addOnSuccessListener {
                    Log.d(ContentValues.TAG, "User document successfully added!")
                    profile = Profile(name, bio, age)
                    cont.resume(null)
                }
                .addOnFailureListener { e -> cont.resume(e.message) }
        }
    }

    override suspend fun loginUser(email: String, password: String): String? {
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

    override suspend fun getUserById(ids: List<String>): List<User> {
        val collection = db.collection("users")
        // FIXME: use a transaction for this?
        // this is massively parallel, to hopefully be fast enough
        return coroutineScope { ids.windowed(10, 10, true)
            .map { async {
                collection.whereIn(FieldPath.documentId(), it)
                    .get()
                    .await()
                    .documents
                    .map { userInfoFromProfile(convertUserDocument(it), it.id, "") }
            } }
            .flatMap { it.await() }
        }
    }

    override suspend fun attachCommunity(id: String) =
        GreenTraceProviders.communityManager!!.addUserToCommunity(uid()!!, id)

    override suspend fun detachCommunity(id: String) =
        GreenTraceProviders.communityManager!!.removeUserFromCommunity(uid()!!, id)

    override suspend fun getCommunities() =
        GreenTraceProviders.communityManager!!.getCommunitiesByUID(uid()!!)

    companion object {
        private fun userInfoInternal(
            profile: Profile,
            firebaseUser: FirebaseUser
        ): User = userInfoFromProfile(profile, firebaseUser.uid, firebaseUser.email!!)

        private fun userInfoFromProfile(profile: Profile, id: String, email: String) =
            User(
                email,
                profile.name,
                profile.bio,
                profile.age,
                id
            )

        private fun convertUserDocument(document: DocumentSnapshot) = Profile(
            document.getString("name") ?: "(null)",
            document.getString("bio") ?: "",
            document.getField<Int>("age") ?: -1
        )

        private suspend fun loadUserDocument(uid: String?): Profile? {
            if (uid == null) return null
            val db = FirebaseFirestore.getInstance()
            val userDocRef = db.collection("users").document(uid)
            try {
                val document = userDocRef.get().await()
                if (document != null && document.exists()) {
                    Log.d(ContentValues.TAG, "Successfully fetched user document")
                    return convertUserDocument(document)
                }
            } catch (e: Exception) {
                Log.e(ContentValues.TAG, "Failed to fetch user document", e)
            }

            return null
        }

        @Suppress("UNCHECKED_CAST")
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