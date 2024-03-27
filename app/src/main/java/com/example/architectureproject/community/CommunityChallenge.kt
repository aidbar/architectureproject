package com.example.architectureproject.community

data class CommunityChallenge(
    val id: String,
    val name: String,
    val desc: String,
    val goal: Float,
    val impact: Float
)

data class CommunityChallengeState(
    val id: String,
    val active: Boolean,
    val lastSeen: Long,
    val progress: Float
)