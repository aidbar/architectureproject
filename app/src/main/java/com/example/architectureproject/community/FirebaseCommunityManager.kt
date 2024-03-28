package com.example.architectureproject.community

import android.util.Log
import com.example.architectureproject.GreenTraceProviders
import com.example.architectureproject.R
import com.example.architectureproject.profile.User
import com.google.firebase.firestore.AggregateField
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.getField
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.ZonedDateTime
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.min

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
        observers.getOrPut(obs.cid) {
            val observers = hashSetOf<CommunityObserver>()
            val registration =
                if (obs.cid.isEmpty())
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
                    .document(obs.cid)
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
        val obDoc = observers[obs.cid]
        val obsForId = obDoc?.observers
        obsForId?.let {
            it.remove(obs)
            if (it.isNotEmpty()) return@let
            observers.remove(obs.cid)
            obDoc.reg.remove()
        }
    }

   private suspend fun newChallenges(now: ZonedDateTime, id: String): List<CommunityChallenge>? {
        val collection = db.collection("communities")
            .document(id)
            .collection("challenge_states")

        // fetch the community's recently seen challenges
        val recentIds = collection
            .whereGreaterThan("lastSeen", ZonedDateTime.now().minusMonths(2).toEpochSecond())
            .get()
            .await()
            .documents
            .map { it.id }

        // randomly fetch challenges
        // FIXME: if recentIds.size > 10, this won't work
        val results = db.collection("challenges")
            .whereNotIn(FieldPath.documentId(), recentIds)
            .orderBy("randomID")
            .orderBy("id")
            .limit(4)
            .get()
            .await()
            .documents

        // re-seed the randomness
        db.runBatch { batch ->
            results.forEach {
                batch.update(it.reference, "randomID", UUID.randomUUID().toString())
            }
        }.await()

        val challenges = results.map { it.toObject(CommunityChallenge::class.java)!! }
        val toActivate = challenges.map {
            CommunityChallengeState(it.id, id, true, now.toEpochSecond())
        }
        val toDeactivate = collection.whereEqualTo("active", true)
            .get()
            .await()
            .documents
            .map { it.reference }

        val updateThreshold = now.minusMonths(1).toEpochSecond()
        val updated = db.runTransaction { txn ->
            val meta = collection.document("metadata")
            val lastUpdate = txn.get(meta).getField<Long>("last_update") ?: 0
            if (lastUpdate > updateThreshold)
                return@runTransaction false

            // update metadata
            txn.set(meta, hashMapOf("last_update" to now.toEpochSecond()), SetOptions.merge())
            // activate new challenges
            toActivate.forEach { txn.set(collection.document(it.id), it) }
            // deactivate old challenges
            toDeactivate.forEach { txn.update(it, "active", false) }
            return@runTransaction true
        }.await()

        if (updated)
            return challenges

        return null
    }

    override suspend fun getChallenges(id: String): List<Pair<CommunityChallenge, CommunityChallengeState>> {
        // NOTE: this function is also responsible for updating challenge sets
        // it's perfectly safe as long as one cannot participate in a challenge prior to viewing it
        // and because we check if challenges are expired using lastSeen before we update them
        val collection = db.collection("communities")
            .document(id)
            .collection("challenge_states")

        val lastUpdate = collection.document("metadata")
            .get()
            .await()
            .getField<Long>("last_update")!!

        // if stale, update
        val now = ZonedDateTime.now()
        if (lastUpdate <= now.minusMonths(1).toEpochSecond()) {
            val new = newChallenges(now, id)
            if (new != null)
                return new.map {
                    it to CommunityChallengeState(it.id, id, true, now.toEpochSecond())
                }

            // someone beat us to the update, re-fetch
            return getChallenges(id)
        }

        return collection.whereEqualTo("active", true)
            .get()
            .await()
            .documents
            .map {
                db.collection("challenges")
                .document(it.id)
                .get()
            }
            .map {
                val result = it.await()
                result.toObject(CommunityChallenge::class.java)!! to
                        result.toObject(CommunityChallengeState::class.java)!!
            }
    }

    override suspend fun addChallengeProgress(challenge: CommunityChallenge, progressDelta: Float): Boolean {
        // query all challenge states across all communities, where this challenge is active
        val challengeStates = db.collectionGroup("challenge_states")
            .whereEqualTo("id", challenge.id)
            .whereEqualTo("active", true)
            .get()
            .await()
            .documents

        val impactDelta = challenge.impact / challenge.goal * progressDelta
        val now = ZonedDateTime.now()

        // update all relevant progress entries
        // re-fetch data in a txn to reduce race conditions
        // return true if anything got updated, else false
        return db.runTransaction { txn ->
            // prepare list of items to update using txn reads
            val states = challengeStates.map {
                it.reference to
                        txn.get(it.reference).toObject(CommunityChallengeState::class.java)!!
            }
                .filter { (_, state) ->
                    // ensure challenge is not expired and not completed
                    state.active && state.progress < challenge.goal &&
                            state.lastSeen > now.minusMonths(1).toEpochSecond()
                }

            // perform all txn writes
            states.forEach { (ref, state) ->
                // clamp total to goal
                txn.update(
                    ref, "progress",
                    min(state.progress + progressDelta, challenge.goal)
                )

                db.collection("communities")
                    .document(state.communityId)
                    .collection("challenge_event_log")
                    .document()
                    .let {
                        txn.set(it, ChallengeProgressEvent(
                            GreenTraceProviders.userProvider.uid()!!,
                            now.toEpochSecond(),
                            challenge.id,
                            challenge.name,
                            progressDelta,
                            impactDelta
                        ))
                    }
            }
            states.isNotEmpty()
        }.await()
    }

    override suspend fun challengeImpact(cid: String, uid: String, total: Boolean) =
        db.collection("communities")
            .document(cid)
            .collection("challenge_event_log")
            .let {
                if (uid.isEmpty()) it
                else it.whereEqualTo("uid", uid)
            }
            .let { query ->
                if (total) query
                else
                    db.collection("communities")
                        .document(cid)
                        .collection("challenge_states")
                        .document("metadata")
                        .get()
                        .await()
                        .getField<Long>("last_update")!!
                        .let { query.whereGreaterThanOrEqualTo("date", it) }
            }
            .aggregate(AggregateField.sum("impact"))
            .get(AggregateSource.SERVER)
            .await()
            .getDouble(AggregateField.sum("impact"))!!
            .toFloat()

    override suspend fun registerChallengesObserver(obs: CommunityChallengesObserver) {
        TODO("Not yet implemented")
    }

    override suspend fun unregisterChallengesObserver(obs: CommunityChallengesObserver) {
        TODO("Not yet implemented")
    }
}