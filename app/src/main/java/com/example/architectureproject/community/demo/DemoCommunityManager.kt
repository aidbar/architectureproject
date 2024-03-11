package com.example.architectureproject.community.demo

import android.content.Context
import android.net.Uri
import com.example.architectureproject.GreenTraceProviders
import com.example.architectureproject.R
import com.example.architectureproject.community.CommunityInfo
import com.example.architectureproject.community.CommunityManager
import com.example.architectureproject.profile.User
import com.google.firebase.ktx.Firebase
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.UUID

class DemoCommunityManager: CommunityManager {
    private val communities = hashMapOf<String, CommunityInfo>()

    init {
        val data = GreenTraceProviders.applicationContext!!.getSharedPreferences(
            "CommunityDemo", Context.MODE_PRIVATE
        )

        data.all.forEach {
            val stream = ObjectInputStream(ByteArrayInputStream((it.value as String).toByteArray()))
            communities.put(it.key, stream.readObject() as CommunityInfo)
        }

        communities.put("demo1", CommunityInfo(
            "Demo Community 1",
            "demo1",
            "Waterloo, ON, CA",
            GreenTraceProviders.userProvider!!.userInfo(),
            hashSetOf(GreenTraceProviders.userProvider!!.userInfo()),
            makeInviteLink("demo1"),
            R.drawable.image1
        ))
    }

    private fun makeInviteLink(id: String): String =
        "http://greentrace-cb8f7.firebaseapp.com/community.html?id=$id"

    override suspend fun createCommunity(owner: User, name: String, location: String): String {
        val id = UUID.randomUUID().toString()
        val info = CommunityInfo(
            name, id, location,
            owner, hashSetOf(owner), makeInviteLink(id),
            R.drawable.image1
        )
        communities[info.id] = info
        return info.id
    }

    override suspend fun getCommunityById(id: String): CommunityInfo? =
        communities[id]

    override suspend fun updateCommunity(id: String, name: String, location: String) {
        communities[id] = communities[id]!!.copy(name = name, location = location)
    }

    override suspend fun addUserToCommunity(user: User, id: String) {
        (getCommunityById(id)!!.members as HashSet).add(user)
    }

    override suspend fun removeUserFromCommunity(user: User, id: String) {
        (getCommunityById(id)!!.members as HashSet).remove(user)
    }

    protected fun finalize() {
        val data = GreenTraceProviders.applicationContext!!.getSharedPreferences(
            "CommunityDemo", Context.MODE_PRIVATE
        )

        communities.forEach {
            val bos = ByteArrayOutputStream()
            val stream = ObjectOutputStream(bos)
            stream.writeObject(it.value)
            stream.close()
            data.edit().putString(it.key, bos.toByteArray().toString())
        }
    }
}