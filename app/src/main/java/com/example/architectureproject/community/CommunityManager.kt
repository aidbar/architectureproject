package com.example.architectureproject.community

import com.example.architectureproject.profile.User

interface CommunityObserver {
    val cid: String
    fun notify(info: List<CommunityInfo>, invites: List<CommunityInfo>, local: Boolean)
}

interface CommunityChallengesObserver {
    val cid: String
    //val challengeId: String
    fun notify(info: List<Pair<CommunityChallenge, CommunityChallengeState>>, local: Boolean)
}

interface CommunityManager {
    suspend fun createCommunity(owner: User, name: String, location: String): String
    suspend fun getCommunityById(id: String): CommunityInfo?
    suspend fun updateCommunity(id: String, name: String, location: String)
    suspend fun deleteCommunity(id: String)
    suspend fun addUserToCommunity(uid: String, id: String)
    suspend fun removeUserFromCommunity(uid: String, id: String)
    suspend fun getCommunitiesByUID(uid: String): List<CommunityInfo>
    suspend fun getCommunityMembers(id: String): List<User>
    suspend fun getPendingInvites(uid: String): List<CommunityInfo>
    suspend fun declineInvite(uid: String, id: String)
    fun registerObserver(obs: CommunityObserver)
    fun unregisterObserver(obs: CommunityObserver)
    suspend fun getChallenges(id: String): List<Pair<CommunityChallenge, CommunityChallengeState>>
    suspend fun addChallengeProgress(challenge: CommunityChallenge, progressDelta: Float): Boolean
    suspend fun challengeImpact(cid: String, uid: String = "", total: Boolean = false): Float
    suspend fun registerChallengesObserver(obs: CommunityChallengesObserver)
    suspend fun unregisterChallengesObserver(obs: CommunityChallengesObserver)
    suspend fun inviteUser(uid: String, cid: String)
    suspend fun addCommunityChallenge(challenge: CommunityChallenge): String
}
