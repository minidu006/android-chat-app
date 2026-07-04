package com.example.chatchat.model

data class AppUser(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val bio: String = "",
    val birthDate: String = "",
    val imageUrl: String = ""
)

data class ChatMessage(
    val id: String = "",
    val senderId: String = "",
    val text: String = "",
    val timestamp: Long = 0L
)

data class Conversation(
    val roomId: String = "",
    val otherUserId: String = "",
    val otherUserName: String = "",
    val lastMessage: String = "",
    val lastMessageTime: Long = 0L
)

data class CallLogItem(
    val name: String,
    val date: String,
    val missed: Boolean
)
