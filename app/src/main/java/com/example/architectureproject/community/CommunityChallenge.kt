package com.example.architectureproject.community

import androidx.annotation.DrawableRes
import java.io.Serializable

data class CommunityChallenge(
    val name: String,
    val desc: String,
    val goal: Float,
    val impact: Float,
    val id: String = "",
    @DrawableRes val icon: Int? = null,
    val randomID: String = ""
): Serializable {
    private constructor() : this("", "", .0f, .0f)
}

data class CommunityChallengeState(
    val id: String,
    val communityId: String,
    val active: Boolean = false,
    val lastSeen: Long = 0,
    val progress: Float = 0f
): Serializable {
    private constructor() : this("", "")
}

data class ChallengeProgressEvent(
    val uid: String, val date: Long,
    val challengeId: String, val name: String,
    val progressDelta: Float, val impact: Float
): Serializable {
    private constructor() : this("", 0, "", "", 0f, 0f)
}