package com.example.architectureproject.community

import android.util.Log
import com.example.architectureproject.GreenTraceProviders
import com.example.architectureproject.R
import com.example.architectureproject.profile.User
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Filter
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
            "members" to listOf(owner.uid),
            "invited" to listOf<String>()
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
        db.runBatch { batch ->
            batch.update(doc, "members", FieldValue.arrayUnion(uid))
            batch.update(doc, "invited", FieldValue.arrayRemove(uid))
        }.await()
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

    override suspend fun getPendingInvites(uid: String): List<CommunityInfo> =
        coroutineScope {
            db.collection("communities")
                .whereArrayContains("invited", uid)
                .get()
                .await()
                .documents
                .map { async { convertCommunityDocument(it) } }
                .map { it.await()!! }
        }

    override suspend fun declineInvite(uid: String, id: String) {
        db.collection("communities")
            .document(id)
            .update("invited", FieldValue.arrayRemove(uid))
    }

    @Suppress("UNCHECKED_CAST")
    override suspend fun inviteUser(uid: String, cid: String) {
        db.runTransaction { txn ->
            val ref = db.collection("communities")
                .document(cid)
            val members = txn.get(ref).get("members") as List<String>
            if (members.contains(uid)) return@runTransaction
            txn.update(ref, "invited", FieldValue.arrayUnion(uid))
        }.await()
    }

    @Suppress("UNCHECKED_CAST")
    override fun registerObserver(obs: CommunityObserver) {
        val uid = GreenTraceProviders.userProvider.uid()!!
        observers.getOrPut(obs.id) {
            val observers = hashSetOf<CommunityObserver>()
            val registration =
                if (obs.id.isEmpty())
                    db.collection("communities")
                        .where(Filter.or(
                            Filter.arrayContains("invited", uid),
                            Filter.arrayContains("members", uid)
                        ))
                        .addSnapshotListener { snapshot, e ->
                            if (e != null) {
                                Log.e("FirebaseCommunityManager.registerObserver", "Listen failed.", e)
                                return@addSnapshotListener
                            }

                            val local = snapshot?.metadata?.hasPendingWrites() ?: false
                            if (snapshot == null) {
                                observers.forEach { it.notify(listOf(), listOf(), local) }
                                return@addSnapshotListener
                            }

                            coroutineScope.launch {
                                val (invitedDoc, listDoc) = snapshot.documents
                                    .partition {
                                        val invited = it.get("invited") as List<String>? ?: listOf()
                                        invited.contains(uid)
                                    }
                                val list = listDoc
                                            .map { async { convertCommunityDocument(it)!! } }
                                            .map { it.await() }
                                val invited = invitedDoc
                                    .map { async { convertCommunityDocument(it)!! } }
                                    .map { it.await() }
                                observers.forEach { it.notify(list, invited, local) }
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

                        val local = doc?.metadata?.hasPendingWrites() ?: false
                        if (doc == null || !doc.exists()) {
                            observers.forEach { it.notify(listOf(), listOf(), local) }
                            return@addSnapshotListener
                        }

                        coroutineScope.launch {
                            val info = convertCommunityDocument(doc)!!
                            val invited = doc.get("invited") as List<String>? ?: listOf()
                            observers.forEach {
                                it.notify(
                                    listOf(info),
                                    if (invited.contains(uid)) listOf(info) else listOf(),
                                    local)
                            }
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