package com.example.architectureproject.community

data class CommunityChallenge(
    val id: String,
    val name: String,
    val desc: String,
    val goal: Float,
    val impact: Float,
    val randomID: String = ""
) {
    private constructor() : this("", "", "", .0f, .0f)
}

data class CommunityChallengeState(
    val id: String,
    val active: Boolean = false,
    val lastSeen: Long = 0,
    val progress: Float = 0f
) {
    private constructor() : this("")
}

data class ChallengeProgressEvent(
    val uid: String, val date: Long,
    val challengeId: String, val name: String,
    val progressDelta: Float, val impact: Float
)