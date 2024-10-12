package com.grupo1.iachat.components

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.grupo1.iachat.models.Message

data class Conversations(
    val id: String,
    val messages: List<Message>,
    val timestamp: String
)

fun saveMessages(sharedPreferences: SharedPreferences, conversation: Conversations) {
    val gson = Gson()
    val serializedItems = sharedPreferences.getString("saveConversation", null)
    val itemListType = object : TypeToken<MutableList<Conversations>>() {}.type

    val conversations: MutableList<Conversations> = if (serializedItems != null) {
        gson.fromJson(serializedItems, itemListType)
    } else {
        mutableListOf()
    }

    conversations.add(conversation)

    val updatedItems = gson.toJson(conversations)
    sharedPreferences.edit().putString("saveConversation", updatedItems).apply()
}

fun loadMessages(sharedPreferences: SharedPreferences): List<Conversations> {
    val gson = Gson()
    val serializedItems = sharedPreferences.getString("saveConversation", null)
    val itemListType = object : TypeToken<List<Conversations>>() {}.type
    return if (serializedItems != null) {
        gson.fromJson(serializedItems, itemListType)
    } else {
        emptyList()
    }
}