package com.xentoryxlabs.chat.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class ConversationType {
    DIRECT, GROUP
}

@Serializable
data class Conversation(
    @SerialName("_id")
    val id: String,
    val type: ConversationType,
    val name: String?,
    val members: List<String>,
    val createdBy: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
data class Message(
    @SerialName("_id")
    val id: String,
    val conversationId: String,
    val senderId: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val readBy: List<String> = emptyList()
)
