package com.example.architectureproject.community

import android.util.Log
import com.example.architectureproject.GreenTraceProviders
import com.example.architectureproject.R
import com.example.architectureproject.profile.User
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class FirebaseCommunityManager : CommunityManager {
    private data class ObservedDocument(
        val reg: ListenerRegistration,
        val observers: MutableSet<CommunityObserver>)
    private val db = FirebaseFirestore.getInstance()
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private val observers = hashMapOf<String, ObservedDocument>()
    override suspend fun createCommunity(owner: User, name: String, location: String): String {
        val doc = db.collection("communities").document()
        val update = hashMapOf(
            "owner" to owner.uid,
            "name" to name,
            "location" to location,
            "members" to listOf(owner.uid)
        )

        return suspendCoroutine { cont ->
            doc.set(update)
                .addOnSuccessListener {
                    cont.resume(doc.id)
                }
                .addOnFailureListener { cont.resume("") }
        }
    }

    private fun makeInviteLink(id: String): String =
        "http://greentrace-cb8f7.firebaseapp.com/community.html?id=$id"

    private suspend fun convertCommunityDocument(document: DocumentSnapshot): CommunityInfo? {
        // firebase apparently does not support joins
        //   this might be a perf nightmare
        // FIXME: should this massively concurrent read be wrapped in a transaction?
        if (!document.exists())
            return null

        val owner = document.getString("owner")!!
            .let { GreenTraceProviders.userProvider.getUserById(listOf(it)).first() }

        return CommunityInfo(
            document.getString("name")!!,
            document.id,
            document.getString("location")!!,
            owner,
            makeInviteLink(document.id),
            R.drawable.image1 // FIXME: use real image
        )
    }

    override suspend fun getCommunityById(id: String): CommunityInfo? {
        val doc = db.collection("communities").document(id)
        val document = doc.get().await()

        return convertCommunityDocument(document)
    }

    override suspend fun updateCommunity(id: String, name: String, location: String) {
        val doc = db.collection("communities").document(id)
        val update = hashMapOf<String, Any>(
            "name" to name,
            "location" to location
        )

        doc.update(update).await()
    }

    override suspend fun addUserToCommunity(uid: String, id: String) {
        val doc = db.collection("communities").document(id)
        doc.update("members", FieldValue.arrayUnion(uid)).await()
    }

    override suspend fun removeUserFromCommunity(uid: String, id: String) {
        val doc = db.collection("communities").document(id)
        doc.update("members", FieldValue.arrayRemove(uid)).await()
    }

    override suspend fun getCommunitiesByUID(uid: String): List<CommunityInfo> {
        val collection = db.collection("communities")
        return coroutineScope {
            collection.whereArrayContains("members", uid)
                .get()
                .await()
                .documents
                .map { async { convertCommunityDocument(it)!! } }
                .map { it.await() }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override suspend fun getCommunityMembers(id: String): List<User> =
        db.collection("communities").document(id)
            .get()
            .await()
            .get("members")
            .let { GreenTraceProviders.userProvider.getUserById(it as List<String>) }

    override fun registerObserver(obs: CommunityObserver) {
        val uid = GreenTraceProviders.userProvider.uid()!!
        observers.getOrPut(obs.id) {
            val observers = hashSetOf<CommunityObserver>()
            val registration =
                if (obs.id.isEmpty())
                    db.collection("communities")
                        .whereArrayContains("members", uid)
                        .addSnapshotListener { snapshot, e ->
                            if (e != null) {
                                Log.e("FirebaseCommunityManager.registerObserver", "Listen failed.", e)
                                return@addSnapshotListener
                            }

                            if (snapshot == null || snapshot.isEmpty)
                                return@addSnapshotListener

                            coroutineScope.launch {
                                val list = snapshot
                                            .documents
                                            .map { async { convertCommunityDocument(it)!! } }
                                            .map { it.await() }
                                val local = snapshot.metadata.hasPendingWrites()
                                observers.forEach { it.notify(list, local) }
                            }
                        }
                else
                    db.collection("communities")
                    .document(obs.id)
                    .addSnapshotListener { doc, e ->
                        if (e != null) {
                            Log.e("FirebaseCommunityManager.registerObserver", "Listen failed.", e)
                            return@addSnapshotListener
                        }

                        if (doc == null || !doc.exists()) return@addSnapshotListener
                        coroutineScope.launch {
                            val local = doc.metadata.hasPendingWrites()
                            val info = convertCommunityDocument(doc)!!
                            observers.forEach { it.notify(listOf(info), local) }
                        }
                    }
            ObservedDocument(registration, observers)
        }.apply { observers.add(obs) }
    }

    override fun unregisterObserver(obs: CommunityObserver) {
        val obDoc = observers[obs.id]
        val obsForId = obDoc?.observers
        obsForId?.let {
            it.remove(obs)
            if (it.isNotEmpty()) return@let
            observers.remove(obs.id)
            obDoc.reg.remove()
        }
    }
}