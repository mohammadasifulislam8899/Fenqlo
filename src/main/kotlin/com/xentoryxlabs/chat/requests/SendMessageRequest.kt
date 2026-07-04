package com.xentoryxlabs.chat.requests

import kotlinx.serialization.Serializable

@Serializable
data class SendMessageRequest(
    val content: String
)
