package com.xentoryxlabs.chat.requests

import com.xentoryxlabs.chat.models.ConversationType
import kotlinx.serialization.Serializable

@Serializable
data class CreateConversationRequest(
    val type: ConversationType,
    val name: String?,
    val members: List<String>
)
